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
package mb.rxui.property.swing;

import static mb.rxui.EventLoop.SWING_EVENT_LOOP;

import java.util.function.Consumer;
import java.util.function.Supplier;

import mb.rxui.dispatcher.PropertyDispatcher;
import mb.rxui.disposables.Disposable;
import mb.rxui.property.PropertySource;

/**
 * A property source for Swing components.<br>
 * <br>
 * @param <M> the type of values this source provides
 */
public abstract class SwingPropertySource<M, L, C> implements PropertySource<M> {
    
    private final Supplier<M> getter;
    private final Consumer<M> setter;

    protected SwingPropertySource(Supplier<M> getter, 
                                  Consumer<M> setter, 
                                  C component,
                                  PropertyDispatcher<M> dispatcher) {
        this.getter = getter;
        this.setter = setter;
        SWING_EVENT_LOOP.checkInEventLoop();
        dispatcher.onDisposed(addListener(component, createListener(dispatcher)));
    }

    @Override
    public void setValue(M newValue) {
        setter.accept(newValue);
    }

    @Override
    public M get() {
        return getter.get();
    }
    
    protected abstract L createListener(PropertyDispatcher<M> dispatcher);
    
    protected abstract Disposable addListener(C component, L listener);
}
