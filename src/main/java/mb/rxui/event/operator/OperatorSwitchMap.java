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
import mb.rxui.event.EventStream;
import mb.rxui.event.EventSubscriber;
import mb.rxui.subscription.RollingSubscription;

/**
 * {@link OperatorSwitchMap} transforms a stream by switching between streams to
 * emit based on the switch function. <br>
 * <br>
 * In other words the switch function will produce a new stream for any value of
 * the source stream. This new stream will be connected to the downstream
 * subscriber. This is really like a switch for streams.
 * 
 * 
 * @param <E>
 *            the type of the events emitted by the source stream
 * @param <R>
 *            the type of the stream created by the switch function
 */
public class OperatorSwitchMap<E, R> implements Operator<E, R> {
    
    private final Function<E, EventStream<R>> switchFunction;
    
    public OperatorSwitchMap(Function<E, EventStream<R>> switchFunction) {
        this.switchFunction = requireNonNull(switchFunction);
    }
    
    @Override
    public EventSubscriber<E> apply(EventSubscriber<R> childSubscriber) {

        RollingSubscription streamSubscription = new RollingSubscription();
        
        EventObserver<E> sourceObserver = 
                EventObserver.create(event -> streamSubscription.set(switchFunction.apply(event).onEvent(childSubscriber::onEvent)),
                                     childSubscriber::onCompleted);
        
        childSubscriber.doOnDispose(streamSubscription::dispose);
        
        return new EventSubscriber<>(sourceObserver);
    }
}
