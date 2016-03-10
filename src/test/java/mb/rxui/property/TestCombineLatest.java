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
package mb.rxui.property;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;
import mb.rxui.subscription.Subscription;

@RunWith(SwingTestRunner.class)
public class TestCombineLatest {
    @Test
    public void testCombineLatest2() throws Exception {
        Property<String> property = Property.create("tacos");
        Property<Integer> property2 = Property.create(15);
        
        PropertyStream<String> stream = 
            PropertyStream.combine(property, property2, (food, amount) -> "I ate [" + amount + "] " + food);
        
        PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
        
        assertFalse(property.hasObservers());
        assertFalse(property2.hasObservers());
        
        Subscription subscription = stream.observe(observer);
        verify(observer).onChanged("I ate [15] tacos");
        verifyNoMoreInteractions(observer);
        
        assertTrue(property.hasObservers());
        assertTrue(property2.hasObservers());
        
        property.setValue("burritos");
        verify(observer).onChanged("I ate [15] burritos");
        verifyNoMoreInteractions(observer);
        
        property2.setValue(20);
        verify(observer).onChanged("I ate [20] burritos");
        verifyNoMoreInteractions(observer);
        
        property.dispose();
        property.setValue("tacos");
        verifyNoMoreInteractions(observer);
        
        property2.setValue(30);
        verify(observer).onChanged("I ate [30] burritos");
        verifyNoMoreInteractions(observer);
        
        assertFalse(subscription.isDisposed());
        
        property2.dispose();
        property2.setValue(40);
        verify(observer).onDisposed();
        verifyNoMoreInteractions(observer);
        
        assertTrue(subscription.isDisposed());
    }
    
    @Test
    public void testUnsubscribe() throws Exception {
        Property<String> property = Property.create("tacos");
        Property<Integer> property2 = Property.create(15);
        
        PropertyStream<String> stream = 
            PropertyStream.combine(property, property2, (food, amount) -> "I ate [" + amount + "] " + food);
        
        PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
        
        assertFalse(property.hasObservers());
        assertFalse(property2.hasObservers());
        
        Subscription subscription = stream.observe(observer);
        
        assertTrue(property.hasObservers());
        assertTrue(property2.hasObservers());
        
        subscription.dispose();
        assertFalse(property.hasObservers());
        assertFalse(property2.hasObservers());
    }
}
