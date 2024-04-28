/*
 * doSonarAPI: SonarQube AsyncAPI Plugin
 * Copyright (C) 2021-2022 Apiaddicts
 * contacta AT apiaddicts DOT org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.apiaddicts.apitools.dosonarapi.checks;

import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstNodeType;
import java.util.Set;
import org.sonar.check.Rule;
import org.apiaddicts.apitools.dosonarapi.api.AsyncApiCheck;
import org.apiaddicts.apitools.dosonarapi.api.v4.AsyncApiGrammar;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

@Rule(key = SummaryCapitalCheck.CHECK_KEY)
public class SummaryCapitalCheck extends AsyncApiCheck {

    public static final String CHECK_KEY = "SummaryCapital";

    @Override
    public Set<AstNodeType> subscribedKinds() {
        return Sets.newHashSet(AsyncApiGrammar.CHANNEL, AsyncApiGrammar.OPERATION, AsyncApiGrammar.MESSAGE); 
    }

    @Override
    public void visitNode(JsonNode node) {
        JsonNode channelsNode = node.get("channels");
        if (channelsNode != null) {
            for (JsonNode channel : channelsNode.propertyMap().values()) {
                JsonNode publishNode = channel.get("publish");
                if (publishNode != null) {
                    checkSummaryFormat(publishNode.get("summary"));
                }
            }
        }
    }

    private void checkSummaryFormat(JsonNode summaryNode) {
        if (summaryNode.isMissing()) {
            return;
        }

        String summary = summaryNode.getTokenValue();
        summary = summary == null ? "" : summary.trim();

        if (summary.isEmpty() || !Character.isUpperCase(summary.charAt(0)) || !summary.endsWith(".")) {
            addIssue("Summaries must start with a capital letter.", summaryNode);
        }
    }
}