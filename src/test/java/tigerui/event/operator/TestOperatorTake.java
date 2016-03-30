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
public class TestOperatorTake {
    @Test
    public void testTake() {
        EventSubject<String> events = EventSubject.create();
        EventStream<String> takeStream = events.take(2);
        
        Consumer<String> onEvent = Mockito.mock(Consumer.class);
        Runnable onCompleted = Mockito.mock(Runnable.class);
        
        Subscription subscription = takeStream.observe(EventObserver.create(onEvent, onCompleted));
        assertFalse(subscription.isDisposed());
        
        events.publish("tacos");
        Mockito.verify(onEvent).accept("tacos");
        
        events.publish("burritos");
        Mockito.verify(onEvent).accept("burritos");
        verify(onCompleted).run();
        
        events.publish("fajitas");
        verifyNoMoreInteractions(onEvent, onCompleted);
    }
    
    @Test
    public void testDisposeUnsubscribesObserver() {
        EventSubject<String> events = EventSubject.create();
        EventStream<String> takeStream = events.take(1);
        
        Consumer<String> onEvent = Mockito.mock(Consumer.class);
        Runnable onCompleted = Mockito.mock(Runnable.class);
        
        Subscription subscription = takeStream.observe(EventObserver.create(onEvent, onCompleted));
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

        EventStream<String> takeStream = events.take(1);
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        Subscription subscription = takeStream.observe(observer);

        assertTrue(events.hasObservers());
        
        subscription.dispose();
        assertFalse(events.hasObservers());
    }
    
    @Test
    public void testSubscribeAfterDisposed() {
        EventSubject<String> events = EventSubject.create();
        EventStream<String> takeStream = events.take(1);
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        
        events.dispose();
        
        Subscription subscription = takeStream.observe(observer);

        verify(observer, Mockito.never()).onEvent(Mockito.anyString());
        verify(observer).onCompleted();
        Mockito.verifyNoMoreInteractions(observer);
        
        assertTrue(subscription.isDisposed());
        assertFalse(events.hasObservers());
    }
}
