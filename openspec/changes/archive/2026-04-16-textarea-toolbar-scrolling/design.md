## Context

`TextAnnotation` covers short text labels. There is no annotation type for multi-line paragraph text. The model, serialisation (`PdfTapirPackageService`), flattener (`PdfAnnotationFlattener`), canvas renderer (`PdfCanvas`), and properties panel (`PropertiesPanel`) would all need a new annotation type wired through them.

The toolbar is built in `EditorToolBar` using a fixed button/icon size defined in code. The canvas `ScrollPane` has `setPannable(false)` and scroll events are not forwarded to the scroll pane — making mouse-wheel navigation impossible today.

## Goals / Non-Goals

**Goals:**
- New `TextareaAnnotation` with word-wrap (default on), no-wrap mode, background fill (transparent default), border toggle, vertical alignment (top/middle/bottom), and font color
- Larger tool button icons and labels in `EditorToolBar`
- Persistent `scroll-pan` preference: when enabled, mouse-wheel events scroll the canvas `ScrollPane`; preference is stored in `~/.pdftapir/preferences.properties`

**Non-Goals:**
- Rich text / mixed formatting within a single textarea (each textarea is uniformly styled)
- Auto-grow / dynamic height (textarea has a fixed bounding box set at placement time)
- Custom font families (existing font size + bold/italic covers typography needs)
- Horizontal scrollbar within no-wrap textarea (clipped at box boundary)

## Decisions

**`TextareaAnnotation` as a separate model class (not a flag on `TextAnnotation`).**
- `TextAnnotation` is semantically a single-line label; `TextareaAnnotation` has a distinct property set (wrap, fill, border, vertical alignment, font color) and distinct rendering logic.
- Reusing `TextAnnotation` would add nullable/ignored fields and conditional rendering branches. A clean separation keeps both classes simple.

**Word-wrap via PDFBox `PDPageContentStream` line-breaking loop for flattening.**
- PDFBox has no built-in word-wrap for `PDPageContentStream`. We implement a simple greedy line-breaker: split on whitespace, accumulate words, emit a new line when the next word would exceed the box width. This is the standard PDFBox approach and requires no extra dependency.

**Canvas word-wrap via JavaFX `Text` measure + manual line splitting.**
- The canvas renderer uses `gc.measureText(word).getWidth()` to compute line breaks at the current font size and box width, matching the flatten logic closely enough for a faithful live preview.

**Background fill: stored as a hex string `"#RRGGBB"` or `"transparent"`.**
- Consistent with how colours are commonly expressed; easy to parse with `Color.web()` in JavaFX and `PDColor` in PDFBox.

**Font color: stored as a hex string, default `"#000000"`.**
- Extends naturally from the fill approach; PDFBox `setNonStrokingColor` takes RGB.

**Toolbar size: increase button preferred size and icon size via `EditorToolBar`.**
- The toolbar currently sets button sizes inline. Increasing `prefWidth`/`prefHeight` and the icon `ImageView` size is self-contained. A CSS approach is an alternative but adds a style sheet dependency; inline sizing is simpler for a fixed change.

**Preferences file at `~/.pdftapir/preferences.properties`.**
- Standard Java `Properties` load/store to a known user-home path. Avoids requiring a preferences library. The directory is created on first write if absent.
- A `PreferencesService` (new singleton-style service) owns load/save. `MainWindow` reads the preference at startup to configure the scroll handler and writes on toggle.

**Scroll-pan wired via `addEventFilter` on the `ScrollPane`.**
- When the preference is on, a `ScrollEvent` filter on the `ScrollPane` translates wheel delta into `scrollPane.setVvalue(...)` and `setHvalue(...)` adjustments, consuming the event so it does not also zoom. When the preference is off, the filter is removed.

## Risks / Trade-offs

- **Word-wrap divergence between canvas and flatten**: The canvas uses JavaFX font metrics; the flattener uses PDFBox/AWT metrics. Line breaks may differ slightly at edge cases. Mitigation: use the same greedy algorithm with the same threshold; minor visual differences are acceptable.
- **No-wrap clipping**: In no-wrap mode, long lines are clipped at the annotation boundary in both canvas and flatten. Users are responsible for sizing the box. This is explicit in the spec.
- **Preferences file location on Windows**: `System.getProperty("user.home")` returns the correct home directory on all platforms. Tested path: `C:\Users\<name>\.pdftapir\preferences.properties`.

## Migration Plan

No migration needed. `TextareaAnnotation` is a new annotation type; existing files are unaffected. `PdfTapirPackageService` reads an unknown annotation type gracefully (existing code skips unrecognised entries).
