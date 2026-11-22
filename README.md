# RuneLite Selective Preset Manager

Source project for a selective RuneLite configuration preset manager.

**Development status:** Not compiled or validated against RuneLite yet; not approved for Plugin Hub. Do not represent this commit as a production release.

## Current functionality
- Named and tagged presets with explicit configuration keys
- Apply only settings included in a preset
- Persist recovery snapshots before modifying settings
- Restore previous values, including after a client restart
- JSON import/export with validation and confirmation

## Known limitations
- Configuration keys are manually entered; plugin discovery is not implemented.
- Does not enable or disable other plugins.
- Some plugins require a restart for changed configuration to take effect.
- Never export or share private configuration data such as tokens or credentials.
- The Gradle dependency uses latest.release and needs to be pinned before release.
- This commit has not been build-tested. A Gradle wrapper and tests are still required.

## Development
Run `gradle test` and `gradle build` using Java 11+ and Gradle.
Review Plugin Hub rules and RuneLite public APIs before submission.
