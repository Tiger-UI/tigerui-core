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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import mb.rxui.Counter;
import mb.rxui.event.EventObserver;
import mb.rxui.event.EventStream;
import mb.rxui.event.EventSubscriber;
import mb.rxui.subscription.CompositeSubscription;
import mb.rxui.subscription.Subscription;

/**
 * A Publisher that merges many event streams into one stream.
 * 
 * @param <R>
 *            the type of the events published by the source streams.
 */
public class MergeEventPublisher<R> implements EventPublisher<R> {
    
    private final Collection<EventStream<R>> streams;
    
    public MergeEventPublisher(Collection<EventStream<R>> streams) {
        this.streams = requireNonNull(streams);
    }

    @Override
    public Subscription subscribe(EventObserver<R> observer) {
        
        Counter counter = new Counter();
        EventSubscriber<R> mergeSubscriber = new EventSubscriber<>(observer);
        
        List<Subscription> subscriptions = 
                streams.stream()
                       .map(stream -> stream.observe(createObserver(counter, mergeSubscriber)))
                       .collect(Collectors.toList());
        
        CompositeSubscription subscription = new CompositeSubscription(subscriptions);
        
        mergeSubscriber.doOnDispose(subscription::dispose);
        
        return mergeSubscriber;
    }

    private EventObserver<R> createObserver(Counter counter, EventSubscriber<R> mergeSubscriber) {
        return EventObserver.create(mergeSubscriber::onEvent, () -> {
            if (counter.incrementAndGet() == streams.size()) {
                mergeSubscriber.onCompleted();
            }
        });
    }
}
