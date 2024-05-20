package org.apiaddicts.apitools.dosonarapi.api.v4;

import org.apiaddicts.apitools.dosonarapi.asyncapi.BaseNodeTest;
import org.junit.Test;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

public class BindingsTest extends BaseNodeTest<AsyncApiGrammar> {

  private static void correctBindings(JsonNode node) {
    JsonNode binding = node.at("/mqtt");
    assertEquals("2", binding, "/qos");
    assertEquals("false", binding, "/retain");

    binding = node.at("/amqp");
    assertEquals("gzip", binding, "/contentEncoding");
    assertEquals("text", binding, "/messageType");
  }

  @Test
  public void can_parse_message_bindings() {
    JsonNode node = parseResource(AsyncApiGrammar.MESSAGE_BINDINGS, "/models/v4/bindings.yaml");
    correctBindings(node);
  }
}
