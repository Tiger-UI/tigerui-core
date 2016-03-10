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
package mb.rxui.subscription;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import mb.rxui.subscription.CompositeSubscription;
import mb.rxui.subscription.Subscription;

public class TestCompositeSubscription {
    
    @Test
    public void testDispose() {
        Subscription subscription1 = Mockito.mock(Subscription.class);
        Subscription subscription2 = Mockito.mock(Subscription.class);
        Subscription subscription3 = Mockito.mock(Subscription.class);
        
        InOrder inOrder = Mockito.inOrder(subscription1, subscription2, subscription3);
        
        CompositeSubscription subscriptions = new CompositeSubscription();
        subscriptions.add(subscription1);
        subscriptions.add(subscription2);
        subscriptions.add(subscription3);
        
        assertFalse(subscriptions.isDisposed());
        
        subscriptions.dispose();
        assertTrue(subscriptions.isDisposed());
        inOrder.verify(subscription1).dispose();
        inOrder.verify(subscription2).dispose();
        inOrder.verify(subscription3).dispose();
        inOrder.verifyNoMoreInteractions();
    }
    
    @Test
    public void testCallingDisposeTwice() {
        
        Subscription subscription = Mockito.mock(Subscription.class);
        
        CompositeSubscription subscriptions = new CompositeSubscription();
        subscriptions.add(subscription);
        
        // calling dispose twice calls dispose on the composed subscriptions inly once
        subscriptions.dispose();
        subscriptions.dispose();
        Mockito.verify(subscription).dispose();
    }
    
    @Test
    public void testAddAfterDiposedDisposesSubscription() throws Exception {
        
        Subscription subscription = Mockito.mock(Subscription.class);
        
        CompositeSubscription subscriptions = new CompositeSubscription();
        subscriptions.dispose();
        
        // if the composite subscription is already disposed, calling add disposes the subscription.
        subscriptions.add(subscription);
        Mockito.verify(subscription).dispose();
    }
    
    @Test
    public void testCreateFromList() throws Exception {
        Subscription subscription1 = Mockito.mock(Subscription.class);
        Subscription subscription2 = Mockito.mock(Subscription.class);
        Subscription subscription3 = Mockito.mock(Subscription.class);
        
        InOrder inOrder = Mockito.inOrder(subscription1, subscription2, subscription3);
        
        CompositeSubscription subscriptions = new CompositeSubscription(Arrays.asList(subscription1, subscription2, subscription3));
        
        assertFalse(subscriptions.isDisposed());
        
        subscriptions.dispose();
        assertTrue(subscriptions.isDisposed());
        inOrder.verify(subscription1).dispose();
        inOrder.verify(subscription2).dispose();
        inOrder.verify(subscription3).dispose();
        inOrder.verifyNoMoreInteractions();
    }
    
    @Test
    public void testCreateFromVarArgs() throws Exception {
        Subscription subscription1 = Mockito.mock(Subscription.class);
        Subscription subscription2 = Mockito.mock(Subscription.class);
        Subscription subscription3 = Mockito.mock(Subscription.class);
        
        InOrder inOrder = Mockito.inOrder(subscription1, subscription2, subscription3);
        
        CompositeSubscription subscriptions = new CompositeSubscription(subscription1, subscription2, subscription3);
        
        assertFalse(subscriptions.isDisposed());
        
        subscriptions.dispose();
        assertTrue(subscriptions.isDisposed());
        inOrder.verify(subscription1).dispose();
        inOrder.verify(subscription2).dispose();
        inOrder.verify(subscription3).dispose();
        inOrder.verifyNoMoreInteractions();
    }
}
