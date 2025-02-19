/*
 * doSonarAPI: SonarQube AsyncAPI Plugin
 * Copyright (C) 2024-2024 Apiaddicts
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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.error.AnalysisError;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.LogTester;
import org.apiaddicts.apitools.dosonarapi.checks.AsyncApiCheckList;
import org.apiaddicts.apitools.dosonarapi.checks.ParsingErrorCheck;
import org.apiaddicts.apitools.dosonarapi.asyncapi.metrics.AsyncApiMetrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AsyncApiScannerSensorTest {
  private final Path baseDir = Paths.get("src/test/resources/sensor").toAbsolutePath();
  @org.junit.Rule
  public LogTester logTester = new LogTester();
  private SensorContextTester context;
  private ActiveRules activeRules;

  @Before
  public void init() {
    context = SensorContextTester.create(baseDir);
  }

  @Test
  public void sensor_descriptor() {
    activeRules = (new ActiveRulesBuilder()).build();
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("AsyncAPI Scanner Sensor");
    assertThat(descriptor.languages()).containsOnly("asyncapi");
    assertThat(descriptor.type()).isEqualTo(InputFile.Type.MAIN);
  }

  @Test
  public void test_issues_asyncapi() {
    activeRules = (new ActiveRulesBuilder())
        .create(RuleKey.of(AsyncApiCheckList.REPOSITORY_KEY, "ChannelAmbiguity"))
        .activate()
        .build();

    inputFile("file1.yaml");
    sensor().execute(context);

    String key = "moduleKey:file1.yaml";

    assertThat(context.measure(key, CoreMetrics.NCLOC).value()).isEqualTo(35);
    assertThat(context.measure(key, AsyncApiMetrics.CHANNELS_COUNT).value()).isEqualTo(2); 
    assertThat(context.measure(key, AsyncApiMetrics.ASYNCAPI_OPERATIONS_COUNT).value()).isEqualTo(2);
    assertThat(context.measure(key, CoreMetrics.COMPLEXITY).value()).isEqualTo(14);
    assertThat(context.measure(key, CoreMetrics.COMMENT_LINES).value()).isEqualTo(3);

    assertThat(context.allIssues()).hasSize(0);

    assertThat(context.allAnalysisErrors()).isEmpty();
}

  @Test
  public void parse_error() {
    inputFile("parse-error.yaml");
    activeRules = (new ActiveRulesBuilder())
      .create(RuleKey.of(AsyncApiCheckList.REPOSITORY_KEY, ParsingErrorCheck.CHECK_KEY))
      .activate()
      .build();
    sensor().execute(context);
    assertThat(context.allIssues()).hasSize(1);
    assertThat(context.allAnalysisErrors())
        .extracting(e -> e.inputFile().filename(), e -> e.location().line(), e -> e.location().lineOffset(), AnalysisError::message)
        .containsExactlyInAnyOrder(
            tuple("parse-error.yaml", 3, 2, "Missing required properties: [version]")
        );
  }

  @Test
  public void parse_yaml_break_comment_ok() {
    inputFile("parse-yaml.yaml");
    activeRules = (new ActiveRulesBuilder())
            .create(RuleKey.of(AsyncApiCheckList.REPOSITORY_KEY, ParsingErrorCheck.CHECK_KEY))
            .activate()
            .build();
    sensor().execute(context);
    assertThat(context.allIssues()).hasSize(0);
    assertThat(context.allAnalysisErrors()).hasSize(0);
  }

  @Test
  public void parse_yaml_tabs_ok() {
    inputFile("parse-error-tabs.json");
    activeRules = (new ActiveRulesBuilder())
            .create(RuleKey.of(AsyncApiCheckList.REPOSITORY_KEY, ParsingErrorCheck.CHECK_KEY))
            .activate()
            .build();
    sensor().execute(context);
    assertThat(context.allIssues()).hasSize(0);
    assertThat(context.allAnalysisErrors()).hasSize(0);
  }

  //@Test
  public void test_folder() {
    List<String> files = listFiles("files");
    List<String> errorFiles = new LinkedList<>();
    for (String file: files) {
      context = SensorContextTester.create(baseDir);
      inputFile(file);
      activeRules = (new ActiveRulesBuilder()).create(RuleKey.of(AsyncApiCheckList.REPOSITORY_KEY, ParsingErrorCheck.CHECK_KEY))
              .activate().build();
      sensor().execute(context);
      if (!context.allIssues().isEmpty() || !context.allAnalysisErrors().isEmpty()) errorFiles.add(file);
    }
    System.out.println(errorFiles);
  }

  @Test
  public void cancelled_analysis() {
    inputFile("file1.yaml");
    activeRules = (new ActiveRulesBuilder()).build();
    context.setCancelled(true);
    sensor().execute(context);
    assertThat(context.allAnalysisErrors()).isEmpty();
  }

  private AsyncApiScannerSensor sensor() {
    CheckFactory checkFactory = new CheckFactory(activeRules);
    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(Mockito.any(InputFile.class))).thenReturn(fileLinesContext);
    return new AsyncApiScannerSensor(checkFactory, fileLinesContextFactory, new NoSonarFilter());
  }

  private InputFile inputFile(String name) {
    DefaultInputFile inputFile = TestInputFileBuilder.create("moduleKey", name)
      .setModuleBaseDir(baseDir)
      .setCharset(StandardCharsets.UTF_8)
      .setType(InputFile.Type.MAIN)
      .setLanguage(AsyncApi.KEY)
      .initMetadata(TestUtils.fileContent(new File(baseDir.toFile(), name), StandardCharsets.UTF_8))
      .build();
    context.fileSystem().add(inputFile);
    return inputFile;
  }

  private List<String> listFiles(String folderName) {
    File folder = baseDir.resolve(folderName).toAbsolutePath().toFile();
    return listFilesInFolder(folder, baseDir.toFile());
  }
  private static List<String> listFilesInFolder(File folder, File baseFolder) {
    File[] files = folder.listFiles();
    List<String> allPaths = new ArrayList<>();
    for (File file : files) {
      if (file.isFile()) {
        String relativePath = baseFolder.toURI().relativize(file.toURI()).getPath();
        allPaths.add(relativePath);
      } else if (file.isDirectory()) {
        allPaths.addAll(listFilesInFolder(file, baseFolder));
      }
    }
    return allPaths;
  }
}
