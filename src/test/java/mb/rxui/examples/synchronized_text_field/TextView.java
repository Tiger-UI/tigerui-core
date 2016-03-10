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

import static mb.rxui.property.javafx.JavaFxProperties.fromFxProperty;
import static mb.rxui.property.swing.TextPropertySource.createTextProperty;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import mb.rxui.property.Property;

public interface TextView<V> {

    Property<String> getTextProperty();

    V getView();
    
    
    static TextView<JComponent> from(JTextComponent textComponent) {
        return new TextViewImpl<>(textComponent, createTextProperty(textComponent));
    }
    
    static TextView<JComponent> defaultSwingTextView() {
        return from(new JTextField());
    }
    
    static TextView<Control> from(TextInputControl textInputControl) {
        return new TextViewImpl<>(textInputControl, fromFxProperty(textInputControl.textProperty()));
    }
    
    static TextView<Control> defaultJavaFxTextView() {
        return from(new TextField());
    }
}