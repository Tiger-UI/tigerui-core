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
package mb.rxui.property.operator;

import java.util.Optional;
import java.util.function.Predicate;

import mb.rxui.property.PropertyObserver;
import mb.rxui.property.PropertySubscriber;
import mb.rxui.property.publisher.PropertyPublisher;
import mb.rxui.subscription.Subscription;

/**
 * Filters values based on some predicate. Emits an Optional empty if the
 * current value of the property does not satisfy the predicate.<br>
 * <br>
 * NOTE: This differs to {@link OperatorFilter} since it will always emit a
 * value when the current value of the property changes.
 * 
 * @param <M>
 *            the type of the value for the property to filter.
 */
public class OperatorFilterToOptional<M> implements PropertyOperator<M, Optional<M>> {
    private final Predicate<M> predicate;
    
    /**
     * @param predicate some predicate to filter the property by.
     */
    public OperatorFilterToOptional(Predicate<M> predicate) {
        this.predicate = predicate;
    }
    
    @Override
    public PropertyPublisher<Optional<M>> apply(PropertyPublisher<M> source) {
        
        return new PropertyPublisher<Optional<M>>() {
            
            @Override
            public Optional<M> get() {
                return filteredValue(source.get());
            }

            private Optional<M> filteredValue(M currentValue) {
                return Optional.of(currentValue).filter(predicate);
            }

            @Override
            public Subscription subscribe(PropertyObserver<Optional<M>> observer) {
                
                PropertySubscriber<Optional<M>> subscriber = new PropertySubscriber<>(observer);
                
                Subscription sourceSubscriber = 
                        source.subscribe(PropertyObserver.<M>create(value -> subscriber.onChanged(get()),
                                                                    subscriber::onDisposed));
                
                subscriber.doOnDispose(sourceSubscriber::dispose);
                
                return subscriber;
            }
        };
    }
}
