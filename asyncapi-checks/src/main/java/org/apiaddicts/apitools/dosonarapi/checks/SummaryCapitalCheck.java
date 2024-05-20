package org.apiaddicts.apitools.dosonarapi.checks;

import com.google.common.collect.ImmutableSet;
import com.sonar.sslr.api.AstNodeType;
import org.sonar.check.Rule;
import org.apiaddicts.apitools.dosonarapi.api.AsyncApiCheck;
import org.apiaddicts.apitools.dosonarapi.api.v4.AsyncApiGrammar;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

import java.util.Set;

@Rule(key = SummaryCapitalCheck.KEY)
public class SummaryCapitalCheck extends AsyncApiCheck {

    public static final String KEY = "SummaryCapital";
    private static final String MESSAGE = "Summaries must start with a capital letter and end with a period.";

    @Override
    public Set<AstNodeType> subscribedKinds() {
        return ImmutableSet.of(AsyncApiGrammar.ROOT, AsyncApiGrammar.CHANNEL, AsyncApiGrammar.OPERATION, AsyncApiGrammar.MESSAGE);
    }

    @Override
    public void visitNode(JsonNode node) {
        if (AsyncApiGrammar.ROOT.equals(node.getType())) {
            checkInfoSummary(node);
            checkChannelsSummary(node);
        }
        if (AsyncApiGrammar.CHANNEL.equals(node.getType())) {
            visitChannelsNode(node);
        }
        if (AsyncApiGrammar.OPERATION.equals(node.getType()) || AsyncApiGrammar.MESSAGE.equals(node.getType())) {
            checkSummaryFormat(node.get("summary"));
        }
    }

    private void checkInfoSummary(JsonNode rootNode) {
        JsonNode infoNode = rootNode.get("info");
        if (infoNode != null) {
            checkSummaryFormat(infoNode.get("summary"));
        }
    }

    private void checkChannelsSummary(JsonNode rootNode) {
        JsonNode channelsNode = rootNode.get("channels");
        if (channelsNode != null) {
            for (JsonNode channel : channelsNode.propertyMap().values()) {
                checkSummaryFormat(channel.get("summary"));
            }
        }
    }

    protected void visitChannelsNode(JsonNode channelNode) {
        for (JsonNode operationNode : channelNode.propertyMap().values()) {
            if (operationNode.isObject()) {
                JsonNode publishNode = operationNode.get("publish");
                if (publishNode != null) {
                    checkSummaryFormat(publishNode.get("summary"));
                }
                JsonNode subscribeNode = operationNode.get("subscribe");
                if (subscribeNode != null) {
                    checkSummaryFormat(subscribeNode.get("summary"));
                }
            }
        }
    }

    private void checkSummaryFormat(JsonNode summaryNode) {
        // Verifica primero si el nodo de resumen es nulo o si el valor está ausente.
        if (summaryNode == null || summaryNode.isMissing()) {
            // No hacer nada si el nodo está ausente, ya que eso está permitido según tu criterio.
            return;
        }
    
        // Asegúrate de manejar el caso en que getTokenValue() devuelva null.
        String summary = summaryNode.getTokenValue();
        summary = summary == null ? "" : summary.trim();
    
        // Ahora verifica si el resumen está vacío después de eliminar espacios en blanco.
        if (summary.isEmpty()) {
            addIssue( MESSAGE, summaryNode);
            return;
        }
    
        // Verifica que el resumen comience con mayúscula y termine con punto.
        if (!Character.isUpperCase(summary.charAt(0)) || !summary.endsWith(".")) {
            addIssue(MESSAGE, summaryNode);
        }
    }
}
