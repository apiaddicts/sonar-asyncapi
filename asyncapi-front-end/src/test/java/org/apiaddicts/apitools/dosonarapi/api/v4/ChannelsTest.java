package org.apiaddicts.apitools.dosonarapi.api.v4;

import org.apiaddicts.apitools.dosonarapi.asyncapi.BaseNodeTest;
import org.junit.Test;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

public class ChannelsTest extends BaseNodeTest<AsyncApiGrammar> {
    @Test
    public void can_parse_path_item() {
      JsonNode node = parseResource(AsyncApiGrammar.CHANNELS, "/models/v4/channels.yaml");
  
      assertPropertyKeys(node).containsOnly("pets");
    }
  }
