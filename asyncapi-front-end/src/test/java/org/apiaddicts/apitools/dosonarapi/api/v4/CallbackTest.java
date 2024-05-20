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
package org.apiaddicts.apitools.dosonarapi.api.v4;

import org.apiaddicts.apitools.dosonarapi.asyncapi.BaseNodeTest;
import org.junit.Test;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

public class CallbackTest extends BaseNodeTest<AsyncApiGrammar> {

  private static void correctCallback(JsonNode node) {
    JsonNode callback = node.at("/myCallback");
    assertEquals("http://example.com/callback", callback, "/$ref");

    JsonNode subscribe = callback.at("/subscribe");
    assertEquals("subscribeOperation", subscribe, "/operationId");
    assertEquals("Subscribe operation summary", subscribe, "/summary");
    assertEquals("Subscribe operation description", subscribe, "/description");

    JsonNode message = subscribe.at("/message");
    assertEquals("Message description", message, "/description");
    JsonNode payload = message.at("/payload");
    assertEquals("object", payload, "/type");
    assertPropertyKeys(payload, "/properties").containsExactlyInAnyOrder("name", "age");
    assertEquals("string", payload, "/properties/name/type");
    assertEquals("integer", payload, "/properties/age/type");
  }

  @Test
  public void can_parse_callback_with_operations() {
    JsonNode node = parseResource(AsyncApiGrammar.CALLBACKS, "/models/v4/callback.yaml");
    correctCallback(node);
  }
}
