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
package org.apiaddicts.apitools.dosonarapi.api.v2;

import org.junit.Test;
import org.apiaddicts.apitools.dosonarapi.openapi.BaseNodeTest;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

public class ContactTest extends BaseNodeTest<OpenApi2Grammar> {
  @Test
  public void can_parse_contact() {
    JsonNode node = parseResource(OpenApi2Grammar.CONTACT, "/models/shared/contact.yaml");

    assertEquals("API Support", node, "/name");
    assertEquals("http://www.swagger.io/support", node, "/url");
    assertEquals("support@swagger.io", node, "/email");
  }
}
