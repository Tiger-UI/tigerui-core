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
package mb.rxui.event;

import static java.util.Objects.requireNonNull;

import mb.rxui.property.Property;

/**
 * An Event Binding is a special type of Event Observer that represents an
 * observer that binds an event stream to a property. When dispatching events
 * event bindings are processed last, this guarantees that all stream observers
 * are notified and up to date before processing any [event stream --> property]
 * event propagation. 
 * The reasoning behind this is that it should help to remove glitches and
 * redundant updates. See <a href="http://stackoverflow.com/a/25141234">glich
 * description</a>.
 *
 * @param <E>
 *            the type of data observed by this observer.
 */
public class EventBinding<E> implements EventObserver<E> {

    private final Property<E> boundProperty;

    public EventBinding(Property<E> boundProperty) {
        this.boundProperty = requireNonNull(boundProperty);
    }
    
    @Override
    public void onEvent(E event) {
        boundProperty.setValue(event);
    }

    @Override
    public void onCompleted() {
        // nothing to do, we don't need to dispose a property when a source stream closes
    }

    @Override
    public boolean isBinding() {
        return true;
    }
}
