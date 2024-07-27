/*
 * doSonarAPI: SonarQube AsyncAPI Plugin
 * Copyright (C) 2024-2024 Apiaddicts
 * contacta AT apiaddicts DOT org
 *
 * This program is free software; you can redistribute it y/or
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
package org.apiaddicts.apitools.dosonarapi.api;

import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstNodeType;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apiaddicts.apitools.dosonarapi.api.v4.AsyncApiGrammar;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

/**
 * A check on channels.
 */
public abstract class ResourceCheck extends AsyncApiCheck {
  private Set<String> channelPaths;

  @Override
  public final Set<AstNodeType> subscribedKinds() {
    return Sets.newHashSet(AsyncApiGrammar.CHANNEL);
  }

  @Override
  protected void visitFile(JsonNode root) {
    List<String> channels = root.at("/channels").propertyNames();
    this.channelPaths = extractResourceChannel(channels);
  }

  @Override
  protected final void visitNode(JsonNode node) {
    String channel = node.key().stringValue();
    if (!channelPaths.contains(ChannelUtils.trimTrailingSlash(channel))) {
      return;
    }
    visitResource(node);
  }

  protected abstract void visitResource(JsonNode node);

  private static Set<String> extractResourceChannel(List<String> paths) {
    Collections.sort(paths);
    Set<String> extractedPaths = new HashSet<>();
    
    for (int i = 0; i < paths.size(); ++i) {
        String path = ChannelUtils.trimTrailingSlash(paths.get(i));
        String[] fragments = path.split("/");
        
        if (fragments.length == 0 || fragments[fragments.length - 1].isEmpty() || ChannelUtils.isVariable(fragments[fragments.length - 1])) {
            continue;
        }
        
        if (fragments.length > 2 && !ChannelUtils.isVariable(fragments[fragments.length - 2])) {
            extractedPaths.add(path);
        } else if (i < paths.size() - 1) {
            String childPath = ChannelUtils.trimTrailingSlash(paths.get(i + 1));
            if (childPath.startsWith(path) && ChannelUtils.isVariable(ChannelUtils.terminalSegment(childPath))) {
                extractedPaths.add(path);
            }
        }
    }
    
    return extractedPaths;
  }
}
