package com.fluffy.luffs.weight.converter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Weight Converter
 *
 * @author chrisluff
 */
public class WeightConverter extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        AnchorPane root = FXMLLoader.load(this.getClass().getResource("/fxml/Main.fxml"));
        Scene scene = new Scene(root);

        scene.getStylesheets()
                .add(this.getClass().getResource("/css/Style.css").toExternalForm());
        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
