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
package org.apiaddicts.apitools.dosonarapi.api;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import org.apiaddicts.apitools.dosonarapi.asyncapi.AsyncApiConfiguration;
import org.apiaddicts.apitools.dosonarapi.asyncapi.parser.AsyncApiv4Parser;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.YamlParser;

public class TestAsyncApiVisitorRunner {

  private TestAsyncApiVisitorRunner() {
  }

  public static void scanFile(File file, AsyncApiVisitor... visitors) {
    AsyncApiVisitorContext context = createContext(file);
    for (AsyncApiVisitor visitor : visitors) {
      visitor.scanFile(context);
    }
  }

  public static void scanFileForComments(File file, AsyncApiVisitor... visitors) {
    AsyncApiVisitorContext context = createContextWithComments(file);
    for (AsyncApiVisitor visitor : visitors) {
      visitor.scanFile(context);
    }
  }

  public static AsyncApiVisitorContext createContextWithComments(File file) {
    AsyncApiConfiguration configuration = new AsyncApiConfiguration(StandardCharsets.UTF_8, true);
    YamlParser parser = AsyncApiv4Parser.createV4(configuration);
    return createContext(file, parser);
  }

  public static AsyncApiVisitorContext createContext(File file) {
    AsyncApiConfiguration configuration = new AsyncApiConfiguration(StandardCharsets.UTF_8, true);
    YamlParser parser = AsyncApiv4Parser.createV4(configuration);
    return createContext(file, parser);
  }

  public static AsyncApiVisitorContext createContext(File file, YamlParser parser) {
    TestAsyncApiFile asyncApiFile = new TestAsyncApiFile(file);
    try {
      String content = getContent(file);
      JsonNode rootTree = parser.parse(content);
      return new AsyncApiVisitorContext(rootTree, parser.getIssues(), asyncApiFile);
    } catch (IOException ex) {
      throw new IllegalStateException("Error reading file", ex);
    }
  }

  private static String getContent(File file) throws IOException {
    String[] lines = new String(Files.readAllBytes(Paths.get(file.getPath()))).split("\n");
    for (int i = 1; i < lines.length; i++) {
      if (!lines[i].trim().isEmpty()) continue;
      int n = lines[i-1].indexOf(lines[i-1].trim());
      if (n < 0) n = 0;
      lines[i] = String.join("", Collections.nCopies(n, " ")) + "#";
    }
    return String.join("\n", lines);
  }

  private static class TestAsyncApiFile implements AsyncApiFile {

    private final File file;

    public TestAsyncApiFile(File file) {
      this.file = file;
    }

    @Override
    public String content() {
      try {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new IllegalStateException("Cannot read " + file, e);
      }
    }

    @Override
    public String fileName() {
      return file.getName();
    }
  }

}