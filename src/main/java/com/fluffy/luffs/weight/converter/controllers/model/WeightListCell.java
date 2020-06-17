/*
MIT License

Copyright (c) 2020 Chris Luff

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
package com.fluffy.luffs.weight.converter.controllers.model;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class WeightListCell extends ListCell<PastWeight> {

    HBox listHBox = new HBox();
    Label weightLabel = new Label();
    Label dateLabel = new Label();
    Pane seperator = new Pane();
    Button deleteButton = new Button("DELETE");
    Double width;

    public WeightListCell(double width) {
        super();
        HBox.setHgrow(seperator, Priority.ALWAYS);
        listHBox.setSpacing(5);
        listHBox.getChildren().addAll(weightLabel, dateLabel, seperator, deleteButton);

        listHBox.getStyleClass().add("listHBox");
        weightLabel.getStyleClass().add("weightLabel");
        dateLabel.getStyleClass().add("dateLabel");
        deleteButton.getStyleClass().add("deleteButton");
        
        this.width = width;
        
    }

    @Override
    protected void updateItem(PastWeight pw, boolean empty) {
        super.updateItem(pw, empty);
        setText(null);
        if (empty) {
            setGraphic(null);
        } else {
            weightLabel.setText(String.format("%s", pw.getWeight()));
            dateLabel.setText(String.format("%s %s", pw.getPastWeightWeekDay(), pw.getPastWeightFormattedDate()));
            setGraphic(listHBox);
            deleteButton.translateXProperty().set(width);
        }
    }
}
