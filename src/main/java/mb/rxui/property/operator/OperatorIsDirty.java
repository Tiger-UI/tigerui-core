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

import java.util.concurrent.atomic.AtomicBoolean;

import mb.rxui.Subscription;
import mb.rxui.property.PropertyObserver;
import mb.rxui.property.PropertySubscriber;
import mb.rxui.property.publisher.PropertyPublisher;

public class OperatorIsDirty<M> implements PropertyOperator<M, Boolean> {

    private final M originalValue;
    
    public OperatorIsDirty(M originalValue) {
        this.originalValue = originalValue;
    }
    
    @Override
    public PropertyPublisher<Boolean> apply(PropertyPublisher<M> sourcePublisher) {
        return new PropertyPublisher<Boolean>() {
            @Override
            public Boolean get() {
                return isDirty(sourcePublisher.get());
            }

            private boolean isDirty(M currentValue) {
                return ! originalValue.equals(currentValue);
            }

            @Override
            public Subscription subscribe(PropertyObserver<Boolean> observer) {
                
                PropertySubscriber<Boolean> isDirtySubscriber = new PropertySubscriber<>(observer);
                
                Subscription sourceSubscriber = 
                        sourcePublisher.subscribe(PropertyObserver.create(value -> isDirtySubscriber.onChanged(get()),
                                                  isDirtySubscriber::onDisposed));
                
                isDirtySubscriber.doOnDispose(sourceSubscriber::dispose);
                
                return isDirtySubscriber;
            }
        };
    }
}
