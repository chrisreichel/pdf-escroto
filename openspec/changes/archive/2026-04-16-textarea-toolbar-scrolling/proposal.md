## Why

Users need to annotate PDFs with larger blocks of text that wrap naturally within the annotation boundary — the existing text box is designed for short single-line labels, not paragraph-style content. The toolbar icons and labels are also too small on high-DPI and larger displays. Finally, there is no way to scroll through a document with the mouse wheel, which makes navigating multi-page PDFs tedious.

## What Changes

- **Textarea annotation**: A new annotation type for multi-line, word-wrapped text blocks. Wrapping is on by default with a toggle to disable it. Additional properties: background fill (default transparent), border visibility, vertical alignment (top / middle / bottom), and font color.
- **Toolbar size**: Toolbar tool buttons are made larger — bigger icons and bigger label text — so they are easier to click and read on any screen size.
- **Scroll-to-pan preference**: A persistent application preference that, when enabled, lets the user scroll the document canvas with the mouse wheel. The preference is saved to a file on the filesystem and survives app restarts.

## Capabilities

### New Capabilities
- `textarea-annotation`: Place a multi-line word-wrapped text block on a PDF page, with wrapping, background fill, border, vertical alignment, and font color options
- `toolbar-size`: Larger tool button icons and labels in the editor toolbar
- `scroll-pan-preference`: Persistent mouse-wheel scrolling preference stored on the filesystem

### Modified Capabilities

## Impact

- **`TextareaAnnotation`** (new model class, extends `Annotation`)
- **`PdfTapirPackageService`** — serialize/deserialize textarea annotations
- **`PdfAnnotationFlattener`** — render textarea (word-wrap, fill, border, vertical alignment, font color) into page content
- **`PdfCanvas`** — draw textarea annotations in the editor canvas
- **`PropertiesPanel`** — textarea properties section (content, font size, font color, bold, italic, alignment, wrap toggle, vertical alignment, background fill, border toggle)
- **`EditorToolBar`** — larger icon/text sizes; new Textarea tool button
- **`MainWindow`** or new `PreferencesService` — load/save preferences file; wire scroll-pan toggle to canvas panning behaviour
- **No new Maven dependencies**
