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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import mb.rxui.EventLoop;
import mb.rxui.annotations.RequiresTest;
import mb.rxui.disposables.Disposable;
import mb.rxui.event.EventObserver;
import mb.rxui.event.EventSubscriber;

/**
 * An Operator that will throttles emissions from the lifted event stream, such
 * that an event will only be emitted after an amount of event silence.
 * 
 * @param <M>
 *            type of events this operator debounces.
 */
@RequiresTest
public class OperatorDebounce<M> implements Operator<M, M> {
    
    private final EventLoop eventLoop;
    private final long delay;
    private final TimeUnit timeUnit;
    
    private Optional<Disposable> currentDisposable = Optional.empty();

    public OperatorDebounce(EventLoop eventLoop, long delay, TimeUnit timeUnit) {
        this.eventLoop = eventLoop;
        this.delay = delay;
        this.timeUnit = timeUnit;
    }

    @Override
    public EventSubscriber<M> apply(EventSubscriber<M> childSubscriber) {
        
        EventObserver<M> sourceObserver = EventObserver.create(value -> {
            currentDisposable.ifPresent(Disposable::dispose);
            currentDisposable = Optional.of(eventLoop.schedule(() -> childSubscriber.onEvent(value), delay, timeUnit));
        } , () -> {
            currentDisposable.ifPresent(Disposable::dispose);
            childSubscriber.onCompleted();
        });
        
        return new EventSubscriber<>(sourceObserver);
    }
}
