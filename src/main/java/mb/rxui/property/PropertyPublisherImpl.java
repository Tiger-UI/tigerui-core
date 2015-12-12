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

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Base implementation of a property publisher.
 *
 * @param <T>
 *            the type of value the property provides
 */
final class PropertyPublisherImpl<T> implements PropertyPublisher<T> {
    
    private final Supplier<T> propertySupplier;
    private final Dispatcher<T> dispatcher;
    
    public PropertyPublisherImpl(Supplier<T> propertySupplier, Dispatcher<T> dispatcher) {
        this.propertySupplier = requireNonNull(propertySupplier);
        this.dispatcher = requireNonNull(dispatcher);
    }
    
    
    /**
     * Adds an observer to this dispatcher.
     * 
     * @param observer
     *            some property subscriber.
     * @return TODO
     * @throws IllegalStateException if called when the dispatcher has already been disposed.
     */
    @Override
    public PropertySubscriber<T> subscribe(PropertyObserver<T> observer) {

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
