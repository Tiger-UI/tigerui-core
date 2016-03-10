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

import static javafx.application.Platform.isFxApplicationThread;
import static javax.swing.SwingUtilities.isEventDispatchThread;
import static mb.rxui.Preconditions.checkState;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import mb.rxui.disposables.Disposable;

/**
 * A event loop captures a thread and provides a mechanism to assert that the
 * current thread matches this context. Additionally a event loop can be used to
 * schedule a runnable to run at some fixed time in the future on this context's
 * thread.
 * <p>
 * NOTE: An event loop guarantees that only one runnable is executing at a time.
 * Scheduling Runnables through an event loop serializes their execution. This
 * differs than an arbitrary {@link Executor} in the sense that an executor
 * cannot guarantee serialized execution unless it's concurrency is 1.
 * <p>
 * TODO: Consider adding invokeAndWait.
 */
public interface EventLoop {
    /**
     * Checks if the current thread matches the thread for this context.
     * 
     * @throws IllegalStateException
     *             if the current thread does not match the valid thread for
     *             this event loop.
     */
    default void checkInEventLoop() {
        checkState(isInEventLoop(),
                   "Method should have been called on the " + getThreadName()
                        + " Thread, but instead it was called from: " + 
                   Thread.currentThread());
    }
    
    /**
     * @return true if the current thread matches the thread of this event loop, false otherwise.
     */
    boolean isInEventLoop();
    
    /**
     * Invokes the provided runnable now.
     * 
     * @param runnable
     *            some runnable to run.
     * @throws IllegalStateException
     *             if the current thread does not match the valid thread for
     *             this event loop.
     */
    default void invokeNow(Runnable runnable) {
        checkInEventLoop();
        /*
         * TODO: consider tracking how long it takes to run the runnable and
         * logging a message if it take too long.
         */
        runnable.run();
    }

    /**
     * Schedules a runnable to be executed on this event loop at a later time.
     * This is achieved by adding it to the event loop's queue. When this
     * runnable will be executed is a function of how many pending items there
     * are in the queue.
     * 
     * @param runnable
     *            some runnable to execute at a later time.
     * @return a {@link Disposable} that can be used to cancel the execution of
     *         this runnable should it not have already been executed.
     */
    Disposable invokeLater(Runnable runnable);
    
    /**
     * Invokes the provided runnable immediately if on the event loop already,
     * otherwise schedules a the runnable to execute later using the method
     * {@link #invokeLater(Runnable)}
     * 
     * <p>
     * <b>NOTE:</b> Use this only if you need to, since calling this method
     * without really knowing what you are doing can cause events to be
     * re-ordered, which may or may not be a problem.
     * 
     * @param runnable
     *            some runnable to invoke
     * @return a {@link Disposable} that can be used to cancel the execution of
     *         this runnable should it not have already been executed.
     */
    default Disposable invokeNowOrLater(Runnable runnable) {
        if (isInEventLoop()) {
            runnable.run();
            return () -> {};
        }
        
        return invokeLater(runnable);
    }
    
    /**
     * Schedules some runnable to execute at some later time.
     * 
     * @param runnable
     *            some runnable to execute
     * @param delay
     *            some delay from now.
     * @param timeUnit
     *            the time unit of the delay
     * @return a {@link Disposable} that can be used to cancel the scheduled
     *         runnable.
     */
    Disposable schedule(Runnable runnable, long time, TimeUnit timeUnit);
    
    /**
     * Gets the name of the event loop thread
     * 
     * @return The name of the event loop thread, initially either
     *         "JavaFx Platform" or "Event Disptach"
     */
    String getThreadName();

    static EventLoop createEventLoop() {
        if (isEventDispatchThread())
            return SWING_EVENT_LOOP;

        if (isFxApplicationThread())
            return JAVAFX_EVENT_LOOP;

        throw new IllegalStateException(
                "Thread: [" + Thread.currentThread() + "] cannot be used to back a event loop.");
    }

    static final EventLoop SWING_EVENT_LOOP = new SwingEventLoop();
    static final EventLoop JAVAFX_EVENT_LOOP = new JavaFxEventLoop();
}
