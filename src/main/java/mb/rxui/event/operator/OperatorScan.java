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
 * An operator that will scan a stream and add the previous generated value with all emitted events.
 * 
 * Starts off with a seed, so an event is emitted immediately upon subscribing.
 *
 * @param <E> the type of the events scanned
 * @param <R> the type of the generated values.
 */
public class OperatorScan<E, R> implements Operator<E, R> {
   
    private final BiFunction<E, R, R> scanFunction;
    private final R seed;
    
    public OperatorScan(BiFunction<E, R, R> scanFunction, R seed) {
        this.seed = seed;
        this.scanFunction = requireNonNull(scanFunction);
    }
    
    @Override
    public EventSubscriber<E> apply(EventSubscriber<R> childSubscriber) {
        // FIXME: this call escapes the re-entrancy protection since it does not go through the dispatcher.
        childSubscriber.onEvent(seed);
        
        AtomicReference<R> lastValue = new AtomicReference<>(seed);

        EventObserver<E> sourceObserver = 
                EventObserver.create(value -> { 
                                         lastValue.set(scanFunction.apply(value, lastValue.get()));
                                         childSubscriber.onEvent(lastValue.get());
                                     },
                                     childSubscriber::onCompleted);
        
        return new EventSubscriber<>(sourceObserver);
    }
}
