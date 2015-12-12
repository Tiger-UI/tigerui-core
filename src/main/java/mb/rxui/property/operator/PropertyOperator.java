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

import java.util.function.Function;

import mb.rxui.property.PropertyPublisher;

/**
 * A {@link PropertyOperator} is some function that can convert a property
 * publish of one kind to another
 * 
 * @param <Downstream>
 *            the source property publisher (the one that is being converted)
 * @param <Upstream>
 *            the target property publisher (the result of converting the source
 *            publisher)
 */
public interface PropertyOperator<Downstream, Upstream> extends Function<PropertyPublisher<Downstream>, PropertyPublisher<Upstream>> {

}
