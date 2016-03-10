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
package mb.rxui.event.publisher;

import static java.util.Objects.requireNonNull;

import mb.rxui.event.EventObserver;
import mb.rxui.event.EventStream;
import mb.rxui.event.EventSubscriber;
import mb.rxui.subscription.CompositeSubscription;
import mb.rxui.subscription.Subscription;

/**
 * A Flatten Publisher can flatten a nested stream of stream into a stream.
 * 
 * @param <E>
 *            the type of events published by the stream of streams
 */
public class FlattenPublisher<E> implements EventPublisher<E> {
    
    private final EventStream<EventStream<E>> streamOfStreams;
    
    public FlattenPublisher(EventStream<EventStream<E>> streamOfStreams) {
        this.streamOfStreams = requireNonNull(streamOfStreams);
    }

    @Override
    public Subscription subscribe(EventObserver<E> observer) {

        EventSubscriber<E> flattenSubscriber = new EventSubscriber<>(observer);

        CompositeSubscription subscriptions = new CompositeSubscription();
        
        Subscription streamSub = 
                streamOfStreams.map(stream -> stream.onEvent(flattenSubscriber::onEvent))
                               .observe(EventObserver.create(subscriptions::add,
                                                             flattenSubscriber::onCompleted));

        subscriptions.add(streamSub);

        flattenSubscriber.doOnDispose(subscriptions::dispose);

        return flattenSubscriber;
    }
}
