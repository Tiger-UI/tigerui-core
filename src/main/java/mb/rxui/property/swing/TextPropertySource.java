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
package mb.rxui.property.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import mb.rxui.dispatcher.PropertyDispatcher;
import mb.rxui.disposables.Disposable;
import mb.rxui.property.Property;

/**
 * A Swing text property source.
 */
public class TextPropertySource extends SwingPropertySource<String, DocumentListener, Document> {

    private TextPropertySource(JTextComponent textComponent, PropertyDispatcher<String> dispatcher) {
        super(textComponent::getText, textComponent::setText, textComponent.getDocument(), dispatcher);
    }
    
    public static Property<String> createTextProperty(JTextComponent textComponent) {
        return Property.create(dispatcher -> new TextPropertySource(textComponent, dispatcher));
    }

    @Override
    protected Disposable addListener(Document document, DocumentListener listener) {
        document.addDocumentListener(listener);
        return () -> document.removeDocumentListener(listener);
    }
    
    @Override
    protected DocumentListener createListener(PropertyDispatcher<String> dispatcher) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                dispatcher.dispatch(get());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                dispatcher.dispatch(get());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // nothing to do. Change updates relate to text attributes.
            }
        };
    }
}
