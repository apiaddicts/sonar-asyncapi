# Test Guide for AsyncAPI 2.6 and 3.x Migration

## Overview

This guide explains how to use the provided test files to verify your checks work correctly with both AsyncAPI 2.6 and 3.x specifications.

## Test File Structure

```
asyncapi-checks/src/test/resources/
├── v2.6/
│   ├── valid-minimal.yaml
│   ├── valid-complete.yaml
│   ├── valid-with-servers.yaml
│   ├── valid-with-messages.yaml
│   ├── invalid-no-summary.yaml
│   ├── invalid-lowercase-summary.yaml
│   └── invalid-no-protocol.yaml
└── v3.1/
    ├── valid-minimal.yaml
    ├── valid-complete.yaml
    ├── valid-with-servers.yaml
    ├── valid-with-messages.yaml
    ├── invalid-missing-address.yaml
    ├── invalid-bad-summary.yaml
    ├── invalid-missing-operation-action.yaml
    └── invalid-server-no-protocol.yaml
```

## Test Files Description

### Valid Files (Should Pass All Checks)

#### v2.6/valid-minimal.yaml
- **Purpose**: Minimal valid AsyncAPI 2.6 document
- **Contains**: Basic channels with subscribe operation
- **Use For**: Testing basic parsing and navigation
- **File Size**: ~10 lines

#### v2.6/valid-complete.yaml
- **Purpose**: Complete AsyncAPI 2.6 with all common features
- **Contains**: 
  - Info with contact and license
  - Multiple servers (array)
  - Multiple channels with pub/sub
  - Components with schemas and security schemes
  - Tags and external docs
- **Use For**: Full integration testing
- **File Size**: ~100 lines

#### v2.6/valid-with-servers.yaml
- **Purpose**: Test server array handling
- **Contains**: Multiple servers with different protocols (MQTT, AMQP, Kafka)
- **Use For**: Server validation checks
- **File Size**: ~30 lines

#### v2.6/valid-with-messages.yaml
- **Purpose**: Test message definitions
- **Contains**: Multiple channels with detailed message definitions and components
- **Use For**: Message and payload validation
- **File Size**: ~50 lines

#### v3.1/valid-minimal.yaml
- **Purpose**: Minimal valid AsyncAPI 3.1 document
- **Contains**: Channels with address, operations object
- **Use For**: Testing v3.x structure parsing
- **File Size**: ~20 lines

#### v3.1/valid-complete.yaml
- **Purpose**: Complete AsyncAPI 3.1 with all features
- **Contains**:
  - Info with contact and license
  - Multiple servers (map structure)
  - Multiple channels with address field
  - Operations object with action field
  - Components with schemas and security
  - Tags and external docs
- **Use For**: Full v3.x integration testing
- **File Size**: ~120 lines

#### v3.1/valid-with-servers.yaml
- **Purpose**: Test server map handling
- **Contains**: Multiple servers with different protocols (map structure)
- **Use For**: Server validation in v3.x
- **File Size**: ~40 lines

#### v3.1/valid-with-messages.yaml
- **Purpose**: Test message definitions in v3.x
- **Contains**: Channels with messages, operations with message refs
- **Use For**: Message validation in v3.x
- **File Size**: ~60 lines

### Invalid Files (Should Trigger Violations)

#### v2.6/invalid-no-summary.yaml
- **Violations**: 
  - Missing operation summary
- **Expected Issues**: 1 issue
- **Use For**: Testing SummaryCapitalCheck

#### v2.6/invalid-lowercase-summary.yaml
- **Violations**:
  - Info summary starts with lowercase
  - Operation summaries missing periods or lowercase
- **Expected Issues**: 3 issues
- **Use For**: Testing SummaryCapitalCheck with multiple violations

#### v2.6/invalid-no-protocol.yaml
- **Violations**:
  - First server missing protocol field
- **Expected Issues**: 1 issue
- **Use For**: Testing server validation checks

#### v3.1/invalid-missing-address.yaml
- **Violations**:
  - Channel missing address field (required in v3.x)
- **Expected Issues**: 1 issue
- **Use For**: Testing ChannelAddressValidationCheck (NEW rule)

#### v3.1/invalid-bad-summary.yaml
- **Violations**:
  - Info summary starts with lowercase and no period
  - Operation summaries with formatting issues
- **Expected Issues**: 3 issues
- **Use For**: Testing summary validation in v3.x

#### v3.1/invalid-missing-operation-action.yaml
- **Violations**:
  - Operation missing action field (required in v3.x)
- **Expected Issues**: 1 issue
- **Use For**: Testing OperationActionValidationCheck (NEW rule)

#### v3.1/invalid-server-no-protocol.yaml
- **Violations**:
  - First server missing protocol field
- **Expected Issues**: 1 issue
- **Use For**: Testing server validation in v3.x

## How to Use These Files in Unit Tests

### Pattern 1: Testing a V2.6-Specific Check

```java
import org.apiaddicts.apitools.dosonarapi.api.TestAsyncApiVisitorRunner;
import org.junit.Test;

public class SummaryCapitalCheckTest {

  private AsyncApiFile asyncApiFile;

  @Test
  public void shouldPassV26WithValidSummary() {
    asyncApiFile = checkFile("src/test/resources/v2.6/valid-complete.yaml");
    assertThat(check).noIssues();
  }

  @Test
  public void shouldFailV26WithLowercaseSummary() {
    asyncApiFile = checkFile("src/test/resources/v2.6/invalid-lowercase-summary.yaml");
    assertThat(check).hasIssues(3);
  }

  private AsyncApiFile checkFile(String filePath) {
    return TestAsyncApiVisitorRunner.scanFileForTestPurpose(new File(filePath), check);
  }
}
```

### Pattern 2: Testing Version-Aware Check

```java
import org.apiaddicts.apitools.dosonarapi.plugin.AsyncApiVersion;
import org.apiaddicts.apitools.dosonarapi.api.AsyncApiVisitorContext;

public class ServerValidationCheckTest {

  @Test
  public void shouldValidateServerProtocolV26() {
    // Test with v2.6 file
    AsyncApiFile file = loadFile("src/test/resources/v2.6/invalid-no-protocol.yaml");
    AsyncApiVisitorContext context = createContextV26(file);
    
    check.scanFileForIssues(context);
    assertThat(check).hasIssues(1);
  }

  @Test
  public void shouldValidateServerProtocolV31() {
    // Test with v3.1 file
    AsyncApiFile file = loadFile("src/test/resources/v3.1/invalid-server-no-protocol.yaml");
    AsyncApiVisitorContext context = createContextV31(file);
    
    check.scanFileForIssues(context);
    assertThat(check).hasIssues(1);
  }

  private AsyncApiVisitorContext createContextV26(AsyncApiFile file) {
    return new AsyncApiVisitorContext(rootTree, issues, file, AsyncApiVersion.v2_x);
  }

  private AsyncApiVisitorContext createContextV31(AsyncApiFile file) {
    return new AsyncApiVisitorContext(rootTree, issues, file, AsyncApiVersion.v3_x);
  }
}
```

### Pattern 3: Testing New V3-Only Check

```java
public class ChannelAddressValidationCheckTest {

  @Test
  public void shouldPassV31WithChannelAddress() {
    asyncApiFile = checkFileV31("src/test/resources/v3.1/valid-complete.yaml");
    assertThat(check).noIssues();
  }

  @Test
  public void shouldFailV31WithoutChannelAddress() {
    asyncApiFile = checkFileV31("src/test/resources/v3.1/invalid-missing-address.yaml");
    assertThat(check).hasIssues(1);
  }

  @Test
  public void shouldSkipV26() {
    // V3-only check should not apply to v2.6
    asyncApiFile = checkFileV26("src/test/resources/v2.6/valid-complete.yaml");
    assertThat(check).noIssues();
  }
}
```

## Test Coverage Checklist

For each check you update, verify:

### For v2.6 Files
- [ ] Passes with valid-minimal.yaml
- [ ] Passes with valid-complete.yaml
- [ ] Passes with valid-with-servers.yaml (if server validation)
- [ ] Passes with valid-with-messages.yaml (if message validation)
- [ ] Fails with appropriate invalid-*.yaml files
- [ ] Reports correct number of issues

### For v3.1 Files
- [ ] Passes with valid-minimal.yaml
- [ ] Passes with valid-complete.yaml
- [ ] Passes with valid-with-servers.yaml (if server validation)
- [ ] Passes with valid-with-messages.yaml (if message validation)
- [ ] Fails with appropriate invalid-*.yaml files
- [ ] Detects v3.x-specific violations (address, action)

### Cross-Version
- [ ] Same validation logic works for both versions where applicable
- [ ] Different paths used where structure differs (servers, operations)
- [ ] No false positives in either version
- [ ] Consistent error messages across versions

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=SummaryCapitalCheckTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=SummaryCapitalCheckTest#shouldFailV26WithLowercaseSummary
```

### Run with Coverage
```bash
mvn test jacoco:report
```

### Run Integration Tests
```bash
mvn -Pits clean install
```

## File Naming Convention

When adding new test files, follow this convention:

```
{descriptor}-{type}-{status}.yaml

descriptor:  What's being tested (summary, servers, operations, address, etc.)
type:        What type of content (minimal, complete, with-servers, with-messages)
status:      valid or invalid
```

Examples:
- `valid-minimal.yaml` ✅
- `invalid-missing-address.yaml` ✅
- `invalid-lowercase-summary.yaml` ✅
- `valid-with-security-schemes.yaml` ✅

## Adding New Test Files

When you need to test a new scenario:

1. **Identify the scenario**: What are you testing?
2. **Choose base version**: v2.6, v3.1, or both?
3. **Create file**: Use naming convention
4. **Document violations**: List what makes it invalid
5. **Add test case**: Create corresponding unit test

Example:
```yaml
asyncapi: "3.1.0"
# ... your content ...
```

Add to `InvalidMissingOperationIdCheck.yaml` and test:
```java
@Test
public void shouldFailWithMissingOperationId() {
  // Test implementation
}
```

## Debugging Test Files

If a test file isn't behaving as expected:

1. **Validate YAML syntax**:
   ```bash
   yamllint file.yaml
   ```

2. **Check file can be parsed**:
   ```bash
   cat file.yaml | python -m yaml
   ```

3. **Verify version detection**:
   - Check `asyncapi:` field is correct
   - Ensure it starts with "2." or "3."

4. **Review file paths**:
   - Ensure relative paths point to correct location
   - Check resource folder structure

5. **Add debug output**:
   ```java
   System.out.println("Version: " + context.getVersion());
   System.out.println("Issues: " + issues.size());
   ```

## Test File Statistics

| Category | v2.6 | v3.1 | Total |
|----------|------|------|-------|
| Valid | 4 | 4 | 8 |
| Invalid | 3 | 4 | 7 |
| **Total** | **7** | **8** | **15** |

Coverage:
- ✅ Minimal cases
- ✅ Complete cases with all features
- ✅ Server validation (array vs map)
- ✅ Message/payload validation
- ✅ Summary validation
- ✅ V3-specific features (address, action)
- ✅ Version detection edge cases

## Common Test Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| Test file not found | Wrong path | Check path in test is relative to project root |
| Invalid YAML | Syntax error | Validate with `yamllint` |
| Version not detected | Missing asyncapi field | Add `asyncapi: "2.6.0"` or `"3.1.0"` |
| Too many/few issues | Wrong expectation | Count violations in file |
| Same test passes v2.6 & v3.1 | Expected | Some rules apply to both versions |

## Next Steps

1. **Create test files**: Done ✅
2. **Write unit tests**: Use patterns above
3. **Run tests**: `mvn test`
4. **Verify both versions**: Test v2.6 and v3.1
5. **Add integration tests**: Use ITS profile

## Reference

- AsyncAPI 2.6 Spec: https://www.asyncapi.com/docs/specifications/v2.6.0
- AsyncAPI 3.1 Spec: https://www.asyncapi.com/docs/specifications/v3.1.0
- Test Patterns: See `MIGRATION_QUICK_REFERENCE.md`
- Check Examples: See `MIGRATION_GUIDE.md`
