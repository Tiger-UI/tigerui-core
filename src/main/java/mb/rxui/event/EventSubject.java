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

import mb.rxui.EventLoop;
import mb.rxui.dispatcher.Dispatcher;
import mb.rxui.dispatcher.EventDispatcher;
import mb.rxui.disposables.Disposable;
import mb.rxui.event.publisher.EventPublisher;
import mb.rxui.subscription.Subscription;

/**
 * An instance of an {@link EventStream} that is also an {@link EventSource} and can
 * publish events into the stream. This is roughly equivalent to a
 * {@link rx.subjects.PublishSubject}. An event subject can be used to publish
 * events into an event stream.
 * 
 * @param <E>
 *            the type of data this subject emits.
 */
public class EventSubject<E> extends EventStream<E> implements EventSource<E>, Disposable {

    private final EventSourcePublisher<E> publisher;
    private final EventLoop eventLoop;

    private EventSubject(EventSourcePublisher<E> publisher, EventLoop eventLoop) {
        super(publisher, eventLoop);
        this.publisher = requireNonNull(publisher);
        this.eventLoop = requireNonNull(eventLoop);
    }

    /**
     * Creates a new event subject.
     * 
     * @return An {@link EventSubject}
     */
    public static <E> EventSubject<E> create() {
        return new EventSubject<>(new EventSourcePublisher<>(), EventLoop.createEventLoop());
    }

    @Override
    public final void publish(E event) {
        eventLoop.checkInEventLoop();
        publisher.publish(event);
    }

    @Override
    public final void dispose() {
        eventLoop.checkInEventLoop();
        publisher.dispose();
    }
    
    /**
     * @return true if this event subject has observers, false otherwise.
     * @throws IllegalStateException
     *             if called from a thread other than the thread that this event
     *             subject was created on.
     */
    public final boolean hasObservers() {
        eventLoop.checkInEventLoop();
        return publisher.hasSubscribers();
    }

    private static class EventSourcePublisher<E> implements EventSource<E>, EventPublisher<E>, Disposable {
        
        private final EventDispatcher<E> dispatcher;

        public EventSourcePublisher() {
            dispatcher = Dispatcher.createEventDispatcher();
        }

        @Override
        public void publish(E event) {
            // stop publishing events when the dispatcher is destroyed
            if (dispatcher.isDisposed())
                return;
            
            // block reentrant events.
            if (dispatcher.isDispatching())
                return;
            
            dispatcher.dispatch(event);
        }

        @Override
        public Subscription subscribe(EventObserver<E> observer) {
            return dispatcher.subscribe(observer);
        }

        @Override
        public void dispose() {
            if (dispatcher.isDisposed())
                return;
            
            dispatcher.dispose();
        }
        
        private boolean hasSubscribers() {
            return dispatcher.getSubscriberCount() > 0;
        }
    }
}
