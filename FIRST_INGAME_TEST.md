# First in-game test

This is a development test, not a Plugin Hub release. The launcher and dependencies have
not yet been verified against a successful CI build.

## Windows setup

1. Install a JDK 17 and Gradle 8.14 (or use a compatible Gradle installation).
2. Clone this repository, then open PowerShell in the cloned folder.
3. Run `gradle --no-daemon clean test build`.
4. If the build succeeds, run `gradle runDev`.
5. Wait for the RuneLite client to launch, then look for **Preset Manager** in the sidebar.

The launcher is under `src/test/java` so it is not included in the distributable JAR.
If `runDev` fails to compile, copy the complete Gradle error log and fix the API
compatibility issue before proceeding. Do not install the unverified JAR into a normal
RuneLite profile.

## Safe first test

Use a disposable RuneLite profile or back up your configuration first.
Do not test with sensitive keys or settings you cannot afford to lose.

1. Open **My Presets**, create a preset named `Test Setup`, and tag it `Testing`.
2. Rename and duplicate the preset. Check search and tab navigation.
3. Add one harmless, known configuration key, capture its current value, and change
   the saved value.
4. Activate the preset. Confirm only that key changed.
5. Restore previous settings. Confirm the original value returns.
6. Activate again, restart the development client, and try restoring.
7. Open **Boss Library**, select Theatre of Blood, dismiss one recommendation,
   restart, and verify the dismissal remains saved.
8. Check icon readability, clipping, scrolling, and high-DPI behavior.

## Build artifacts

The **Java build** GitHub Actions workflow runs on pushes and can also be triggered
manually from the Actions tab. A successful run uploads `preset-manager-jar` as an
artifact; failed tests may upload `test-reports`.

A successful JAR build alone does **not** mean the plugin is ready for Plugin Hub.
In-client testing and Plugin Hub review remain separate gates.
