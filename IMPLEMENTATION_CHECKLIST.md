# AsyncAPI 3.x Support Implementation Checklist

## Phase 1: Version Detection & Core Infrastructure ✅

### 1.1 Version Enum
- [x] Create `AsyncApiVersion` enum in `sonar-asyncapi-plugin` module
  - [x] Add `v2_x` and `v3_x` constants
  - [x] Add helper methods: `isVersion2()`, `isVersion3()`, `getLabel()`

### 1.2 Version Detection in Analyzer
- [x] Update `AsyncApiAnalyzer.detectAsyncApiVersion()` method
  - [x] Parse asyncapi field from root
  - [x] Return appropriate `AsyncApiVersion`
  - [x] Default to v2_x on parse error

### 1.3 Context Updates
- [x] Update `AsyncApiVisitorContext` to carry version
  - [x] Add `version` field
  - [x] Update constructor overloads
  - [x] Add `getVersion()` getter
  - [x] Maintain backward compatibility

### 1.4 Path Resolution Utility
- [x] Create `AsyncApiPathResolver` helper class
  - [x] Encapsulate version-aware navigation
  - [x] Provide methods: `getChannels()`, `getOperations()`, `getServers()`
  - [x] Support channel address access (V3 only)

---

## Phase 2: Check Updates

### 2.1 New Version-Aware Check Base Class
- [x] Create `VersionAwareAsyncApiCheck` abstract class
  - [x] Implement `visitFile()` to route by version
  - [x] Provide `visitFileV2()` and `visitFileV3()` hooks
  - [x] Add helper methods for common V2/V3 patterns

### 2.2 Update Existing Checks

#### SummaryCapitalCheck
- [ ] Add V3 operations support
- [ ] Test with V2.6 sample file
- [ ] Test with V3.1 sample file
- [ ] Document channel summary checking (if applicable in V3)

#### ChannelAmbiguityCheck
- [ ] Handle V3 channel structure changes
- [ ] Test both versions
- [ ] Update issue messages if needed

#### InfoDescriptionFormatCheck
- [ ] Verify works for both versions (should not need changes)
- [ ] Add tests for V3

#### ParsingErrorCheck / AsyncApiParsingErrorCheck
- [ ] Should already handle both versions
- [ ] Verify no version-specific issues

### 2.3 New Checks for V3 Features

#### ChannelAddressValidationCheck
```
Requirement: Validate that channels in V3.x have address field
Locations: Channel level
Severity: Major (channels require address in V3)
```

#### OperationsValidationCheck
```
Requirement: Validate operations object (V3 only)
Checks:
- operationId presence/format
- action field presence (send/receive)
- channel reference validity
Severity: Major
```

#### OperationActionValidationCheck
```
Requirement: Validate action field values
Valid values: "send", "receive"
Severity: Major
```

---

## Phase 3: Grammar Updates

### 3.1 AsyncApiGrammar Verification
- [ ] Verify grammar already supports 3.0.0 and 3.1.0 versions
- [ ] Check OPERATIONS rule is defined
- [ ] Check CHANNEL rule includes address property
- [ ] Check OPERATION rule includes action property
- [ ] Test grammar parsing with sample V3.1 files

### 3.2 Grammar Test Files
Create test resources:
```
asyncapi-front-end/src/test/resources/
├── v2.6/
│   ├── minimal.yaml
│   ├── with-servers.yaml
│   ├── with-channels.yaml
│   ├── with-tags.yaml
│   └── with-external-docs.yaml
└── v3.1/
    ├── minimal.yaml
    ├── with-servers-map.yaml
    ├── with-channels-and-address.yaml
    ├── with-operations.yaml
    └── with-tags.yaml
```

---

## Phase 4: Test Coverage

### 4.1 Unit Tests

#### Version Detection Tests
- [ ] Test detection of "2.6.0" → v2_x
- [ ] Test detection of "3.0.0" → v3_x
- [ ] Test detection of "3.1.0" → v3_x
- [ ] Test fallback on parse error → v2_x
- [ ] Test missing asyncapi field → v2_x

#### Path Resolver Tests
- [ ] Test `getChannels()` for both versions
- [ ] Test `getOperations()` returns null for V2
- [ ] Test `getOperations()` returns object for V3
- [ ] Test `getChannelAddress()` for V3 only

#### Check Tests for Each Rule
For each check, create:
- V2.6 valid test case
- V2.6 invalid test case
- V3.1 valid test case (if applicable)
- V3.1 invalid test case (if applicable)

### 4.2 Integration Tests (ITS)

Create test projects:
```
its/src/test/resources/
├── asyncapi-v26-project/
│   ├── sonar-project.properties
│   └── src/
│       ├── valid.yaml
│       ├── invalid-servers.yaml
│       ├── invalid-channels.yaml
│       └── invalid-operations.yaml
└── asyncapi-v31-project/
    ├── sonar-project.properties
    └── src/
        ├── valid.yaml
        ├── invalid-channels-no-address.yaml
        ├── invalid-operations.yaml
        └── invalid-server-structure.yaml
```

### 4.3 Regression Testing

- [ ] Run existing V2.6 tests with updated code
- [ ] Verify all V2.6 tests still pass
- [ ] Check no performance regression
- [ ] Verify memory usage is acceptable

---

## Phase 5: Documentation

### 5.1 README Updates
- [ ] Add "AsyncAPI v3.0.0, v3.1.0" to features list
- [ ] Add migration notes section
- [ ] Document new V3-specific rules

### 5.2 Changelog
- [ ] Add AsyncAPI 3.x support entry
- [ ] List new rules added for V3
- [ ] Document breaking changes (if any)
- [ ] Note version detection behavior

### 5.3 Custom Rules Documentation
- [ ] Document how to make custom rules version-aware
- [ ] Provide code examples
- [ ] Link to VersionAwareAsyncApiCheck

---

## Phase 6: Version Release

### 6.1 Version Bump
- [ ] Update pom.xml: 1.1.0 → 1.2.0
- [ ] Update sonar-plugin descriptor
- [ ] Update all version references

### 6.2 Build & Package
- [ ] `mvn clean install` passes
- [ ] `mvn -Pits clean install` passes
- [ ] JAR plugin builds successfully
- [ ] No dependency conflicts

### 6.3 Release Artifacts
- [ ] Create GitHub release with notes
- [ ] Deploy to Maven Central
- [ ] Publish SonarQube marketplace entry
- [ ] Update website documentation

---

## Test File Templates

### V2.6 Sample (minimal valid)
```yaml
asyncapi: "2.6.0"
info:
  title: My API
  version: 1.0.0
  description: Test API
channels:
  user.created:
    description: User creation events
    subscribe:
      operationId: onUserCreated
      summary: Receive user creation event
      message:
        payload:
          type: object
```

### V3.1 Sample (minimal valid)
```yaml
asyncapi: "3.1.0"
info:
  title: My API
  version: 1.0.0
  description: Test API
channels:
  userCreated:
    address: user/created
    description: User creation events
    messages:
      userCreatedMessage:
        payload:
          type: object
operations:
  onUserCreated:
    action: receive
    channel:
      $ref: '#/channels/userCreated'
    operationId: onUserCreated
    summary: Receive user creation event
```

### V3.1 Sample with Servers (Map structure)
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
  test:
    host: test-broker.example.com
    port: 1883
    protocol: mqtt
```

---

## Known Issues & Considerations

### Issue 1: Backward Compatibility with AsyncApiVisitorContext
**Impact**: Existing code creating AsyncApiVisitorContext
**Solution**: Added overload constructors that default to v2_x
**Status**: ✅ Handled

### Issue 2: Server Validation Logic Divergence
**Impact**: Servers are array in V2, map in V3
**Solution**: Version-aware iteration in checks
**Status**: ⚠️ Requires careful testing

### Issue 3: Operations Navigation
**Impact**: Completely different paths in V2 vs V3
**Solution**: AsyncApiPathResolver abstraction
**Status**: ✅ Handled

### Issue 4: New Rules Needed for V3
**Impact**: Channel address validation, operations validation
**Solution**: Create new rules; tag as V3-specific in SonarQube
**Status**: 📋 To do

---

## Estimated Effort

| Phase | Tasks | Effort |
|-------|-------|--------|
| 1: Core Infrastructure | 4 | 4-6 hours |
| 2: Check Updates | 15+ | 12-16 hours |
| 3: Grammar Verification | 5 | 2-3 hours |
| 4: Test Coverage | 20+ | 8-12 hours |
| 5: Documentation | 6 | 3-4 hours |
| 6: Release | 5 | 2-3 hours |
| **Total** | **50+** | **31-44 hours** |

---

## Sign-Off Criteria

- [ ] All unit tests passing
- [ ] All integration tests passing
- [ ] No performance regression
- [ ] Documentation complete and reviewed
- [ ] Code reviewed and merged
- [ ] Release notes published
- [ ] Version deployed to Maven Central
- [ ] SonarQube marketplace updated
