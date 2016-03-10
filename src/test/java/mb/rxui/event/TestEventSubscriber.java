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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class TestEventSubscriber {
    private EventSubscriber<String> subscriber;
    private EventObserver<String> observer;
    
    @Before
    public void setup() {
        observer = Mockito.mock(EventObserver.class);
        subscriber = new EventSubscriber<>(observer);
    }

    @Test
    public void testOnEvent() throws Exception {
        subscriber.onEvent("tacos");
        Mockito.verify(observer).onEvent("tacos");
    }
    
    @Test
    public void testOnCompleted() throws Exception {
        
        Runnable onUnsubscribedAction = Mockito.mock(Runnable.class);
        InOrder inOrder = Mockito.inOrder(onUnsubscribedAction, observer);
        
        subscriber.doOnDispose(onUnsubscribedAction);
        
        assertFalse(subscriber.isDisposed());
        
        subscriber.onCompleted();
        inOrder.verify(observer).onCompleted();
        inOrder.verify(onUnsubscribedAction).run();
        inOrder.verifyNoMoreInteractions();
        
        assertTrue(subscriber.isDisposed());
    }
    
    @Test
    public void testDispose() throws Exception {
        Runnable onUnsubscribedAction = Mockito.mock(Runnable.class);
        InOrder inOrder = Mockito.inOrder(onUnsubscribedAction, observer);
        
        subscriber.doOnDispose(onUnsubscribedAction);
        
        assertFalse(subscriber.isDisposed());
        
        subscriber.dispose();
        inOrder.verify(observer, never()).onCompleted();
        inOrder.verify(onUnsubscribedAction).run();
        inOrder.verifyNoMoreInteractions();
        
        assertTrue(subscriber.isDisposed());
    }
    
    @Test
    public void testReceiveNoEventsAfterDispose() throws Exception {
        subscriber.dispose();
        Mockito.verify(observer, never()).onCompleted();
        
        subscriber.onEvent("tacos");
        Mockito.verify(observer, never()).onEvent(Mockito.anyString());
    }
    
    @Test
    public void testReceiveNoEventsAfterOnCompleted() throws Exception {
        subscriber.onCompleted();
        Mockito.verify(observer).onCompleted();
        
        subscriber.onEvent("tacos");
        Mockito.verify(observer, never()).onEvent(Mockito.anyString());
        
        subscriber.onCompleted();
        Mockito.verifyNoMoreInteractions(observer);
    }
    
    @Test
    public void testOnEventThrows() throws Exception {
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        Mockito.doThrow(new RuntimeException()).when(observer).onEvent(Mockito.anyString());
        EventSubscriber<String> subscriber = new EventSubscriber<>(observer);
        
        try {
            subscriber.onEvent("tacos");
        } catch(RuntimeException exception) {
            exception.printStackTrace();
            fail("should not have thrown an exception");
        }
    }
    
    @Test
    public void testOnCompletedThrows() throws Exception {
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        Mockito.doThrow(new RuntimeException()).when(observer).onCompleted();
        EventSubscriber<String> subscriber = new EventSubscriber<>(observer);
        
        try {
            subscriber.onCompleted();
        } catch(RuntimeException exception) {
            fail("should not have thrown an exception");
        }
    }
    
    @Test
    public void testDoOnDisposedThrows() throws Exception {
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        EventSubscriber<String> subscriber = new EventSubscriber<>(observer);
        
        Runnable onDisposedActionThatThrows = Mockito.mock(Runnable.class);
        Mockito.doThrow(new RuntimeException()).when(onDisposedActionThatThrows).run();
        subscriber.doOnDispose(onDisposedActionThatThrows);

        Runnable onDisposedAction = Mockito.mock(Runnable.class);
        subscriber.doOnDispose(onDisposedAction);
        
        InOrder inOrder = Mockito.inOrder(onDisposedActionThatThrows, onDisposedAction);
        
        try {
            subscriber.dispose();
        } catch(RuntimeException exception) {
            fail("should not have thrown an exception");
        }
        
        inOrder.verify(onDisposedActionThatThrows).run();
        inOrder.verify(onDisposedAction).run();
        inOrder.verifyNoMoreInteractions();
    }
}
