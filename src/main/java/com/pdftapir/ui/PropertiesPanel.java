package com.pdftapir.ui;

import com.pdftapir.command.EditAnnotationCommand;
import com.pdftapir.command.UndoManager;
import com.pdftapir.model.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

public class PropertiesPanel {

    private final VBox        node = new VBox(6);
    private final UndoManager undoManager;
    private final Runnable    onRedraw;
    private Annotation        current;

    // Shared geometry fields
    private final TextField xField = new TextField();
    private final TextField yField = new TextField();
    private final TextField wField = new TextField();
    private final TextField hField = new TextField();

    // Text-only section
    private final TextField        textField       = new TextField();
    private final TextField        fontField       = new TextField();
    private final ColorPicker      textColorPicker = new ColorPicker(Color.BLACK);
    private final ComboBox<String> fontFamilyBox   = new ComboBox<>();
    private final CheckBox         boldBox         = new CheckBox("Bold");
    private final CheckBox         italicBox       = new CheckBox("Italic");
    private final ToggleButton     alignLeftBtn    = new ToggleButton("L");
    private final ToggleButton     alignCenterBtn  = new ToggleButton("C");
    private final ToggleButton     alignRightBtn   = new ToggleButton("R");
    private final ToggleGroup      alignGroup      = new ToggleGroup();
    private final VBox             textSection     = new VBox(4);

    // Checkbox-only section
    private final TextField   labelField    = new TextField();
    private final CheckBox    checkedBox    = new CheckBox("Checked");
    private final CheckBox    borderlessBox = new CheckBox("Borderless");
    private final ColorPicker cbColorPicker = new ColorPicker(Color.BLACK);
    private final VBox        cbSection     = new VBox(4);

    // Textarea-only section
    private final TextArea      taContentArea    = new TextArea();
    private final TextField     taFontSizeField  = new TextField();
    private final ColorPicker   taFontColorPicker = new ColorPicker(Color.BLACK);
    private final CheckBox      taBoldBox        = new CheckBox("Bold");
    private final CheckBox      taItalicBox      = new CheckBox("Italic");
    private final ToggleButton  taAlignLeftBtn   = new ToggleButton("L");
    private final ToggleButton  taAlignCenterBtn = new ToggleButton("C");
    private final ToggleButton  taAlignRightBtn  = new ToggleButton("R");
    private final ToggleGroup   taAlignGroup     = new ToggleGroup();
    private final CheckBox      taWrapBox        = new CheckBox("Word wrap");
    private final ToggleButton  taVAlignTopBtn   = new ToggleButton("Top");
    private final ToggleButton  taVAlignMidBtn   = new ToggleButton("Middle");
    private final ToggleButton  taVAlignBotBtn   = new ToggleButton("Bottom");
    private final ToggleGroup   taVAlignGroup    = new ToggleGroup();
    private final CheckBox      taTransparentBox = new CheckBox("Transparent bg");
    private final ColorPicker   taFillPicker     = new ColorPicker(Color.WHITE);
    private final CheckBox      taBorderBox      = new CheckBox("Show border");
    private final VBox          taSection        = new VBox(4);

    public PropertiesPanel(UndoManager undoManager, Runnable onRedraw) {
        this.undoManager = undoManager;
        this.onRedraw    = onRedraw;
        node.getStyleClass().add("properties-panel");
        node.setPrefWidth(200);
        node.setPadding(new Insets(10));
        fontFamilyBox.getItems().addAll(
                "System", "Arial", "Times New Roman", "Courier New", "Georgia", "Verdana");
        alignLeftBtn.setToggleGroup(alignGroup);
        alignCenterBtn.setToggleGroup(alignGroup);
        alignRightBtn.setToggleGroup(alignGroup);
        alignLeftBtn.setUserData("LEFT");
        alignCenterBtn.setUserData("CENTER");
        alignRightBtn.setUserData("RIGHT");

        taAlignLeftBtn.setToggleGroup(taAlignGroup);
        taAlignCenterBtn.setToggleGroup(taAlignGroup);
        taAlignRightBtn.setToggleGroup(taAlignGroup);
        taAlignLeftBtn.setUserData("LEFT");
        taAlignCenterBtn.setUserData("CENTER");
        taAlignRightBtn.setUserData("RIGHT");

        taVAlignTopBtn.setToggleGroup(taVAlignGroup);
        taVAlignMidBtn.setToggleGroup(taVAlignGroup);
        taVAlignBotBtn.setToggleGroup(taVAlignGroup);
        taVAlignTopBtn.setUserData("TOP");
        taVAlignMidBtn.setUserData("MIDDLE");
        taVAlignBotBtn.setUserData("BOTTOM");

        taContentArea.setPrefRowCount(4);
        taContentArea.setWrapText(true);
        taWrapBox.setSelected(true);
        taBorderBox.setSelected(true);
        taTransparentBox.setSelected(true);

        buildLayout();
        showAnnotation(null);
    }

    private void buildLayout() {
        var styleRow  = new HBox(6, boldBox, italicBox);
        var alignRow  = new HBox(2, alignLeftBtn, alignCenterBtn, alignRightBtn);

        textSection.getChildren().addAll(
                label("Text"), textField,
                label("Font size"), fontField,
                label("Font color"), textColorPicker,
                label("Font family"), fontFamilyBox,
                styleRow,
                label("Alignment"), alignRow);

        cbSection.getChildren().addAll(
                label("Label"), labelField,
                checkedBox,
                borderlessBox,
                label("Checkmark color"), cbColorPicker);

        var taStyleRow  = new HBox(6, taBoldBox, taItalicBox);
        var taAlignRow  = new HBox(2, taAlignLeftBtn, taAlignCenterBtn, taAlignRightBtn);
        var taVAlignRow = new HBox(2, taVAlignTopBtn, taVAlignMidBtn, taVAlignBotBtn);
        taSection.getChildren().addAll(
                label("Content"), taContentArea,
                label("Font size"), taFontSizeField,
                label("Font color"), taFontColorPicker,
                taStyleRow,
                label("Alignment"), taAlignRow,
                taWrapBox,
                label("Vertical align"), taVAlignRow,
                taTransparentBox, label("Fill color"), taFillPicker,
                taBorderBox);

        node.getChildren().addAll(
                bold("Properties"),
                label("X"), xField, label("Y"), yField,
                label("W"), wField, label("H"), hField,
                textSection, cbSection, taSection
        );

        // Commit geometry on Enter or focus-lost
        commitOnChange(xField, this::commitMove);
        commitOnChange(yField, this::commitMove);
        commitOnChange(wField, this::commitResize);
        commitOnChange(hField, this::commitResize);

        // Text content
        commitOnChange(textField, () -> {
            if (!(current instanceof TextAnnotation ta)) return;
            String old = ta.getText();
            String nw  = textField.getText();
            if (old.equals(nw)) return;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { ta.setText(nw);  onRedraw.run(); },
                    () -> { ta.setText(old); onRedraw.run(); }));
        });

        // Font size
        commitOnChange(fontField, () -> {
            if (!(current instanceof TextAnnotation ta)) return;
            float old = ta.getFontSize();
            try {
                float nw = Float.parseFloat(fontField.getText());
                if (old == nw) return;
                undoManager.execute(new EditAnnotationCommand(
                        () -> { ta.setFontSize(nw);  onRedraw.run(); },
                        () -> { ta.setFontSize(old); onRedraw.run(); }));
            } catch (NumberFormatException ignored) {}
        });

        // Text font color
        textColorPicker.setOnAction(e -> {
            if (!(current instanceof TextAnnotation ta)) return;
            String old = ta.getFontColor();
            String nw  = toHex(textColorPicker.getValue());
            if (old.equalsIgnoreCase(nw)) return;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { ta.setFontColor(nw);  onRedraw.run(); },
                    () -> { ta.setFontColor(old); onRedraw.run(); }));
        });

        // Font family
        fontFamilyBox.setOnAction(e -> {
            if (!(current instanceof TextAnnotation ta)) return;
            String old = ta.getFontFamily();
            String nw  = fontFamilyBox.getValue();
            if (nw == null || nw.equals(old)) return;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { ta.setFontFamily(nw);  onRedraw.run(); },
                    () -> { ta.setFontFamily(old); onRedraw.run(); }));
        });

        // Bold
        boldBox.setOnAction(e -> {
            if (!(current instanceof TextAnnotation ta)) return;
            boolean nw  = boldBox.isSelected();
            boolean old = !nw;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { ta.setBold(nw);  onRedraw.run(); },
                    () -> { ta.setBold(old); onRedraw.run(); }));
        });

        // Italic
        italicBox.setOnAction(e -> {
            if (!(current instanceof TextAnnotation ta)) return;
            boolean nw  = italicBox.isSelected();
            boolean old = !nw;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { ta.setItalic(nw);  onRedraw.run(); },
                    () -> { ta.setItalic(old); onRedraw.run(); }));
        });

        // Alignment
        alignGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (!(current instanceof TextAnnotation ta)) return;
            if (newToggle == null) return;
            String nw  = (String) newToggle.getUserData();
            String old = ta.getTextAlign();
            if (nw.equals(old)) return;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { ta.setTextAlign(nw);  onRedraw.run(); },
                    () -> { ta.setTextAlign(old); onRedraw.run(); }));
        });

        // Checkbox label
        commitOnChange(labelField, () -> {
            if (!(current instanceof CheckboxAnnotation ca)) return;
            String old = ca.getLabel();
            String nw  = labelField.getText();
            if (old.equals(nw)) return;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { ca.setLabel(nw);  onRedraw.run(); },
                    () -> { ca.setLabel(old); onRedraw.run(); }));
        });

        // Checkbox checked state
        checkedBox.setOnAction(e -> {
            if (!(current instanceof CheckboxAnnotation ca)) return;
            boolean nw  = checkedBox.isSelected();
            boolean old = !nw;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { ca.setChecked(nw);  onRedraw.run(); },
                    () -> { ca.setChecked(old); onRedraw.run(); }));
        });

        // Borderless
        borderlessBox.setOnAction(e -> {
            if (!(current instanceof CheckboxAnnotation ca)) return;
            boolean nw  = borderlessBox.isSelected();
            boolean old = !nw;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { ca.setBorderless(nw);  onRedraw.run(); },
                    () -> { ca.setBorderless(old); onRedraw.run(); }));
        });

        // Checkmark color
        cbColorPicker.setOnAction(e -> {
            if (!(current instanceof CheckboxAnnotation ca)) return;
            String old = ca.getCheckmarkColor();
            String nw  = toHex(cbColorPicker.getValue());
            if (old.equalsIgnoreCase(nw)) return;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { ca.setCheckmarkColor(nw);  onRedraw.run(); },
                    () -> { ca.setCheckmarkColor(old); onRedraw.run(); }));
        });

        // --- Textarea wiring ---

        // Content
        taContentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!(current instanceof TextareaAnnotation txa)) return;
            if (txa.getContent().equals(newVal)) return;
            String old = txa.getContent();
            undoManager.execute(new EditAnnotationCommand(
                    () -> { txa.setContent(newVal); onRedraw.run(); },
                    () -> { txa.setContent(old);    onRedraw.run(); }));
        });

        // Font size
        commitOnChange(taFontSizeField, () -> {
            if (!(current instanceof TextareaAnnotation txa)) return;
            float old = txa.getFontSize();
            try {
                float nw = Float.parseFloat(taFontSizeField.getText());
                if (old == nw) return;
                undoManager.execute(new EditAnnotationCommand(
                        () -> { txa.setFontSize(nw);  onRedraw.run(); },
                        () -> { txa.setFontSize(old); onRedraw.run(); }));
            } catch (NumberFormatException ignored) {}
        });

        // Font color
        taFontColorPicker.setOnAction(e -> {
            if (!(current instanceof TextareaAnnotation txa)) return;
            String old = txa.getFontColor();
            String nw  = toHex(taFontColorPicker.getValue());
            if (old.equalsIgnoreCase(nw)) return;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { txa.setFontColor(nw);  onRedraw.run(); },
                    () -> { txa.setFontColor(old); onRedraw.run(); }));
        });

        // Bold
        taBoldBox.setOnAction(e -> {
            if (!(current instanceof TextareaAnnotation txa)) return;
            boolean nw = taBoldBox.isSelected(), old = !nw;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { txa.setBold(nw);  onRedraw.run(); },
                    () -> { txa.setBold(old); onRedraw.run(); }));
        });

        // Italic
        taItalicBox.setOnAction(e -> {
            if (!(current instanceof TextareaAnnotation txa)) return;
            boolean nw = taItalicBox.isSelected(), old = !nw;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { txa.setItalic(nw);  onRedraw.run(); },
                    () -> { txa.setItalic(old); onRedraw.run(); }));
        });

        // Alignment
        taAlignGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (!(current instanceof TextareaAnnotation txa)) return;
            if (newToggle == null) return;
            String nw = (String) newToggle.getUserData(), old = txa.getTextAlign();
            if (nw.equals(old)) return;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { txa.setTextAlign(nw);  onRedraw.run(); },
                    () -> { txa.setTextAlign(old); onRedraw.run(); }));
        });

        // Wrap
        taWrapBox.setOnAction(e -> {
            if (!(current instanceof TextareaAnnotation txa)) return;
            boolean nw = taWrapBox.isSelected(), old = !nw;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { txa.setWrap(nw);  onRedraw.run(); },
                    () -> { txa.setWrap(old); onRedraw.run(); }));
        });

        // Vertical alignment
        taVAlignGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (!(current instanceof TextareaAnnotation txa)) return;
            if (newToggle == null) return;
            String nw = (String) newToggle.getUserData(), old = txa.getVerticalAlign();
            if (nw.equals(old)) return;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { txa.setVerticalAlign(nw);  onRedraw.run(); },
                    () -> { txa.setVerticalAlign(old); onRedraw.run(); }));
        });

        // Transparent background toggle
        taTransparentBox.setOnAction(e -> {
            if (!(current instanceof TextareaAnnotation txa)) return;
            boolean transparent = taTransparentBox.isSelected();
            taFillPicker.setDisable(transparent);
            String nw  = transparent ? "transparent" : toHex(taFillPicker.getValue());
            String old = txa.getBackgroundFill();
            if (nw.equalsIgnoreCase(old)) return;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { txa.setBackgroundFill(nw);  onRedraw.run(); },
                    () -> { txa.setBackgroundFill(old); onRedraw.run(); }));
        });

        // Fill color
        taFillPicker.setOnAction(e -> {
            if (!(current instanceof TextareaAnnotation txa)) return;
            if (taTransparentBox.isSelected()) return;
            String old = txa.getBackgroundFill();
            String nw  = toHex(taFillPicker.getValue());
            if (nw.equalsIgnoreCase(old)) return;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { txa.setBackgroundFill(nw);  onRedraw.run(); },
                    () -> { txa.setBackgroundFill(old); onRedraw.run(); }));
        });

        // Border toggle
        taBorderBox.setOnAction(e -> {
            if (!(current instanceof TextareaAnnotation txa)) return;
            boolean nw = taBorderBox.isSelected(), old = !nw;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { txa.setShowBorder(nw);  onRedraw.run(); },
                    () -> { txa.setShowBorder(old); onRedraw.run(); }));
        });
    }

    public void showAnnotation(Annotation a) {
        current = a;
        boolean has = a != null;
        xField.setDisable(!has);
        yField.setDisable(!has);
        wField.setDisable(!has);
        hField.setDisable(!has);
        textSection.setVisible(a instanceof TextAnnotation);
        textSection.setManaged(a instanceof TextAnnotation);
        cbSection.setVisible(a instanceof CheckboxAnnotation);
        cbSection.setManaged(a instanceof CheckboxAnnotation);
        taSection.setVisible(a instanceof TextareaAnnotation);
        taSection.setManaged(a instanceof TextareaAnnotation);

        if (!has) {
            xField.clear(); yField.clear(); wField.clear(); hField.clear();
            return;
        }

        xField.setText(fmt(a.getX()));
        yField.setText(fmt(a.getY()));
        wField.setText(fmt(a.getWidth()));
        hField.setText(fmt(a.getHeight()));

        if (a instanceof TextAnnotation ta) {
            textField.setText(ta.getText());
            fontField.setText(String.valueOf((int) ta.getFontSize()));
            textColorPicker.setValue(parseColor(ta.getFontColor()));
            String family = ta.getFontFamily();
            fontFamilyBox.setValue(fontFamilyBox.getItems().contains(family) ? family : "System");
            boldBox.setSelected(ta.isBold());
            italicBox.setSelected(ta.isItalic());
            String align = ta.getTextAlign() == null ? "LEFT" : ta.getTextAlign();
            alignGroup.getToggles().stream()
                    .filter(t -> align.equals(t.getUserData()))
                    .findFirst()
                    .ifPresent(t -> alignGroup.selectToggle(t));
        } else if (a instanceof CheckboxAnnotation ca) {
            labelField.setText(ca.getLabel());
            checkedBox.setSelected(ca.isChecked());
            borderlessBox.setSelected(ca.isBorderless());
            cbColorPicker.setValue(parseColor(ca.getCheckmarkColor()));
        } else if (a instanceof TextareaAnnotation txa) {
            taContentArea.setText(txa.getContent());
            taFontSizeField.setText(String.valueOf((int) txa.getFontSize()));
            taFontColorPicker.setValue(parseColor(txa.getFontColor()));
            taBoldBox.setSelected(txa.isBold());
            taItalicBox.setSelected(txa.isItalic());
            String taAlign = txa.getTextAlign() == null ? "LEFT" : txa.getTextAlign();
            taAlignGroup.getToggles().stream()
                    .filter(t -> taAlign.equals(t.getUserData()))
                    .findFirst().ifPresent(taAlignGroup::selectToggle);
            taWrapBox.setSelected(txa.isWrap());
            String va = txa.getVerticalAlign() == null ? "TOP" : txa.getVerticalAlign();
            taVAlignGroup.getToggles().stream()
                    .filter(t -> va.equals(t.getUserData()))
                    .findFirst().ifPresent(taVAlignGroup::selectToggle);
            boolean transparent = "transparent".equalsIgnoreCase(txa.getBackgroundFill());
            taTransparentBox.setSelected(transparent);
            taFillPicker.setDisable(transparent);
            if (!transparent) taFillPicker.setValue(parseColor(txa.getBackgroundFill()));
            taBorderBox.setSelected(txa.isShowBorder());
        }
    }

    private void commitMove() {
        if (current == null) return;
        var ann = current;
        try {
            double oldX = ann.getX(), oldY = ann.getY();
            double newX = Double.parseDouble(xField.getText());
            double newY = Double.parseDouble(yField.getText());
            if (oldX == newX && oldY == newY) return;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { ann.setX(newX); ann.setY(newY); onRedraw.run(); },
                    () -> { ann.setX(oldX); ann.setY(oldY); onRedraw.run(); }));
        } catch (NumberFormatException ignored) {}
    }

    private void commitResize() {
        if (current == null) return;
        var ann = current;
        try {
            double oldW = ann.getWidth(), oldH = ann.getHeight();
            double newW = Double.parseDouble(wField.getText());
            double newH = Double.parseDouble(hField.getText());
            if (oldW == newW && oldH == newH) return;
            undoManager.execute(new EditAnnotationCommand(
                    () -> { ann.setWidth(newW); ann.setHeight(newH); onRedraw.run(); },
                    () -> { ann.setWidth(oldW); ann.setHeight(oldH); onRedraw.run(); }));
        } catch (NumberFormatException ignored) {}
    }

    private void commitOnChange(TextField tf, Runnable action) {
        tf.setOnAction(e -> action.run());
        tf.focusedProperty().addListener((obs, was, focused) -> { if (!focused) action.run(); });
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int) Math.round(c.getRed()   * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue()  * 255));
    }

    private Color parseColor(String hex) {
        try { return Color.web(hex); } catch (Exception e) { return Color.BLACK; }
    }

    private Label label(String text) {
        var l = new Label(text);
        l.getStyleClass().add("label");
        return l;
    }

    private Label bold(String text) {
        var l = new Label(text);
        l.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        return l;
    }

    private String fmt(double v) { return String.format("%.1f", v); }

    public Node getNode() { return node; }
}
