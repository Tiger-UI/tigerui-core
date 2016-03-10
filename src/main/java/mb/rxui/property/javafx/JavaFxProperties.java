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

import static mb.rxui.EventLoop.JAVAFX_EVENT_LOOP;

import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import mb.rxui.property.Property;
import mb.rxui.property.PropertyStream;

public enum JavaFxProperties {
    ;  // no instances
    
    /**
     * Creates a Property from a JavaFx Property.
     * 
     * @param property
     *            some JavaFx property to create a {@link Property} for.
     * @return a new {@link Property} that is backed by a JavaFx Property.
     * @throws IllegalStateException
     *             if this method is called from a thread other that the JavaFx
     *             Platform Thread
     */
    public static <M> Property<M> fromFxProperty(javafx.beans.property.Property<M> property) {
        JAVAFX_EVENT_LOOP.checkInEventLoop();
        return Property.create(dispatcher -> new JavaFxPropertySource<>(property, dispatcher));
    }
    
    /**
     * Creates a property stream from an observable value.
     * 
     * @param observableValue
     *            some observable value to create a property stream for
     * @return a new {@link PropertyStream} that is backed by the provided
     *         observable value.
     * @throws IllegalStateException
     *             if the provided {@link ObservableValue} is actually an
     *             instance of a {@link WritableValue}, or if this method is
     *             called from a thread other that the JavaFx Platform Thread
     */
    public static <M> PropertyStream<M> fromObservableValue(ObservableValue<M> observableValue) {
        JAVAFX_EVENT_LOOP.checkInEventLoop();
        // TODO: need to figure out a way to prevent creating a PropertyStream from a WritableValue
//        checkArgument(! WritableValue.class.isAssignableFrom(observableValue.getClass()), 
//                     "Use [" + JavaFxPropertySource.class.getSimpleName() + "] instead, " + 
//                     "to ensure to be protected against re-entrancy." );
        return PropertyStream.create(new ObservableValuePropertyPublisher<>(observableValue));
    }
}
