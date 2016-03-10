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
package mb.rxui.dispatcher;

import static java.util.Objects.requireNonNull;
import static mb.rxui.dispatcher.Dispatcher.Type.EVENT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import mb.rxui.event.EventBinding;
import mb.rxui.event.EventObserver;
import mb.rxui.event.EventStream;
import mb.rxui.event.EventSubscriber;

/**
 * An {@link EventDispatcher} is a {@link Dispatcher} suitable to be used with
 * an {@link EventStream}.
 * 
 * An Event Dispatcher will always dispatch to regular subscribers first, then
 * to {@link EventBinding}s. During event dispatch all properties are paused. So
 * each event dispatch sequence follows these steps:
 * <ol>
 * <li>All properties are paused via
 * {@link Dispatchers#pausePropertyDispatchers()}. This allows the property
 * value to be updated but stops the update propagation
 * <li>All subscribers to this event dispatcher are notified starting with
 * regular subscribers and ending with all bindings.
 * <li>All properties are resumed. At this point all properties that would be
 * affected by the event to be dispatched have been updated so glitches should
 * be prevented.
 * </ol>
 * <p>
 * 
 * @param <V>
 *            the type of events this dispatcher dispatches.
 */
public class EventDispatcher<V> extends AbstractDispatcher<V, EventSubscriber<V>, EventObserver<V>> {

    private final List<EventSubscriber<V>> subscribers;
    private static final Comparator<? super EventSubscriber<?>> SUBSCRIBER_COMPARATOR = createComparator();

    private EventDispatcher(List<EventSubscriber<V>> subscribers) {
        super(subscribers, subscriber -> subscriber::onEvent, subscriber -> subscriber::onCompleted, EVENT);
        this.subscribers = requireNonNull(subscribers);
    }
    
    static <E> EventDispatcher<E> create() {
        return new EventDispatcher<>(new ArrayList<>());
    }

    @Override
    public EventSubscriber<V> subscribe(EventObserver<V> observer) {
        
        EventSubscriber<V> subscriber = new EventSubscriber<>(wrapObserver(observer));
        
        if (observer instanceof EventSubscriber)
            subscriber.doOnDispose(((EventSubscriber<V>)observer)::dispose);
        
        if(isDisposed()) {
            subscriber.onCompleted();
            return subscriber;
        }
        
        subscriber.doOnDispose(() -> subscribers.remove(subscriber));
        subscribers.add(subscriber);
        subscribers.sort(SUBSCRIBER_COMPARATOR);
        
        return subscriber;
    }

    private EventObserver<V> wrapObserver(EventObserver<V> observer) {
        return new EventObserver<V>() {
            @Override
            public void onEvent(V event) {
                dispatchOrQueue(() -> {
                    setDispatchingToBinding(observer.isBinding());
                    observer.onEvent(event);
                    setDispatchingToBinding(false);
                });
            }

            @Override
            public void onCompleted() {
                dispatchOrQueue(observer::onCompleted);
            }

            @Override
            public boolean isBinding() {
                return observer.isBinding();
            }
        };
    }
    
    private static Comparator<? super EventSubscriber<?>> createComparator() {
        return (subscriber1, subscriber2) -> {
            if (subscriber1.isBinding() && !subscriber2.isBinding())
                return 1;
            
            if (!subscriber1.isBinding() && subscriber2.isBinding())
                return -1;
            
            return 0;
        };
    }
}
