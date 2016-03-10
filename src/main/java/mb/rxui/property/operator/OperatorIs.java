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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mb.rxui.property.PropertyObserver;
import mb.rxui.property.PropertySubscriber;
import mb.rxui.property.publisher.PropertyPublisher;
import mb.rxui.subscription.Subscription;

/**
 * A {@link PropertyOperator} that transforms the property stream into a
 * boolean property stream by checking for equality between each emitted
 * value and the value provided in the constructor.
 *
 * @param <M> the type of values to compare for equality.
 */
public class OperatorIs<M> implements PropertyOperator<M, Boolean> {

    private final List<M> values;
    
    public OperatorIs(List<M> values) {
        this.values = new ArrayList<>(requireNonNull(values));
    }
    
    @SuppressWarnings("unchecked")
    public OperatorIs(M value, M... additionalValues) {
        this(createList(value, additionalValues));
    }

    @Override
    public PropertyPublisher<Boolean> apply(PropertyPublisher<M> source) {
        return new PropertyPublisher<Boolean>() {
            
            @Override
            public Boolean get() {
                return is(source.get());
            }

            private boolean is(M currentValue) {
                return values.contains(currentValue);
            }

            @Override
            public Subscription subscribe(PropertyObserver<Boolean> observer) {
                
                PropertySubscriber<Boolean> isSubscriber = new PropertySubscriber<>(observer);
                
                Subscription sourceSubscriber = 
                        source.subscribe(PropertyObserver.<M>create(value -> isSubscriber.onChanged(get()),
                                                                    isSubscriber::onDisposed));
                
                isSubscriber.doOnDispose(sourceSubscriber::dispose);
                
                return isSubscriber;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <M> List<M> createList(M value, M... additionalValues) {
        List<M> values = new ArrayList<>();
        values.add(value);
        values.addAll(Arrays.asList(additionalValues));
        return values;
    }
}
