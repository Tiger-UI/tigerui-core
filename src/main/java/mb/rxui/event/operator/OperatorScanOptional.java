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

import java.util.Optional;
import java.util.function.BiFunction;

import mb.rxui.event.EventObserver;
import mb.rxui.event.EventSubscriber;

/**
 * An operator that will scan a stream and add the previous generated value with all emitted events.
 * 
 * Starts off with an optional empty, so that no event is emitted until the source stream emits an event
 *
 * @param <E> the type of the events scanned
 * @param <R> the type of the generated values.
 */
public class OperatorScanOptional<E, R> implements Operator<E, R> {
    
    private final BiFunction<E, Optional<R>, R> scanFunction;
    private Optional<R> lastValue;
    
    public OperatorScanOptional(BiFunction<E, Optional<R>, R> scanFunction) {
        this.scanFunction = requireNonNull(scanFunction);
        this.lastValue = Optional.empty();
    }
    
    @Override
    public EventSubscriber<E> apply(EventSubscriber<R> childSubscriber) {
        EventObserver<E> sourceObserver = 
                EventObserver.create(value -> { 
                                         lastValue = Optional.of(scanFunction.apply(value, lastValue));
                                         childSubscriber.onEvent(lastValue.get());
                                     },
                                     childSubscriber::onCompleted);
        
        return new EventSubscriber<>(sourceObserver);
    }
}
