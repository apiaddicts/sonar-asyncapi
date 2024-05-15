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
