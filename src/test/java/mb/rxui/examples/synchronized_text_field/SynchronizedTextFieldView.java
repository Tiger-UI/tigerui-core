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

import static mb.rxui.examples.synchronized_text_field.TextView.defaultSwingTextView;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import mb.rxui.property.Property;

public final class SynchronizedTextFieldView {
    private final Property<String> textProperty;
    private final JComponent component;
    private final TextComponent<JComponent> textComponent1;
    private final TextComponent<JComponent> textComponent2;
    
    public SynchronizedTextFieldView(String initialTextValue) {
        textProperty = Property.create(initialTextValue);
        textComponent1 = buildTextComponent(textProperty);
        textComponent2 = buildTextComponent(textProperty);
        component = buildUI(textComponent1, textComponent2);
    }

    private static TextComponent<JComponent> buildTextComponent(Property<String> textProperty) {
        TextComponent<JComponent> textComponent = new TextComponent<>(defaultSwingTextView(), "");
        textComponent.getModel().getTextProperty().synchronize(textProperty);
        return textComponent;
    }
    
    private static JComponent buildUI(TextComponent<JComponent> textComponent1,
                                      TextComponent<JComponent> textComponent2) {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        
        panel.add(textComponent1.getView());
        panel.add(textComponent2.getView());
        
        return panel;
    }

    public Property<String> getTextProperty() {
        return textProperty;
    }
    
    public JComponent getComponent() {
        return component;
    }
}