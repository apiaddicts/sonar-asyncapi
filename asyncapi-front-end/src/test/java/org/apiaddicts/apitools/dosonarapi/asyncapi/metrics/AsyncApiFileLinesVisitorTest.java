/*
 * doSonarAPI: SonarQube OpenAPI Plugin
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
package org.apiaddicts.apitools.dosonarapi.asyncapi.metrics;

import java.io.File;

import org.apiaddicts.apitools.dosonarapi.asyncapi.metrics.AsyncApiFileLinesVisitor;
import org.junit.Test;
import org.apiaddicts.apitools.dosonarapi.api.TestAsyncApiVisitorRunner;

import static org.assertj.core.api.Assertions.assertThat;


public class AsyncApiFileLinesVisitorTest {
  private static final File BASE_DIR = new File("src/test/resources/metrics");

  @Test
  public void can_report_metrics() {
    AsyncApiFileLinesVisitor visitor = new AsyncApiFileLinesVisitor();

    TestAsyncApiVisitorRunner.scanFile(new File(BASE_DIR, "file-lines.yaml"), visitor);

    // sonar extensions are counted as lines of code
    assertThat(visitor.getLinesOfCode()).hasSize(25);
    assertThat(visitor.getLinesOfCode()).containsOnly( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 27, 28);
    assertThat(visitor.getLinesOfComments()).hasSize(3);
    assertThat(visitor.getLinesOfComments()).containsOnly(11, 25, 26);

    // x-nosonar is a global modifier, it is ignored in the report
    assertThat(visitor.getLinesWithNoSonar()).hasSize(2);
    assertThat(visitor.getLinesWithNoSonar()).containsOnly(17, 12);
  }

  @Test
  public void correctly_reports_strings_with_embedded_newline() {
    AsyncApiFileLinesVisitor visitor = new AsyncApiFileLinesVisitor();

    TestAsyncApiVisitorRunner.scanFile(new File(BASE_DIR, "embedded-newlines-lines.yaml"), visitor);

    assertThat(visitor.getLinesOfCode()).hasSize(25);
    assertThat(visitor.getLinesOfCode()).containsOnly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 27, 28);
  }

  @Test
  public void correctly_reports_multiline_strings() {
    AsyncApiFileLinesVisitor visitor = new AsyncApiFileLinesVisitor();

    TestAsyncApiVisitorRunner.scanFile(new File(BASE_DIR, "multiline-lines.yaml"), visitor);

    assertThat(visitor.getLinesOfCode()).hasSize(27);
    assertThat(visitor.getLinesOfCode()).containsOnly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 29, 30);
  }
}
