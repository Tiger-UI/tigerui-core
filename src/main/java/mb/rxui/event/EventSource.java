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
package mb.rxui.event;

/**
 * A source of events for an {@link EventStream}.
 * 
 * @param <E>
 *            the type of data published by this {@link EventSource}
 */
public interface EventSource<E> {
    /**
     * Publishes a new event into the {@link EventStream}
     * 
     * @param event
     *            some event to publish.
     */
    void publish(E event);
}