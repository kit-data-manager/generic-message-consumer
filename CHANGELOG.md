# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added

### Fixed

### Security

### Changed

### Deprecated

### Removed

## [1.1.0] - 2023-12-08
### Added
* Added port, username, and password properties by @ThomasJejkal in https://github.com/kit-data-manager/generic-message-consumer/pull/16

### Fixed
* Added NPE check for receiver properties if not configured

### Security
* Bump org.springframework.boot to 3.1.1
* Bump io.freefair.lombok to 8.1.0
* Bump io.freefair.maven-publish-java to 8.1.0
* Bump org.owasp.dependencycheck to 8.3.1
* Bump edu.kit.datamanager:service-base to 1.2.0
* Bump jacoco to 0.8.10

## [1.0.0] - 2023-01-11
### Added
- Switch to GitHub Actions for CI

### Changed
- Added support for Java 17 (build/tests)
- Handling of schedule changed (checking for pending messages)
- Update to gradle version 7.6
- Update to spring-boot 2.7.7
- Update to spring-doc 1.6.14
- Update to h2 2.1.214
- Update to io.freefair.lombok 6.6.1
- Update to org.owasp.dependencycheck 7.4.4
- Update to service-base 1.1.0

### Removes
- Remove dependencies of powermock for tests

## [0.2.0] - date 2020-12-15
### Changed
- Configuration settings

## [0.1.2] - date 2020-10-05
### Fixed
- Exception handling.

## [0.1.1] - date 2020-09-29
### Fixed
- Bindings

### Fixed
- Typos in documentation
- Swagger-UI (POST methods now handled correct but still broken) 

## [0.1] - date 2020-07-02
Initial Revision

[Unreleased]: https://github.com/kit-data-manager/generic-message-consumer/compare/1.1.0...HEAD
[1.1.0]: https://github.com/kit-data-manager/generic-message-consumer/compare/1.0.0..1.1.0
[1.0.0]: https://github.com/kit-data-manager/generic-message-consumer/compare/v0.2.0...1.0.0
[0.2.0]: https://github.com/kit-data-manager/generic-message-consumer/compare/0.1.2...v0.2.0
[0.1.2]: https://github.com/kit-data-manager/generic-message-consumer/compare/0.1.1...0.1.2
[0.1.1]: https://github.com/kit-data-manager/generic-message-consumer/compare/0.1...0.1.1
[0.1]: https://github.com/kit-data-manager/generic-message-consumer/releases/tag/0.1

