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
package org.apiaddicts.apitools.dosonarapi.checks;

import org.apiaddicts.apitools.dosonarapi.api.AsyncApiCheck;
import org.apiaddicts.apitools.dosonarapi.api.AsyncApiPathResolver;
import org.apiaddicts.apitools.dosonarapi.api.AsyncApiVisitorContext;
import org.apiaddicts.apitools.dosonarapi.api.AsyncApiVersion;
import org.apiaddicts.apitools.dosonarapi.sslr.yaml.grammar.JsonNode;

public abstract class VersionAwareAsyncApiCheck extends AsyncApiCheck {

  protected AsyncApiPathResolver pathResolver;

  @Override
  public final void scanFile(AsyncApiVisitorContext context) {
    this.pathResolver = new AsyncApiPathResolver(context.rootTree(), context.getVersion());
    if (context.getVersion().isVersion2()) {
      visitFileV2(context);
    } else if (context.getVersion().isVersion3()) {
      visitFileV3(context);
    }
    super.scanFile(context);
  }

  protected void visitFileV2(AsyncApiVisitorContext context) {
  }

  protected void visitFileV3(AsyncApiVisitorContext context) {
  }

  protected AsyncApiVersion getVersion(AsyncApiVisitorContext context) {
    return context.getVersion();
  }

  protected void visitChannelsV2(JsonNode channelNode, JsonNode operationNode) {
    JsonNode publish = operationNode.get("publish");
    if (publish != null) {
      visitOperationV2(operationNode.getPointer(), publish, "publish");
    }
    JsonNode subscribe = operationNode.get("subscribe");
    if (subscribe != null) {
      visitOperationV2(operationNode.getPointer(), subscribe, "subscribe");
    }
  }

  protected void visitChannelsV3(JsonNode channelNode) {
    JsonNode operationsNode = channelNode.get("operations");
    if (operationsNode != null) {
      for (JsonNode operation : operationsNode.propertyMap().values()) {
        visitOperationV3(channelNode.getPointer(), operation);
      }
    }
  }

  protected void visitOperationV2(String channelPointer, JsonNode operation, String operationType) {
  }

  protected void visitOperationV3(String channelPointer, JsonNode operation) {
  }

  protected void visitServersV2(JsonNode serversNode) {
    if (serversNode != null && serversNode.isArray()) {
      for (JsonNode server : serversNode.elements()) {
        visitServerV2(server);
      }
    }
  }

  protected void visitServersV3(JsonNode serversNode) {
    if (serversNode != null && serversNode.isObject()) {
      for (JsonNode server : serversNode.propertyMap().values()) {
        visitServerV3(server);
      }
    }
  }

  protected void visitServerV2(JsonNode server) {
  }

  protected void visitServerV3(JsonNode server) {
  }
}
