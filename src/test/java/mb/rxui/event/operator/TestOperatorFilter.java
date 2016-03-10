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
package mb.rxui.event.operator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;
import mb.rxui.event.EventStream;
import mb.rxui.event.EventObserver;
import mb.rxui.event.EventSubject;
import mb.rxui.subscription.Subscription;

@RunWith(SwingTestRunner.class)
public class TestOperatorFilter {
    @Test
    public void testMap() {
        EventSubject<String> events = EventSubject.create();
        EventStream<String> filteredStream = events.filter(val -> val.length() > 4);
        
        Consumer<String> onEvent = Mockito.mock(Consumer.class);
        Runnable onCompleted = Mockito.mock(Runnable.class);
        
        Subscription subscription = filteredStream.observe(EventObserver.create(onEvent, onCompleted));
        assertFalse(subscription.isDisposed());
        
        events.publish("tacos");
        Mockito.verify(onEvent).accept("tacos");
        
        events.publish("burritos");
        Mockito.verify(onEvent).accept("burritos");
        
        // Just double checking that an event stream does not suppress consecutive duplicates values.
        events.publish("burritos");
        Mockito.verify(onEvent, times(2)).accept("burritos");
        Mockito.verifyNoMoreInteractions(onCompleted);
        
        // this should be filtered
        events.publish("the");
        Mockito.verifyNoMoreInteractions(onCompleted);
    }
    
    @Test
    public void testDisposeUnsubscribesObserver() {
        EventSubject<String> events = EventSubject.create();
        EventStream<String> filteredStream = events.filter(val -> val.contains("tac"));
        
        Consumer<String> onEvent = Mockito.mock(Consumer.class);
        Runnable onCompleted = Mockito.mock(Runnable.class);
        
        Subscription subscription = filteredStream.observe(EventObserver.create(onEvent, onCompleted));
        assertFalse(subscription.isDisposed());
        
        events.dispose();
        verify(onCompleted).run();
        assertTrue(subscription.isDisposed());
        Mockito.verifyNoMoreInteractions(onEvent, onCompleted);
    }
    
    @Test
    public void testUnsubscribeRemovesObserver() {
        EventSubject<String> events = EventSubject.create();
        
        assertFalse(events.hasObservers());

        EventStream<String> filteredStream = events.filter(val -> val.contains("tacos"));
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        Subscription subscription = filteredStream.observe(observer);

        assertTrue(events.hasObservers());
        
        subscription.dispose();
        assertFalse(events.hasObservers());
    }
    
    @Test
    public void testSubscribeAfterDisposed() {
        EventSubject<String> events = EventSubject.create();
        EventStream<String> filteredStream = events.filter(val -> val.contains("tacos"));
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        
        events.dispose();
        
        Subscription subscription = filteredStream.observe(observer);

        verify(observer, Mockito.never()).onEvent(Mockito.anyString());
        verify(observer).onCompleted();
        Mockito.verifyNoMoreInteractions(observer);
        
        assertTrue(subscription.isDisposed());
        assertFalse(events.hasObservers());
    }
}
