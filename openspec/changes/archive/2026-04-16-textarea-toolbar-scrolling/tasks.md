## 1. TextareaAnnotation Model

- [x] 1.1 Create `TextareaAnnotation` class with fields: `content`, `fontSize`, `bold`, `italic`, `textAlign`, `fontColor` (hex, default `#000000`), `wrap` (boolean, default true), `backgroundFill` (hex or `transparent`, default `transparent`), `showBorder` (boolean, default true), `verticalAlign` (`TOP`/`MIDDLE`/`BOTTOM`, default `TOP`)
- [x] 1.2 Add getters and setters for all fields

## 2. Serialisation

- [x] 2.1 In `PdfTapirPackageService`, add serialisation for `TextareaAnnotation` (type key `textarea`)
- [x] 2.2 In `PdfTapirPackageService`, add deserialisation for `TextareaAnnotation` with safe defaults for all fields

## 3. PDF Flattening

- [x] 3.1 In `PdfAnnotationFlattener`, add `drawTextarea()` method: draw background fill rect, optional border rect, then render text lines using greedy word-wrap loop
- [x] 3.2 Implement greedy word-wrap: split content on whitespace, accumulate words until line exceeds box width using PDFBox font metrics, emit lines
- [x] 3.3 Apply vertical alignment: compute total text block height and offset start Y for middle/bottom
- [x] 3.4 Apply font color via `setNonStrokingColor` before text rendering
- [x] 3.5 In no-wrap mode, render a single line per newline character (clipped at box boundary)

## 4. Canvas Rendering

- [x] 4.1 In `PdfCanvas.drawAnnotation()`, add case for `TextareaAnnotation`: fill background, draw optional border, then render wrapped text lines using JavaFX `gc.measureText()` greedy line-breaker
- [x] 4.2 Apply vertical alignment offset in canvas renderer
- [x] 4.3 Apply font color via `gc.setFill(Color.web(fontColor))`

## 5. Properties Panel

- [x] 5.1 Add textarea section to `PropertiesPanel`: content `TextArea`, font size, bold/italic toggles, text alignment buttons
- [x] 5.2 Add font color picker (ColorPicker) to textarea section
- [x] 5.3 Add wrap toggle (CheckBox, default checked) to textarea section
- [x] 5.4 Add vertical alignment toggle buttons (Top / Middle / Bottom)
- [x] 5.5 Add background fill color picker (ColorPicker + transparent toggle) to textarea section
- [x] 5.6 Add border toggle (CheckBox, default checked) to textarea section
- [x] 5.7 Wire all controls with undo support; populate from model in `showAnnotation()`

## 6. Toolbar — Textarea Tool Button

- [x] 6.1 Add `TEXTAREA` to the `Tool` enum
- [x] 6.2 Add Textarea tool button to `EditorToolBar`
- [x] 6.3 In `PdfCanvas`, handle drag-to-create for the `TEXTAREA` tool (same pattern as TEXT tool, creates a `TextareaAnnotation`)

## 7. Toolbar — Larger Button Size

- [x] 7.1 In `EditorToolBar`, increase tool button `prefWidth`/`prefHeight` and icon `ImageView` size to a larger value
- [x] 7.2 Increase the font size of tool button labels

## 8. Scroll-Pan Preference — Service

- [x] 8.1 Create `PreferencesService` with `load()` and `save()` using `java.util.Properties` backed by `~/.pdftapir/preferences.properties`
- [x] 8.2 Add `isScrollPan()` and `setScrollPan(boolean)` methods; create the directory on first write if absent

## 9. Scroll-Pan Preference — UI Wiring

- [x] 9.1 Add "Scroll with mouse wheel" `CheckMenuItem` to the View menu in `MainWindow`
- [x] 9.2 On startup, read preference from `PreferencesService` and set the `CheckMenuItem` state
- [x] 9.3 When preference is enabled, add a `ScrollEvent` filter on the `ScrollPane` that translates wheel delta to `setVvalue`/`setHvalue` adjustments
- [x] 9.4 When preference is disabled, remove the `ScrollEvent` filter
- [x] 9.5 On toggle, persist the new value via `PreferencesService.save()`

## 10. Verification

- [x] 10.1 Verify textarea placement, wrapping, and no-wrap mode on canvas
- [x] 10.2 Verify textarea properties (font color, fill, border, vertical alignment) render correctly in canvas and in exported flattened PDF
- [x] 10.3 Verify textarea annotations survive a save/reopen round-trip
- [x] 10.4 Verify toolbar buttons are visibly larger (requires running the app)
- [x] 10.5 Verify scroll preference persists across app restarts (requires running the app)
- [x] 10.6 Verify mouse wheel scrolls canvas when preference is on, does nothing when off (requires running the app)
