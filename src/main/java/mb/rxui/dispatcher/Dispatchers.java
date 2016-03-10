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

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import mb.rxui.EventLoop;
import mb.rxui.Observer;
import mb.rxui.Subscriber;
import mb.rxui.annotations.RequiresTest;
import mb.rxui.dispatcher.Dispatcher.Type;

@RequiresTest
public class Dispatchers {
    private static final Dispatchers instance = new Dispatchers();

    private final Map<AbstractDispatcher<?, ?, ?>, Void> dispatchers = new WeakHashMap<>();

    private PropertyDispatcherFactory propertyDispatcherFactory = PropertyDispatcher::create;
    private EventDispatcherFactory eventDispatcherFactory = EventDispatcher::create;
    
    
    private Dispatchers() {
    } // Singleton

    public static Dispatchers getInstance() {
        return instance;
    }
    
    /**
     * Creates a function that accepts a runnable and returns a runnable that is
     * wrapped with calls to re-establish the current dispatch state.
     * <p>
     * This is used to protect against reentrant calls from time shifted
     * dispatches. For example calling debounce would schedule the current
     * dispatch some time in the future at which point the isDispatching state
     * would be lost. It would therefore be possible to create an endless loop
     * if the callback was reentrant. Re-establishing the isDispatching state as
     * is done here assures that reentrant calls are ignored even when they are
     * time shifted.
     * <p>
     * TODO: move this into the {@link EventLoop} class as part of the method
     * call to schedule with timeout.
     * 
     * @return a {@link Function} that when called will capture the current
     *         state of the dispatchers and wrap the provided runnable with
     *         calls to restore this state.
     */
    public Runnable wrapRunnableWithCurrentDispatchState(Runnable runnableToWrap) {

        Map<AbstractDispatcher<?, ?, ?>, Void> dispatchingDispatchers = new WeakHashMap<>();

        dispatchers.keySet().stream().filter(Dispatcher::isDispatching)
                .forEach(dispatcher -> dispatchingDispatchers.put(dispatcher, null));

        return () -> {
            // capture the current state of the dispatchers since it could
            // have changed since warp was called
            List<Runnable> dispatchStateRestoreList = dispatchingDispatchers.keySet().stream()
                    .map(Dispatchers::createStateRestorer).collect(Collectors.toList());

            // toggle all the dispatchers to true that where captured when
            // wrapping the runnable
            dispatchingDispatchers.keySet().forEach(dispatcher -> dispatcher.setDispatching(true));

            // perform the dispatch
            runnableToWrap.run();

            // restore the dispatch state to what is was before running the
            // runnable.
            dispatchStateRestoreList.forEach(Runnable::run);
        };
    }
    
    public boolean isDispatching() {
        return dispatchers.keySet().stream().filter(Dispatcher::isDispatching).findAny().isPresent();
    }
    
    public boolean isDispatchingBinding() {
        return dispatchers.keySet().stream().filter(AbstractDispatcher::isDispatchingToBinding).findAny().isPresent();
    }

    <M> PropertyDispatcher<M> createPropertyDispatcher() {
        return addDispatcher(propertyDispatcherFactory.create());
    }

    void setPropertyDispatcherFactory(PropertyDispatcherFactory propertyDispatcherFactory) {
        this.propertyDispatcherFactory = propertyDispatcherFactory;
    }
    
    <E> EventDispatcher<E> createEventDispatcher() {
        return addDispatcher(eventDispatcherFactory.create());
    }

    void setEventDispatcherFactory(EventDispatcherFactory eventDispatcherFactory) {
        this.eventDispatcherFactory = eventDispatcherFactory;
    }

    void pausePropertyDispatchers() {
        dispatchers.keySet()
                   .stream()
                   .filter(dispatcher -> dispatcher.getType() == Type.PROPERTY)
                   .forEach(AbstractDispatcher::pause);
    }

    void resumePropertyDispatchers() {
        dispatchers.keySet()
                   .stream()
                   .filter(dispatcher -> dispatcher.getType() == Type.PROPERTY)
                   .forEach(AbstractDispatcher::resume);
    }

    private static Runnable createStateRestorer(AbstractDispatcher<?, ?, ?> dispatcher) {
        boolean dispatching = dispatcher.isDispatching();
        return () -> dispatcher.setDispatching(dispatching);
    }

    private <V, S extends Subscriber & Observer<V>, O extends Observer<V>, D extends AbstractDispatcher<V, S, O>> D addDispatcher(
            D eventDispatcher) {
        dispatchers.put(eventDispatcher, null);
        return eventDispatcher;
    }
    
    public static interface PropertyDispatcherFactory
    {
        <M> PropertyDispatcher<M> create();
    }
    
    public static interface EventDispatcherFactory
    {
        <M> EventDispatcher<M> create();
    }
}
