/*
 * doSonarAPI: SonarQube AsyncAPI Plugin
 * Copyright (C) 2024-2024 Apiaddicts
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
package org.apiaddicts.apitools.dosonarapi.api;

import org.apiaddicts.apitools.dosonarapi.api.AsyncApiVersion;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

public class AsyncApiPathResolver {

  private final JsonNode rootNode;
  private final AsyncApiVersion version;

  public AsyncApiPathResolver(JsonNode rootNode, AsyncApiVersion version) {
    this.rootNode = rootNode;
    this.version = version;
  }

  public JsonNode getChannels() {
    if (rootNode == null) return null;
    return rootNode.get("channels");
  }

  public JsonNode getOperations() {
    if (rootNode == null) return null;
    if (version.isVersion3()) {
      return rootNode.get("operations");
    }
    return null;
  }

  public JsonNode getServers() {
    if (rootNode == null) return null;
    return rootNode.get("servers");
  }

  public JsonNode getComponents() {
    if (rootNode == null) return null;
    return rootNode.get("components");
  }

  public String getChannelAddress(JsonNode channelNode) {
    if (channelNode == null) return null;
    if (version.isVersion3()) {
      JsonNode addressNode = channelNode.get("address");
      if (addressNode != null && !addressNode.isMissing()) {
        return addressNode.getTokenValue();
      }
    }
    return null;
  }

  public JsonNode getOperationFromChannel(JsonNode channelNode, String operationType) {
    if (channelNode == null || !version.isVersion2()) {
      return null;
    }
    return channelNode.get(operationType);
  }

  public JsonNode getPublishOperation(JsonNode channelNode) {
    return getOperationFromChannel(channelNode, "publish");
  }

  public JsonNode getSubscribeOperation(JsonNode channelNode) {
    return getOperationFromChannel(channelNode, "subscribe");
  }

  public boolean isVersion3() {
    return version.isVersion3();
  }

  public boolean isVersion2() {
    return version.isVersion2();
  }
}
