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
package mb.rxui.disposables;

import static java.util.Objects.requireNonNull;

/**
 * A Runnable that can be disposed (cancelled). Once disposed calling run will
 * not run the wrapped runnable.
 */
public class DisposableRunnable implements Disposable, Runnable {
    
    private final Runnable runnable;
    private boolean isDisposed;
    
    public DisposableRunnable(Runnable runnable) {
        this.runnable = requireNonNull(runnable);
        this.isDisposed = false;
    }

    @Override
    public void run() {
        if(!isDisposed)
            runnable.run();
    }

    @Override
    public void dispose() {
        isDisposed = true;
    }
}
