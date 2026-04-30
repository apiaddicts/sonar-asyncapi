# AsyncAPI 2.6 → 3.0/3.1 Migration Guide

## Overview

This guide details the structural changes between AsyncAPI 2.6 and 3.x versions and how to update Sonar checks accordingly.

## Key Structural Differences

### 1. Servers: Map → Object

**AsyncAPI 2.6 (Array of Server Objects):**
```yaml
asyncapi: "2.6.0"
info:
  title: My API
  version: 1.0.0
servers:
  - url: mqtt://broker.example.com
    protocol: mqtt
    description: Production broker
  - url: mqtt://test-broker.example.com
    protocol: mqtt
    description: Test broker
```

**AsyncAPI 3.0/3.1 (Map/Object of Server Objects):**
```yaml
asyncapi: "3.1.0"
info:
  title: My API
  version: 1.0.0
servers:
  production:
    host: broker.example.com
    port: 1883
    protocol: mqtt
    description: Production broker
  test:
    host: test-broker.example.com
    port: 1883
    protocol: mqtt
    description: Test broker
```

**Sonar Check Impact:**
- **2.6**: Servers is an array → iterate using `.getElements()`
- **3.x**: Servers is a map → iterate using `.propertyMap().values()`

```java
// Version-aware server iteration
if (context.getVersion().isVersion2()) {
  JsonNode serversArray = rootNode.get("servers");
  if (serversArray != null) {
    for (JsonNode server : serversArray.getElements()) {
      checkServer(server);
    }
  }
} else if (context.getVersion().isVersion3()) {
  JsonNode serversMap = rootNode.get("servers");
  if (serversMap != null) {
    for (JsonNode server : serversMap.propertyMap().values()) {
      checkServer(server);
    }
  }
}
```

### 2. Channels Structure: publish/subscribe → operations

**AsyncAPI 2.6 (Operations inside channels):**
```yaml
channels:
  user/created:
    description: Channel for user creation events
    subscribe:
      summary: Subscribe to user creation
      operationId: onUserCreated
      message:
        payload:
          type: object
    publish:
      summary: Publish user creation
      operationId: publishUserCreated
      message:
        payload:
          type: object
```

**AsyncAPI 3.0/3.1 (Separate operations object + channel messages):**
```yaml
channels:
  userCreated:
    address: user/created
    description: Channel for user creation events
    messages:
      userCreatedMessage:
        payload:
          type: object

operations:
  onUserCreated:
    action: receive
    channel:
      $ref: '#/channels/userCreated'
    summary: Subscribe to user creation
    operationId: onUserCreated
  publishUserCreated:
    action: send
    channel:
      $ref: '#/channels/userCreated'
    summary: Publish user creation
    operationId: publishUserCreated
```

**Sonar Check Pattern:**

```java
// 2.6: Check operations within channels
if (context.getVersion().isVersion2()) {
  JsonNode channelsNode = rootNode.get("channels");
  for (JsonNode channel : channelsNode.propertyMap().values()) {
    JsonNode publishOp = channel.get("publish");
    JsonNode subscribeOp = channel.get("subscribe");
    
    if (publishOp != null) {
      validateOperation(publishOp, "publish");
    }
    if (subscribeOp != null) {
      validateOperation(subscribeOp, "subscribe");
    }
  }
}

// 3.x: Check top-level operations object
if (context.getVersion().isVersion3()) {
  JsonNode operationsNode = rootNode.get("operations");
  if (operationsNode != null) {
    for (JsonNode operation : operationsNode.propertyMap().values()) {
      String action = getActionType(operation); // "send" or "receive"
      validateOperation(operation, action);
    }
  }
}
```

### 3. Channels: New `address` field (3.x only)

**AsyncAPI 3.x introduces `address` as a direct property:**

```yaml
channels:
  userCreated:
    address: user/created  # New in 3.x
    messages:
      userCreatedMessage:
        $ref: '#/components/messages/UserCreated'
```

**Check Implementation:**

```java
if (context.getVersion().isVersion3()) {
  String address = channel.get("address").getTokenValue();
  if (address != null && address.isEmpty()) {
    addIssue("Channel address should not be empty", channel);
  }
}
```

### 4. Tags: Same structure, different locations

**2.6 & 3.x:** Tags can exist at root or operation level

**Check Pattern (works for both):**
```java
JsonNode tagsNode = node.get("tags");
if (tagsNode != null) {
  for (JsonNode tag : tagsNode.getElements()) {
    if (tag.isObject()) {
      validateTag(tag);
    }
  }
}
```

### 5. ExternalDocs: Same structure, different locations

**2.6 & 3.x:** ExternalDocs can exist at root, info, operation, or message level

**Check Pattern (works for both):**
```java
JsonNode externalDocsNode = node.get("externalDocs");
if (externalDocsNode != null && externalDocsNode.isObject()) {
  JsonNode urlNode = externalDocsNode.get("url");
  if (urlNode == null || urlNode.isMissing()) {
    addIssue("externalDocs must have a url", externalDocsNode);
  }
}
```

## Rule Migration Mapping

| Rule | 2.6 Location | 3.x Location | Changes |
|------|------|------|---------|
| Summary Validation | Root, Info, Channel, Operation | Root, Info, Channel, Operation (in operations object) | Add check for operations object |
| Tags Validation | Root, Operation | Root, Operation (in operations object) | Add check for operations object |
| External Docs | Root, Info, Operation | Root, Info, Operation (in operations object), Channel (new) | Add channel support, update operation paths |
| Servers | Array at root | Map at root | Change iteration from array to map |
| Channel Description | Channel properties | Channel properties | No change |
| Message Payload | publish/subscribe → message | operations → message | Navigation path changes |
| Bindings | Operation, Message, Server, Channel | Same + Operations binding | Add operations binding support |

## Implementation Examples

### Example 1: Version-Aware Server Validation Check

```java
@Rule(key = "ServerValidation")
public class ServerValidationCheck extends AsyncApiCheck {

  @Override
  public Set<AstNodeType> subscribedKinds() {
    return ImmutableSet.of(AsyncApiGrammar.ROOT);
  }

  @Override
  public void visitNode(JsonNode node) {
    if (AsyncApiGrammar.ROOT.equals(node.getType())) {
      validateServers(node);
    }
  }

  private void validateServers(JsonNode rootNode) {
    JsonNode serversNode = rootNode.get("servers");
    if (serversNode == null || serversNode.isMissing()) {
      return;
    }

    // Get version from async api field
    String version = getAsyncApiVersion(rootNode);
    
    if (version.startsWith("2.")) {
      validateServersV2(serversNode);
    } else if (version.startsWith("3.")) {
      validateServersV3(serversNode);
    }
  }

  private void validateServersV2(JsonNode serversArray) {
    for (JsonNode server : serversArray.getElements()) {
      validateServer(server);
    }
  }

  private void validateServersV3(JsonNode serversMap) {
    for (JsonNode server : serversMap.propertyMap().values()) {
      validateServer(server);
    }
  }

  private void validateServer(JsonNode server) {
    JsonNode protocolNode = server.get("protocol");
    if (protocolNode == null || protocolNode.isMissing()) {
      addIssue("Server must specify a protocol", server);
    }
  }

  private String getAsyncApiVersion(JsonNode rootNode) {
    JsonNode versionNode = rootNode.get("asyncapi");
    return versionNode != null ? versionNode.getTokenValue() : "2.6.0";
  }
}
```

### Example 2: Channel Operations Check (Handles Both Versions)

```java
@Rule(key = "OperationIdValidation")
public class OperationIdValidationCheck extends AsyncApiCheck {

  @Override
  public Set<AstNodeType> subscribedKinds() {
    return ImmutableSet.of(AsyncApiGrammar.ROOT);
  }

  @Override
  public void visitNode(JsonNode node) {
    if (AsyncApiGrammar.ROOT.equals(node.getType())) {
      validateAllOperations(node);
    }
  }

  private void validateAllOperations(JsonNode rootNode) {
    String version = getAsyncApiVersion(rootNode);

    if (version.startsWith("2.")) {
      validateOperationsV2(rootNode);
    } else if (version.startsWith("3.")) {
      validateOperationsV3(rootNode);
    }
  }

  private void validateOperationsV2(JsonNode rootNode) {
    JsonNode channelsNode = rootNode.get("channels");
    if (channelsNode == null) return;

    for (JsonNode channel : channelsNode.propertyMap().values()) {
      checkOperationV2(channel, "publish");
      checkOperationV2(channel, "subscribe");
    }
  }

  private void checkOperationV2(JsonNode channel, String operationType) {
    JsonNode operation = channel.get(operationType);
    if (operation != null && operation.isObject()) {
      validateOperationIdPresent(operation);
    }
  }

  private void validateOperationsV3(JsonNode rootNode) {
    JsonNode operationsNode = rootNode.get("operations");
    if (operationsNode == null) return;

    for (JsonNode operation : operationsNode.propertyMap().values()) {
      validateOperationIdPresent(operation);
    }
  }

  private void validateOperationIdPresent(JsonNode operation) {
    JsonNode operationIdNode = operation.get("operationId");
    if (operationIdNode == null || operationIdNode.isMissing()) {
      addIssue("Operation must have an operationId", operation);
    }
  }

  private String getAsyncApiVersion(JsonNode rootNode) {
    JsonNode versionNode = rootNode.get("asyncapi");
    return versionNode != null ? versionNode.getTokenValue() : "2.6.0";
  }
}
```

### Example 3: Using AsyncApiPathResolver

```java
@Rule(key = "ChannelAddressValidation")
public class ChannelAddressValidationCheck extends AsyncApiCheck {

  @Override
  public Set<AstNodeType> subscribedKinds() {
    return ImmutableSet.of(AsyncApiGrammar.ROOT);
  }

  @Override
  public void visitNode(JsonNode node) {
    if (AsyncApiGrammar.ROOT.equals(node.getType())) {
      validateChannelAddresses(node);
    }
  }

  private void validateChannelAddresses(JsonNode rootNode) {
    String version = getAsyncApiVersion(rootNode);
    AsyncApiPathResolver resolver = new AsyncApiPathResolver(rootNode, 
      version.startsWith("3.") ? AsyncApiVersion.v3_x : AsyncApiVersion.v2_x);

    JsonNode channelsNode = resolver.getChannels();
    if (channelsNode == null) return;

    for (JsonNode channel : channelsNode.propertyMap().values()) {
      if (resolver.isVersion3()) {
        String address = resolver.getChannelAddress(channel);
        if (address == null || address.isEmpty()) {
          addIssue("Channel must have an address in AsyncAPI 3.x", channel);
        }
      }
    }
  }

  private String getAsyncApiVersion(JsonNode rootNode) {
    JsonNode versionNode = rootNode.get("asyncapi");
    return versionNode != null ? versionNode.getTokenValue() : "2.6.0";
  }
}
```

## Testing Strategy

### Test File Structure for Version Support

```
asyncapi-checks/src/test/resources/
├── v2.6/
│   ├── valid-channels.yaml
│   ├── valid-servers.yaml
│   └── invalid-operations.yaml
└── v3.1/
    ├── valid-channels.yaml
    ├── valid-servers.yaml
    └── invalid-operations.yaml
```

### Test Example

```java
public class OperationIdValidationCheckTest {

  @Test
  public void shouldValidateOperationIdInAsyncApi26() {
    asyncApiFile = checkResource("v2.6/operations-without-id.yaml");
    AssertThat.assertThat(check).hasIssues(1);
  }

  @Test
  public void shouldValidateOperationIdInAsyncApi31() {
    asyncApiFile = checkResource("v3.1/operations-without-id.yaml");
    AssertThat.assertThat(check).hasIssues(1);
  }

  private AsyncApiFile checkResource(String resourcePath) {
    return checkFileV(new ResourceFile(resourcePath));
  }
}
```

## Migration Checklist

- [ ] Update AsyncApiAnalyzer to detect versions
- [ ] Create AsyncApiVersion enum
- [ ] Update AsyncApiVisitorContext to carry version info
- [ ] Create AsyncApiPathResolver utility
- [ ] Review all existing checks for version awareness
- [ ] Update server validation checks (array → map)
- [ ] Update operation validation checks (channel → operations object)
- [ ] Add channel address validation for 3.x
- [ ] Create test cases for both versions
- [ ] Update integration tests
- [ ] Update plugin documentation
- [ ] Version bump: 1.1.0 → 1.2.0

## Backward Compatibility Notes

1. **Default to 2.6 if version detection fails** - Existing checks work with 2.6 logic
2. **Version-aware checks use if/else** - No breaking changes to check interface
3. **AsyncApiVisitorContext always has version** - Default to v2_x for safety
4. **No removal of existing rules** - All 2.6 rules remain functional
5. **Server validation needs dual logic** - Only breaking point; carefully test

## Future Considerations

- AsyncAPI 4.0 support (similar pattern)
- Deprecated field warnings for migration path
- Version-specific reporting in SonarQube
- Custom rule guidance for version support
