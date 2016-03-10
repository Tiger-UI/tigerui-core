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

import mb.rxui.property.PropertyStream;
import mb.rxui.property.PropertyObserver;
import mb.rxui.property.PropertySubscriber;
import mb.rxui.property.publisher.PropertyPublisher;
import mb.rxui.subscription.Subscription;

/**
 * An operator that limits the number of values dispatched to subscribers.<br>
 * <br>
 * NOTE: the get method of the new {@link PropertyStream} created via this
 * operator will always return the latest value of the property stream it
 * was derived from.
 * 
 * @param <M>
 *            the type of value the property provides
 */
public class OperatorTake<M> implements PropertyOperator<M, M> {

    private final int takeTotal;
    
    public OperatorTake(int takeTotal) {
        this.takeTotal = takeTotal;
    }
    
    @Override
    public PropertyPublisher<M> apply(PropertyPublisher<M> source) {
        
        return new PropertyPublisher<M>() {

            @Override
            public M get() {
                return source.get();
            }
            
            @Override
            public Subscription subscribe(PropertyObserver<M> observer) {
                PropertySubscriber<M> takesubscriber = new TakeSubscriber<>(observer, takeTotal);
                
                Subscription sourceSubscriber = 
                        source.subscribe(PropertyObserver.<M>create(takesubscriber::onChanged, takesubscriber::onDisposed));
                
                takesubscriber.doOnDispose(sourceSubscriber::dispose);
                
                return takesubscriber;
            }
        };
    }
    
    private static class TakeSubscriber<M> extends PropertySubscriber<M> {

        private final int takeTotal;
        private int takeCount = 0;
        
        public TakeSubscriber(PropertyObserver<M> observer, int takeCount) {
            super(observer);
            this.takeTotal = takeCount;
        }
        
        @Override
        public void onChanged(M newValue) {
            super.onChanged(newValue);
            takeCount++;
            
            if (takeCount == takeTotal)
                onDisposed();
        }
    }
}
