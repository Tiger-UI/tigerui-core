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
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;
import mb.rxui.event.EventObserver;
import mb.rxui.event.EventStream;
import mb.rxui.event.EventSubject;
import mb.rxui.subscription.Subscription;

@RunWith(SwingTestRunner.class)
public class TestOperatorScan {
    @Test
    public void testScan() {
        EventSubject<String> events = EventSubject.create();
        EventStream<Integer> scannedStream = events.scan((string, last) -> (string.length() + last), 5);
        
        Consumer<Integer> onEvent = Mockito.mock(Consumer.class);
        Runnable onCompleted = Mockito.mock(Runnable.class);
        
        Subscription subscription = scannedStream.observe(EventObserver.create(onEvent, onCompleted));
        assertFalse(subscription.isDisposed());
        Mockito.verify(onEvent).accept(5);
        
        events.publish("tacos");
        Mockito.verify(onEvent).accept(10);
        
        events.publish("burritos");
        Mockito.verify(onEvent).accept(18);
        
        // Just double checking that an event stream does not suppress consecutive duplicates values.
        events.publish("burritos");
        Mockito.verify(onEvent).accept(26);
        Mockito.verifyNoMoreInteractions(onCompleted);
    }
    
    @Test
    public void testDisposeUnsubscribesObserver() {
        EventSubject<String> events = EventSubject.create();
        EventStream<Integer> scannedStream = events.scan((value, last) -> value.length(), 0);
        
        Consumer<Integer> onEvent = Mockito.mock(Consumer.class);
        Runnable onCompleted = Mockito.mock(Runnable.class);
        
        Subscription subscription = scannedStream.observe(EventObserver.create(onEvent, onCompleted));
        assertFalse(subscription.isDisposed());
        verify(onEvent).accept(0);
        
        events.dispose();
        verify(onCompleted).run();
        assertTrue(subscription.isDisposed());
        Mockito.verifyNoMoreInteractions(onEvent, onCompleted);
    }
    
    @Test
    public void testUnsubscribeRemovesObserver() {
        EventSubject<String> events = EventSubject.create();
        
        assertFalse(events.hasObservers());

        EventStream<Integer> scannedStream = events.scan((value, last) -> value.length(), 0);
        EventObserver<Integer> observer = Mockito.mock(EventObserver.class);
        Subscription subscription = scannedStream.observe(observer);

        assertTrue(events.hasObservers());
        
        subscription.dispose();
        assertFalse(events.hasObservers());
    }
    
    @Test
    public void testSubscribeAfterDisposed() {
        EventSubject<String> events = EventSubject.create();
        EventStream<Integer> scannedStream = events.scan((value, last) -> value.length(), 0);
        EventObserver<Integer> observer = Mockito.mock(EventObserver.class);
        
        events.dispose();
        
        Subscription subscription = scannedStream.observe(observer);

        verify(observer).onEvent(0);
        verify(observer).onCompleted();
        Mockito.verifyNoMoreInteractions(observer);
        
        assertTrue(subscription.isDisposed());
        assertFalse(events.hasObservers());
    }
}
