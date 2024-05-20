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
