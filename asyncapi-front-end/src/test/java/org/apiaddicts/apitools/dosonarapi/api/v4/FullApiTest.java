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

public class FullApiTest extends BaseNodeTest<AsyncApiGrammar> {
    @Test
    public void can_parse_full_contract() {
        JsonNode node = parseResource(AsyncApiGrammar.ROOT, "/models/v4/full-api.yaml");

        assertEquals("2.3.0", node, "/asyncapi");
        assertEquals("urn:example:rpcclient", node, "/id");
        assertEquals("application/json", node, "/defaultContentType");

        // Verify info section
        assertEquals("Sample AsyncAPI", node, "/info/title");
        assertEquals("1.0.0", node, "/info/version");
        assertEquals("A sample API that uses a petstore as an example to demonstrate features in the AsyncAPI 2.3.0 specification", node, "/info/description");
        assertEquals("http://example.com/terms/", node, "/info/termsOfService");

        // Verify contact section
        assertEquals("API Support", node, "/info/contact/name");
        assertEquals("http://www.example.com/support", node, "/info/contact/url");
        assertEquals("support@example.com", node, "/info/contact/email");

        // Verify license section
        assertEquals("Apache 2.0", node, "/info/license/name");
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0.html", node, "/info/license/url");

        // Verify servers section
        assertEquals("api.example.com", node, "/servers/production/url");
        assertEquals("https", node, "/servers/production/protocol");
        assertEquals("Production server", node, "/servers/production/description");

        // Verify channels section
        assertKeys(node.at("/channels").properties()).containsOnly("pets", "pets/{id}");
        assertEquals("Channel for pet events", node, "/channels/pets/description");

        // Verify publish operation for pets
        assertEquals("Notify about a new pet", node, "/channels/pets/publish/summary");
        assertEquals("petAdded", node, "/channels/pets/publish/operationId");

        // Verify message for pets
        assertEquals("application/json", node, "/channels/pets/publish/message/contentType");

        // Verify message headers for pets
        assertEquals("string", node, "/channels/pets/publish/message/headers/properties/Authorization/type");

        // Verify message payload for pets
        assertEquals("integer", node, "/channels/pets/publish/message/payload/properties/id/type");
        assertEquals("string", node, "/channels/pets/publish/message/payload/properties/name/type");

        // Verify publish operation for pets/{id}
        System.out.println("Summary: " + node.at("/channels/pets/{id}/publish/summary").getTokenValue());

        // Verify message for pets/{id}

        // Verify message headers for pets/{id}

        // Verify message payload for pets/{id}

        // Verify components/messages
        assertPropertyKeys(node, "/components/messages").containsOnly("PetAdded");
        assertEquals("application/json", node, "/components/messages/PetAdded/contentType");

        // Verify components/schemas
        assertPropertyKeys(node, "/components/schemas").containsOnly("Pet", "Error");
        assertEquals("integer", node, "/components/schemas/Pet/properties/id/type");
        assertEquals("string", node, "/components/schemas/Pet/properties/name/type");

        // Verify components/securitySchemes
        assertPropertyKeys(node, "/components/securitySchemes").containsOnly("basicAuth", "apiKey");
        assertEquals("http", node, "/components/securitySchemes/basicAuth/type");
        assertEquals("basic", node, "/components/securitySchemes/basicAuth/scheme");
        assertEquals("apiKey", node, "/components/securitySchemes/apiKey/type");
        assertEquals("api_key", node, "/components/securitySchemes/apiKey/name");
        assertEquals("header", node, "/components/securitySchemes/apiKey/in");

        // Verify externalDocs
        assertEquals("Find more info here", node, "/externalDocs/description");
        assertEquals("http://example.com", node, "/externalDocs/url");
    }
}
