package com.fluffy.luffs.weight.converter;

import java.util.Optional;
import java.util.regex.Pattern;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * WeightConverterController controller containing visual logic.
 */
public class WeightConverterController {

    @FXML
    private TextField weightValue;
    @FXML
    private Label weight;
    @FXML
    private ChoiceBox<Weight> convertFromChoice;
    @FXML
    private Label infoLabel;

    /**
     * Initializes the controller class.
     */
    public void initialize() {

        convertFromChoice.getItems().addAll(Weight.values());
        convertFromChoice.getSelectionModel().selectedItemProperty().addListener((ov, t, t1) -> {
            setInfoLabel(t1.getFullName());
            Optional.ofNullable(weightValue.getText()).filter(v -> !v.isBlank()).ifPresent(value -> weightValue.setText(t1.convertInput(value)));
        });

        convertFromChoice.getSelectionModel().select(0);

        weightValue.positionCaret(0);
        weightValue.setTextFormatter(new TextFormatter<String>(change -> Optional.ofNullable(change)
                .filter(c -> convertFromChoice.getSelectionModel()
                .getSelectedItem()
                .getCompilePattern()
                .matcher(c.getControlNewText()).matches())
                .get()
        ));

        weightValue.textProperty().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            if (t1.matches("-?\\d+(\\.\\d+)?")) {
                weight.setText(convertFromChoice.getSelectionModel().getSelectedItem().getFormulaResult(t1));
            }

            if (t1.isEmpty()) {
                resetResult();
            }
        });

    }

    private void resetResult() {
        weight.setText("0st 0lbs 0oz");
    }

    private void setInfoLabel(String value) {
        String info = new StringBuilder()
                .append("Weight Converter \n")
                .append("\n")
                .append(String.format("Enter the weight in %s \n", value))
                .append("\n")
                .toString();

        infoLabel.setText(info);
    }

    private enum Weight {

        KG("Kilograms") {
            @Override
            public Pattern getCompilePattern() {
                return Pattern.compile("-?((\\d{0,3})|(\\d+\\.\\d{0,2}))");
            }

            @Override
            public String getFormulaResult(String value) {
                return Formula.create().kilosToStones(Double.parseDouble(value));
            }

            @Override
            public String convertInput(String value) {
                return Formula.create().poundsToKilos(Double.parseDouble(value));
            }
        },
        LBS("Pounds") {
            @Override
            public Pattern getCompilePattern() {
                return Pattern.compile("-?(\\d{0,3})");
            }

            @Override
            public String getFormulaResult(String value) {
                return Formula.create().poundsToStones(Double.parseDouble(value));
            }

            @Override
            public String convertInput(String value) {
                return Formula.create().kilosToPounds(Double.parseDouble(value));
            }

        };

        private final String fullName;

        private Weight(String fullName) {
            this.fullName = fullName;
        }

        public String getFullName() {
            return fullName;
        }

        public abstract Pattern getCompilePattern();

        public abstract String getFormulaResult(String value);

        public abstract String convertInput(String value);
    }

}
