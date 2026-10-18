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
import com.fluffy.luffs.weight.converter.controllers.model.WeightTrend;
import com.fluffy.luffs.weight.converter.storage.Database;
import com.fluffy.luffs.weight.converter.storage.Migration;
import com.fluffy.luffs.weight.converter.i18n.I18n;
import com.gluonhq.attach.keyboard.KeyboardService;
import com.gluonhq.attach.keyboard.KeyboardType;
import com.gluonhq.attach.util.Platform;

import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** WeightConverterController controller containing visual logic. */
public class WeightConverterController extends StackPane {

  private static final Logger LOG = Logger.getLogger(WeightConverterController.class.getName());

  private final TextField weightValue = new TextField();
  private final Button saveWeight = new Button(I18n.text("action.save"));
  private final Label infoLabel = new Label();
  private final ListView<PastWeight> pastWeights = new ListView<>();
  private final WeightConverterToggleButton kgButton = new WeightConverterToggleButton(Weight.KG);
  private final WeightConverterToggleButton lbsButton = new WeightConverterToggleButton(Weight.LBS);
  private final WeightConverterToggleButton stones = new WeightConverterToggleButton(Weight.STONES);

  private final ImageView trendimageView = new ImageView();

  private final VBox vBox = new VBox();
  private final HBox toogles = new HBox();
  private final ToggleGroup toggleGroup = new ToggleGroup();

  public static WeightConverterController create() throws FileNotFoundException {

    return new WeightConverterController().initialize();
  }

  /** Initializes the controller class. */
  private WeightConverterController initialize() throws FileNotFoundException {

    setCSS();

    Label placeholderLabel = new Label(I18n.text("history.empty"));
    placeholderLabel.setWrapText(true);
    placeholderLabel.getStyleClass().add("placeholderLabel");
    placeholderLabel.applyCss();

    kgButton.setToggleGroup(toggleGroup);
    lbsButton.setToggleGroup(toggleGroup);
    stones.setToggleGroup(toggleGroup);
    stones.setDisable(true);

    toogles.getChildren().addAll(List.of(kgButton, lbsButton, stones));
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
    KeyboardService.create()
        .ifPresent(
            service -> service.setKeyboardTypeForNode(weightValue, KeyboardType.DECIMAL_PAD));
    weightValue
        .textProperty()
        .addListener(
            (ObservableValue<? extends String> ov, String t, String t1) ->
                Optional.ofNullable(t1)
                    .filter(this::isValidWeight)
                    .ifPresentOrElse(this::setWeightText, this::resetResult));
    resetResult();

    saveWeight.setOnMouseClicked(eh -> saveNewPastWeight());

    Database.Factory.create().createDatabse();
    new Migration().doMigration();
    refreshPastWeights();

    saveWeight.setMaxWidth(Double.MAX_VALUE);

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
    WeightTrend trend = WeightTrend.calculate(pastWeights.getItems());
    trendimageView.setAccessibleText(I18n.text(switch (trend) {
      case UP -> "trend.up";
      case DOWN -> "trend.down";
      case NONE -> "trend.none";
    }));
    return switch (trend) {
      case UP -> getImage("/images/up.png");
      case DOWN -> getImage("/images/down.png");
      case NONE -> null;
    };
  }

  private Image getImage(String imageLocation) {

    return new Image(
        WeightConverterController.class.getResource(imageLocation).toExternalForm(),
        100,
        100,
        false,
        true);
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
        .filter(this::isValidWeight)
        .ifPresent(value -> weightValue.setText(
            I18n.displayNumber(weight.convertInput(I18n.normalizeNumber(value)))));
  }

  private void transitionDeleteButton(Button deleteButton, Direction direction) {
    Timeline timeline = new Timeline();
    KeyValue kv =
        new KeyValue(
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
            (WeightListCell t) ->
                Optional.ofNullable(((HBox) t.getGraphic()))
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

    if (!isValidWeight(weightValue.getText())) {
      return;
    }

    LOG.log(Level.INFO, "Saving...");

    String input = I18n.normalizeNumber(weightValue.getText());
    Weight selected = getSelectedWeight();
    Database.Factory.create().setWeight(selected.getFormulaResult(input), selected.getKilos(input));
    refreshPastWeights();
  }

  private void refreshPastWeights() {
    pastWeights.setItems(getPastWeights());
    trendimageView.setImage(setTrend());
  }

  private static ObservableList<PastWeight> getPastWeights() {

    return Database.Factory.create()
        .getPastWeights()
        .sorted(Comparator.comparing(PastWeight::getDate).reversed());
  }

  private void removePastWeight(long id) {
    Database.Factory.create().deletePastWeight(id);
    refreshPastWeights();
  }

  private void setWeightText(String inputString) {
    saveWeight.setText(I18n.displayWeight(
        getSelectedWeight().getFormulaResult(I18n.normalizeNumber(inputString))));
    saveWeight.setDisable(false);
  }

  private boolean isValidWeight(String value) {
    return value != null
        && I18n.normalizeNumber(value).matches("-?\\d+(\\.\\d+)?")
        && Double.isFinite(Double.parseDouble(I18n.normalizeNumber(value)));
  }

  private Weight getSelectedWeight() {

    return Optional.of(toggleGroup)
        .map(ToggleGroup::getSelectedToggle)
        .filter(WeightConverterToggleButton.class::isInstance)
        .map(WeightConverterToggleButton.class::cast)
        .map(WeightConverterToggleButton::getWeight)
        .orElseThrow();
  }

  private void resetResult() {
    saveWeight.setText(I18n.text("action.save"));
    saveWeight.setDisable(true);
  }

  private void setInfoLabel(String value) {
    infoLabel.setText(I18n.text("input.prompt", value));
    infoLabel.setMaxWidth(Double.MAX_VALUE);
  }
}
