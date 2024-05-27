/*
 * doSonarAPI: SonarQube AsyncAPI Plugin
 * Copyright (C) 2021-2022 Apiaddicts
 * contacta AT apiaddicts DOT org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.apiaddicts.apitools.dosonarapi.plugin;

import com.sonar.sslr.api.RecognitionException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apiaddicts.apitools.dosonarapi.asyncapi.AsyncApiConfiguration;
import org.apiaddicts.apitools.dosonarapi.plugin.cpd.AsyncApiCpdAnalyzer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.apiaddicts.apitools.dosonarapi.asyncapi.metrics.AsyncApiFileLinesVisitor;
import org.apiaddicts.apitools.dosonarapi.asyncapi.metrics.AsyncApiFileMetrics;
import org.apiaddicts.apitools.dosonarapi.asyncapi.metrics.AsyncApiMetrics;
import org.apiaddicts.apitools.dosonarapi.asyncapi.parser.AsyncApiParser;
import org.apiaddicts.apitools.dosonarapi.api.IssueLocation;
import org.apiaddicts.apitools.dosonarapi.api.AsyncApiCheck;
import org.apiaddicts.apitools.dosonarapi.api.AsyncApiFile;
import org.apiaddicts.apitools.dosonarapi.api.AsyncApiVisitorContext;
import org.apiaddicts.apitools.dosonarapi.api.PreciseIssue;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.ValidationException;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.YamlParser;

public class AsyncApiAnalyzer {
    private static final Logger LOG = Loggers.get(AsyncApiAnalyzer.class);

    private final SensorContext context;
    private final List<InputFile> inputFiles;
    private final AsyncApiChecks checks;
    private final NoSonarFilter noSonarFilter;
    private final AsyncApiCpdAnalyzer cpdAnalyzer;
    private final AsyncApiConfiguration configuration;
    private FileLinesContextFactory fileLinesContextFactory;

    public AsyncApiAnalyzer(SensorContext context, AsyncApiChecks checks, FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter, List<InputFile> inputFiles) {
        this.context = context;
        this.checks = checks;
        this.fileLinesContextFactory = fileLinesContextFactory;
        this.noSonarFilter = noSonarFilter;
        this.cpdAnalyzer = new AsyncApiCpdAnalyzer(context);
        this.inputFiles = inputFiles;
        this.configuration = new AsyncApiConfiguration(context.fileSystem().encoding(), true);
    }

    public void scanFiles() {
        for (InputFile inputFile : inputFiles) {
            if (context.isCancelled()) {
                return;
            }
            scanFile(inputFile);
        }
    }

    private void scanFile(InputFile inputFile) {
        AsyncApiFile asyncApiFile = SonarQubeAsyncApiFile.create(inputFile);
        AsyncApiVisitorContext visitorContext;
        try {
            String content = getContent(inputFile);
            JsonNode rootNode = AsyncApiParser.createGeneric(configuration).parse(content);

            // Use the generic parser as there's no need to distinguish versions
            YamlParser targetParser = AsyncApiParser.createGeneric(configuration);
            visitorContext = new AsyncApiVisitorContext(targetParser.parse(content), targetParser.getIssues(), asyncApiFile);
            saveMeasures(inputFile, visitorContext);
        } catch (ValidationException e) {
            visitorContext = new AsyncApiVisitorContext(asyncApiFile, e);
            LOG.error("Error during file validation: " + inputFile.filename() + "\n" + e.formatMessage());
        } catch (RecognitionException e) {
            visitorContext = new AsyncApiVisitorContext(asyncApiFile, e);
            LOG.error("Unable to parse file in recognition: " + inputFile.filename() + "\n" + e.getMessage());
        } catch (IOException ex) {
            RecognitionException re = new RecognitionException(0, ex.getMessage());
            visitorContext = new AsyncApiVisitorContext(asyncApiFile, re);
            LOG.error("Unable to parse file in i/o: " + inputFile.filename() + "\n" + ex.getMessage());
        }

        for (AsyncApiCheck check : checks.all()) {
            saveIssues(inputFile, check, check.scanFileForIssues(visitorContext));
        }
    }

    /**
     * This method is required to avoid a parsing issue with yaml,
     * sometimes, when an empty line is followed by a comment, it breaks the parser,
     * it also replaces tabs by spaces to avoid another parsing error
     *
     * FIXME: Try to solve in the yaml parser lib
     */
    private String getContent(InputFile inputFile) throws IOException {
        String[] lines = inputFile.contents().split("\n");
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].replace("\t", " ");
            lines[i] = lines[i].replace("\\/", "//");
            lines[i] = lines[i].replace("!!", "  ");
            if (!lines[i].trim().isEmpty()) continue;
            if (i > 0) {
                int n = lines[i - 1].indexOf(lines[i - 1].trim());
                if (n < 0) n = 0;
                lines[i] = String.join("", Collections.nCopies(n, " ")) + "#";
            }
        }
        return String.join("\n", lines);
    }

    private void saveIssues(InputFile inputFile, AsyncApiCheck check, List<PreciseIssue> issues) {
        RuleKey ruleKey = checks.ruleKeyFor(check);
        for (PreciseIssue preciseIssue : issues) {
            NewIssue newIssue = context.newIssue().forRule(ruleKey);
            Integer cost = preciseIssue.cost();
            if (cost != null) {
                newIssue.gap(cost.doubleValue());
            }
            newIssue.at(newLocation(inputFile, newIssue, preciseIssue.primaryLocation()));
            for (IssueLocation secondaryLocation : preciseIssue.secondaryLocations()) {
                newIssue.addLocation(newLocation(inputFile, newIssue, secondaryLocation));
            }
            newIssue.save();
        }
    }

    private static NewIssueLocation newLocation(InputFile inputFile, NewIssue issue, IssueLocation location) {
        NewIssueLocation newLocation = issue.newLocation().on(inputFile);
        if (location.startLine() != IssueLocation.UNDEFINED_LINE) {
            TextRange range;
            if (location.startLineOffset() == IssueLocation.UNDEFINED_OFFSET) {
                range = inputFile.selectLine(location.startLine());
            } else {
                try {
                    range = inputFile.newRange(location.startLine(), location.startLineOffset(), location.endLine(), location.endLineOffset());
                } catch (IllegalArgumentException e) {
                    try {
                        range = inputFile.selectLine(location.startLine());
                    } catch (IllegalArgumentException e2) {
                        range = inputFile.selectLine(1);
                    }
                }
            }
            newLocation.at(range);
        }

        String message = location.message();
        if (message != null) {
            newLocation.message(message);
        }
        return newLocation;
    }

    private void saveMeasures(InputFile inputFile, AsyncApiVisitorContext visitorContext) {
        AsyncApiFileMetrics fileMetrics = new AsyncApiFileMetrics(visitorContext);
        AsyncApiFileLinesVisitor fileLinesVisitor = fileMetrics.fileLinesVisitor();

        cpdAnalyzer.pushCpdTokens(inputFile, visitorContext);
        noSonarFilter.noSonarInFile(inputFile, fileLinesVisitor.getLinesWithNoSonar());

        Set<Integer> linesOfCode = fileLinesVisitor.getLinesOfCode();
        Set<Integer> linesOfComments = fileLinesVisitor.getLinesOfComments();

        saveMetricOnFile(inputFile, CoreMetrics.NCLOC, linesOfCode.size());
        saveMetricOnFile(inputFile, CoreMetrics.COMMENT_LINES, linesOfComments.size());
        saveMetricOnFile(inputFile, AsyncApiMetrics.CHANNELS_COUNT, fileMetrics.numberOfChannels());
        saveMetricOnFile(inputFile, CoreMetrics.COMPLEXITY, fileMetrics.complexity());

        FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(inputFile);
        for (int line : linesOfCode) {
            fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, 1);
        }
        for (int line : linesOfComments) {
            fileLinesContext.setIntValue(CoreMetrics.COMMENT_LINES_DATA_KEY, line, 1);
        }
        fileLinesContext.save();
    }

    private void saveMetricOnFile(InputFile inputFile, Metric<Integer> metric, Integer value) {
        context.<Integer>newMeasure().withValue(value).forMetric(metric).on(inputFile).save();
    }
}
