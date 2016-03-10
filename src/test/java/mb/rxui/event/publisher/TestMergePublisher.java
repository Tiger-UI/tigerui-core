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
package mb.rxui.event.publisher;

import static org.junit.Assert.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import mb.rxui.SwingTestRunner;
import mb.rxui.event.EventObserver;
import mb.rxui.event.EventStream;
import mb.rxui.event.EventSubject;
import mb.rxui.subscription.Subscription;

@RunWith(SwingTestRunner.class)
public class TestMergePublisher {
    
    private EventSubject<String> events1;
    private EventSubject<String> events2;
    private EventSubject<String> events3;
    private EventStream<String> mergedStream;
    @Mock private EventObserver<String> observer;
    
    @Before
    public void setup() throws Exception {
        SwingUtilities.invokeAndWait(() -> {            
            MockitoAnnotations.initMocks(this);
            events1 = EventSubject.create();
            events2 = EventSubject.create();
            events3 = EventSubject.create();
            
            mergedStream = EventStream.merge(events1, events2, events3);
        });
    }
    
    @Test
    public void testMergeSubscribesToAllStreams() {
        
        // there should be no subscribers, yet.
        assertFalse(events1.hasObservers());
        assertFalse(events2.hasObservers());
        assertFalse(events3.hasObservers());
        
        mergedStream.observe(observer);
        
        // each source stream should have been subscribed to
        assertTrue(events1.hasObservers());
        assertTrue(events2.hasObservers());
        assertTrue(events3.hasObservers());
    }

    @Test
    public void testEmitFromSourceStreams() {
        
        Subscription subscription = mergedStream.observe(observer);
        assertFalse(subscription.isDisposed());
        verifyNoMoreInteractions(observer);
        
        events1.publish("tacos");
        Mockito.verify(observer).onEvent("tacos");
        
        events2.publish("burritos");
        Mockito.verify(observer).onEvent("burritos");
        
        events3.publish("fajitas");
        verify(observer).onEvent("fajitas");
        verifyNoMoreInteractions(observer);
    }
    
    @Test
    public void testCompleteAllSourcesUnsubscribesSubscriber() {
        
        Subscription subscription = mergedStream.observe(observer);
        assertFalse(subscription.isDisposed());
        
        verify(observer, never()).onEvent(Mockito.any());
        
        // dispose the first stream
        events1.dispose();
        verify(observer, never()).onCompleted();
        assertFalse(subscription.isDisposed());
        
        // after disposing a stream it should not participate in the merge any more
        events1.publish("tacos");
        verify(observer, never()).onEvent(Mockito.any());
        // after disposing a stream the other remaining streams should participate in the merge
        events2.publish("tacos");
        verify(observer).onEvent("tacos");
        events3.publish("burritos");
        verify(observer).onEvent("burritos");
        

        // dispose the second stream
        events2.dispose();
        verify(observer, never()).onCompleted();
        assertFalse(subscription.isDisposed());
        
        // after disposing a stream it should not participate in the merge any more
        events2.publish("nachos");
        verify(observer, never()).onEvent("nachos");
        // // after disposing a stream the other remaining streams should participate in the merge
        events3.publish("enchiladas");
        verify(observer).onEvent("enchiladas");
        
        
        // disposing the last stream should complete and unsubscribe the merged stream
        events3.dispose();
        verify(observer).onCompleted();
        assertTrue(subscription.isDisposed());
        
        events1.publish("quesadilas");
        events2.publish("quesadilas");
        events3.publish("quesadilas");
        verify(observer, never()).onEvent("quesadilas");
    }
    
    @Test
    public void testUnsubscribeRemovesObserver() {
        Subscription subscription = mergedStream.observe(observer);

        // each source stream should have been subscribed to
        assertTrue(events1.hasObservers());
        assertTrue(events2.hasObservers());
        assertTrue(events3.hasObservers());
        
        subscription.dispose();
        
        // each source stream should have be unsubscribed
        assertFalse(events1.hasObservers());
        assertFalse(events2.hasObservers());
        assertFalse(events3.hasObservers());
    }
    
    @Test
    public void testSubscribeAfterDisposed() {
        
        events1.dispose();
        events2.dispose();
        events3.dispose();
        
        Subscription subscription = mergedStream.observe(observer);

        verify(observer, never()).onEvent(Mockito.any());
        verify(observer).onCompleted();
        Mockito.verifyNoMoreInteractions(observer);
        
        assertTrue(subscription.isDisposed());
        assertFalse(events1.hasObservers());
        assertFalse(events2.hasObservers());
        assertFalse(events3.hasObservers());
    }
    
    @Test
    public void testReentrancyBlocked() {
        mergedStream.observe(observer);
        mergedStream.onEvent(events1::publish);
        
        // there should be only one "tacos" event, since the reentrant call should be blocked
        events1.publish("tacos");
        verify(observer).onEvent("tacos");
    }
}
