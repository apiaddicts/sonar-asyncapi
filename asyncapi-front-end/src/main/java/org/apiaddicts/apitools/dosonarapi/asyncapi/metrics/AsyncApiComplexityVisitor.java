package org.apiaddicts.apitools.dosonarapi.asyncapi.metrics;

import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstNodeType;
import java.util.HashSet;

import org.apiaddicts.apitools.dosonarapi.api.AsyncApiVisitor;
import org.apiaddicts.apitools.dosonarapi.api.v4.AsyncApiGrammar;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

public class AsyncApiComplexityVisitor extends AsyncApiVisitor {

  private static final HashSet<AstNodeType> COMPLEXITY_TYPES = Sets.newHashSet(
      AsyncApiGrammar.CHANNEL, AsyncApiGrammar.OPERATION, AsyncApiGrammar.MESSAGE,
      AsyncApiGrammar.SCHEMA, AsyncApiGrammar.PARAMETER,
      AsyncApiGrammar.CHANNEL, AsyncApiGrammar.OPERATION, AsyncApiGrammar.MESSAGE,
      AsyncApiGrammar.SCHEMA, AsyncApiGrammar.PARAMETER
  );
  private int complexity;

  @Override
  protected boolean isSubscribed(AstNodeType nodeType) {
    return COMPLEXITY_TYPES.contains(nodeType);
  }

  @Override
  public void visitFile(JsonNode node) {
    complexity = 0;
  }

  @Override
  public void visitNode(JsonNode node) {
    if (!node.isRef()) {
      complexity++;
    }
  }

  public int getComplexity() {
    return complexity;
  }
}
