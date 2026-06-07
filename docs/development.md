# Development Guide

This guide explains how to develop, build, test, and debug the **Copy Project Context for AI** plugin.

## Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK | 17+ |
| IntelliJ IDEA | 2024.2+ |
| Gradle | Wrapper included |

## Opening the Project

1. Clone the repository.
2. Open IntelliJ IDEA.
3. Choose **Open** and select the repository root.
4. Import the Gradle project when prompted.
5. Wait for dependency indexing to complete.

## Gradle Tasks

### Build

```bash
./gradlew build
```

Windows:

```powershell
.\gradlew.bat build
```

Compiles sources and assembles the plugin artifact.

### Run Sandbox IDE

```bash
./gradlew runIde
```

Launches a child IntelliJ instance with the plugin installed. Use this for manual testing.

### Verify Plugin

```bash
./gradlew verifyPlugin
```

Runs plugin compatibility/verification checks provided by the IntelliJ Platform Gradle Plugin.

### Build Distribution ZIP

```bash
./gradlew buildPlugin
```

The plugin ZIP is generated under:

```
build/distributions/
```

## Source Layout

```
copy-project-context-for-ai/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── src/main/
│   ├── kotlin/com/aicontext/plugin/
│   │   ├── actions/
│   │   ├── models/
│   │   ├── services/
│   │   ├── settings/
│   │   └── utils/
│   └── resources/META-INF/plugin.xml
└── docs/
```

## Key Extension Points

### plugin.xml

- Registers application service: `SettingsService`
- Registers settings page: `PluginSettingsConfigurable`
- Registers action in `ProjectViewPopupMenu`
- Declares dependency on `com.intellij.modules.platform`

### Action Update Thread

`CopyProjectContextAction` uses `ActionUpdateThread.BGT` for thread-safe enablement checks.

### Background Processing

File traversal and formatting run in `Task.Backgroundable` to avoid blocking the UI thread.

## Manual Test Checklist

- [ ] Action appears in Project View context menu
- [ ] Action disabled when nothing is selected
- [ ] Single file export works
- [ ] Folder export works recursively
- [ ] Mixed file/folder selection works
- [ ] `.gitignore` rules are respected
- [ ] `.cursorignore` / `.aiderignore` / `.copilotignore` are respected
- [ ] Default exclusions skip `node_modules`, `.git`, binaries, etc.
- [ ] Output contains project tree and file sections
- [ ] Code blocks use expected language identifiers
- [ ] Clipboard contains full output
- [ ] Success notification shows file and character counts
- [ ] Limit dialog appears for large selections
- [ ] Settings persist across IDE restarts

## Debugging Tips

### Logging

The plugin uses `com.intellij.openapi.diagnostic.Logger`.

View IDE logs:

- **Help → Show Log in Explorer** (Windows)
- **Help → Show Log in Finder** (macOS)

### Breakpoints

Set breakpoints in:

- `CopyProjectContextAction`
- `FileCollectorService`
- `IgnoreRulesService`
- `ContextFormatterService`

Then run `runIde` in debug mode.

### Common Issues

**Action not visible**

- Ensure a project is open.
- Confirm `plugin.xml` action group is `ProjectViewPopupMenu`.

**Empty output**

- Verify selected paths are inside project root.
- Check whether ignore rules exclude all selected files.

**Encoding issues**

- Files are read as UTF-8. Non-UTF-8 files may produce replacement characters.

## Platform Version Configuration

Platform version is configured in `gradle.properties`:

```properties
platformVersion=2024.2.5
pluginSinceBuild=242
```

When targeting newer IDEs, update these values and run `./gradlew verifyPlugin`.

## Contribution Workflow

1. Create a branch.
2. Implement changes.
3. Run `./gradlew build`.
4. Test in `runIde` sandbox.
5. Update `CHANGELOG.md` for user-visible changes.
6. Open a pull request.

See [CONTRIBUTING.md](../CONTRIBUTING.md) for full contribution guidelines.
