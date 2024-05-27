/*
 * doSonarAPI: SonarQube AsyncAPI Plugin
 * Copyright (C) 2021 Apiaddicts
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
package org.apiaddicts.apitools.dosonarapi.it.plugin;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import java.io.File;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.WsMeasures.Measure;

import static org.apiaddicts.apitools.dosonarapi.it.plugin.Tests.getMeasure;
import static org.apiaddicts.apitools.dosonarapi.it.plugin.Tests.getMeasureAsDouble;
import static org.apiaddicts.apitools.dosonarapi.it.plugin.Tests.getMeasureAsInt;
import static org.assertj.core.api.Assertions.assertThat;

public class MetricsTest {

  private static final String PROJECT_KEY = "metrics";

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  @BeforeClass
  public static void startServer() {
    orchestrator.getServer().provisionProject(PROJECT_KEY, PROJECT_KEY);
    orchestrator.getServer().associateProjectToQualityProfile(PROJECT_KEY, "asyncapi", "two_rules");
    SonarScanner build = SonarScanner.create()
      .setProjectDir(new File("projects/metrics"))
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_KEY)
      .setProjectVersion("1.0-SNAPSHOT")
      .setSourceDirs("src");
    orchestrator.executeBuild(build);
  }

  @Test
  public void project_level() {
    // Size
    assertThat(getProjectMeasureAsInt("lines")).isEqualTo(25); // Total lines in the AsyncAPI document
    assertThat(getProjectMeasureAsInt("files")).isEqualTo(1); // Assuming the AsyncAPI definition is in one file

    // Documentation
    assertThat(getProjectMeasureAsInt("ncloc")).isEqualTo(22); // Non-comment lines of code, assuming headers aren't counted as comments
    assertThat(getProjectMeasureAsInt("comment_lines")).isEqualTo(3); // Lines that are comments
    assertThat(getProjectMeasureAsDouble("comment_lines_density")).isEqualTo((3.0 / 25) * 100); // Percentage of comment lines

    // Complexity
    assertThat(getProjectMeasureAsInt("complexity")).isEqualTo(3); // Simplified estimation of complexity

    // Duplication
    assertThat(getProjectMeasureAsDouble("duplicated_lines")).isZero();
    assertThat(getProjectMeasureAsDouble("duplicated_blocks")).isZero();
    assertThat(getProjectMeasureAsDouble("duplicated_files")).isZero();
    assertThat(getProjectMeasureAsDouble("duplicated_lines_density")).isZero();

    // Rules
    assertThat(getProjectMeasureAsDouble("violations")).isEqualTo(2); // Hypothetical analysis of best practices or linting violations
  }

  @Test
  public void directory_level() {
    // Size
    assertThat(getDirectoryMeasureAsInt("files")).isEqualTo(1); // Total files in the directory, assuming 1 AsyncAPI document
    assertThat(getDirectoryMeasureAsInt("directories")).isEqualTo(0); // Assuming the AsyncAPI document is at the root with no subdirectories

    // Documentation
    assertThat(getDirectoryMeasureAsInt("ncloc")).isEqualTo(22); // Non-comment lines of code, assuming headers aren't counted as comments
    assertThat(getDirectoryMeasureAsInt("comment_lines")).isEqualTo(3); // Lines that are comments
    assertThat(getDirectoryMeasureAsDouble("comment_lines_density")).isEqualTo((3.0 / 25) * 100); // Percentage of comment lines

    // Complexity
    assertThat(getDirectoryMeasureAsInt("complexity")).isEqualTo(3); // Simplified estimation of complexity

    // Duplication
    assertThat(getDirectoryMeasureAsInt("duplicated_lines")).isZero();
    assertThat(getDirectoryMeasureAsInt("duplicated_blocks")).isZero();
    assertThat(getDirectoryMeasureAsInt("duplicated_files")).isZero();
    assertThat(getDirectoryMeasureAsDouble("duplicated_lines_density")).isZero();

    // Rules
    assertThat(getDirectoryMeasureAsInt("violations")).isEqualTo(2); // Hypothetical analysis of best practices or linting violations
  }

  @Test
  public void file_level() {
    // Size
    assertThat(getFileMeasureAsInt("lines")).isEqualTo(25); // Total lines in the AsyncAPI document
    assertThat(getFileMeasureAsInt("files")).isEqualTo(1); // Just one AsyncAPI file
    // Adapting to AsyncAPI: operations become actions within channels, paths become channels, and schemas are based on messages
    assertThat(getFileMeasureAsInt("operations_count")).isEqualTo(1); // Number of subscribe operations within channels
    assertThat(getFileMeasureAsInt("channels_count")).isEqualTo(1); // Channels in AsyncAPI
    assertThat(getFileMeasureAsInt("messages_count")).isEqualTo(1); // Assuming one main message schema based on "petListRequest"

    // Documentation
    assertThat(getFileMeasureAsInt("ncloc")).isEqualTo(22); // Non-comment lines, assuming the YAML structure without comments
    assertThat(getFileMeasureAsInt("comment_lines")).isEqualTo(3); // Lines that are comments (e.g., Some header)
    assertThat(getFileMeasureAsDouble("comment_lines_density")).isEqualTo((3.0 / 25) * 100); // Percentage of comment lines

    // Complexity
    assertThat(getFileMeasureAsInt("complexity")).isEqualTo(3); // Simplified estimation of complexity, assuming a basic structure for the document

    // Duplication
    assertThat(getFileMeasureAsInt("duplicated_lines")).isZero();
    assertThat(getFileMeasureAsInt("duplicated_blocks")).isZero();
    assertThat(getFileMeasureAsInt("duplicated_files")).isZero();
    assertThat(getFileMeasureAsDouble("duplicated_lines_density")).isZero();

    // Rules
    assertThat(getFileMeasureAsInt("violations")).isEqualTo(2); // Hypothetical linting or style guide violations, adjusted for AsyncAPI specifics
  }

  @Test
  public void should_be_compatible_with_DevCockpit() {

    assertThat(getFileMeasure("ncloc_data").getValue().split(";"))
      .doesNotContain("1=1")
      .contains("5=1");
    assertThat(getFileMeasure("comment_lines_data").getValue().split(";"))
        .doesNotContain("5=1")
        .contains("2=1");
  }

  /* Helper methods */

  private Integer getProjectMeasureAsInt(String metricKey) {
    return getMeasureAsInt(PROJECT_KEY, metricKey);
  }

  private Double getProjectMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble(PROJECT_KEY, metricKey);
  }

  private Integer getDirectoryMeasureAsInt(String metricKey) {
    return getMeasureAsInt(keyFor("openapi/v2"), metricKey);
  }

  private Double getDirectoryMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble(keyFor("openapi/v2"), metricKey);
  }

  private Measure getFileMeasure(String metricKey) {
    return getMeasure(keyFor("openapi/v2/sample.yaml"), metricKey);
  }

  private Integer getFileMeasureAsInt(String metricKey) {
    return getMeasureAsInt(keyFor("openapi/v2/sample.yaml"), metricKey);
  }

  private Double getFileMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble(keyFor("openapi/v2/sample.yaml"), metricKey);
  }

  private static String keyFor(String s) {
    return PROJECT_KEY + ":src/" + s;
  }

}
