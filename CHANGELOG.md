# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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