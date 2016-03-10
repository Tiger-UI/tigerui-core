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
package mb.rxui.event;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import mb.rxui.Observer;

/**
 * An observer that can be used to 'react' to events from an {@link EventStream}.
 * 
 * @param <E>
 *            the type of data the event stream emits.
 */
public interface EventObserver<E> extends Observer<E> {

    /**
     * Called whenever the stream this observer observes emits a new event
     * 
     * @param event
     *            some event, emitted by the stream this observer observes.
     */
    void onEvent(E event);
    
    /**
     * Called when the stream that this observer observes is completed.
     */
    void onCompleted();
    
    // Factory methods

    /**
     * Creates an event observer that only observes onChanged events.
     * 
     * @param eventHandler
     *            some consumer of onChanged events.
     * @return a new {@link EventObserver} that only observes onEvent
     *         events.
     */
    static <E> EventObserver<E> create(Consumer<E> eventHandler) {
        return create(eventHandler, () -> {});
    }

    /**
     * Creates an event observer that only observes onCompleted events.
     * 
     * @param onCompleted
     *            some runnable to run when the observed property is completed.
     * @return a new {@link EventObserver} that only observes onCompleted
     *         events.
     */
    static <E> EventObserver<E> create(Runnable onCompleted) {
        return create(newValue -> {}, onCompleted);
    }

    /**
     * Creates an event observer that observes, both onEvent and
     * onCompleted events.
     * 
     * @param eventHandler
     *            some consumer of onEvent events.
     * @param onCompleted
     *            some runnable to run when the observed property is completed.
     * @return a new {@link EventObserver}
     * @throws NullPointerException
     *             if either the eventHandler or onCompleted arguments are null.
     */
    static <E> EventObserver<E> create(Consumer<E> eventHandler, Runnable onCompleted) {
        requireNonNull(eventHandler);
        requireNonNull(onCompleted);

        return new EventObserver<E>() {
            @Override
            public void onEvent(E event) {
                eventHandler.accept(event);
            }

            @Override
            public void onCompleted() {
                onCompleted.run();
            }
        };
    }
}
