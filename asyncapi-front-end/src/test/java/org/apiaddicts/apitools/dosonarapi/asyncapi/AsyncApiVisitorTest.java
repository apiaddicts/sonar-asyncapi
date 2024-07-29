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
package org.apiaddicts.apitools.dosonarapi.asyncapi;

import com.sonar.sslr.api.AstNodeType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.apiaddicts.apitools.dosonarapi.api.AsyncApiVisitor;
import org.apiaddicts.apitools.dosonarapi.api.TestAsyncApiVisitorRunner;
import org.apiaddicts.apitools.dosonarapi.api.v4.AsyncApiGrammar;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncApiVisitorTest {

  @Test
  public void can_visit_specific_nodes_from_yaml() {
    AsyncApiTestVisitor visitor = new AsyncApiTestVisitor();
    TestAsyncApiVisitorRunner.scanFile(new File(AsyncApiVisitorTest.class.getResource("/petstore.yaml").getFile()), visitor);
    assertThat(visitor.pathKeys).containsExactly("/pets", "/pets/{petId}");
    assertThat(visitor.fileName).isEqualTo("petstore.yaml");
  }

  @Test
  public void can_visit_specific_nodes_from_json() {
    AsyncApiTestVisitor visitor = new AsyncApiTestVisitor();
    TestAsyncApiVisitorRunner.scanFile(new File(AsyncApiVisitorTest.class.getResource("/petstore.json").getFile()), visitor);
    assertThat(visitor.pathKeys).containsExactly("/pets", "/pets/{petId}");
    assertThat(visitor.fileName).isEqualTo("petstore.json");
  }

  public class AsyncApiTestVisitor extends AsyncApiVisitor {

    private List<String> pathKeys = new ArrayList<>();
    private String fileName;

    @Override
    public boolean isSubscribed(AstNodeType type) {
      return type == AsyncApiGrammar.CHANNEL;
    }

    @Override
    public void visitNode(JsonNode node) {
      pathKeys.add(node.key().getTokenValue());
      fileName = getContext().asyncApiFile().fileName();
    }

  }

}
