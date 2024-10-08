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
package org.apiaddicts.apitools.dosonarapi.api.v4;

import org.apiaddicts.apitools.dosonarapi.asyncapi.BaseNodeTest;
import org.junit.Test;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

public class ChannelTest extends BaseNodeTest<AsyncApiGrammar> {

    private static void correctChannel(JsonNode node) {
        // Verify the channel description and server reference
        JsonNode channel = node.at("/myChannel");
        assertEquals("A simple channel description", channel, "/description");
        assertEquals("production", channel, "/servers");

        // Verify publish operation properties
        JsonNode publish = channel.at("/publish");
        assertEquals("publishOperation", publish, "/operationId");
        assertEquals("publish summary", publish, "/summary");
        assertEquals("publish description", publish, "/description");

        // Verify message properties
        JsonNode message = publish.at("/message");
        assertEquals("dimLight", message, "/name");
        assertEquals("hola", message, "/title");
        assertEquals("Command a particular streetlight to dim the lights.", message, "/summary");
        assertEquals("application/json", message, "/contentType");

        // Verify headers in the message
        JsonNode headers = message.at("/headers");
        assertEquals("object", headers, "/type");
        assertPropertyKeys(headers.at("/properties")).containsExactlyInAnyOrder("my-app-header");
        JsonNode myAppHeader = headers.at("/properties/my-app-header");
        assertEquals("integer", myAppHeader, "/type");
        assertEquals("0", myAppHeader, "/minimum");
        assertEquals("100", myAppHeader, "/maximum");

        // Verify payload in the message
        JsonNode payload = message.at("/payload");
        assertEquals("object", payload, "/type");
        assertPropertyKeys(payload.at("/properties")).containsExactlyInAnyOrder("percentage", "sentAt");

        JsonNode percentage = payload.at("/properties/percentage");
        assertEquals("integer", percentage, "/type");
        assertEquals("Percentage to which the light should be dimmed to.", percentage, "/description");
        assertEquals("0", percentage, "/minimum");
        assertEquals("100", percentage, "/maximum");

        JsonNode sentAt = payload.at("/properties/sentAt");
        assertEquals("string", sentAt, "/type");
        assertEquals("date-time", sentAt, "/format");
        assertEquals("Date and time when the message was sent.", sentAt, "/description");
    }

    @Test
    public void can_parse_channel_with_operations() {
        JsonNode node = parseResource(AsyncApiGrammar.CHANNELS, "/models/v4/channels.yaml");
        correctChannel(node);
    }
}
