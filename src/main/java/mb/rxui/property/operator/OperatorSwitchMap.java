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

import java.util.function.Function;

import mb.rxui.property.PropertyObserver;
import mb.rxui.property.PropertyStream;
import mb.rxui.property.PropertySubscriber;
import mb.rxui.property.publisher.PropertyPublisher;
import mb.rxui.subscription.RollingSubscription;
import mb.rxui.subscription.Subscription;

/**
 * An operator that will produce a new property stream that uses the provided
 * function to switch the bound source stream.
 *
 * @param <M>
 *            the type of the parent stream
 * @param <R>
 *            the type of the switched child stream
 */
public class OperatorSwitchMap<M, R> implements PropertyOperator<M, R> {
    
    private final Function<M, PropertyStream<R>> switchMapFunction;
    
    public OperatorSwitchMap(Function<M, PropertyStream<R>> switchMapFunction) {
        this.switchMapFunction = requireNonNull(switchMapFunction);
    }

    @Override
    public PropertyPublisher<R> apply(PropertyPublisher<M> sourcePublisher) {
        
        return new PropertyPublisher<R>() {
            
            private PropertyStream<R> currentStream = switchMapFunction.apply(sourcePublisher.get());

            @Override
            public R get() {
                return currentStream.get();
            }

            @Override
            public Subscription subscribe(PropertyObserver<R> childObserver) {
                RollingSubscription sourceSubscription = new RollingSubscription();
                
                PropertySubscriber<R> childSubscriber = new PropertySubscriber<>(childObserver);
                
                PropertyObserver<M> sourceObserver = PropertyObserver.create(value -> {
                    currentStream = switchMapFunction.apply(value);
                    sourceSubscription.set(switchMapFunction.apply(value).onChanged(childSubscriber::onChanged));
                } , childSubscriber::onDisposed);
                
                Subscription subscription = sourcePublisher.subscribe(sourceObserver);
                
                childSubscriber.doOnDispose(sourceSubscription::dispose);
                childSubscriber.doOnDispose(subscription::dispose);

                return childSubscriber;
            }
        };
    }
}
