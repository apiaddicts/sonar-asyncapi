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
package org.apiaddicts.apitools.dosonarapi.asyncapi.metrics;

import java.io.File;

import org.apiaddicts.apitools.dosonarapi.asyncapi.metrics.AsyncApiFileMetrics;
import org.junit.Test;
import org.apiaddicts.apitools.dosonarapi.api.TestAsyncApiVisitorRunner;

import static org.assertj.core.api.Assertions.assertThat;


public class AsyncApiFileMetricsTest {
  @Test
  public void operations() {
    assertThat(metrics("operations.yaml").numberOfOperations()).isEqualTo(2);
  }

  @Test
  public void channels() {
    assertThat(metrics("channels.yaml").numberOfChannels()).isEqualTo(1);
  }

  //TODO 1
  /*@Test
  public void schemas() {
    assertThat(metrics("schemas.yaml").numberOfSchemas()).isEqualTo(2);
  }

  @Test
  public void complexity() {
    assertThat(metrics("complexity.yaml").complexity()).isEqualTo(7);
  }*/

  private AsyncApiFileMetrics metrics(String fileName) {
    File baseDir = new File("src/test/resources/metrics/");
    File file = new File(baseDir, fileName);
    return new AsyncApiFileMetrics(TestAsyncApiVisitorRunner.createContext(file));
  }
}
