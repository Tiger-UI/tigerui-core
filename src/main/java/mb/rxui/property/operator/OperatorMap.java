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

import java.util.function.Function;

import mb.rxui.property.PropertyObserver;
import mb.rxui.property.PropertySubscriber;
import mb.rxui.property.publisher.PropertyPublisher;
import mb.rxui.subscription.Subscription;

public class OperatorMap<S, R> implements PropertyOperator<S, R>{

    private final Function<S, R> mapper;
    
    public OperatorMap(Function<S, R> mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public PropertyPublisher<R> apply(PropertyPublisher<S> source) {
        
        return new PropertyPublisher<R>() {
            @Override
            public R get() {
                return mapper.apply(source.get());
            }

            @Override
            public Subscription subscribe(PropertyObserver<R> observer) {
                
                PropertySubscriber<R> subscriber = new PropertySubscriber<>(observer);
                
                Subscription sourceSubscriber = 
                        source.subscribe(PropertyObserver.create(val -> subscriber.onChanged(get()),
                                                                 subscriber::onDisposed));
                
                subscriber.doOnDispose(sourceSubscriber::dispose);
                
                return subscriber;
            }
        };
    }
}
