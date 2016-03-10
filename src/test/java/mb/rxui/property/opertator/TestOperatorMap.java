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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;
import mb.rxui.property.Property;
import mb.rxui.property.PropertyStream;
import mb.rxui.subscription.Subscription;
import mb.rxui.property.PropertyObserver;

@RunWith(SwingTestRunner.class)
public class TestOperatorMap {
    @Test
    public void testMap() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyStream<Integer> stream = property.map(String::length);
        PropertyObserver<Integer> observer = Mockito.mock(PropertyObserver.class);

        stream.observe(observer);
        
        verify(observer).onChanged(5);
        assertEquals(new Integer(5), stream.get());
        verifyNoMoreInteractions(observer);
        
        property.setValue("burritos");
        verify(observer).onChanged(8);
        assertEquals(new Integer(8), stream.get());
        verifyNoMoreInteractions(observer);
    }
    
    @Test
    public void testDisposeUnsubscribesObserver() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyStream<Integer> stream = property.map(String::length);
        PropertyObserver<Integer> observer = Mockito.mock(PropertyObserver.class);

        Subscription subscription = stream.observe(observer);
        
        verify(observer).onChanged(5);
        
        property.dispose();
        verify(observer).onDisposed();
        assertTrue(subscription.isDisposed());
        Mockito.verifyNoMoreInteractions(observer);
    }
    
    @Test
    public void testUnsubscribeRemovesObserver() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyStream<Integer> stream = property.map(String::length);
        PropertyObserver<Integer> observer = Mockito.mock(PropertyObserver.class);

        assertFalse(property.hasObservers());

        Subscription subscription = stream.observe(observer);
        assertTrue(property.hasObservers());
        
        subscription.dispose();
        assertFalse(property.hasObservers());
    }
    
    @Test
    public void testSubscribeAfterDispose() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyStream<Integer> stream = property.map(String::length);
        PropertyObserver<Integer> observer = Mockito.mock(PropertyObserver.class);
        
        property.dispose();
        
        Subscription subscription = stream.observe(observer);
        verify(observer).onChanged(5);
        verify(observer).onDisposed();
        Mockito.verifyNoMoreInteractions(observer);
        
        assertFalse(property.hasObservers());
        assertTrue(subscription.isDisposed());
    }
    
    @Test
    public void testOnlyEmitsIfValueChanged() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyObserver<Integer> observer = Mockito.mock(PropertyObserver.class);
        
        property.map(String::length).observe(observer);
        Mockito.verify(observer).onChanged(5);
        
        property.setValue("house");
        Mockito.verifyNoMoreInteractions(observer);
    }
}
