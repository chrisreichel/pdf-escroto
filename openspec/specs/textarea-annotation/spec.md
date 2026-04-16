## ADDED Requirements

### Requirement: User can place a textarea annotation on a page
The application SHALL provide a "Textarea" tool that lets the user click and drag to define a rectangular bounding box on the canvas; a multi-line text block is created inside that box.

#### Scenario: Textarea tool is available in the toolbar
- **WHEN** the application has a document open
- **THEN** a "Textarea" tool button is visible in the editor toolbar

#### Scenario: Dragging places a textarea annotation
- **WHEN** the Textarea tool is selected and the user clicks and drags on the canvas
- **THEN** a new textarea annotation is created with the dragged bounding box

#### Scenario: Textarea annotation is visible on the canvas
- **WHEN** a textarea annotation exists on the current page
- **THEN** it is rendered on the canvas with its text content inside the bounding box

### Requirement: Textarea text wraps within the bounding box by default
The application SHALL wrap text at word boundaries when the text exceeds the annotation width; wrapping SHALL be enabled by default.

#### Scenario: Long text wraps to the next line
- **WHEN** a textarea annotation has text that exceeds the width of its bounding box
- **THEN** the text wraps onto subsequent lines within the box

#### Scenario: Wrapping can be disabled
- **WHEN** the user disables the wrap option in the properties panel
- **THEN** text no longer wraps and long lines are clipped at the box boundary

#### Scenario: Wrapping state is preserved on save and reopen
- **WHEN** the document is saved and reopened
- **THEN** the wrap setting of each textarea annotation is restored

### Requirement: Textarea annotation has configurable visual properties
The application SHALL allow the user to configure font size, bold, italic, text alignment, font color, background fill, border visibility, and vertical alignment for each textarea annotation.

#### Scenario: Font color can be set
- **WHEN** the user changes the font color in the properties panel
- **THEN** the textarea text is rendered in the selected color on the canvas and in the exported PDF

#### Scenario: Background fill can be set to a solid color
- **WHEN** the user sets a background fill color
- **THEN** the annotation bounding box is filled with that color behind the text

#### Scenario: Background fill defaults to transparent
- **WHEN** a new textarea annotation is created
- **THEN** the background fill is transparent

#### Scenario: Border can be hidden
- **WHEN** the user disables the border option
- **THEN** no border is drawn around the annotation bounding box

#### Scenario: Border is visible by default
- **WHEN** a new textarea annotation is created
- **THEN** a border is rendered around the bounding box

#### Scenario: Vertical alignment can be set to top, middle, or bottom
- **WHEN** the user selects a vertical alignment option
- **THEN** the text block is positioned at the top, middle, or bottom of the bounding box accordingly

### Requirement: Textarea annotations are flattened into the PDF on export and save
The application SHALL render textarea annotations (including wrapping, font color, fill, border, and vertical alignment) as fixed page content when saving or exporting a flattened PDF.

#### Scenario: Textarea text is present in the saved PDF
- **WHEN** the document is saved and opened in an external PDF viewer
- **THEN** the textarea text is visible as page content at the correct position

#### Scenario: Word-wrapped text is preserved in the flattened PDF
- **WHEN** the document is saved with a wrapping textarea annotation
- **THEN** line breaks in the flattened PDF match the wrap preview shown in the editor
