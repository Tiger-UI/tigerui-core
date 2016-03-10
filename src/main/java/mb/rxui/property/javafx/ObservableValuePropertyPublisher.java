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
package mb.rxui.property.javafx;

import static java.util.Objects.requireNonNull;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import mb.rxui.property.PropertyObserver;
import mb.rxui.property.PropertySubscriber;
import mb.rxui.property.publisher.PropertyPublisher;
import mb.rxui.subscription.Subscription;

public final class ObservableValuePropertyPublisher<T> implements PropertyPublisher<T> {

    private final ObservableValue<T> observableValue;
    
    ObservableValuePropertyPublisher(ObservableValue<T> observableValue) {
        this.observableValue = requireNonNull(observableValue);
    }
    
    @Override
    public T get() {
        return observableValue.getValue();
    }

    @Override
    public Subscription subscribe(PropertyObserver<T> observer) {
        PropertySubscriber<T> subscriber = new PropertySubscriber<>(observer);
        
        // adds a listener to the observable value
        ChangeListener<? super T> listener = (observable, oldValue, newValue) -> subscriber.onChanged(newValue);
        observableValue.addListener(listener);

        // push the latest value to the subscriber and add unsubscribe action
        subscriber.onChanged(get());
        subscriber.doOnDispose(() -> observableValue.removeListener(listener));
        
        return subscriber;
    }
}
