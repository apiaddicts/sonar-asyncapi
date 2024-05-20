package org.apiaddicts.apitools.dosonarapi.api.v4;

import org.apiaddicts.apitools.dosonarapi.asyncapi.BaseNodeTest;
import org.junit.Test;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

public class SchemaTest extends BaseNodeTest<AsyncApiGrammar> {
    @Test
    public void can_parse_simple_schema() {
        JsonNode node = parseResource(AsyncApiGrammar.SCHEMA, "/models/shared/schema/simple.yaml");

        assertEquals("object", node, "/type");
        assertEquals("A simple model definition", node, "/description");
        assertElements(node, "/required").containsExactly("name", "address");

        JsonNode properties = node.at("/properties");
        assertPropertyKeys(properties).hasSize(3);

        assertEquals("string", properties, "/name/type");
        assertIsRef("#/components/schemas/Address", properties, "/address");

        assertEquals("integer", properties, "/age/type");
        assertEquals("int32", properties, "/age/format");
        assertEquals("0", properties, "/age/minimum");
    }

    @Test
    public void can_parse_schema_with_map_to_string() {
        JsonNode node = parseResource(AsyncApiGrammar.SCHEMA, "/models/shared/schema/string-to-string.yaml");
        assertEquals("object", node, "/type");
        assertEquals("string", node, "/additionalProperties/type");
    }

    @Test
    public void can_parse_schema_with_map_to_model() {
        JsonNode node = parseResource(AsyncApiGrammar.SCHEMA, "/models/shared/schema/string-to-model.yaml");
        assertEquals("object", node, "/type");
        assertIsRef("#/components/schemas/ComplexModel", node, "/additionalProperties");
    }

    @Test
    public void can_parse_schema_with_example() {
        JsonNode node = parseResource(AsyncApiGrammar.SCHEMA, "/models/shared/schema/with-example.yaml");

        assertEquals("object", node, "/type");
        assertElements(node, "/required").containsExactly("name");

        JsonNode properties = node.at("/properties");
        assertPropertyKeys(properties).hasSize(2);

        assertEquals("string", properties, "/name/type");
        assertEquals("integer", properties, "/id/type");
        assertEquals("int64", properties, "/id/format");

        assertEquals("Puma", node, "/example/name");
        assertEquals("1", node, "/example/id");
    }

    @Test
    public void can_parse_schema_with_composition() {
        JsonNode node = parseResource(AsyncApiGrammar.SCHEMA, "/models/shared/schema/with-composition.yaml");

        assertIsRef("#/components/schemas/ErrorModel", node, "/allOf/0");

        assertEquals("object", node, "/allOf/1/type");
        assertElements(node, "/allOf/1/required").containsExactly("rootCause");

        JsonNode properties = node.at("/allOf/1/properties");
        assertPropertyKeys(properties).hasSize(1);
        assertEquals("string", properties, "/rootCause/type");
    }
}
