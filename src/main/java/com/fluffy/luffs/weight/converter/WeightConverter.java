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

package com.fluffy.luffs.weight.converter;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

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

        addEasterEggHandler(root);

        scene.getStylesheets()
                .add(this.getClass().getResource("/css/Style.css").toExternalForm());
        stage.setScene(scene);

        stage.show();
    }

    private void addEasterEggHandler(AnchorPane root) {
        GameApplication.embeddedLaunch(new GameApplication() {
            @Override
            protected void initSettings(GameSettings settings) {
                settings.setApplicationMode(ApplicationMode.RELEASE);
            }
        });

        VBox vbox = (VBox) root.getChildren().get(0);

        root.getScene().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode().equals(KeyCode.DIGIT4)) {

                FXGL.animationBuilder()
                        .duration(Duration.seconds(1))
                        .interpolator(Interpolators.BOUNCE.EASE_OUT())
                        .autoReverse(true)
                        .repeat(2)
                        .translate(FXGLMath.random(vbox.getChildren()).get())
                        .from(Point2D.ZERO)
                        .to(new Point2D(300, 0))
                        .buildAndPlay();
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }

}
