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
package tigerui.event.operator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import tigerui.SwingTestRunner;
import tigerui.event.EventObserver;
import tigerui.event.EventStream;
import tigerui.event.EventSubject;
import tigerui.subscription.Subscription;

@RunWith(SwingTestRunner.class)
public class TestOperatorChanges {
    @Test
    public void testChanges() {
        EventSubject<String> events = EventSubject.create();
        EventStream<String> changesStream = events.changes((string, last) -> (string.length() + last));
        
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        
        Subscription subscription = changesStream.observe(observer);
        assertFalse(subscription.isDisposed());
        verifyNoMoreInteractions(observer);
        
        events.publish("tacos");
        verifyNoMoreInteractions(observer);
        
        events.publish("burritos");
        Mockito.verify(observer).onEvent("5burritos");
        
        // Just double checking that an event stream does not suppress consecutive duplicates values.
        events.publish("burritos");
        verify(observer).onEvent("8burritos");
        verifyNoMoreInteractions(observer);
    }
    
    @Test
    public void testDisposeUnsubscribesObserver() {
        EventSubject<String> events = EventSubject.create();
        EventStream<String> changesStream = events.changes((string, last) -> (string.length() + last));
        
        Consumer<String> onEvent = Mockito.mock(Consumer.class);
        Runnable onCompleted = Mockito.mock(Runnable.class);
        
        Subscription subscription = changesStream.observe(EventObserver.create(onEvent, onCompleted));
        assertFalse(subscription.isDisposed());
        verify(onEvent, never()).accept(Mockito.any());
        
        events.dispose();
        verify(onCompleted).run();
        assertTrue(subscription.isDisposed());
        Mockito.verifyNoMoreInteractions(onEvent, onCompleted);
    }
    
    @Test
    public void testUnsubscribeRemovesObserver() {
        EventSubject<String> events = EventSubject.create();
        
        assertFalse(events.hasObservers());

        EventStream<String> changesStream = events.changes((string, last) -> (string.length() + last));
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        Subscription subscription = changesStream.observe(observer);

        assertTrue(events.hasObservers());
        
        subscription.dispose();
        assertFalse(events.hasObservers());
    }
    
    @Test
    public void testSubscribeAfterDisposed() {
        EventSubject<String> events = EventSubject.create();
        EventStream<String> changesStream = events.changes((string, last) -> (string.length() + last));
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        
        events.dispose();
        
        Subscription subscription = changesStream.observe(observer);

        verify(observer, never()).onEvent(Mockito.any());
        verify(observer).onCompleted();
        Mockito.verifyNoMoreInteractions(observer);
        
        assertTrue(subscription.isDisposed());
        assertFalse(events.hasObservers());
    }
}
