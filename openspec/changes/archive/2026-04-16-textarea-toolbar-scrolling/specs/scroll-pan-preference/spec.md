## ADDED Requirements

### Requirement: User can enable mouse-wheel scrolling on the document canvas
The application SHALL provide a toggleable preference that, when enabled, allows the user to scroll the document canvas vertically (and horizontally with Shift) using the mouse wheel.

#### Scenario: Scroll preference toggle is accessible
- **WHEN** the application is running
- **THEN** a "Scroll with mouse wheel" option is visible and toggleable in the application (e.g. View menu or preferences)

#### Scenario: Mouse wheel scrolls the canvas when preference is enabled
- **WHEN** the scroll preference is enabled and the user moves the mouse wheel over the canvas
- **THEN** the document canvas scrolls vertically in the direction of the wheel movement

#### Scenario: Mouse wheel does not scroll when preference is disabled
- **WHEN** the scroll preference is disabled
- **THEN** mouse wheel events over the canvas do not scroll the document

### Requirement: Scroll preference is persisted across application restarts
The application SHALL save the scroll preference to the filesystem and restore it when the application is next launched.

#### Scenario: Preference is saved on toggle
- **WHEN** the user enables or disables the scroll preference
- **THEN** the new value is written to `~/.pdftapir/preferences.properties`

#### Scenario: Preference is restored on launch
- **WHEN** the application starts and a preferences file exists
- **THEN** the scroll preference is read from the file and the scroll behaviour matches the saved value

#### Scenario: Application starts normally when no preferences file exists
- **WHEN** the application starts and no preferences file exists
- **THEN** the scroll preference defaults to disabled and the application launches without error
