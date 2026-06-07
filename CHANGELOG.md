# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-06-07

### Added

- Initial release of **Copy Project Context for AI**
- Project View context menu action for copying selected files and folders
- Recursive file collection with duplicate prevention and alphabetical sorting
- AI-friendly output with project tree and markdown code blocks
- Language detection for common file extensions
- Default ignore rules for build artifacts, dependencies, and binary files
- Support for `.gitignore`, `.cursorignore`, `.aiderignore`, and `.copilotignore`
- Clipboard integration with success and failure notifications
- Configurable limits for files, characters, and single file size
- Confirmation dialog when configured limits are exceeded
- Settings page under **Tools → Copy Project Context for AI**
- Persistent settings using IntelliJ `PersistentStateComponent`

### Compatibility

- IntelliJ Platform 2024.2+ (`242`)
- All JetBrains IDEs based on the IntelliJ Platform

[1.0.0]: https://github.com/example/copy-project-context-for-ai/releases/tag/v1.0.0
