package org.apiaddicts.apitools.dosonarapi.api.v4;

import org.apiaddicts.apitools.dosonarapi.asyncapi.BaseNodeTest;
import org.junit.Test;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

public class ComponentsTest extends BaseNodeTest<AsyncApiGrammar> {

    private static void correctComponents(JsonNode node) {
        // Verificar schemas
        JsonNode schemas = node.at("/schemas");
        assertPropertyKeys(schemas).containsExactlyInAnyOrder("User", "Address");
        JsonNode userSchema = schemas.at("/User");
        assertEquals("object", userSchema, "/type");
        assertPropertyKeys(userSchema, "/properties").containsExactlyInAnyOrder("name", "age");
        assertEquals("string", userSchema, "/properties/name/type");
        assertEquals("integer", userSchema, "/properties/age/type");

        // Verificar messages
        JsonNode messages = node.at("/messages");
        assertPropertyKeys(messages).containsExactlyInAnyOrder("UserCreated", "UserUpdated");
        JsonNode userCreatedMessage = messages.at("/UserCreated");
        assertEquals("User created message", userCreatedMessage, "/description");
        JsonNode userCreatedPayload = userCreatedMessage.at("/payload");
        assertEquals("object", userCreatedPayload, "/type");
        assertPropertyKeys(userCreatedPayload, "/properties").containsExactlyInAnyOrder("name", "age");
        assertEquals("string", userCreatedPayload, "/properties/name/type");
        assertEquals("integer", userCreatedPayload, "/properties/age/type");
        
        JsonNode userUpdatedMessage = messages.at("/UserUpdated");
        assertEquals("User updated message", userUpdatedMessage, "/description");
        JsonNode userUpdatedPayload = userUpdatedMessage.at("/payload");
        assertEquals("object", userUpdatedPayload, "/type");
        assertPropertyKeys(userUpdatedPayload, "/properties").containsExactlyInAnyOrder("name", "age");
        assertEquals("string", userUpdatedPayload, "/properties/name/type");
        assertEquals("integer", userUpdatedPayload, "/properties/age/type");

        // Verificar security schemes
        JsonNode securitySchemes = node.at("/securitySchemes");
        assertPropertyKeys(securitySchemes).containsExactlyInAnyOrder("basicAuth", "apiKey");
        JsonNode basicAuthScheme = securitySchemes.at("/basicAuth");
        assertEquals("http", basicAuthScheme, "/type");
        assertEquals("basic", basicAuthScheme, "/scheme");
    }

    @Test
    public void can_parse_components() {
        JsonNode node = parseResource(AsyncApiGrammar.COMPONENTS, "/models/v4/components.yaml");
        correctComponents(node);
    }
}
