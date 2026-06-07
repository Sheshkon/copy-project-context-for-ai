# Marketplace assets

Files in this folder are **not bundled into the plugin ZIP**. They are stored here for version control and uploaded manually to [JetBrains Marketplace](https://plugins.jetbrains.com).

## Folder structure

```
docs/marketplace/
├── logo/
│   └── plugin-icon.png      ← plugin logo (upload to Marketplace)
└── screenshots/
    ├── 01-context-menu.png  ← screenshots for the Media carousel
    ├── 02-settings.png
    └── ...
```

## Plugin logo

| Property | Recommendation |
|----------|----------------|
| Path | `docs/marketplace/logo/plugin-icon.png` |
| Format | PNG (transparent background preferred) |
| Size | **128×128 px** (square) |

**Where to upload:** after publishing the plugin, open your plugin page → **Edit** → **General Information** → **Plugin Icon**.

## Screenshots (description images)

| Property | Recommendation |
|----------|----------------|
| Path | `docs/marketplace/screenshots/` |
| Format | PNG or JPG |
| Size | **1200×760 px** minimum (16:10 aspect ratio) |

Suggested screenshots:

1. Context menu with **Copy Selected to AI** action
2. Success notification after copying
3. Settings page (limits, ignore rules, output options)
4. Example of pasted AI context (optional)

**Where to upload:** plugin page → **Edit** → **Media** → **Plugin Screenshots**.

JetBrains displays these in a carousel at the top of the listing. Inline images inside `description.html` are not recommended — use the Media section instead.

## Text description

The marketplace description is **not** stored here. Edit `description.html` in the project root; Gradle injects it into `plugin.xml` at build time.

Release notes for the current version: edit `change-notes.html` in the project root.

## Publishing checklist

1. Put `plugin-icon.png` in `docs/marketplace/logo/`
2. Add screenshots to `docs/marketplace/screenshots/`
3. Update `description.html` and `change-notes.html` if needed
4. Build: `.\gradlew.bat buildPlugin`
5. Upload the ZIP from `build/distributions/` to JetBrains Marketplace
6. On the plugin edit page, upload logo and screenshots from this folder
