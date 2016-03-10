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

import java.util.function.Predicate;

import mb.rxui.event.EventObserver;
import mb.rxui.event.EventSubscriber;

/**
 * Filters values from an event stream, such that only those values which
 * satisfy the provided predicate are emitted.
 * 
 * @param <T>
 *            the type of values filtered by this operator
 */
public final class OperatorFilter<T> implements Operator<T, T> {
    
    private final Predicate<T> predicate;
    
    public OperatorFilter(Predicate<T> predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public EventSubscriber<T> apply(EventSubscriber<T> childSubscriber) {

        EventObserver<T> sourceObserver = EventObserver.create(value -> {
            if (predicate.test(value))
                childSubscriber.onEvent(value);
        } , childSubscriber::onCompleted);

        return new EventSubscriber<>(sourceObserver);
    }
}
