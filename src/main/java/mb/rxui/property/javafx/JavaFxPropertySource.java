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
package mb.rxui.property.javafx;

import static java.util.Objects.requireNonNull;
import static mb.rxui.EventLoop.JAVAFX_EVENT_LOOP;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import mb.rxui.dispatcher.Dispatcher;
import mb.rxui.dispatcher.PropertyDispatcher;
import mb.rxui.disposables.Disposable;
import mb.rxui.property.PropertySource;

/**
 * A property source for JavaFx properties
 * 
 * @param <M>
 *            the type of data the property represents.
 */
public class JavaFxPropertySource<M> implements PropertySource<M> {
    
    private final Property<M> fxProperty;
    
    /**
     * Creates a JavaFx property source
     * @param fxProperty some JavaFx property to back this property
     * @param dispatcher a {@link Dispatcher} that will be used to dispatch the values
     * @throws IllegalStateException if called outside of the Platform thread.
     */
    JavaFxPropertySource(Property<M> fxProperty, PropertyDispatcher<M> dispatcher) {
        JAVAFX_EVENT_LOOP.checkInEventLoop();
        this.fxProperty = requireNonNull(fxProperty);
        dispatcher.onDisposed(addPropertyListener(dispatcher, fxProperty));
    }
    
    @Override
    public void setValue(M newValue) {
        fxProperty.setValue(newValue);
    }

    @Override
    public M get() {
        return fxProperty.getValue();
    }
    
    private static <M> Disposable addPropertyListener(PropertyDispatcher<M> dispatcher, Property<M> property) {
        ChangeListener<M> listener = (observable, oldValue, newValue) -> dispatcher.dispatch(newValue);
        property.addListener(listener);
        return () -> property.removeListener(listener);
    }
}
