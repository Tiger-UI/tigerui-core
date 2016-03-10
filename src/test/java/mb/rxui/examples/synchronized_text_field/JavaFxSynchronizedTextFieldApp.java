/**
 * Copyright 2015 Mike Baum
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package mb.rxui.examples.synchronized_text_field;

import static mb.rxui.examples.synchronized_text_field.TextView.defaultJavaFxTextView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class JavaFxSynchronizedTextFieldApp extends Application {
    
    public JavaFxSynchronizedTextFieldApp() {}
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        TextComponent<Control> textComponent1 = new TextComponent<>(defaultJavaFxTextView(), "tacos");
        TextComponent<Control> textComponent2 = new TextComponent<>(defaultJavaFxTextView(), "");
        textComponent2.getModel().getTextProperty().synchronize(textComponent1.getModel().getTextProperty());
        
        initUI(textComponent1, textComponent2, primaryStage);
    }

    private static void initUI(TextComponent<Control> textComponent1, 
                               TextComponent<Control> textComponent2,
                               Stage primaryStage) {

        primaryStage.setTitle("Synchronized Text Field Test App");
        
        GridPane gridPane = new GridPane();
        gridPane.add(textComponent1.getView(), 1, 1);
        gridPane.add(textComponent2.getView(), 1, 2);
        Scene scene = new Scene(gridPane);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}