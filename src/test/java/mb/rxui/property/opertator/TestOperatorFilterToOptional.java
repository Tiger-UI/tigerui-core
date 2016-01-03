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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import mb.rxui.property.Property;
import mb.rxui.property.PropertyObservable;
import mb.rxui.property.PropertyObserver;
import mb.rxui.property.Subscription;

public class TestOperatorFilterToOptional {
    @Test
    public void testFilter() throws Exception {
        Property<String> property = Property.create("taco");
        
        PropertyObserver<Optional<String>> observer = mock(PropertyObserver.class);
        
        PropertyObservable<Optional<String>> observable = property.filterToOptional(value -> value.equals("taco"));
        
        Subscription subscription = observable.observe(observer);
        verify(observer).onChanged(Optional.of("taco"));
        
        property.setValue("burrito");
        assertEquals(observable.get(), Optional.empty());
        verify(observer).onChanged(Optional.empty());
        
        subscription.dispose();
        assertEquals(observable.get(), Optional.empty());
    }
    
    @Test
    public void testDisposeUnsubscribesObserver() throws Exception {
        Property<String> property = Property.create("taco");
        PropertyObservable<Optional<String>> observable = property.filterToOptional(value -> value.equals("taco"));
        PropertyObserver<Optional<String>> observer = Mockito.mock(PropertyObserver.class);
        
        Subscription subscription = observable.observe(observer);
        verify(observer).onChanged(Optional.of("taco"));
        
        property.dispose();
        verify(observer).onDisposed();
        assertTrue(subscription.isDisposed());
        Mockito.verifyNoMoreInteractions(observer);
    }
    
    @Test
    public void testUnsubscribeRemovesObserver() throws Exception {
        Property<String> property = Property.create("taco");
        PropertyObservable<Optional<String>> observable = property.filterToOptional(value -> value.equals("taco"));
        PropertyObserver<Optional<String>> observer = Mockito.mock(PropertyObserver.class);
        
        assertFalse(property.hasObservers());
        
        Subscription subscription = observable.observe(observer);
        verify(observer).onChanged(Optional.of("taco"));
        assertTrue(property.hasObservers());
        
        subscription.dispose();
        assertFalse(property.hasObservers());
    }
    
    @Test
    public void testSubscribeAfterDispose() throws Exception {
        Property<String> property = Property.create("taco");
        PropertyObservable<Optional<String>> observable = property.filterToOptional(value -> value.equals("taco"));
        PropertyObserver<Optional<String>> observer = Mockito.mock(PropertyObserver.class);
        
        property.dispose();
        
        Subscription subscription = observable.observe(observer);
        verify(observer).onChanged(Optional.of("taco"));
        verify(observer).onDisposed();
        Mockito.verifyNoMoreInteractions(observer);
        
        assertFalse(property.hasObservers());
        assertTrue(subscription.isDisposed());
    }
    
    @Test
    public void testOnlyEmitsIfChanged() throws Exception {
        Property<String> property = Property.create("taco");
        
        PropertyObservable<Optional<String>> observable = property.filterToOptional(value -> value.equals("taco"));
        PropertyObserver<Optional<String>> observer = Mockito.mock(PropertyObserver.class);
        
        observable.observe(observer);
        Mockito.verify(observer).onChanged(Optional.of("taco"));
        
        property.setValue("burrito");
        Mockito.verify(observer).onChanged(Optional.empty());
        
        property.setValue("fajita");
        Mockito.verifyNoMoreInteractions(observer);
    }
}
