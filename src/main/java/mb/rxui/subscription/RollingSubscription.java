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
package mb.rxui.subscription;

import java.util.Optional;

/**
 * A subscription that can be updated, via the method {@link #set(Subscription)}
 * . When setting a new subscription the current one will be disposed.
 */
public class RollingSubscription implements Subscription {
    
    private Optional<Subscription> subscription = Optional.empty();
    private boolean disposed = false;
    
    @Override
    public void dispose() {
        if(disposed)
            return;
        
        disposed = true;
        subscription.ifPresent(Subscription::dispose);
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
    
    /**
     * Updates the subscription held by this rolling subscription. Disposes the
     * existing subscription if present.
     * 
     * @param subscription
     *            some subscription to set
     */
    public void set(Subscription subscription) {
        if(disposed) {            
            subscription.dispose();
        } else {
            this.subscription.ifPresent(Subscription::dispose);
            this.subscription = Optional.of(subscription);
        }
    }
}
