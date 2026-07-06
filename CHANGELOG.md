# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [2.0.2-beta-2] - 2026-07-06

### Fixed
- Accept AsyncAPI-native security scheme types (`scramSha256`, `scramSha512`, `userPassword`, `X509`, `symmetricEncryption`, `asymmetricEncryption`, `plain`, `gssapi`, `httpApiKey`); previously caused validation failure for any type outside `http`, `apiKey`, `oauth2`, `openIdConnect`.
- Fix `visitServersV2` in `VersionAwareAsyncApiCheck` to iterate map-form servers correctly.

## [2.0.2-beta-1] - 2026-06-25

### Fixed
- Allow `$ref` in root-level servers; previously caused `Unexpected property: "$ref"` and `Missing required properties: [protocol]`.
- Allow `$ref` in tag entries (root, `info`, servers, channels, operations); previously caused `Unexpected property: "$ref"`.
- Accept `availableScopes` inside OAuth2 flows (AsyncAPI 3.0 rename of `scopes`); previously rejected as unexpected.
- Make `scopes` optional in OAuth2 flows for 3.0 compatibility; previously required, causing false `Missing required properties: [scopes]` errors.
- Accept `scopes` at the `oauth2` security-scheme level (valid in AsyncAPI 3.0); previously rejected as unexpected.

## [2.0.1] - 2026-06-09

### Changed
- Lowered the Sonar Plugin API baseline from `13.5.0.4319` to `11.1.0.2693` to broaden SonarQube compatibility. The plugin now loads on SonarQube Server 2025.1.x LTA (Plugin API 11.x) as well as newer Community Build releases (e.g. 25.9, Plugin API 13.x).
- Aligned the test-scoped `sonar-plugin-api-impl` to `25.2.0.102705` to match the new API baseline.

## [2.0.0] - 2026-05-06

### Added
- **Major**: Upgraded core dependencies and plugins to latest stable versions.
- Added `schemaFormat` as a valid property of the Message Object.

## [1.2.0] - 2026-04-30

### Added
- Support for AsyncAPI v3 (v3.1) specification alongside existing v2.x support.
- `AsyncApiVersion` class for detecting and handling different AsyncAPI spec versions.
- `AsyncApiPathResolver` for improved path resolution logic across spec versions.
- `VersionAwareAsyncApiCheck` base class to enable version-aware rule implementations.

### Changed
- `ChannelAmbiguityCheck` and `SummaryCapitalCheck` updated to support AsyncAPI v3 documents.
- Refactored AsyncAPI version handling and path resolution logic for cleaner separation of concerns.


## [1.1.0] - 2026-04-09

### Added
- New support for Avro schemas.
- Added support for `operations` section in AsyncAPI documents, allowing actions with channels and bindings to be defined.
- Extended AsyncAPI grammar to handle bindings and payload definitions in messages.