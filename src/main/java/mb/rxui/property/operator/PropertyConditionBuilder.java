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
package mb.rxui.property.operator;

import static java.util.Objects.requireNonNull;
import static mb.rxui.Functions.TRUE;

import java.util.ArrayList;
import java.util.List;

import mb.rxui.property.PropertyStream;
import mb.rxui.subscription.Subscription;

/**
 * A {@link PropertyConditionBuilder} can be used to setup some multi-condition
 * filter to apply to a property. The action that is provided in the
 * {@link #then(Runnable)} clause will only be executed if any of the provided
 * {@link #or(Object)} conditions are met.
 * 
 * @param <M>
 *            the type of the values the underlying property emits.
 */
public class PropertyConditionBuilder<M> {
    
    private final PropertyStream<M> stream;
    private final List<M> values;
    
    public PropertyConditionBuilder(PropertyStream<M> stream, M firstValue) {
        this.stream = requireNonNull(stream);
        this.values = new ArrayList<>();
        values.add(requireNonNull(firstValue));
    }

    /**
     * Adds another value to check for equality to this condition
     * 
     * @param value
     *            some other value to check for equality
     * @return this {@link PropertyConditionBuilder} with a new value added to
     *         it's equality check.
     */
    public PropertyConditionBuilder<M> or(M value) {
        values.add(value);
        return this;
    }
    
    /**
     * Adds some action to perform if the current value of the underlying
     * property matches one of the provided values added through
     * {@link #or(Object)}.
     * 
     * @param action
     *            some action to perform if this composite condition evaluates
     *            to true.
     * @return a {@link Subscription} that can be used to cancel the
     *         subscription.
     */
    public Subscription then(Runnable action) {
        return stream.lift(new OperatorIs<M>(values)).filter(TRUE).onEvent(value -> action.run());
    }
}
