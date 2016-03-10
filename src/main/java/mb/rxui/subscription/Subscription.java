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

import mb.rxui.disposables.Disposable;

/**
 * A subscription can be used to stop observing some property or stream.
 */
public interface Subscription extends Disposable {
    /**
     * @return true if this subscription is disposed, false otherwise
     */
    boolean isDisposed();
    
    
    public static Subscription DISPOSED_SUBSCRIPTION = new Subscription() {
        @Override
        public void dispose() {}
        
        @Override
        public boolean isDisposed() {
            return true;
        }
    };
    
    /**
     * Creates a Subscription from a disposable.
     * 
     * @param disposable
     *            some {@link Disposable} that should be run when terminating
     *            the subscription.
     * @return A new {@link Subscription}
     */
    static Subscription create(Disposable disposable) {
        return new Subscription() {
            private boolean isDisposed = false;

            @Override
            public void dispose() {
                if (isDisposed)
                    return;

                isDisposed = true;
                disposable.dispose();
            }

            @Override
            public boolean isDisposed() {
                return isDisposed;
            }
        };
    }
    
    /**
     * Creates a simple Subscription that can be used as a cancellation token.
     * 
     * @return a new {@link Subscription} that can be used to signal
     *         cancellation.
     */
    static Subscription create() {
        return create(() -> {});
    }
}
