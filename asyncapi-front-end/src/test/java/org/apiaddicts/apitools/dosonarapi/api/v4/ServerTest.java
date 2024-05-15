package org.apiaddicts.apitools.dosonarapi.api.v4;

import org.apiaddicts.apitools.dosonarapi.asyncapi.BaseNodeTest;
import org.junit.Test;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

public class ServerTest extends BaseNodeTest<AsyncApiGrammar> {

  private static void correctServer(JsonNode node) {
    JsonNode op = node.at("/production");
    assertEquals("The production MQTT broker", op, "/description");
    assertEquals("mqtt://{username}.gigantic-server.com:{port}/{basePath}", op, "/url");
    assertEquals("mqtt", op, "/protocol");
    assertPropertyKeys(op, "/variables").containsExactlyInAnyOrder("username", "port", "basePath");
    assertEquals("demo", op, "/variables/username/default");
    assertEquals("this value is assigned by the service provider, in this example `gigantic-server.com`", op, "/variables/username/description");
    assertElements(op, "/variables/port/enum").containsExactly("1883", "8883");
    assertEquals("1883", op, "/variables/port/default");
    assertEquals("v2", op, "/variables/basePath/default");
  }

  @Test
  public void can_parse_server_with_variable() {
    JsonNode node = parseResource(AsyncApiGrammar.SERVERS, "/models/v4/server.yaml");
    correctServer(node);
  }
}
