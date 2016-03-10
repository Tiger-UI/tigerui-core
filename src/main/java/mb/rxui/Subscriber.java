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

import java.util.ArrayList;
import java.util.List;

import mb.rxui.subscription.Subscription;

/**
 * Base class for all subscribers. Provides a mechanism to dispose a
 * subscription and notify on disposed.
 */
public class Subscriber implements Subscription {

    private final List<Runnable> onDisposedActions;
    private boolean isDisposed = false;

    public Subscriber() {
        onDisposedActions = new ArrayList<>();
    }

    @Override
    public void dispose() {
        if(isDisposed)
            return;
        
        isDisposed = true;
        onDisposedActions.stream().forEach(Callbacks::runSafeCallback);
        onDisposedActions.clear();
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }
    
    /**
     * Adds some action to perform when this property subscription is
     * disposed.
     * 
     * @param onDisposedAction
     *            some action to run when this property subscription is
     *            disposed.
     */
    public void doOnDispose(Runnable onDisposedAction) {
        if(isDisposed)
            return;
        
        onDisposedActions.add(onDisposedAction);
    }
}