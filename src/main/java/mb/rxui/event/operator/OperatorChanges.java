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
package mb.rxui.event.operator;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import mb.rxui.event.EventObserver;
import mb.rxui.event.EventSubscriber;

/**
 * An operator that will transform a stream by applying a function to the current value
 * and the last value emitted by the stream.
 * 
 * This operator will not emit a value until at least two events have been
 * emitted by the source stream after subscribing.
 *
 * @param <E>
 *            the type of the events scanned
 * @param <R>
 *            the type of the generated values.
 */
public class OperatorChanges<E, C> implements Operator<E, C> {
    
    private final BiFunction<E, E, C> changeEventFactory;
    
    public OperatorChanges(BiFunction<E, E, C> changeEventFactory) {
        this.changeEventFactory = requireNonNull(changeEventFactory);
    }

    @Override
    public EventSubscriber<E> apply(EventSubscriber<C> childSubscriber) {
        AtomicReference<E> lastValue = new AtomicReference<>();

        EventObserver<E> sourceObserver = 
                EventObserver.create(value -> emitValueOrSetLast(lastValue, value, childSubscriber),
                                     childSubscriber::onCompleted);
        
        return new EventSubscriber<>(sourceObserver);
    }
    
    private void emitValueOrSetLast(AtomicReference<E> lastValue, E currentValue, EventSubscriber<C> subscriber) {
        if(lastValue.get() != null) {            
            subscriber.onEvent(changeEventFactory.apply(lastValue.get(), currentValue));
        }
        lastValue.set(currentValue);
    }
}
