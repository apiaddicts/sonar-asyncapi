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
package org.apiaddicts.apitools.dosonarapi.checks;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.sonar.sslr.api.AstNodeType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.apiaddicts.apitools.dosonarapi.api.IssueLocation;
import org.apiaddicts.apitools.dosonarapi.api.AsyncApiCheck;
import org.apiaddicts.apitools.dosonarapi.api.PreciseIssue;
import org.apiaddicts.apitools.dosonarapi.api.v4.AsyncApiGrammar;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

import static org.apiaddicts.apitools.dosonarapi.checks.ChannelAmbiguityCheck.ConflictMode.AMBIGUOUS;
import static org.apiaddicts.apitools.dosonarapi.checks.ChannelAmbiguityCheck.ConflictMode.CLEAR;
import static org.apiaddicts.apitools.dosonarapi.api.PathUtils.isVariable;

@Rule(key = ChannelAmbiguityCheck.CHECK_KEY)
public class ChannelAmbiguityCheck extends AsyncApiCheck {
  public static final String AMBIGUOUS_MESSAGE = "These channels are ambiguous.";
  public static final String CHECK_KEY = "ChannelAmbiguity";

  @VisibleForTesting
  static String[] split(String source) {
    String s = strip(source);
    String[] split = s.split("/");
    if (source.endsWith("/")) {
      String[] result = new String[split.length + 1];
      System.arraycopy(split, 0, result, 0, split.length);
      result[split.length] = "";
      return result;
    } else {
      return split;
    }
  }

  private static String strip(String s) {
    if (s.startsWith("/")) {
      return s.substring(1);
    } else {
      return s;
    }
  }

  @Override
  public Set<AstNodeType> subscribedKinds() {
    return ImmutableSet.of(AsyncApiGrammar.CHANNELS); // Define correct AST node types for AsyncAPI
  }

  @Override
  public void visitNode(JsonNode node) {
    List<List<Bucket>> channelsByLength = sortChannelsByLength(node.propertyMap().values());
    for (List<Bucket> buckets : channelsByLength) {
      for (int i = 0; i < buckets.size() - 1; ++i) {
        for (int j = i + 1; j < buckets.size(); ++j) {
          ConflictMode mode = new ConflictChecker().check(buckets.get(i).channel, buckets.get(j).channel);
          if (mode != CLEAR) {
            String message = AMBIGUOUS_MESSAGE;
            PreciseIssue issue = addIssue(message, buckets.get(i).node);
            issue.secondary(IssueLocation.preciseLocation(message, buckets.get(j).node));
          }
        }
      }
    }
  }

  private List<List<Bucket>> sortChannelsByLength(Collection<JsonNode> properties) {
    ArrayList<List<Bucket>> result = new ArrayList<>();
    for (JsonNode property : properties) {
      JsonNode keyNode = property.key();
      String[] split = split(keyNode.getTokenValue());
      ensureSize(result, split.length);
      List<Bucket> list = result.get(split.length - 1);
      list.add(new Bucket(split, keyNode));
    }
    return result;
  }

  private static void ensureSize(List<List<Bucket>> list, int size) {
    if (list.size() < size) {
      for (int i = list.size(); i <= size; ++i) {
        list.add(new ArrayList<>());
      }
    }
  }

  enum ConflictMode {
    CLEAR,
    AMBIGUOUS
  }

  private static class Bucket {
    private final String[] channel;
    private final JsonNode node;

    private Bucket(String[] channel, JsonNode node) {
      this.channel = channel;
      this.node = node;
    }
  }

  @VisibleForTesting
  static class ConflictChecker {
    public ConflictMode check(String[] first, String[] second) {
      if (first.length != second.length) {
        return CLEAR;
      }
      for (int i = 0; i < first.length; ++i) {
        if (isVariable(first[i]) && isVariable(second[i])) {
          return AMBIGUOUS;
        } else if (!first[i].equals(second[i])) {
          return CLEAR;
        }
      }
      return AMBIGUOUS;
    }
  }
}
