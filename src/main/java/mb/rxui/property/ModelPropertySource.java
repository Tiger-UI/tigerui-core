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

import mb.rxui.dispatcher.PropertyDispatcher;

/**
 * A property source for model properties.
 * 
 * @param <M>
 *            the type of value this property supplies
 */
public class ModelPropertySource<M> implements PropertySource<M> {
    
    private M value;
    private final PropertyDispatcher<M> dispatcher;

    private ModelPropertySource(M initialValue, PropertyDispatcher<M> dispatcher) {
        this.value = requireNonNull(initialValue);
        this.dispatcher = requireNonNull(dispatcher);
    }
    
    @Override
    public void setValue(M newValue) {
        value = newValue;
        dispatcher.dispatch(value);
    }

    @Override
    public M get() {
        return value;
    }
    
    public static <M> PropertySourceFactory<M> createFactory(M initialValue)
    {
        return dispatcher -> new ModelPropertySource<>(initialValue, dispatcher);
    }
}
