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
package mb.rxui.dispatcher;

import static java.util.Objects.requireNonNull;
import static mb.rxui.Callbacks.runSafeCallback;
import static mb.rxui.Preconditions.checkState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import mb.rxui.Observer;
import mb.rxui.Subscriber;
import mb.rxui.disposables.Disposable;

public abstract class AbstractDispatcher<V, S extends Subscriber & Observer<V>, O extends Observer<V>> implements Dispatcher<V, S, O> {

    private final List<S> subscribers;
    private final List<Disposable> disposables;
    private final Function<S, Consumer<V>> dispatchFunction;
    private final Function<S, Runnable> disposeFunction;
    private final Type type;
    private final List<Runnable> pausedDisptaches;

    private boolean isDispatching = false;
    private boolean isDisposed = false;
    private int pauseCount = 0;
    private boolean dispatchingToBinding = false;
    
    protected AbstractDispatcher(List<S> subscribers, 
                                 Function<S, Consumer<V>> dispatchFunction, 
                                 Function<S, Runnable> disposeFunction,
                                 Type type) {
        this.subscribers = requireNonNull(subscribers);
        this.disposables = new ArrayList<>();
        this.dispatchFunction = requireNonNull(dispatchFunction);
        this.disposeFunction = requireNonNull(disposeFunction);
        this.type = requireNonNull(type);
        this.pausedDisptaches = new ArrayList<>();
    }
    
    @Override
    public void dispose() {
        if(isDisposed)
            return;
        
        isDisposed = true;
        
        new ArrayList<>(subscribers).stream().map(disposeFunction).forEach(Runnable::run);
        subscribers.clear();
        
        disposables.forEach(disposable -> runSafeCallback(disposable::dispose));
        disposables.clear();
    }
    
    @Override
    public void onDisposed(Disposable toDispose) {
        if (isDisposed) {            
            toDispose.dispose();
            return;
        }
        
        disposables.add(toDispose);
    }

    @Override
    public void dispatch(V newValue) {
        checkState(!isDisposed, "Dispatcher has been disposed, cannot dispatch: " + newValue);
        dispatchOrQueue(createDisptachValueRunnable(newValue));
    }

    private Runnable createDisptachValueRunnable(V newValue) {
        return () -> {
            boolean isEventDispatcher = getType() == Dispatcher.Type.EVENT;
            
            if (isEventDispatcher)
                Dispatchers.getInstance().pausePropertyDispatchers();
            
            new ArrayList<>(subscribers).stream()
                                        .map(dispatchFunction)
                                        .forEach(consumer -> consumer.accept(newValue));
            
            if(isEventDispatcher)
                Dispatchers.getInstance().resumePropertyDispatchers();
        };
    }

    @Override
    public boolean isDispatching() {
        return isDispatching;
    }
    
    /**
     * Updates the dispatching state of this dispatchers. This is only required
     * in order to re-establish the dispatching state for time shifted event
     * emissions.
     * 
     * @param isDispatching
     *            the new dispatching state to set for this dispatcher
     */
    void setDispatching( boolean isDispatching ) {
        this.isDispatching = isDispatching;
    }
    
    @Override
    public boolean isDisposed() {
        return isDisposed;
    }
    
    @Override
    public int getSubscriberCount() {
        return subscribers.size();
    }
    
    @Override
    public Type getType() {
        return type;
    }
    
    protected void dispatchOrQueue(Runnable disptchRunnable) {
        Runnable wrappedRunnable = wrapRunnableWithIsDispatching(disptchRunnable);
        if (isPaused()) {
            pausedDisptaches.add(wrappedRunnable);
        } else {
            wrappedRunnable.run();
        }
    }
    
    boolean isPaused() {
        return pauseCount > 0;
    }
    
    void pause() {
        pauseCount++;
    }

    void resume() {
        pauseCount--;
        
        if (isPaused())
            return;
        
        pausedDisptaches.forEach(Runnable::run);
        pausedDisptaches.clear();
    }
    
    void setDispatchingToBinding(boolean dispatchingToBinding) {
        this.dispatchingToBinding = dispatchingToBinding;
    }
    
    boolean isDispatchingToBinding() {
        return dispatchingToBinding;
    }

    /**
     * Ensures that when the runnable is running that the dispatch flag is
     * turned on and then off when execution is finished.
     * 
     * @param runnable
     *            some runnable to wrap
     * @return a new runnable that ensures to toggle on/off the dispatch flag
     *         before and after execution.
     */
    private Runnable wrapRunnableWithIsDispatching(Runnable runnable) {
        return () -> {            
            isDispatching = true;
            try {
                runnable.run();
            } finally {            
                isDispatching = false;
            }
        };
    }
}
