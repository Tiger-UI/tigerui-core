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
import static org.junit.Assert.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;
import mb.rxui.property.Property;
import mb.rxui.property.PropertyObserver;
import mb.rxui.subscription.Subscription;
import rx.Notification;
import rx.Observable;
import rx.Observer;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

@RunWith(SwingTestRunner.class)
public class TestEventStream {
    @Test
    public void testOnEvent() {
        String[] events = new String[] { "tacos", "burritos", "fajitas" };
        EventStream<String> stream = EventStream.fromArray("tacos", "burritos", "fajitas");
        
        Consumer<String> eventHandler = Mockito.mock(Consumer.class);
        InOrder inOrder = Mockito.inOrder(eventHandler);

        Subscription subscription = stream.onEvent(eventHandler);
        
        Arrays.asList(events).forEach(event -> inOrder.verify(eventHandler).accept(event));
        inOrder.verifyNoMoreInteractions();
        
        Assert.assertTrue(subscription.isDisposed());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCallOnEventOnWrongThread() throws Exception {
        EventStream<String> stream = callOnIoThread(() -> EventStream.fromArray());
        
        stream.onEvent(Mockito.mock(Consumer.class));
        fail("Trying to call onEvent should have thrown since this is the wrong thread.");
    }

    @Test
    public void testOnCompleted() {
        EventStream<String> stream = EventStream.fromArray();
        
        Runnable onCompleted = Mockito.mock(Runnable.class);
        stream.onCompleted(onCompleted);
        
        Mockito.verify(onCompleted).run();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCallOnCompletedOnWrongThread() throws Exception {
        EventStream<String> stream = callOnIoThread(() -> EventStream.fromArray());
        
        stream.onCompleted(Mockito.mock(Runnable.class));
        fail("Trying to call onCompleted should have thrown since this is the wrong thread.");
    }
    
    @Test
    public void testObserve() {
        String[] events = new String[] { "tacos", "burritos", "fajitas" };
        
        EventStream<String> stream = EventStream.fromArray(events);
        
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        InOrder inOrder = Mockito.inOrder(observer);

        Subscription subscription = stream.observe(observer);
        
        inOrder.verify(observer).onEvent("tacos");
        inOrder.verify(observer).onEvent("burritos");
        inOrder.verify(observer).onCompleted();
        inOrder.verifyNoMoreInteractions();
        
        Assert.assertTrue(subscription.isDisposed());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCallObserveOnWrongThread() throws Exception {
        EventStream<String> stream = callOnIoThread(() -> EventStream.fromArray());
        
        stream.observe(Mockito.mock(EventObserver.class));
        fail("Trying to call observe should have thrown since this is the wrong thread.");
    }
    
    @Test
    public void testToProperty() {
        EventStream<String> stream = EventStream.fromArray("fajitas");
        
        Property<String> property = stream.toProperty("tacos");
        PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
        
        InOrder inOrder = Mockito.inOrder(observer);
        
        Subscription subscription = property.observe(observer);
        
        inOrder.verify(observer).onChanged("fajitas");
        inOrder.verifyNoMoreInteractions();
        
        assertFalse(subscription.isDisposed());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCallToPropertyOnWrongThread() throws Exception {
        EventStream<String> stream = callOnIoThread(() -> EventStream.fromArray());
        
        stream.toProperty("burritos");
        fail("Trying to call toProperty should have thrown since this is the wrong thread.");
    }
    
    @Test
    public void testAsObservable() {
        EventStream<String> stream = EventStream.fromArray("tacos", "burritos", "fajitas");
        
        Observable<String> observable = stream.asObservable();
        
        Observer<String> observer = Mockito.mock(Observer.class);
        InOrder inOrder = Mockito.inOrder(observer);
        
        rx.Subscription subscription = observable.subscribe(observer);
        
        inOrder.verify(observer).onNext("tacos");
        inOrder.verify(observer).onNext("burritos");
        inOrder.verify(observer).onNext("fajitas");
        inOrder.verify(observer).onCompleted();
        inOrder.verifyNoMoreInteractions();
        
        assertTrue(subscription.isUnsubscribed());
    }
    
    @Test
    public void testCallSubscribeOnAnObservableFromWrongThread() throws Exception {
        EventStream<String> stream = EventStream.fromArray();

        Throwable exception = callOnIoThread(() -> {            
            return stream.asObservable()
                         .materialize()
                         .filter(Notification::isOnError)
                         .map(Notification::getThrowable)
                         .toBlocking().first();
            });
        
        assertEquals(IllegalStateException.class, exception.getClass());
    }
    
    @Test
    public void testFromObservable() {
        EventStream<String> stream = EventStream.from(Observable.just("tacos", "burritos", "fajitas"));
        
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        InOrder inOrder = Mockito.inOrder(observer);
        
        Subscription subscription = stream.observe(observer);
        
        inOrder.verify(observer).onEvent("tacos");
        inOrder.verify(observer).onEvent("burritos");
        inOrder.verify(observer).onEvent("fajitas");
        inOrder.verify(observer).onCompleted();
        inOrder.verifyNoMoreInteractions();
        
        assertTrue(subscription.isDisposed());
    }
    
    @Test
    public void testFromPublishSubject() {
        PublishSubject<String> subject = PublishSubject.create();
        EventStream<String> stream = EventStream.from(subject);
        
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        InOrder inOrder = Mockito.inOrder(observer);
        
        assertFalse(subject.hasObservers());
        
        stream.observe(observer);
        
        assertTrue(subject.hasObservers());
        
        subject.onNext("tacos");
        inOrder.verify(observer).onEvent("tacos");
        
        subject.onNext("burritos");
        inOrder.verify(observer).onEvent("burritos");
    }
    
    @Test
    public void testFromPublishSubjectUnsubscribe() {
        PublishSubject<String> subject = PublishSubject.create();
        EventStream<String> stream = EventStream.from(subject);
        
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        
        assertFalse(subject.hasObservers());
        
        Subscription subscription = stream.observe(observer);
        assertTrue(subject.hasObservers());
        
        subscription.dispose();
        assertFalse(subject.hasObservers());
    }
    
    @Test
    public void testFromPublishSubjectCompletes() {
        PublishSubject<String> subject = PublishSubject.create();
        EventStream<String> stream = EventStream.from(subject);
        
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        
        assertFalse(subject.hasObservers());
        
        Subscription subscription = stream.observe(observer);
        assertTrue(subject.hasObservers());
        
        subject.onCompleted();
        assertFalse(subject.hasObservers());
        Mockito.verify(observer).onCompleted();
        assertTrue(subscription.isDisposed());
    }
    
    @Test
    public void testFromBehaviorSubject() {
        BehaviorSubject<String> subject = BehaviorSubject.create("tacos");
        EventStream<String> stream = EventStream.from(subject);
        
        EventObserver<String> observer = Mockito.mock(EventObserver.class);
        InOrder inOrder = Mockito.inOrder(observer);
        
        assertFalse(subject.hasObservers());
        
        stream.observe(observer);
        
        assertTrue(subject.hasObservers());
        
        inOrder.verify(observer).onEvent("tacos");
        
        subject.onNext("burritos");
        inOrder.verify(observer).onEvent("burritos");
    }
    
    @Test
    public void testMergeWithIterable() {
        EventSubject<String> events1 = EventSubject.create();
        EventSubject<String> events2 = EventSubject.create();
        EventSubject<String> events3 = EventSubject.create();
        
        List<EventStream<String>> eventStreams = Arrays.asList(events2, events3);
        
        EventStream<String> mergedStreams = events1.mergeWith(eventStreams);
        
        Consumer<String> eventHandler = Mockito.mock(Consumer.class);
        mergedStreams.onEvent(eventHandler);
        
        events1.publish("tacos");
        verify(eventHandler).accept("tacos");
        
        events2.publish("burritos");
        verify(eventHandler).accept("burritos");
        
        events3.publish("nachos");
        verify(eventHandler).accept("nachos");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMergeThrowsExceptionIfCalledWithNoArgs() {
        EventStream.merge();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMergeThrowsExceptionIfCalledWithEmptyArray() {
        EventStream<String>[] streams = new EventStream[0];
        EventStream.merge(streams);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMergeThrowsIfCalledWithAnEmptyList() {
        EventStream.merge(new ArrayList<>());
    }
    
    @Test
    public void testFromIterable() {
        List<String> stringEvents = Arrays.asList("tacos", "burritos", "fajitas");
        
        EventStream<String> eventStream = EventStream.fromIterable(stringEvents);
        
        Consumer<String> onEvent = mock(Consumer.class);
        Runnable onCompleted = mock(Runnable.class);
        
        InOrder inOrder = inOrder(onEvent, onCompleted);
        
        eventStream.observe(onEvent, onCompleted);
        
        inOrder.verify(onEvent).accept("tacos");
        inOrder.verify(onEvent).accept("burritos");
        inOrder.verify(onEvent).accept("fajitas");
        inOrder.verify(onCompleted).run();
        inOrder.verifyNoMoreInteractions();
        
        // a second subscriber should receive the same events
        Consumer<String> onEvent2 = mock(Consumer.class);
        Runnable onCompleted2 = mock(Runnable.class);
        
        InOrder inOrder2 = inOrder(onEvent2, onCompleted2);
        
        eventStream.observe(onEvent2, onCompleted2);
        
        inOrder2.verify(onEvent2).accept("tacos");
        inOrder2.verify(onEvent2).accept("burritos");
        inOrder2.verify(onEvent2).accept("fajitas");
        inOrder2.verify(onCompleted2).run();
        inOrder2.verifyNoMoreInteractions();
    }
    
    @Test
    public void testFromArray() {
        EventStream<String> eventStream = EventStream.fromArray("tacos", "burritos", "fajitas");
        
        Consumer<String> onEvent = mock(Consumer.class);
        Runnable onCompleted = mock(Runnable.class);
        
        InOrder inOrder = inOrder(onEvent, onCompleted);
        
        eventStream.observe(onEvent, onCompleted);
        
        inOrder.verify(onEvent).accept("tacos");
        inOrder.verify(onEvent).accept("burritos");
        inOrder.verify(onEvent).accept("fajitas");
        inOrder.verify(onCompleted).run();
        inOrder.verifyNoMoreInteractions();
    }
}
