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

import static mb.rxui.ThreadedTestHelper.callOnIoThread;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;
import mb.rxui.subscription.Subscription;

@RunWith(SwingTestRunner.class)
public class TestEventSubject {
    @Test
    public void testPublish() {
        EventSubject<String> subject = EventSubject.create();
        
        Consumer<String> eventHandler = Mockito.mock(Consumer.class);
        subject.onEvent(eventHandler);
        
        subject.publish("tacos");
        Mockito.verify(eventHandler).accept("tacos");
    }
    
    @Test
    public void testSubscribeOnEventAfterDispose() {
        EventSubject<String> subject = EventSubject.create();
        
        subject.dispose();
        
        Consumer<String> eventHandler = Mockito.mock(Consumer.class);
        Subscription subscription = subject.onEvent(eventHandler);
        assertTrue(subscription.isDisposed());
        
        subject.publish("tacos");
        Mockito.verify(eventHandler, Mockito.never()).accept("tacos");
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCallPublishOnWringThread() throws Exception {
        EventSubject<String> subject = callOnIoThread(() -> EventSubject.<String>create());
        
        subject.publish("tacos");
    }
    
    @Test
    public void testSubscriberOnCompletedAfterDispose() {
        EventSubject<String> subject = EventSubject.create();
        
        subject.dispose();
        
        Runnable onCompleted = Mockito.mock(Runnable.class);
        Subscription subscription = subject.onCompleted(onCompleted);
        assertTrue(subscription.isDisposed());
        
        Mockito.verify(onCompleted).run();
    }
    
    @Test
    public void testDispose() {
        EventSubject<String> subject = EventSubject.create();
        
        Runnable onCompletedHandler = Mockito.mock(Runnable.class);
        subject.onCompleted(onCompletedHandler);
        
        subject.dispose();
        Mockito.verify(onCompletedHandler).run();
        assertFalse(subject.hasObservers());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCallDisposeOnWringThread() throws Exception {
        EventSubject<String> subject = callOnIoThread(() -> EventSubject.<String>create());
        
        subject.dispose();
    }
    
    @Test
    public void testHasObservers() {
        EventSubject<String> subject = EventSubject.create();
        assertFalse(subject.hasObservers());
        
        subject.onCompleted(()->{});
        assertTrue(subject.hasObservers());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCallHasObserversOnWringThread() throws Exception {
        EventSubject<String> subject = callOnIoThread(() -> EventSubject.<String>create());
        
        subject.hasObservers();
    }
    
    @Test(timeout=1000)
    public void testReentrancyBlocked() {
        EventSubject<String> subject = EventSubject.create();
        
        Consumer<String> eventHandler = Mockito.mock(Consumer.class);
        subject.onEvent(eventHandler);
        
        // setup the reentrant call
        subject.onEvent(subject::publish);
        
        subject.publish("tacos");
        
        Mockito.verify(eventHandler).accept("tacos");
    }
}
