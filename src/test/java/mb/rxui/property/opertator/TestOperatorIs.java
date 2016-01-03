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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

import mb.rxui.property.Property;
import mb.rxui.property.PropertyObservable;
import mb.rxui.property.PropertyObserver;
import mb.rxui.property.Subscription;
import mb.rxui.property.operator.OperatorIs;

public class TestOperatorIs {
    
    @Test
    public void testMatchesIsPredicate() throws Exception {
        Property<String> property = Property.create("tacos");

        assertFalse(property.hasObservers());
        
        PropertyObservable<Boolean> isObservable = property.lift(new OperatorIs<>("tacos"));
        PropertyObserver<Boolean> propertyObserver = Mockito.mock(PropertyObserver.class);
        Subscription subscription = isObservable.observe(propertyObserver);
        
        assertTrue(property.hasObservers());
        
        Mockito.verify(propertyObserver).onChanged(true);
        
        property.setValue("fajitas");
        Mockito.verify(propertyObserver).onChanged(false);
        
        subscription.dispose();
        
        assertFalse(property.hasObservers());
    }
    
    @Test
    public void testMacthOnManyPredicate() throws Exception {
        Property<String> property = Property.create("tacos");

        PropertyObservable<Boolean> isObservable = property.lift(new OperatorIs<>("tacos", "fajitas"));
        PropertyObserver<Boolean> propertyObserver = Mockito.mock(PropertyObserver.class);
        isObservable.observe(propertyObserver);
        
        Mockito.verify(propertyObserver).onChanged(true);
        
        property.setValue("fajitas");
        Mockito.verifyNoMoreInteractions(propertyObserver);
        
        property.setValue("burritos");
        Mockito.verify(propertyObserver).onChanged(false);
    }
    
    @Test
    public void testSubscribeAfterDispose() throws Exception {
        Property<String> property = Property.create("tacos");
        
        assertFalse(property.hasObservers());

        PropertyObservable<Boolean> isObservable = property.lift(new OperatorIs<>("tacos"));
        
        property.dispose();
        
        PropertyObserver<Boolean> propertyObserver = Mockito.mock(PropertyObserver.class);
        Subscription subscription = isObservable.observe(propertyObserver);
        
        assertFalse(property.hasObservers());
        
        Mockito.verify(propertyObserver).onChanged(true);
        Mockito.verify(propertyObserver).onDisposed();
        assertTrue(subscription.isDisposed());
    }
}
