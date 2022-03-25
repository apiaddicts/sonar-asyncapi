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

import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstNodeType;
import java.util.Map;
import java.util.Set;
import org.sonar.check.Rule;
import org.apiaddicts.apitools.dosonarapi.api.OpenApiCheck;
import org.apiaddicts.apitools.dosonarapi.api.v2.OpenApi2Grammar;
import org.apiaddicts.apitools.dosonarapi.api.v3.OpenApi3Grammar;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

@Rule(key = NoContentIn204Check.CHECK_KEY)
public class NoContentIn204Check extends OpenApiCheck {
  public static final String CHECK_KEY = "NoContentIn204";

  @Override
  public Set<AstNodeType> subscribedKinds() {
    return Sets.newHashSet(OpenApi2Grammar.OPERATION, OpenApi3Grammar.OPERATION);
  }

  @Override
  protected void visitNode(JsonNode node) {
    JsonNode responsesNode = node.at("/responses");
    Map<String, JsonNode> responses = responsesNode.propertyMap();

    responses.entrySet().stream()
        .filter(e -> e.getKey().equals("204"))
        .map(Map.Entry::getValue)
        .forEach(this::checkNoContent);
  }

  private void checkNoContent(JsonNode response) {
    JsonNode effective = response.resolve();
    if (hasContent(effective)) {
      addIssue("204 No Content MUST NOT return anything.", response.key());
    }
  }

  private static boolean hasContent(JsonNode effective) {
    return effective.getType() instanceof OpenApi2Grammar && !effective.get("schema").isMissing()
        || effective.getType() instanceof OpenApi3Grammar && ! effective.get("content").isMissing();
  }
}
