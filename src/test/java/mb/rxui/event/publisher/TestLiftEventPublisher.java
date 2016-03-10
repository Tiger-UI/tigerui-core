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

import org.junit.Test;
import org.mockito.Mockito;

import mb.rxui.event.EventObserver;
import mb.rxui.event.EventSubscriber;
import mb.rxui.event.operator.Operator;
import mb.rxui.subscription.Subscription;

public class TestLiftEventPublisher {
    @Test
    public void testSubscribe() {
        Operator<String, Integer> operator = Mockito.mock(Operator.class);
        EventSubscriber<String> parentSubscriber = Mockito.mock(EventSubscriber.class);

        Mockito.when(operator.apply(Mockito.any())).thenReturn(parentSubscriber);
        
        EventPublisher<String> publisher = Mockito.mock(EventPublisher.class);
        Mockito.when(publisher.subscribe(Mockito.any())).thenReturn(new EventSubscriber<>(EventObserver.create(()->{})));
        
        LiftEventPublisher<String, Integer> lifter = new LiftEventPublisher<>(operator, publisher);
        
        EventObserver<Integer> observer = Mockito.mock(EventObserver.class);
        
        lifter.subscribe(observer);
        
        Mockito.verify(operator).apply(Mockito.any(EventSubscriber.class));
        Mockito.verify(publisher).subscribe(parentSubscriber);
    }
    
    @Test
    public void testDiposeUnsubscribesFromSource() {
        Operator<String, Integer> operator = Mockito.mock(Operator.class);
        EventSubscriber<String> parentSubscriber = Mockito.mock(EventSubscriber.class);
        EventSubscriber<String> sourceSubscriber = Mockito.mock(EventSubscriber.class);

        Mockito.when(operator.apply(Mockito.any())).thenReturn(parentSubscriber);
        
        EventPublisher<String> publisher = Mockito.mock(EventPublisher.class);
        Mockito.when(publisher.subscribe(Mockito.any())).thenReturn(sourceSubscriber);
        
        LiftEventPublisher<String, Integer> lifter = new LiftEventPublisher<>(operator, publisher);
        
        EventObserver<Integer> observer = Mockito.mock(EventObserver.class);
        
        Subscription subscription = lifter.subscribe(observer);
        
        subscription.dispose();
        Mockito.verify(sourceSubscriber).dispose();
    }
}
