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
package mb.rxui;

import mb.rxui.property.PropertySubscriber;
import mb.rxui.subscription.Subscription;

/**
 * A publisher is some class that can publish values and can be subscribed to.
 * 
 * @param <O>
 *            the type of the Observer that the publisher publishes its values
 *            to.
 */
public interface Publisher<V, O extends Observer<V>> {
    /**
     * Subscribes to this property publisher.
     * 
     * @param observer
     *            some observer to subscribe to this property publisher
     * @return a {@link PropertySubscriber} that can be used to cancel the
     *         subscription
     */
    Subscription subscribe(O observer);
}
