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
package mb.rxui.property;

/**
 * A Property Binding is a special type of Property Observer that represents an
 * observer that binds one property to another. When dispatching events property
 * bindings are processed first, this guarantees that all properties have the up
 * to date value instantly at the same time before any other observers receive
 * updates. The reasoning behind this is that it should help to remove glitches
 * and redundant updates. See
 * <a href="http://stackoverflow.com/a/25141234">glich description</a>.
 *
 * @param <M>
 *            the type of data observed by this observer.
 */
public class PropertyBinding<M> implements PropertyObserver<M> {
    
    private final Property<M> boundProperty;
    
    public PropertyBinding(Property<M> boundProperty) {
        this.boundProperty = boundProperty;
    }

    @Override
    public void onChanged(M newValue) {
        boundProperty.setValue(newValue);
    }

    @Override
    public void onDisposed() {
        // nothing to do, we don't need to dispose the bound property.
    }

    @Override
    public boolean isBinding() {
        return true;
    }
}
