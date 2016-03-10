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
package mb.rxui.property.publisher;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.function.Supplier;

import mb.rxui.dispatcher.PropertyDispatcher;
import mb.rxui.property.PropertyObserver;
import mb.rxui.property.PropertySubscriber;
import mb.rxui.subscription.Subscription;

/**
 * Default implementation of a property publisher.
 *
 * @param <T>
 *            the type of value the property provides
 */
final class PropertyPublisherImpl<T> implements PropertyPublisher<T> {
    
    private final Supplier<T> propertySupplier;
    private final PropertyDispatcher<T> dispatcher;
    
    /**
     * Creates a new property publisher.
     * 
     * @param propertySupplier
     *            some supplier that provides this publisher with it's values.
     * @param dispatcher
     *            some dispatcher that can be used to dispatch Property ->
     *            onChanged events. This is only used during subscription to
     *            send the first value to the subscriber.
     * @throws IllegalArgumentException
     *             if the provided supplier does not provide an initial value.
     */
    PropertyPublisherImpl(Supplier<T> propertySupplier, PropertyDispatcher<T> dispatcher) {
        this.propertySupplier = requireNonNull(propertySupplier);
        this.dispatcher = requireNonNull(dispatcher);
        Objects.requireNonNull(propertySupplier.get(), "A property publisher must be initialized with a value");
    }
    
    
    /**
     * Adds an observer to this dispatcher.
     * 
     * @param observer
     *            some property subscriber.
     * @return A {@link PropertySubscriber} that wraps the provided observer.
     *         The subscriber ensures that the contract of property is upheld,
     *         i.e. blocks reentrancy and prevents an exception from stopping
     *         other callbacks from executing.
     */
    @Override
    public Subscription subscribe(PropertyObserver<T> observer) {

        PropertySubscriber<T> subscriber = dispatcher.subscribe(observer);
        
        // push the latest value to the subscriber
        subscriber.onChanged(get());

        // dispose if this property is already disposed
        if (dispatcher.isDisposed())
            subscriber.onDisposed();
        
        return subscriber;
    }

    @Override
    public T get() {
        return propertySupplier.get();
    }
}
