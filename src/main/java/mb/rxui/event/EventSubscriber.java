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
import static mb.rxui.Callbacks.runSafeCallback;

import mb.rxui.Subscriber;
import mb.rxui.subscription.Subscription;

/**
 * A subscriber used when subscribing to an {@link EventStream}.
 * <p>
 * NOTES:
 * <li>Once completed, the subscriber will ignore future calls to
 * {@link #onEvent(Object)} or {@link #onCompleted()}.
 * <li>All callbacks are called safely, i.e. if a callback throws it will not
 * interrupt the other callbacks.
 * 
 * @param <E>
 *            the type of data that the {@link EventStream} which this
 *            subscriber is subscribed to emits.
 */
public class EventSubscriber<E> extends Subscriber implements EventObserver<E>, Subscription {
    
    private final EventObserver<E> observer;
    
    public EventSubscriber(EventObserver<E> observer) {
        this.observer = requireNonNull(observer);
    }
    
    public EventSubscriber(EventSubscriber<E> subscriber) {
        this.observer = subscriber;
        doOnDispose(subscriber::dispose);
    }
    
    @Override
    public void onEvent(E event) {
        if(isDisposed())
            return;
        
        runSafeCallback(() -> observer.onEvent(event));
    }

    @Override
    public void onCompleted() {
        if(isDisposed())
            return;
        
        runSafeCallback(observer::onCompleted);
        dispose();
    }

    @Override
    public boolean isBinding() {
        return observer.isBinding();
    }
}
