/*
MIT License

Copyright (c) 2022  Fluffy Luffs

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.fluffy.luffs.weight.converter.controllers;

import com.fluffy.luffs.weight.converter.controllers.model.Direction;
import com.fluffy.luffs.weight.converter.controllers.model.PastWeight;
import com.fluffy.luffs.weight.converter.controllers.model.Weight;
import com.fluffy.luffs.weight.converter.controllers.model.WeightConverterException;
import com.fluffy.luffs.weight.converter.controllers.model.WeightConverterToggleButton;
import com.fluffy.luffs.weight.converter.controllers.model.WeightListCell;
import com.fluffy.luffs.weight.converter.storage.Database;
import com.fluffy.luffs.weight.converter.storage.Migration;
import com.gluonhq.attach.settings.SettingsService;
import com.gluonhq.attach.util.Platform;

import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * WeightConverterController controller containing visual logic.
 */
public class WeightConverterController extends StackPane {

    private final TextField weightValue = new TextField();
    private final Button saveWeight = new Button("0st 0lbs 0oz");
    private final Label infoLabel = new Label();
    private final ListView<PastWeight> pastWeights = new ListView<>();
    private final WeightConverterToggleButton kgButton = new WeightConverterToggleButton(Weight.KG);
    private final WeightConverterToggleButton lbsButton = new WeightConverterToggleButton(Weight.LBS);
    private final ImageView trendimageView = new ImageView();

    private final VBox vBox = new VBox();
    private final HBox toogles = new HBox();
    private final ToggleGroup toggleGroup = new ToggleGroup();

    public static WeightConverterController create() throws FileNotFoundException {

        return new WeightConverterController().initialize();
    }

    /**
     * Initializes the controller class.
     */
    private WeightConverterController initialize() throws FileNotFoundException {

        setCSS();

        String placeholderTextString
                = """
        Choose the weight to convert from Kilograms or Pounds

        Enter a weight in numbers only

        Touch the conversion to save in Stones Pounds and ounces

        Swipe left on a saved weight to delete

        Unlimited entries
        """;

        Label placeholderLabel = new Label(placeholderTextString);
        placeholderLabel.setWrapText(true);
        placeholderLabel.getStyleClass().add("placeholderLabel");
        placeholderLabel.applyCss();

        kgButton.setToggleGroup(toggleGroup);
        lbsButton.setToggleGroup(toggleGroup);

        toogles.getChildren().addAll(List.of(kgButton, lbsButton));
        toggleGroup.selectToggle(kgButton);

        setInfoLabel(getSelectedWeight().getFullName());

        pastWeights.setPlaceholder(placeholderLabel);
        pastWeights.setCellFactory(
                lv -> {
                    WeightListCell cell = new WeightListCell(pastWeights.getWidth());

                    if (Platform.isDesktop()) {
                        cell.setOnMouseClicked(eh -> showDeleteButton(cell));
                    }

                    cell.setOnSwipeLeft(eh -> transitionIn(cell));
                    cell.setOnSwipeRight(eh -> transitionOut(cell));
                    return cell;
                });

        VBox.setVgrow(pastWeights, Priority.ALWAYS);

        kgButton.setOnAction(eh -> setWeightValue(kgButton.getWeight()));
        lbsButton.setOnAction(eh -> setWeightValue(lbsButton.getWeight()));

        weightValue.positionCaret(0);
        weightValue.setTextFormatter(setTextFormatter());
        weightValue
                .textProperty()
                .addListener(
                        (ObservableValue<? extends String> ov, String t, String t1)
                        -> Optional.ofNullable(t1)
                                .filter(str -> str.matches("-?\\d+(\\.\\d+)?"))
                                .ifPresentOrElse(this::setWeightText, this::resetResult));

        saveWeight.setOnMouseClicked(eh -> saveNewPastWeight());

        new Database().createDatabse();
        SettingsService.create()
                .map(service -> service.retrieve("migration"))
                .ifPresentOrElse(
                        migration -> pastWeights.setItems(getPastWeights()),
                        () -> {
                            new Migration().doMigration();
                            pastWeights.setItems(getPastWeights());
                        });

        saveWeight.setMaxWidth(Double.MAX_VALUE);

        trendimageView.setImage(setTrend());
        trendimageView.setOpacity(.20);
        trendimageView.fitWidthProperty().bind(pastWeights.widthProperty().multiply(.80));
        trendimageView.fitHeightProperty().bind(pastWeights.heightProperty().multiply(.80));
        trendimageView.setMouseTransparent(true);

        StackPane stackPane = new StackPane(pastWeights, trendimageView);

        setVBox(toogles, new Insets(50, 50, 0, 50));
        setVBox(weightValue, new Insets(20, 75, 0, 75));
        setVBox(saveWeight, new Insets(5, 20, 10, 20));
        setVBox(infoLabel, new Insets(5, 0, 0, 0));
        setVBox(pastWeights, new Insets(0, 0, 5, 0));

        vBox.getChildren().addAll(List.of(toogles, weightValue, infoLabel, saveWeight, stackPane));

        getChildren().add(vBox);

        return this;

    }

    private Image setTrend() {
        if (pastWeights.getItems().isEmpty()) {
            return null;
        }

        return isIncreasing()
                ? getImage("/images/up.png")
                : getImage("/images/down.png");
    }

    private Image getImage(String imageLocation) {

        return new Image(getClass().getResource(imageLocation).toExternalForm(), 100, 100, false, true);
    }

    private void setVBox(Node node, Insets insets) {
        VBox.setVgrow(node, Priority.NEVER);
        VBox.setMargin(node, insets);
    }

    private void setCSS() {
        getStyleClass().add("stackPane");
        weightValue.getStyleClass().add("text-field");
        saveWeight.getStyleClass().add("saveWeight");
        kgButton.getStyleClass().add("weightConverterToggleButton");
        lbsButton.getStyleClass().add("weightConverterToggleButton");
        toogles.getStyleClass().add("toggles-hbox");
        infoLabel.getStyleClass().add("infoLabel");
    }

    private void setWeightValue(Weight weight) {
        setInfoLabel(weight.getFullName());
        Optional.ofNullable(weightValue.getText())
                .filter(v -> !v.isBlank())
                .ifPresent(value -> weightValue.setText(weight.convertInput(value)));
    }

    private void transitionDeleteButton(Button deleteButton, Direction direction) {
        Timeline timeline = new Timeline();
        KeyValue kv
                = new KeyValue(
                        deleteButton.translateXProperty(),
                        direction.equals(Direction.IN) ? 0 : pastWeights.getWidth(),
                        Interpolator.EASE_OUT);
        KeyFrame kf = new KeyFrame(Duration.millis(200), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    private void showDeleteButton(WeightListCell cell) {
        if (getDeleteButton(cell).translateXProperty().get() == pastWeights.getWidth()) {
            transitionDeleteButton(getDeleteButton(cell), Direction.IN);
            getDeleteButton(cell).setOnAction(eh -> removePastWeight(cell.getItem().getId()));
        } else {
            transitionDeleteButton(getDeleteButton(cell), Direction.OUT);
        }
    }

    private Button getDeleteButton(WeightListCell cell) {

        return Optional.ofNullable(cell)
                .map(
                        (WeightListCell t)
                        -> Optional.ofNullable(((HBox) t.getGraphic()))
                                .filter(c -> c.getChildren().size() == 4)
                                .map(l -> (Button) l.getChildren().get(3))
                                .orElseThrow(
                                        () -> new WeightConverterException("Could not create delete control")))
                .orElseThrow();
    }

    private void transitionIn(WeightListCell cell) {
        transitionDeleteButton(getDeleteButton(cell), Direction.IN);
        getDeleteButton(cell).setOnAction(eh -> removePastWeight(cell.getItem().getId()));
    }

    private void transitionOut(WeightListCell cell) {
        transitionDeleteButton(getDeleteButton(cell), Direction.OUT);
    }

    private void saveNewPastWeight() {

        Optional.of(saveWeight.getText())
                .filter(str -> !str.equals("0st 0lbs 0oz"))
                .ifPresent(
                        weight -> {
                            new Database().setWeight(weight, Double.parseDouble(weightValue.getText()));
                            pastWeights.setItems(getPastWeights());
                            trendimageView.setImage(setTrend());
                        });
    }

    private boolean isIncreasing() {
        List<Double> weights = pastWeights.getItems().stream().map(PastWeight::getKilos).collect(Collectors.toList());
        Double previousAverage = IntStream.range(1, Optional.of(weights.size()).orElse(1)).limit(5)
                .mapToObj(weights::get).collect(Collectors.averagingDouble(Double::doubleValue));
        Double average = weights.stream().limit(5).collect(Collectors.averagingDouble(Double::doubleValue));

//        System.out.println("Previous average: " + previousAverage + " Current average " + average);

        return previousAverage <= average;
    }

    private static ObservableList<PastWeight> getPastWeights() {

        return new Database()
                .getPastWeights()
                .sorted(Comparator.comparing(PastWeight::getDate).reversed());
    }

    private void removePastWeight(long id) {
        new Database().deletePastWeight(id);
        pastWeights.setItems(getPastWeights());
    }

    private void setWeightText(String inputString) {
        saveWeight.setText(getSelectedWeight().getFormulaResult(inputString));
    }

    private TextFormatter<String> setTextFormatter() {
        return new TextFormatter<>(
                change
                -> Optional.ofNullable(change)
                        .filter(
                                c
                                -> getSelectedWeight()
                                        .getCompilePattern()
                                        .matcher(c.getControlNewText())
                                        .matches())
                        .get());
    }

    private Weight getSelectedWeight() {

        return Optional.of(toggleGroup).map(ToggleGroup::getSelectedToggle).filter(WeightConverterToggleButton.class::isInstance)
                .map(WeightConverterToggleButton.class::cast)
                .map(WeightConverterToggleButton::getWeight)
                .orElseThrow();

    }

    private void resetResult() {
        saveWeight.setText("0st 0lbs 0oz");
    }

    private void setInfoLabel(String value) {
        infoLabel.setText(String.format("Enter the weight in %s %n", value));
        infoLabel.setMaxWidth(Double.MAX_VALUE);
    }
}
