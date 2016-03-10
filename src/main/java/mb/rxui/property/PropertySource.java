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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import mb.rxui.dispatcher.PropertyDispatcher;

/**
 * A source of property values.
 * 
 * @param <M> The type of the values this property source provides.
 */
public interface PropertySource<M> extends Consumer<M>, Supplier<M> {
    /**
     * Sets the current value of this property.
     * 
     * @param value
     *            a new value to set to this property.
     * @throws NullPointerException
     *             if an attempt is made to set the value to null.
     * @throws IllegalStateException
     *             if called from a thread other than the one that this property
     *             was created from.
     */
    void setValue(M newValue);
    
    /**
     * Implementation of Consumer, redirects to {@link #setValue(Object)}
     * 
     * @throws NullPointerException
     *             if an attempt is made to set the value to null.
     * @throws IllegalStateException
     *             if called from a thread other than the one that this property
     *             was created from.
     */
    default public void accept(M newValue)
    {
        setValue(newValue);
    }
    
    M get();
    
    /**
     * Simple Factory interface for creating a {@link PropertySource}
     * 
     * @param <M>
     *            The type of the values the created property source will
     *            provide.
     */
    static interface PropertySourceFactory<M> extends Function<PropertyDispatcher<M>, PropertySource<M>> {}
}
