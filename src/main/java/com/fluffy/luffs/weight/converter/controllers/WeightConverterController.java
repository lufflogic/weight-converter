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
import com.fluffy.luffs.weight.converter.controllers.model.WeightListCell;
import com.fluffy.luffs.weight.converter.storage.Database;
import com.gluonhq.attach.statusbar.StatusBarService;
import com.gluonhq.attach.storereview.StoreReviewService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.attach.util.Services;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/** WeightConverterController controller containing visual logic. */
public class WeightConverterController {

  @FXML private TextField weightValue;
  @FXML private Button saveWeight;
  @FXML private ChoiceBox<Weight> convertFromChoice;
  @FXML private Label infoLabel;
  @FXML private ListView<PastWeight> pastWeights;

  /**
   * Initializes the controller class.
   *
   * @throws java.io.FileNotFoundException
   */
  public void initialize() throws FileNotFoundException {

    Services.get(StatusBarService.class).ifPresent((StatusBarService t) -> t.setColor(Color.GRAY));
    Database.createDatabse();

    String placeholderTextString =
        """
        Choose the weight to convert to Kilograms or Pounds using the drop down menu

        Enter a weight in numbers only

        Touch the result to save in Stones Pounds and ounces

        Swipe left to remove a saved weight
        """;
    Label placeholderLabel = new Label(placeholderTextString);
    placeholderLabel.setWrapText(true);
    placeholderLabel.getStyleClass().add("placeholderLabel");
    placeholderLabel.applyCss();

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

    pastWeights.setItems(getPastWeights());

    convertFromChoice.getItems().addAll(Weight.values());
    convertFromChoice
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (ov, t, t1) -> {
              setInfoLabel(t1.getFullName());
              Optional.ofNullable(weightValue.getText())
                  .filter(v -> !v.isBlank())
                  .ifPresent(value -> weightValue.setText(t1.convertInput(value)));
            });
    convertFromChoice.getSelectionModel().select(0);

    weightValue.positionCaret(0);
    weightValue.setTextFormatter(setTextFormatter());
    weightValue
        .textProperty()
        .addListener(
            (ObservableValue<? extends String> ov, String t, String t1) ->
                Optional.ofNullable(t1)
                    .filter(str -> str.matches("-?\\d+(\\.\\d+)?"))
                    .ifPresentOrElse(this::setWeightText, this::resetResult));

    saveWeight.setOnMouseClicked(eh -> saveNewPastWeight());
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
    Optional.of(saveWeight.getText())
        .filter(str -> !str.equals("0st 0lbs 0oz"))
        .ifPresent(
            weight -> {
              Database.setWeight(weight);
              pastWeights.setItems(getPastWeights());
            });
  }

  public static ObservableList<PastWeight> getPastWeights() {
    return Database.getPastWeights().stream()
        .collect(Collectors.toCollection(FXCollections::observableArrayList))
        .sorted(Comparator.comparing(PastWeight::getDate).reversed());
  }

  private void removePastWeight(long id) {
    Database.deletePastWeight(id);
    pastWeights.setItems(getPastWeights());
  }

  private void setWeightText(String inputString) {
    saveWeight.setText(
        convertFromChoice.getSelectionModel().getSelectedItem().getFormulaResult(inputString));
  }

  private TextFormatter<String> setTextFormatter() {
    return new TextFormatter<>(
        change ->
            Optional.ofNullable(change)
                .filter(
                    c ->
                        convertFromChoice
                            .getSelectionModel()
                            .getSelectedItem()
                            .getCompilePattern()
                            .matcher(c.getControlNewText())
                            .matches())
                .get());
  }

  private void resetResult() {
    saveWeight.setText("0st 0lbs 0oz");
  }

  private void setInfoLabel(String value) {
    infoLabel.setText(String.format("Enter the weight in %s %n", value));
  }
  
}
