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
package mb.rxui.property;

import static java.util.Objects.requireNonNull;
import static mb.rxui.Callbacks.runSafeCallback;

import java.util.Optional;

import mb.rxui.Subscriber;

/**
 * A subscriber of Property events. A property can emit one or many
 * {@link #onChanged(Object)} events followed by one {@link #onDisposed()}
 * event.<br>
 * <br>
 * NOTE: If the underlying observer throws an exception while handling the a
 * callback, the exception will not propagate. For now it is simply printed.
 * Perhaps in the future a global error handler should be added. Alternatively a
 * property could be built with a specific exception handler.Í
 * 
 * @param <M>
 *            the type of value the property manages.
 */
public class PropertySubscriber<M> extends Subscriber implements PropertyObserver<M> {
    
    private final PropertyObserver<M> observer;
    
    private Optional<M> lastValue = Optional.empty();
    
    public PropertySubscriber(PropertyObserver<M> observer) {
        this.observer = requireNonNull(observer);
    }

    @Override
    public void onChanged(M newValue) {
        if(isDisposed())
            return;
        
        if(lastValue.isPresent() && lastValue.get().equals(newValue))
            return;
        
        lastValue = Optional.of(newValue);
        
        runSafeCallback(() -> observer.onChanged(newValue));
    }
    
    @Override
    public void onDisposed() {
        if(isDisposed())
            return;
        
        runSafeCallback(observer::onDisposed);
        dispose();
    }

    @Override
    public boolean isBinding() {
        return observer.isBinding();
    }
}
