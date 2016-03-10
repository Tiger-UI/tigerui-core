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
import mb.rxui.event.EventSubscriber;
import mb.rxui.event.operator.Operator;
import mb.rxui.subscription.Subscription;

/**
 * An {@link EventPublisher} that is used to transform (lift) the emitted event
 * of some stream by some operator.
 * 
 * @param <R>
 *            the type of the data emitted by the stream created by the lift
 *            operation (a.k.a. child stream).
 * @param <T>
 *            the type of the data emitted by the source stream that is to be
 *            lifted (a.k.a. parent stream).
 */
public class LiftEventPublisher<T, R> implements EventPublisher<R> {
    
    private final Operator<T, R> operator;
    private final EventPublisher<T> sourcePublisher;
    
    public LiftEventPublisher(Operator<T, R> operator, EventPublisher<T> sourcePublisher) {
        this.operator = requireNonNull(operator);
        this.sourcePublisher = requireNonNull(sourcePublisher);
    }
    
    @Override
    public Subscription subscribe(EventObserver<R> observer) {
        EventSubscriber<R> subscriber = new EventSubscriber<>(observer);
        
        Subscription subscription = sourcePublisher.subscribe(operator.apply(subscriber));
        
        subscriber.doOnDispose(subscription::dispose);
        
        return subscriber;
    }
}
