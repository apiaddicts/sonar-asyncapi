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

public class ChannelTest extends BaseNodeTest<AsyncApiGrammar> {

  private static void correctChannel(JsonNode node) {
    JsonNode channel = node.at("/myChannel");
    assertEquals("A simple channel description", channel, "/description");

    JsonNode publish = channel.at("/publish");
    assertEquals("publishOperation", publish, "/operationId");
    assertEquals("publish summary", publish, "/summary");
    assertEquals("publish description", publish, "/description");

    JsonNode message = publish.at("/message");
    assertEquals("Message description", message, "/description");
    JsonNode payload = message.at("/payload");
    assertEquals("object", payload, "/type");
    assertPropertyKeys(payload, "/properties").containsExactlyInAnyOrder("name", "age");
    assertEquals("string", payload, "/properties/name/type");
    assertEquals("integer", payload, "/properties/age/type");
  }

  @Test
  public void can_parse_channel_with_operations() {
    JsonNode node = parseResource(AsyncApiGrammar.CHANNELS, "/models/v4/channels.yaml");
    correctChannel(node);
  }
}
