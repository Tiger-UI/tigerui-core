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
package mb.rxui.property.opertator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;
import mb.rxui.property.Property;
import mb.rxui.property.PropertyStream;
import mb.rxui.subscription.Subscription;
import mb.rxui.property.PropertyObserver;

@RunWith(SwingTestRunner.class)
public class TestOperatorTake {
    @Test
    public void testTake() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyStream<String> stream = property.take(3);
        PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
        
        stream.observe(observer);
        verify(observer).onChanged("tacos");
        assertEquals("tacos", stream.get());
        
        property.setValue("burritos");
        verify(observer).onChanged("burritos");
        assertEquals("burritos", stream.get());
        
        property.setValue("fajitas");
        verify(observer).onChanged("fajitas");
        assertEquals("fajitas", stream.get());
        
        // assert that the observer is notified disposed since the amount to take has be taken
        verify(observer).onDisposed();
        Mockito.verifyNoMoreInteractions(observer);
    }
    
    @Test
    public void testDisposeUnsubscribesObserver() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyStream<String> stream = property.take(3);
        PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
        
        Subscription subscription = stream.observe(observer);
        verify(observer).onChanged("tacos");
        
        property.dispose();
        verify(observer).onDisposed();
        assertTrue(subscription.isDisposed());
        Mockito.verifyNoMoreInteractions(observer);
    }
    
    @Test
    public void testUnsubscribeRemovesObserver() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyStream<String> stream = property.take(3);
        PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
        
        assertFalse(property.hasObservers());

        Subscription subscription = stream.observe(observer);
        verify(observer).onChanged("tacos");
        
        assertTrue(property.hasObservers());
        
        subscription.dispose();
        assertFalse(property.hasObservers());
    }
    
    @Test
    public void testSubscribeAfterDispose() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyStream<String> stream = property.take(3);
        PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
        
        property.dispose();
        
        Subscription subscription = stream.observe(observer);
        verify(observer).onChanged("tacos");
        verify(observer).onDisposed();
        Mockito.verifyNoMoreInteractions(observer);
        
        assertFalse(property.hasObservers());
        assertTrue(subscription.isDisposed());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCannotTake0Elements() throws Exception {
        Property.create(10).take(0);
    }
}
