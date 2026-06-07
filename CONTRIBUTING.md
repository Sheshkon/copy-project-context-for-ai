# Contributing to Copy Project Context for AI

Thank you for your interest in contributing! This document explains how to get started, submit changes, and follow project conventions.

## Getting Started

1. Fork the repository on GitHub.
2. Clone your fork locally.
3. Open the project in IntelliJ IDEA (2024.2 or newer recommended).
4. Run the plugin sandbox with:

```bash
./gradlew runIde
```

On Windows:

```powershell
.\gradlew.bat runIde
```

## Development Requirements

- JDK 17 or newer
- IntelliJ IDEA 2024.2+ (Community or Ultimate)
- Gradle (wrapper included)

## Project Structure

```
src/main/kotlin/com/aicontext/plugin/
├── actions/          # User-facing IDE actions
├── models/           # Data models
├── services/         # Core business logic
├── settings/         # Settings UI
└── utils/            # Shared helpers
```

See [docs/architecture.md](docs/architecture.md) and [docs/development.md](docs/development.md) for deeper technical details.

## Coding Guidelines

- Use Kotlin idioms and follow existing package conventions.
- Keep the plugin language-agnostic. Do not introduce language-specific PSI APIs.
- Prefer IntelliJ Platform APIs (`VirtualFile`, `Project`, notifications, clipboard, etc.).
- Add logging for failure paths using `Logger.getInstance(...)`.
- Preserve deterministic output ordering (alphabetical paths, stable tree rendering).
- Keep changes focused and avoid unrelated refactors.

## Testing Changes

Before opening a pull request:

```bash
./gradlew build
./gradlew verifyPlugin
```

Manually verify in the sandbox IDE:

1. Open a sample project.
2. Select files and folders in the Project View.
3. Right-click → **Copy Project Context for AI**.
4. Paste the clipboard output into an editor and inspect formatting.
5. Validate ignore behavior with `.gitignore` and custom ignore files.
6. Test settings changes under **Tools → Copy Project Context for AI**.

## Pull Request Process

1. Create a feature branch from `main`.
2. Make your changes with clear commit messages.
3. Update `CHANGELOG.md` under **Unreleased** (or the appropriate version) for user-visible changes.
4. Ensure the project builds successfully.
5. Open a pull request with:
   - A concise summary of the change
   - Motivation and context
   - Manual test steps
   - Screenshots for UI changes when applicable

## Reporting Issues

When filing an issue, include:

- IDE name and version (e.g., IntelliJ IDEA 2024.2.5)
- Plugin version
- Operating system
- Steps to reproduce
- Expected vs actual behavior
- Relevant logs from **Help → Show Log in Explorer/Finder**

## Code of Conduct

Be respectful and constructive. We aim to maintain a welcoming environment for contributors of all experience levels.

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).
