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
public class TestOperatorIsDirty {

    @Test
    public void testIsDirty() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyStream<Boolean> isDirty = property.isDirty();
        PropertyObserver<Boolean> observer = Mockito.mock(PropertyObserver.class);
        
        isDirty.observe(observer);
        verify(observer).onChanged(false);
        assertEquals(false, isDirty.get());
        
        property.setValue("burritos");
        verify(observer).onChanged(true);
        assertEquals(true, isDirty.get());
        
        Mockito.verifyNoMoreInteractions(observer);
    }
    
    @Test
    public void testDisposeUnsubscribesObserver() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyStream<Boolean> isDirty = property.isDirty();
        PropertyObserver<Boolean> observer = Mockito.mock(PropertyObserver.class);
        
        Subscription subscription = isDirty.observe(observer);
        verify(observer).onChanged(false);
        
        property.dispose();
        verify(observer).onDisposed();
        assertTrue(subscription.isDisposed());
        Mockito.verifyNoMoreInteractions(observer);
    }
    
    @Test
    public void testUnsubscribeRemovesObserver() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyStream<Boolean> isDirty = property.isDirty();
        PropertyObserver<Boolean> observer = Mockito.mock(PropertyObserver.class);
        
        assertFalse(property.hasObservers());
        
        Subscription subscription = isDirty.observe(observer);
        assertTrue(property.hasObservers());
        
        subscription.dispose();
        assertFalse(property.hasObservers());
    }
    
    @Test
    public void testSubscribeAfterDispose() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyStream<Boolean> stream = property.isDirty();
        PropertyObserver<Boolean> observer = Mockito.mock(PropertyObserver.class);
        
        property.dispose();
        
        Subscription subscription = stream.observe(observer);
        verify(observer).onChanged(false);
        verify(observer).onDisposed();
        Mockito.verifyNoMoreInteractions(observer);
        
        assertFalse(property.hasObservers());
        assertTrue(subscription.isDisposed());
    }
    
    @Test
    public void testEmtisOnlyIfValueChanged() throws Exception {
        Property<String> property = Property.create("tacos");
        PropertyObserver<Boolean> observer = Mockito.mock(PropertyObserver.class);
        
        property.isDirty().observe(observer);
        
        Mockito.verify(observer).onChanged(false);
        
        property.setValue("burritos");
        Mockito.verify(observer).onChanged(true);
        
        property.setValue("fajitas");
        Mockito.verifyNoMoreInteractions(observer);
    }
}
