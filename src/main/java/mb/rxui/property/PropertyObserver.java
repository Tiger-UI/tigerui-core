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

import java.util.Objects;
import java.util.function.Consumer;

import mb.rxui.Observer;

/**
 * An observer of property events.
 * 
 * @param <M>
 *            the type of the values emitted by the observed property.
 */
public interface PropertyObserver<M> extends Observer<M> {
    /**
     * Called whenever the value of the property that this observer observes
     * changes.
     * 
     * @param newValue
     *            the new value for the property.
     */
    void onChanged(M newValue);

    /**
     * Called when the property that this observer observes is destroyed.
     */
    void onDisposed();
    
    // Factory methods

    /**
     * Creates a property observer that only observes onChanged events.
     * 
     * @param onChangeListener
     *            some consumer of onChanged events.
     * @return a new {@link PropertyObserver} that only observes onChanged
     *         events.
     */
    static <M> PropertyObserver<M> create(Consumer<M> onChangeListener) {
        return create(onChangeListener, () -> {});
    }

    /**
     * Creates a property observer that only observes onDisposed events.
     * 
     * @param onDestroy
     *            some runnable to run when the observed property is destroyed.
     * @return a new {@link PropertyObserver} that only observes onDisposed
     *         events.
     */
    static <M> PropertyObserver<M> create(Runnable onDestroy) {
        return create(newValue -> {} , onDestroy);
    }

    /**
     * Creates a property observer that observes, both onChanged and onDisposed events.
     * @param onChanged some consumer of onChanged events.
     * @param onDisposed some runnable to run when the observed property is destroyed.
     * @return
     */
    static <M> PropertyObserver<M> create(Consumer<M> onChanged, Runnable onDisposed) {
        Objects.isNull(onChanged);
        Objects.requireNonNull(onDisposed);

        return new PropertyObserver<M>() {
            @Override
            public void onChanged(M newValue) {
                onChanged.accept(newValue);
            }

            @Override
            public void onDisposed() {
                onDisposed.run();
            }
        };
    }
}
