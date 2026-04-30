# Quick Reference: AsyncAPI 2.6 → 3.x Migration Code Patterns

## Core Version Detection Pattern

```java
private AsyncApiVersion detectVersion(JsonNode rootNode) {
  JsonNode versionNode = rootNode.get("asyncapi");
  if (versionNode != null && !versionNode.isMissing()) {
    String version = versionNode.getTokenValue();
    if (version != null && version.startsWith("3.")) {
      return AsyncApiVersion.v3_x;
    }
  }
  return AsyncApiVersion.v2_x;
}
```

## Servers Validation Pattern

```java
// V2: Array iteration
JsonNode servers = rootNode.get("servers");
if (servers != null) {
  for (JsonNode server : servers.getElements()) {
    // validate server
  }
}

// V3: Map iteration
JsonNode servers = rootNode.get("servers");
if (servers != null) {
  for (JsonNode server : servers.propertyMap().values()) {
    // validate server
  }
}

// Version-aware wrapper
void validateAllServers(JsonNode rootNode, AsyncApiVersion version) {
  JsonNode servers = rootNode.get("servers");
  if (servers == null) return;
  
  if (version.isVersion2()) {
    servers.getElements().forEach(this::validateServer);
  } else {
    servers.propertyMap().values().forEach(this::validateServer);
  }
}
```

## Operations Navigation Pattern

```java
// V2: Operations inside channels
JsonNode channels = rootNode.get("channels");
for (JsonNode channel : channels.propertyMap().values()) {
  JsonNode publish = channel.get("publish");
  JsonNode subscribe = channel.get("subscribe");
  if (publish != null) processOperation(publish);
  if (subscribe != null) processOperation(subscribe);
}

// V3: Top-level operations
JsonNode operations = rootNode.get("operations");
if (operations != null) {
  for (JsonNode operation : operations.propertyMap().values()) {
    processOperation(operation);
  }
}

// Version-aware pattern
void processAllOperations(JsonNode rootNode, AsyncApiVersion version) {
  if (version.isVersion2()) {
    JsonNode channels = rootNode.get("channels");
    if (channels != null) {
      for (JsonNode channel : channels.propertyMap().values()) {
        processChannelOperationsV2(channel);
      }
    }
  } else {
    JsonNode operations = rootNode.get("operations");
    if (operations != null) {
      for (JsonNode op : operations.propertyMap().values()) {
        processOperation(op);
      }
    }
  }
}

void processChannelOperationsV2(JsonNode channel) {
  for (String opType : Arrays.asList("publish", "subscribe")) {
    JsonNode op = channel.get(opType);
    if (op != null) processOperation(op);
  }
}
```

## Channel Address Pattern (3.x only)

```java
// Safe for both versions
String getChannelAddress(JsonNode channel, AsyncApiVersion version) {
  if (version.isVersion3()) {
    JsonNode addressNode = channel.get("address");
    if (addressNode != null && !addressNode.isMissing()) {
      return addressNode.getTokenValue();
    }
  }
  return null;
}
```

## Check Class Template

```java
@Rule(key = "MyRuleKey")
public class MyVersionAwareCheck extends AsyncApiCheck {

  @Override
  public Set<AstNodeType> subscribedKinds() {
    return ImmutableSet.of(AsyncApiGrammar.ROOT);
  }

  @Override
  public void visitNode(JsonNode node) {
    if (AsyncApiGrammar.ROOT.equals(node.getType())) {
      AsyncApiVersion version = detectVersion(node);
      
      if (version.isVersion2()) {
        validateV2(node);
      } else {
        validateV3(node);
      }
    }
  }

  private void validateV2(JsonNode root) {
    // V2-specific logic
  }

  private void validateV3(JsonNode root) {
    // V3-specific logic
  }

  private AsyncApiVersion detectVersion(JsonNode root) {
    JsonNode vNode = root.get("asyncapi");
    String v = vNode != null ? vNode.getTokenValue() : "2.6.0";
    return v.startsWith("3.") ? AsyncApiVersion.v3_x : AsyncApiVersion.v2_x;
  }
}
```

## Using AsyncApiPathResolver

```java
public class MyCheck extends AsyncApiCheck {

  private AsyncApiPathResolver resolver;
  private AsyncApiVersion version;

  @Override
  public void visitFile(AsyncApiVisitorContext context) {
    this.version = context.getVersion();
    this.resolver = new AsyncApiPathResolver(context.rootTree(), version);
    super.visitFile(context);
  }

  @Override
  public void visitNode(JsonNode node) {
    if (AsyncApiGrammar.ROOT.equals(node.getType())) {
      // Example: Get channels
      JsonNode channels = resolver.getChannels();
      
      // Example: Get servers with automatic version handling
      JsonNode servers = resolver.getServers();
      
      // Example: Version checks
      if (resolver.isVersion3()) {
        JsonNode operations = resolver.getOperations();
      }
    }
  }
}
```

## Message Access Pattern

```java
// V2: Message is inside publish/subscribe
JsonNode messageV2 = operation.get("message");

// V3: Message is referenced from operations
JsonNode message = operation.get("message"); // Still uses same pattern

// Process message payload (works for both)
void processMessage(JsonNode message) {
  JsonNode payload = message.get("payload");
  if (payload != null) {
    validatePayload(payload);
  }
}
```

## Tags and ExternalDocs Pattern (Same for both versions)

```java
// Tags exist at root, info, operation, message level in both versions
void validateTags(JsonNode node) {
  JsonNode tagsNode = node.get("tags");
  if (tagsNode != null && tagsNode.isArray()) {
    for (JsonNode tag : tagsNode.getElements()) {
      if (tag.isObject()) {
        validateTag(tag);
      } else if (tag.isString()) {
        // V2 allows string references to tag names
      }
    }
  }
}

// ExternalDocs pattern (same for both)
void validateExternalDocs(JsonNode node) {
  JsonNode exDocsNode = node.get("externalDocs");
  if (exDocsNode != null && exDocsNode.isObject()) {
    JsonNode urlNode = exDocsNode.get("url");
    if (urlNode == null || urlNode.isMissing()) {
      addIssue("externalDocs must contain url", exDocsNode);
    }
  }
}
```

## Testing Utility

```java
public class VersionAwareCheckTestHelper {

  public static AsyncApiVisitorContext createContextV26(JsonNode tree) {
    return new AsyncApiVisitorContext(tree, Collections.emptyList(), 
      SonarQubeAsyncApiFile.create(mock(InputFile.class)), 
      AsyncApiVersion.v2_x);
  }

  public static AsyncApiVisitorContext createContextV31(JsonNode tree) {
    return new AsyncApiVisitorContext(tree, Collections.emptyList(), 
      SonarQubeAsyncApiFile.create(mock(InputFile.class)), 
      AsyncApiVersion.v3_x);
  }

  public static JsonNode parseResource(String resourcePath) {
    // Parse YAML/JSON resource
  }
}
```

## Common Pitfalls to Avoid

### ❌ Wrong: Hardcoding version checks without fallback
```java
if (version == "3.0.0") { // WRONG: string comparison
  // ...
}
```

### ✅ Right: Using version enum
```java
if (context.getVersion().isVersion3()) {
  // ...
}
```

### ❌ Wrong: Assuming servers is always an array
```java
for (JsonNode server : servers.getElements()) { // Fails in V3
```

### ✅ Right: Version-aware iteration
```java
if (version.isVersion2()) {
  for (JsonNode server : servers.getElements()) { }
} else {
  for (JsonNode server : servers.propertyMap().values()) { }
}
```

### ❌ Wrong: Ignoring missing nodes
```java
JsonNode ops = rootNode.get("operations"); // Null in V2, object in V3
for (JsonNode op : ops.propertyMap().values()) { // NPE in V2!
```

### ✅ Right: Null-safe access
```java
JsonNode ops = rootNode.get("operations");
if (ops != null && !ops.isMissing()) {
  for (JsonNode op : ops.propertyMap().values()) { }
}
```

## Implementation Checklist

For each existing check:

- [ ] Add version detection logic to visitNode
- [ ] Split logic into validateV2() and validateV3()
- [ ] Handle server array → map iteration
- [ ] Handle channel → operations navigation
- [ ] Add channel address check for V3
- [ ] Test with sample V2.6 file
- [ ] Test with sample V3.1 file
- [ ] Verify no regressions in V2.6 behavior
- [ ] Update check description if needed

## Migration Priority Order

1. **High Priority**: Server validation (breaking change)
2. **High Priority**: Operation validation (major structure change)
3. **Medium Priority**: Tags and description validation
4. **Medium Priority**: External docs validation
5. **Low Priority**: Message payload validation (similar structure)
6. **Low Priority**: Binding validation (similar structure)
