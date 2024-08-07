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

import java.io.IOException;
import org.sonar.api.batch.fs.InputFile;
import org.apiaddicts.apitools.dosonarapi.api.AsyncApiFile;

public abstract class SonarQubeAsyncApiFile implements AsyncApiFile {

  private final InputFile inputFile;

  private SonarQubeAsyncApiFile(InputFile inputFile) {
    this.inputFile = inputFile;
  }

  public static AsyncApiFile create(InputFile inputFile) {
    return new Sq62File(inputFile);
  }

  @Override
  public String fileName() {
    return inputFile.filename();
  }

  public InputFile inputFile() {
    return inputFile;
  }

  private static class Sq62File extends SonarQubeAsyncApiFile {

    public Sq62File(InputFile inputFile) {
      super(inputFile);
    }

    @Override
    public String content() {
      try {
        return inputFile().contents();
      } catch (IOException e) {
        throw new IllegalStateException("Could not read content of input file " + inputFile(), e);
      }
    }

  }

}