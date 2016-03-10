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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import mb.rxui.dispatcher.Dispatcher;
import mb.rxui.dispatcher.EventDispatcher;
import mb.rxui.disposables.Disposable;

public class TestEventDispatcher {
    private EventDispatcher<String> dispatcher;
    private Consumer<String> onEvent;
    private Runnable onDisposed;

    @Before
    public void setup() {
        dispatcher = Dispatcher.createEventDispatcher();
        
        onEvent = Mockito.mock(Consumer.class);
        onDisposed = Mockito.mock(Runnable.class);
        
        dispatcher.subscribe(EventObserver.create(onEvent, onDisposed));
        dispatcher.subscribe(EventObserver.create(event -> assertTrue(dispatcher.isDispatching())));
        assertEquals(2, dispatcher.getSubscriberCount());
    }
    
    @Test
    public void testDispatch() {
        assertFalse(dispatcher.isDispatching());
        dispatcher.dispatch("tacos");
        verify(onEvent).accept("tacos");
        
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        dispatcher.subscribe(observer);
        
        dispatcher.dispatch("tacos");
        verify(observer).onEvent("tacos");
        
        dispatcher.dispatch("burritos");
        verify(onEvent).accept("burritos");
        verify(observer).onEvent("burritos");
    }
    
    @Test
    public void testDispose() throws Exception {
        assertFalse(dispatcher.isDisposed());
        
        dispatcher.dispose();
        assertTrue(dispatcher.isDisposed());
        Mockito.verify(onDisposed).run();
        Mockito.verifyNoMoreInteractions(onDisposed, onEvent);
        
        dispatcher.dispose();
        assertEquals(0, dispatcher.getSubscriberCount());
        Mockito.verifyNoMoreInteractions(onDisposed, onEvent);
    }
    
    @Test
    public void testOnDisposed() throws Exception {
        Disposable disposable = Mockito.mock(Disposable.class);
        dispatcher.onDisposed(disposable);
        
        Mockito.verifyNoMoreInteractions(disposable);
        
        dispatcher.dispose();
        Mockito.verify(disposable).dispose();
    }
    
    @Test
    public void testOnDisposedAfterDisposed() throws Exception {
        dispatcher.dispose();

        Disposable disposable = Mockito.mock(Disposable.class);
        dispatcher.onDisposed(disposable);
        Mockito.verify(disposable).dispose();
        Mockito.verifyNoMoreInteractions(disposable);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testDispatchAfterDisposedThrows() throws Exception {
        dispatcher.dispose();
        dispatcher.dispatch("tacos");
    }
    
    @Test
    public void testOnEventThrowsDoesNotAffectIsDispatching() throws Exception {
        dispatcher.subscribe(EventObserver.create(event -> { throw new RuntimeException(); }));
        
        dispatcher.dispatch("tacos");
        assertFalse(dispatcher.isDispatching());
    }
}
