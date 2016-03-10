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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;
import mb.rxui.event.EventObserver;
import mb.rxui.event.EventStream;
import mb.rxui.event.EventSubject;
import mb.rxui.subscription.Subscription;

@RunWith(SwingTestRunner.class)
public class TestOperatorSwitchMap {
    
    private EventSubject<String> streamA;
    private EventSubject<String> streamB;
    private EventSubject<String> streamC;
    private EventSubject<String> defaultStream;
    private EventSubject<String> streamOfLetters;
    private EventStream<String> switchMapStream;
    
    @Before
    public void setup() throws Exception {
        SwingUtilities.invokeAndWait(() -> {            
            streamA = EventSubject.create();
            streamB = EventSubject.create();
            streamC = EventSubject.create();
            defaultStream = EventSubject.create();
            
            streamOfLetters = EventSubject.create();
            
            switchMapStream = streamOfLetters.switchMap(letter -> {
                if ("A".equals(letter)) return streamA;
                if ("B".equals(letter)) return streamB;
                if ("C".equals(letter)) return streamC;
                return defaultStream;
            });
        });
    }

    @Test
    public void testSwitchMap() {
        
        assertFalse(streamA.hasObservers());
        assertFalse(streamB.hasObservers());
        assertFalse(streamC.hasObservers());
        assertFalse(defaultStream.hasObservers());
        
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        switchMapStream.observe(observer);
        
        assertFalse(streamA.hasObservers());
        assertFalse(streamB.hasObservers());
        assertFalse(streamC.hasObservers());
        assertFalse(defaultStream.hasObservers());
        
        streamA.publish("Aevent");
        streamB.publish("Bevent");
        streamC.publish("Cevent");
        defaultStream.publish("Defaultevent");
        
        Mockito.verify(observer, never()).onEvent(Mockito.any());
        
        streamOfLetters.publish("A");
        
        assertTrue(streamA.hasObservers());
        assertFalse(streamB.hasObservers());
        assertFalse(streamC.hasObservers());
        assertFalse(defaultStream.hasObservers());
        
        streamA.publish("Aevent");
        streamB.publish("Bevent");
        streamC.publish("Cevent");
        defaultStream.publish("Defaultevent");
        
        verify(observer).onEvent("Aevent");
        Mockito.verifyNoMoreInteractions(observer);
        
        streamOfLetters.publish("B");
        
        assertFalse(streamA.hasObservers());
        assertTrue(streamB.hasObservers());
        assertFalse(streamC.hasObservers());
        assertFalse(defaultStream.hasObservers());
        
        streamA.publish("Aevent");
        streamB.publish("Bevent");
        streamC.publish("Cevent");
        defaultStream.publish("Defaultevent");
        
        verify(observer).onEvent("Bevent");
        Mockito.verifyNoMoreInteractions(observer);
        
        streamOfLetters.publish("C");
        
        assertFalse(streamA.hasObservers());
        assertFalse(streamB.hasObservers());
        assertTrue(streamC.hasObservers());
        assertFalse(defaultStream.hasObservers());
        
        streamA.publish("Aevent");
        streamB.publish("Bevent");
        streamC.publish("Cevent");
        defaultStream.publish("Defaultevent");
        
        verify(observer).onEvent("Cevent");
        Mockito.verifyNoMoreInteractions(observer);
    }
    
    @Test
    public void testDisposeUnsubscribesObserver() {
        
        Consumer<String> onEvent = Mockito.mock(Consumer.class);
        Runnable onCompleted = Mockito.mock(Runnable.class);
        
        Subscription subscription = switchMapStream.observe(EventObserver.create(onEvent, onCompleted));
        assertFalse(subscription.isDisposed());
        
        streamOfLetters.dispose();
        verify(onCompleted).run();
        assertTrue(subscription.isDisposed());
        Mockito.verifyNoMoreInteractions(onEvent, onCompleted);
    }
    
    @Test
    public void testUnsubscribeRemovesObserver() throws Exception {
        assertFalse(streamOfLetters.hasObservers());

        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        Subscription subscription = switchMapStream.observe(observer);

        assertFalse(streamA.hasObservers());

        streamOfLetters.publish("A");

        assertTrue(streamOfLetters.hasObservers());
        assertTrue(streamA.hasObservers());

        subscription.dispose();
        assertFalse(streamOfLetters.hasObservers());
        assertFalse(streamA.hasObservers());
    }
    
    @Test
    public void testSubscribeAfterDisposed() {
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        
        streamOfLetters.dispose();
        
        Subscription subscription = switchMapStream.observe(observer);

        verify(observer, Mockito.never()).onEvent(Mockito.anyString());
        verify(observer).onCompleted();
        Mockito.verifyNoMoreInteractions(observer);
        
        assertTrue(subscription.isDisposed());
        assertFalse(streamOfLetters.hasObservers());
    }
    
    @Test
    public void testReentranceBlocked() {
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        
        switchMapStream.observe(observer);
        // this reentrant call should be blocked.
        switchMapStream.onEvent(event -> streamA.publish("burritos"));
        
        streamOfLetters.publish("A");
        streamA.publish("tacos");
        
        verify(observer).onEvent("tacos");
        verify(observer, never()).onEvent("burritos");
    }
}
