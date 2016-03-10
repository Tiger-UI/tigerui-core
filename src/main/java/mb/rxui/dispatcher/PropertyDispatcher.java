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

import static mb.rxui.dispatcher.Dispatcher.Type.PROPERTY;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import mb.rxui.property.PropertyObserver;
import mb.rxui.property.PropertySubscriber;

/**
 * A dispatcher for property change events.
 * 
 * @param <M> the type of value this dispatcher dispatches.
 */
public class PropertyDispatcher<M> extends AbstractDispatcher<M, PropertySubscriber<M>, PropertyObserver<M>> {

    private final List<PropertySubscriber<M>> subscribers;
    private static final Comparator<? super PropertySubscriber<?>> SUBSCRIBER_COMPARATOR = createComparator();
    
    private PropertyDispatcher(List<PropertySubscriber<M>> subscribers) {
        super(subscribers, subscriber -> subscriber::onChanged, subscriber -> subscriber::onDisposed, PROPERTY);
        this.subscribers = subscribers;
    }
    
    static <M> PropertyDispatcher<M> create() {
        return new PropertyDispatcher<>(new ArrayList<>());
    }
    
    @Override
    public PropertySubscriber<M> subscribe(PropertyObserver<M> observer) {
        
        PropertySubscriber<M> subscriber = new PropertySubscriber<>(wrapObserver(observer));
        
        subscriber.doOnDispose(() -> subscribers.remove(subscriber));
        subscribers.add(subscriber);
        subscribers.sort(SUBSCRIBER_COMPARATOR);
        
        return subscriber;
    }
    
    private PropertyObserver<M> wrapObserver(PropertyObserver<M> observer) {
        return new PropertyObserver<M>() {
            @Override
            public void onChanged(M newValue) {
                dispatchOrQueue(() -> {
                    setDispatchingToBinding(observer.isBinding());
                    observer.onChanged(newValue);
                    setDispatchingToBinding(false);
                });
            }

            @Override
            public void onDisposed() {
                dispatchOrQueue(observer::onDisposed);
            }

            @Override
            public boolean isBinding() {
                return observer.isBinding();
            }
        };
    }

    private static Comparator<? super PropertySubscriber<?>> createComparator() {
        return (subscriber1, subscriber2) -> {
            if (subscriber1.isBinding() && !subscriber2.isBinding())
                return -1;
            
            if (!subscriber1.isBinding() && subscriber2.isBinding())
                return 1;
            
            return 0;
        };
    }
}
