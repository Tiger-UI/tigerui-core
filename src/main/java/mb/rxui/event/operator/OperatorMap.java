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

import java.util.function.Function;

import mb.rxui.event.EventObserver;
import mb.rxui.event.EventSubscriber;

/**
 * An {@link Operator} that transforms the emitted events of a stream by some
 * function.
 *
 * @param <I>
 *            the type of the parent stream
 * @param <R>
 *            the type of the child stream
 */
public class OperatorMap<I, R> implements Operator<I, R> {
    
    private final Function<I, R> mapper;

    public OperatorMap(Function<I, R> mapper) {
        this.mapper = requireNonNull(mapper);
    }

    @Override
    public EventSubscriber<I> apply(EventSubscriber<R> childSubscriber) {

        EventObserver<I> sourceObserver = 
                EventObserver.create(value -> childSubscriber.onEvent(mapper.apply(value)),
                                           childSubscriber::onCompleted);
        
        return new EventSubscriber<>(sourceObserver);
    }
}
