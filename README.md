# Preset Manager

A RuneLite plugin for switching between activity-specific configuration presets without replacing your everyday setup.

The idea came from bossing: some encounters call for a very different collection of overlays and alerts, but those settings get in the way everywhere else. Preset Manager aims to make those changes reversible and easy to find.

## How it works

Create a preset for any activity, give it a name and tag, then add the configuration entries you want it to control. Rename or retag it later, or duplicate it to make a variation without changing the original. You can enter a value or capture the current value of an entry. Activating a preset saves the previous values before applying the new ones. Use **Restore previous settings** when you're done.

Only the entries in that preset are changed. Other RuneLite settings are left alone.

For now, configuration groups and keys must be entered manually. This is an early source implementation, not a published Plugin Hub release.

## What's in the repository

- Sidebar panel for creating, selecting and deleting presets
- Custom names and tags, with rename and duplicate controls
- Separate My Presets and Boss Library tabs, with search in each
- Create a personal preset directly from a boss or raid entry
- Capture current configuration value for an existing entry
- Selective apply and restore
- Persistent recovery data across client restarts
- JSON import and export
- Curated encounter suggestions for Theatre of Blood, Inferno and Fortis Colosseum
- Per-encounter dismissal preferences with a reset control

## Built-in encounters and personal presets

The encounter browser currently provides curated recommendations for Theatre of Blood, Chambers of Xeric, Tombs of Amascut, Inferno, Fortis Colosseum, Doom of Mokhaiotl, Zulrah, Vorkath, The Gauntlet and The Nightmare. It does not restrict which presets you can create. Personal presets can be named after a boss, skilling method, minigame, or anything else.

Built-in recommendations are not automatically installed, enabled, or included in your personal presets. The long-term plan is to make the encounter library a convenient starting point while keeping personal presets fully editable.

## Artwork

The navigation icon is a small, original, hand-authored 16-pixel raster drawn from a fixed palette in `PresetIcon.java`. It deliberately avoids emoji, stock symbols and generated illustrations. Its appearance still needs review inside RuneLite at the actual sidebar scale.

## Limitations

Recommendation suggestions are editorial examples, not measured usage rankings. The Boss Library now checks whether matching plugins are present among the plugins loaded by the RuneLite client. This is **not** a reliable installed-versus-not-installed check: a plugin absent from the loaded list may still be installed but disabled. Verified Plugin Hub identifiers, installation links and full installation-state detection are not implemented yet.\n\nThis plugin does **not** enable or disable other plugins. It changes configuration values only. RuneLite does not guarantee that every plugin reacts immediately to configuration changes, and some may need a restart.

Import files can contain arbitrary configuration values. Only import files you trust, and check exported files before sharing them. They may contain sensitive settings.

If recovery data is damaged, the plugin blocks activation rather than overwriting the recovery snapshot. Back up your RuneLite settings before attempting manual recovery.

## Building

The project uses Java and Gradle. From the repository root:

```sh
gradle test
gradle build
```

A Gradle wrapper has not yet been committed. The RuneLite dependency currently uses `latest.release`; pin it to a tested version before a public release.

## Development status

The implementation is still under development. Compilation, in-client testing, compatibility checks and Plugin Hub review have not been completed. See the Actions tab for automated build results when available.

Release blockers: passing CI, verified RuneLite API compatibility, configuration discovery, verified Plugin Hub identifiers, installation detection, further sidebar usability and accessibility review, automated integration tests, and Plugin Hub policy review.

## License

No license has been selected yet. All rights reserved until a license is added.


## Try it in RuneLite (development build)

See [FIRST_INGAME_TEST.md](FIRST_INGAME_TEST.md) for the Windows setup and a safe test plan.
With JDK 17 and Gradle 8.14 installed, run `gradle clean test build`, followed by
`gradle runDev`. The development launcher lives in the test source set and is not
included in the plugin JAR. The launcher and build have not yet been verified in CI.

GitHub Actions also supports manual runs and uploads the JAR on success. Do not
submit or distribute an untested build as a public Plugin Hub release.
