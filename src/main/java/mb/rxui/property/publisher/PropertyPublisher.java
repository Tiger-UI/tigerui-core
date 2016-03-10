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

import java.util.function.Supplier;

import mb.rxui.Publisher;
import mb.rxui.dispatcher.PropertyDispatcher;
import mb.rxui.property.PropertyObserver;
import mb.rxui.property.PropertySubscriber;
import mb.rxui.subscription.Subscription;

/**
 * A {@link PropertyPublisher} represents some source of property updates.
 * 
 * @param <T>
 *            the type of the value this publisher provides
 */
public interface PropertyPublisher<T> extends Publisher<T, PropertyObserver<T>>, Supplier<T> {
    /**
     * Subscribes to this property publisher.
     * 
     * @param observer
     *            some observer to subscribe to this property publisher
     * @return a {@link PropertySubscriber} that can be used to cancel the
     *         subscription
     */
    Subscription subscribe(PropertyObserver<T> observer);
    
    /**
     * Creates a default {@link PropertyPublisher}.
     * @param propertySupplier some supplier that provides property values.
     * @param dispatcher some dispatcher to dispatch property events.
     * @return a new {@link PropertyPublisher}
     */
    static <T> PropertyPublisher<T> create(Supplier<T> propertySupplier, PropertyDispatcher<T> dispatcher) {
        return new PropertyPublisherImpl<>(propertySupplier, dispatcher);
    }
}