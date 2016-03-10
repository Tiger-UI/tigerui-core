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
package mb.rxui.event.operator;

import java.util.function.Function;

import mb.rxui.event.EventSubscriber;

/**
 * An operator converts a subscriber from a child type to a parent type. This is
 * used whenever a stream is transformed, i.e. map.
 * 
 * @param <Parent>
 *            parent stream data type (original stream)
 * @param <Child>
 *            child stream data type (new stream)
 */
public interface Operator<Parent, Child> extends Function<EventSubscriber<Child>, EventSubscriber<Parent>> {}
