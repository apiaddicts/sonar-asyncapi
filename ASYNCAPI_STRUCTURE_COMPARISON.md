# AsyncAPI 2.6 vs 3.x Structure Visual Comparison

## 1. Root Level Structure

### AsyncAPI 2.6
```
asyncapi:
├── asyncapi: "2.6.0"               ← version string
├── info:
│   ├── title
│   ├── version
│   └── description
├── servers: [Array]                 ← Array of servers
│   ├── [0]: {url, protocol, ...}
│   └── [1]: {url, protocol, ...}
├── channels: {Object}               ← Map of channels
│   ├── "user/created":
│   │   ├── publish: {operation}
│   │   ├── subscribe: {operation}
│   │   └── messages: {map}
│   └── "user/updated":
│       ├── publish: {operation}
│       └── subscribe: {operation}
├── components:
│   ├── schemas: {map}
│   ├── messages: {map}
│   └── securitySchemes: {map}
├── tags: [Array]
└── externalDocs: {object}
```

### AsyncAPI 3.0/3.1
```
asyncapi:
├── asyncapi: "3.1.0"               ← version string (3.x)
├── info:
│   ├── title
│   ├── version
│   └── description
├── servers: {Object}                ← Map of servers (NOT array!)
│   ├── "production":
│   │   ├── host
│   │   ├── port
│   │   ├── protocol
│   │   └── pathPrefix
│   └── "test":
│       ├── host
│       ├── port
│       └── protocol
├── channels: {Object}               ← Map of channels (same as V2)
│   ├── "userCreated":
│   │   ├── address: "user/created" ← NEW: direct address
│   │   ├── description
│   │   └── messages: {map}         ← No publish/subscribe here!
│   └── "userUpdated":
│       ├── address: "user/updated"
│       └── messages: {map}
├── operations: {Object}             ← NEW: top-level operations
│   ├── "onUserCreated":
│   │   ├── action: "receive"       ← send | receive
│   │   ├── channel: {ref}
│   │   ├── operationId
│   │   └── message: {ref}
│   ├── "publishUserCreated":
│   │   ├── action: "send"
│   │   ├── channel: {ref}
│   │   └── message: {ref}
│   └── "onUserUpdated":
│       ├── action: "receive"
│       └── channel: {ref}
├── components:
│   ├── schemas: {map}
│   ├── messages: {map}
│   └── securitySchemes: {map}
├── tags: [Array]
└── externalDocs: {object}
```

## 2. Servers Comparison

### V2.6: Array Structure
```yaml
servers:
  - url: mqtt://broker.example.com:1883
    protocol: mqtt
    description: Production broker
    variables:
      port:
        default: "1883"

  - url: mqtt://test-broker.example.com:1883
    protocol: mqtt
    description: Test broker
```

**JSON Path**: `$.servers[0]`, `$.servers[1]`, etc.  
**Iteration**: `servers.getElements()`

### V3.x: Map Structure
```yaml
servers:
  production:
    host: broker.example.com
    port: 1883
    protocol: mqtt
    description: Production broker
    pathname: /mqtt

  test:
    host: test-broker.example.com
    port: 1883
    protocol: mqtt
    description: Test broker
```

**JSON Path**: `$.servers['production']`, `$.servers['test']`  
**Iteration**: `servers.propertyMap().values()`

## 3. Channels & Operations Comparison

### V2.6: Operations Inside Channels
```yaml
channels:
  user/created:
    description: When a user is created
    subscribe:
      operationId: onUserCreated
      summary: Subscribe to user creation events
      message:
        payload:
          type: object
          properties:
            userId:
              type: string

    publish:
      operationId: publishUserCreated
      summary: Publish user creation events
      message:
        payload:
          type: object
```

**Structure**: 
```
channels → channel → {publish, subscribe}
                     └─ message
```

**JSON Path Examples**:
- `$.channels['user/created'].subscribe.operationId`
- `$.channels['user/created'].publish.message.payload`

### V3.x: Separate Operations Object
```yaml
channels:
  userCreated:
    address: user/created      # ← NEW
    description: When a user is created
    messages:
      userCreatedMessage:
        payload:
          type: object
          properties:
            userId:
              type: string

operations:
  onUserCreated:
    action: receive            # ← NEW: send | receive
    channel:
      $ref: '#/channels/userCreated'
    operationId: onUserCreated
    summary: Subscribe to user creation events
    message:
      $ref: '#/components/messages/userCreatedMessage'

  publishUserCreated:
    action: send              # ← NEW
    channel:
      $ref: '#/channels/userCreated'
    operationId: publishUserCreated
    summary: Publish user creation events
    message:
      payload:
        type: object
```

**Structure**:
```
channels → channel → address
         └─ messages

operations → operation → action (send/receive)
           └─ channel (reference)
           └─ message
```

**JSON Path Examples**:
- `$.channels['userCreated'].address`
- `$.operations['onUserCreated'].action`
- `$.operations['onUserCreated'].channel['$ref']`

## 4. Check Navigation Changes

### SummaryCapitalCheck Example

#### V2.6 Paths
```
√ Root info summary: $.info.summary
√ Channel summary: $.channels[].summary
√ Operation summary: $.channels[].publish.summary
√ Operation summary: $.channels[].subscribe.summary
```

#### V3.x Paths
```
√ Root info summary: $.info.summary
√ Channel summary: $.channels[].summary (SAME)
✗ Channel pub/sub: REMOVED (use operations instead)
√ Operation summary: $.operations[].summary (NEW)
```

### ServerValidationCheck Example

#### V2.6 Iteration
```java
JsonNode servers = root.get("servers");
for (JsonNode server : servers.getElements()) {  // Array!
  String protocol = server.get("protocol").getTokenValue();
  // validate...
}
```

#### V3.x Iteration
```java
JsonNode servers = root.get("servers");
for (JsonNode server : servers.propertyMap().values()) {  // Map!
  String protocol = server.get("protocol").getTokenValue();
  // validate (protocol is same field, good!)
}
```

## 5. Message Access Patterns

### V2.6: Message in Operation
```
channels/user/created/subscribe
  └── message
      ├── contentType
      ├── payload
      └── examples
```

```java
JsonNode message = channel.get("subscribe").get("message");
JsonNode payload = message.get("payload");
```

### V3.x: Message via Reference or Direct
```
operations/onUserCreated
  ├── message: {$ref or inline}
  └── channel: {$ref to channels/userCreated}
     └── messages: {map of available messages}
```

```java
JsonNode operation = operations.get("onUserCreated");
JsonNode message = operation.get("message");
// Can be a $ref or inline definition
if (!message.get("$ref").isMissing()) {
  String ref = message.get("$ref").getTokenValue();
  // resolve reference
} else {
  // inline message definition
}
```

## 6. Key Field Migrations

| Field | V2.6 Location | V3.x Location | Status |
|-------|---------------|---------------|--------|
| `operationId` | `channels.X.publish/subscribe` | `operations.X` | ✅ Same field |
| `summary` | `channels.X.publish/subscribe` | `operations.X` | ✅ Same field |
| `description` | `channels.X` | `channels.X` | ✅ Same |
| `message` | `channels.X.publish/subscribe` | `operations.X` | ✅ Same field |
| `payload` | `message.payload` | `message.payload` | ✅ Same |
| `tags` | `channels.X.publish.tags` | `operations.X.tags` | ⚠️ Different path |
| `externalDocs` | `channels.X.publish.externalDocs` | `operations.X.externalDocs` | ⚠️ Different path |
| `bindings` | `channels.X.publish.bindings` | `operations.X.bindings` | ⚠️ Different path |
| **address** | ✗ Not in V2.6 | `channels.X.address` | 🆕 **NEW** |
| **action** | ✗ Not in V2.6 | `operations.X.action` | 🆕 **NEW** |
| **protocol** | `servers[].protocol` | `servers.X.protocol` | ✅ Same field |
| **url** | `servers[].url` | Removed (use host+port) | ❌ Removed |

## 7. Tags & ExternalDocs (Minimal Changes)

These structures work the same in both versions:

### V2.6 & V3.x: Tags Structure
```yaml
tags:
  - name: users
    description: User related operations
    externalDocs:
      url: https://example.com/users

  - name: orders
    description: Order related operations
```

```java
// Works in both versions!
JsonNode tags = node.get("tags");
for (JsonNode tag : tags.getElements()) {
  if (tag.isObject()) {
    String name = tag.get("name").getTokenValue();
    // validate...
  }
}
```

### V2.6 & V3.x: ExternalDocs Structure
```yaml
externalDocs:
  description: Find more info here
  url: https://example.com/docs
```

```java
// Works in both versions!
JsonNode extDocs = node.get("externalDocs");
if (extDocs != null) {
  JsonNode url = extDocs.get("url");
  if (url == null || url.isMissing()) {
    addIssue("externalDocs must have url", extDocs);
  }
}
```

## 8. Decision Tree for Check Updates

```
Check navigates to: ?
├── Root level → No change needed
│   ├── info.*
│   ├── tags
│   ├── externalDocs
│   └── components
│
├── Servers → MUST UPDATE
│   └── V2: array iteration vs V3: map iteration
│
├── Channels
│   ├── Channel properties (description, address) → No change
│   │
│   └── Operations in channel (publish/subscribe)
│       └── MUST UPDATE: Move to top-level operations in V3
│
└── New in V3 only → ADD NEW CHECKS
    ├── operations object
    ├── channel.address
    ├── operation.action
    └── operation.channel references
```

## 9. Validation Rule Impact Matrix

| Rule | Affected | Change | Priority |
|------|----------|--------|----------|
| Server Protocol Required | ✅ Yes | Iteration pattern | 🔴 High |
| Operation ID Required | ✅ Yes | Navigation path | 🔴 High |
| Channel Description | ❌ No | - | 🟢 Low |
| Message Payload Valid | ✅ Partial | Message reference handling | 🟡 Medium |
| Tags Defined | ✅ Yes | New path in V3 operations | 🟡 Medium |
| External Docs URL | ❌ No | - | 🟢 Low |
| **Summary Format** | ✅ Yes | Added V3 operations | 🟡 Medium |
| **Address Format** | 🆕 New | V3 only | 🟡 Medium |
| **Action Valid** | 🆕 New | V3 only | 🟡 Medium |

---

## 10. Copy-Paste Quick Fix Templates

### Template 1: Version-Aware Server Iteration
```java
JsonNode servers = rootNode.get("servers");
if (servers != null) {
  List<JsonNode> serversList = version.isVersion2() 
    ? servers.getElements()
    : new ArrayList<>(servers.propertyMap().values());
  
  for (JsonNode server : serversList) {
    validateServer(server);
  }
}
```

### Template 2: Version-Aware Operation Access
```java
if (version.isVersion2()) {
  JsonNode channels = rootNode.get("channels");
  for (JsonNode channel : channels.propertyMap().values()) {
    checkOperationV2(channel.get("publish"));
    checkOperationV2(channel.get("subscribe"));
  }
} else {
  JsonNode operations = rootNode.get("operations");
  if (operations != null) {
    for (JsonNode op : operations.propertyMap().values()) {
      checkOperationV3(op);
    }
  }
}
```

### Template 3: Safe Field Access
```java
// Always check before accessing
JsonNode field = node.get("fieldName");
if (field != null && !field.isMissing()) {
  String value = field.getTokenValue();
  // process value
}
```

---

**Visual Reference**: Print this page and keep it handy while updating checks!
