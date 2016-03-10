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

import mb.rxui.property.Property;

public class TextViewImpl<V> implements TextView<V> {
    private final Property<String> textProperty;
    private final V textComponent;
    
    TextViewImpl(V textComponent, Property<String> textProperty) {
        this.textProperty = textProperty;
        this.textComponent = textComponent;
    }
    
    @Override
    public V getView() {
        return textComponent;
    }
    
    @Override
    public Property<String> getTextProperty() {
        return textProperty;
    }
}