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

import static org.assertj.core.api.Assertions.assertThat;

public class MessageTest extends BaseNodeTest<AsyncApiGrammar> {

  @Test
  public void can_parse_avro_inline_payload() {
    JsonNode node = parseResource(AsyncApiGrammar.PAYLOAD_SCHEMA, "/models/v4/avroPayload.yaml");
    assertEquals("application/vnd.apache.avro;version=1.9.0", node, "/schemaFormat");
    assertThat(node.at("/schema").isMissing()).isFalse();
    assertThat(issues).isEmpty();
  }

  @Test
  public void can_parse_avro_ref_payload() {
    JsonNode node = parseResource(AsyncApiGrammar.PAYLOAD_SCHEMA, "/models/v4/externalAvroPayload.yaml");
    assertEquals("application/vnd.apache.avro+json;version=1.9.0", node, "/schemaFormat");
    assertEquals("./userSchema.json", node, "/schema/$ref");
    assertThat(issues).isEmpty();
  }
}
