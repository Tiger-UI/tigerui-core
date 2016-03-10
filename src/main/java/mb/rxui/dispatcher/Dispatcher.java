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
package mb.rxui.dispatcher;

import mb.rxui.Observer;
import mb.rxui.Subscriber;
import mb.rxui.disposables.Disposable;
import mb.rxui.property.PropertySubscriber;

/**
 * Used to dispatch events to susbcribers. Guarantees to always flag dispatching
 * whenever a value is being dispatched to a listener.
 *
 * @param <V>
 *            the type of values this dispatcher can dispatch
 */
public interface Dispatcher<V, S extends Subscriber, O extends Observer<V>> extends Disposable {
    
    public static enum Type { PROPERTY, EVENT }

    /**
     * Dispatches the new value.<br>
     * <br>
     * NOTES:<br>
     * 1. Before dispatching the isDispatching flag will be set to true 2. After
     * dispatching the isDispatching flag will be set to false
     * 
     * @param newValue
     * @throws IllegalStateException
     *             if called when the dispatcher is disposed.
     */
    void dispatch(V newValue);
    
    /**
     * Adds some disposable to dispose when this dispatcher is disposed.
     * 
     * @param toDispose
     *            some disposable to dispose when this dispatcher is disposed.
     */
    void onDisposed(Disposable toDispose);

    /**
     * @return true if this dispatcher is currently dispatching a value, false
     *         otherwise
     */
    boolean isDispatching();

    /**
     * @return true if this dispatcher is disposed, false otherwise
     */
    boolean isDisposed();

    /**
     * @return the number of subscribers to this dispatcher.
     */
    int getSubscriberCount();

    /**
     * Adds an observer to this dispatcher.
     * 
     * @param observer
     *            some property subscriber.
     * @return A {@link PropertySubscriber} created from the observer that will
     *         guarantee to toggle dispatching on this dispatcher whenever it
     *         handles the onChanged callback.
     */
    S subscribe(O observer);

    /**
     * @return gets the dispatcher type of this dispatcher. 
     */
    Type getType();

    /**
     * Creates a {@link Dispatcher} to be used to dispatch property events.
     * 
     * @return a new {@link Dispatcher} to be used to dispatch property events.
     */
    static <V> PropertyDispatcher<V> createPropertyDispatcher() {
        return Dispatchers.getInstance().createPropertyDispatcher();
    }
    
    static <E> EventDispatcher<E> createEventDispatcher() {
        return Dispatchers.getInstance().createEventDispatcher();
    }
}