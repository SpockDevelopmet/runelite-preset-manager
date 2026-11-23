# Release verification

Preset Manager must not be submitted to Plugin Hub until the checks below pass.
This checklist tracks evidence, not intended behavior.

## Build and dependencies

- [ ] GitHub Actions completes `clean test build` on Java 17.
- [ ] Generated JAR loads in a current RuneLite client.
- [ ] All imported RuneLite APIs are permitted for Plugin Hub plugins.
- [ ] Runtime dependencies are compatible with Plugin Hub packaging.

## Presets and recovery

- [ ] Create, rename, retag, duplicate and delete a personal preset.
- [ ] Search presets by name and tag; switch between My Presets and Boss Library.
- [ ] Create a personal preset from a built-in encounter.
- [ ] Capture, edit, remove and apply configuration values.
- [ ] Restore values that existed before activation, including previously unset keys.
- [ ] Restart RuneLite with an active preset and restore successfully.
- [ ] Simulate a failed write; ensure the recovery snapshot remains available.
- [ ] Import/export valid JSON; reject invalid or oversized files without data loss.
- [ ] Confirm unrelated configuration values remain unchanged.

## Recommendation UX

- [ ] Verify recommendation names and plugin identifiers against RuneLite/Plugin Hub.
- [x] Show whether a recommended plugin appears in the client’s loaded plugin list (implemented; not verified in client).
- [ ] Distinguish installed, disabled and missing Plugin Hub plugins reliably.
- [ ] Confirm dismissed suggestions remain hidden after restart.
- [ ] Confirm dismissals are scoped to the chosen encounter.
- [ ] Verify missing-plugin installation guidance without automatic installation.

## Visual QA

- [ ] Review icon at the actual 16px RuneLite sidebar scale.
- [ ] Review the tabbed My Presets / Boss Library panel at standard and high-DPI scaling.
- [ ] Check empty, long-name, error, active and restored states.
- [ ] Verify keyboard focus and text clipping.

## Submission

- [ ] Confirm Plugin Hub manifest and metadata requirements.
- [ ] Check license and third-party asset provenance.
- [ ] Submit Plugin Hub PR only after a passing build and in-client testing.
