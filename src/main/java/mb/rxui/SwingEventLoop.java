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

import static mb.rxui.Preconditions.checkArgument;

import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import mb.rxui.dispatcher.Dispatchers;
import mb.rxui.disposables.Disposable;
import mb.rxui.disposables.DisposableRunnable;

/**
 * A event loop that should be used for Swing/AWT applications.
 */
public final class SwingEventLoop implements EventLoop {

    @Override
    public Disposable schedule(Runnable runnable, long time, TimeUnit timeUnit) {
        checkArgument(time >= 0, "Cannot schedule a runnable with a negative time [" + time + "]");
        
        runnable = Dispatchers.getInstance().wrapRunnableWithCurrentDispatchState(runnable);
        
        DisposableRunnable disposableRunnable = new DisposableRunnable(runnable);
        
        Timer timer = new Timer((int)timeUnit.toMillis(time), event -> disposableRunnable.run());
        timer.setRepeats(false);
        timer.start();
        
        return disposableRunnable;
    }

    @Override
    public boolean isInEventLoop() {
        return SwingUtilities.isEventDispatchThread();
    }

    @Override
    public Disposable invokeLater(Runnable runnable) {
        
        DisposableRunnable disposableRunnable = new DisposableRunnable(runnable);
        
        SwingUtilities.invokeLater(disposableRunnable);
        
        return disposableRunnable;
    }

    public String getThreadName() {
        return "Event Dispatch";
    }
}
