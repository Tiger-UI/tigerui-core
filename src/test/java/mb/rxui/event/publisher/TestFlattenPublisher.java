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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
public class TestFlattenPublisher {
    private EventSubject<String> events1;
    private EventSubject<String> events2;
    private EventSubject<String> events3;
    private EventSubject<EventStream<String>> streamOfStreams;
    private EventStream<String> flattenedStream;
    @Mock private EventObserver<String> observer;
    
    @Before
    public void setup() throws Exception {
        SwingUtilities.invokeAndWait(() -> {            
            MockitoAnnotations.initMocks(this);
            
            events1 = EventSubject.create();
            events2 = EventSubject.create();
            events3 = EventSubject.create();
            
            streamOfStreams = EventSubject.create();
            flattenedStream = EventStream.flatten(streamOfStreams);
        });
    }
    
    @Test
    public void testFlattenSubscribesToNestedStreams() {
        
        // there should be no subscribers, yet.
        assertFalse(events1.hasObservers());
        assertFalse(events2.hasObservers());
        assertFalse(events3.hasObservers());
        
        flattenedStream.observe(observer);
        
        // there should still be no observers
        assertFalse(events1.hasObservers());
        assertFalse(events2.hasObservers());
        assertFalse(events3.hasObservers());
        
        streamOfStreams.publish(events1);
        assertTrue(events1.hasObservers());
        assertFalse(events2.hasObservers());
        assertFalse(events3.hasObservers());
        
        streamOfStreams.publish(events2);
        assertTrue(events1.hasObservers());
        assertTrue(events2.hasObservers());
        assertFalse(events3.hasObservers());
        
        streamOfStreams.publish(events3);
        assertTrue(events1.hasObservers());
        assertTrue(events2.hasObservers());
        assertTrue(events3.hasObservers());
    }
    
    @Test
    public void testEmitFromSourceStreams() {
        
        Subscription subscription = flattenedStream.observe(observer);

        streamOfStreams.publish(events1);
        streamOfStreams.publish(events2);
        streamOfStreams.publish(events3);
        
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
    public void testCompleteNestedStreamsDoesNotCompleteFlattenedStream() {
        
        Subscription subscription = flattenedStream.observe(observer);

        streamOfStreams.publish(events1);
        streamOfStreams.publish(events2);
        streamOfStreams.publish(events3);
        
        verify(observer, never()).onEvent(Mockito.any());
        
        // dispose the first stream
        events1.dispose();
        verify(observer, never()).onCompleted();
        assertFalse(subscription.isDisposed());
        
        // after disposing a stream it should not participate in the flatten any more
        events1.publish("tacos");
        verify(observer, never()).onEvent(Mockito.any());
        // after disposing a stream the other remaining streams should participate in the flatten
        events2.publish("tacos");
        verify(observer).onEvent("tacos");
        events3.publish("burritos");
        verify(observer).onEvent("burritos");
        

        // dispose the second stream
        events2.dispose();
        verify(observer, never()).onCompleted();
        assertFalse(subscription.isDisposed());
        
        // after disposing a stream it should not participate in the flatten any more
        events2.publish("nachos");
        verify(observer, never()).onEvent("nachos");
        // after disposing a stream the other remaining streams should participate in the flatten
        events3.publish("enchiladas");
        verify(observer).onEvent("enchiladas");
        
        
        // disposing the last stream should not complete the flatten stream
        events3.dispose();
        verify(observer, never()).onCompleted();
        assertFalse(subscription.isDisposed());
        
        events1.publish("quesadilas");
        events2.publish("quesadilas");
        events3.publish("quesadilas");
        verify(observer, never()).onEvent("quesadilas");
    }
    
    @Test
    public void testUnsubscribeRemovesObserver() {
        Subscription subscription = flattenedStream.observe(observer);

        streamOfStreams.publish(events1);
        streamOfStreams.publish(events2);
        streamOfStreams.publish(events3);

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
    public void testSubscribeAfterNestedStreamsDisposed() {
        
        events1.dispose();
        events2.dispose();
        events3.dispose();
        
        Subscription subscription = flattenedStream.observe(observer);

        streamOfStreams.publish(events1);
        streamOfStreams.publish(events2);
        streamOfStreams.publish(events3);

        // since the event streams where already disposed nothing should have been subscribed to.
        Mockito.verifyNoMoreInteractions(observer);
        
        assertFalse(subscription.isDisposed());
        assertFalse(events1.hasObservers());
        assertFalse(events2.hasObservers());
        assertFalse(events3.hasObservers());
    }
    
    @Test
    public void testSubscribeAfterStreamOfStreamsDisposed() {
        
        streamOfStreams.dispose();
        
        Subscription subscription = flattenedStream.observe(observer);

        streamOfStreams.publish(events1);
        streamOfStreams.publish(events2);
        streamOfStreams.publish(events3);

        // since the stream of streams was already disposed the observer should be completed right away.
        Mockito.verify(observer).onCompleted();
        
        assertTrue(subscription.isDisposed());
        assertFalse(events1.hasObservers());
        assertFalse(events2.hasObservers());
        assertFalse(events3.hasObservers());
    }
    
    @Test
    public void testReentrancyBlocked() {
        flattenedStream.observe(observer);
        streamOfStreams.publish(events1);
        
        flattenedStream.observe(observer);
        flattenedStream.onEvent(events1::publish);
        
        // there should be only one "tacos" event, since the reentrant call should be blocked
        events1.publish("tacos");
        verify(observer).onEvent("tacos");
    }
}
