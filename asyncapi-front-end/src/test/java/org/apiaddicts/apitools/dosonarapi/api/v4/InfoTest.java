package org.apiaddicts.apitools.dosonarapi.api.v4;

import org.apiaddicts.apitools.dosonarapi.asyncapi.BaseNodeTest;
import org.junit.Test;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.ValidationException;

public class InfoTest extends BaseNodeTest<AsyncApiGrammar> {
    @Test
    public void can_parse_info_with_missing_optionals() {
      JsonNode node = parseResource(AsyncApiGrammar.INFO, "/models/shared/info/minimal.yaml");
  
      assertMissing(node.at("/description"));
      assertMissing(node.at("/termsOfService"));
      assertMissing(node.at("/contact"));
      assertMissing(node.at("/license"));
  
      assertEquals("simple model", node, "/title");
  
      assertEquals("1.0.0", node, "/version");
    }
  
    @Test
    public void can_parse_info_with_flow() {
      JsonNode node = parseResource(AsyncApiGrammar.INFO, "/models/shared/info/withFlow.yaml");
  
      assertEquals("\nThis is a multiline description", node, "/description");
    }
  
    @Test
    public void can_parse_full_info() {
      JsonNode node = parseResource(AsyncApiGrammar.INFO, "/models/shared/info/full.yaml");
  
      assertEquals("http://example.com/terms/", node, "/termsOfService");
      assertEquals("API Support", node, "/contact/name");
      assertEquals("http://www.example.com/support", node, "/contact/url");
      assertEquals("support@example.com", node, "/contact/email");
      assertEquals("Apache 2.0", node, "/license/name");
      assertEquals("https://www.apache.org/licenses/LICENSE-2.0.html", node, "/license/url");
    }
  
    @Test(expected = ValidationException.class)
    public void throws_when_parsing_incomplete_object() {
      parseResource(AsyncApiGrammar.INFO, "/models/shared/info/incomplete.yaml");
    }
  }
