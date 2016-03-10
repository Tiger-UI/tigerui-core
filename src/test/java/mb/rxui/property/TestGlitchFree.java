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

import static mb.rxui.property.PropertyStream.combine;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;
import mb.rxui.event.EventSubject;

@RunWith(SwingTestRunner.class)
public class TestGlitchFree {
    
    @Test
    public void testNoGlitchFromEventStream() {
        EventSubject<Integer> integers = EventSubject.create();
        Property<Integer> property1 = Property.create(0);
        Property<Integer> property2 = Property.create(0);
        
        property1.bind(integers);
        property2.bind(integers);
        
        PropertyStream<Integer> combinedProperty = 
                Property.combine(property1, property2, (a, b) -> a + b);
        
        Consumer<Integer> consumer = Mockito.mock(Consumer.class);
        combinedProperty.onChanged(consumer);
        
        verify(consumer).accept(0);
        
        integers.publish(2);

        // Assert that no '2' change was notified, since that would be a glitch.
        verify(consumer).accept(4);
        verifyNoMoreInteractions(consumer);
    }
    
    @Test
    public void testNoGlitchFromProperties() {
        Property<Integer> property = Property.create(0);
        
        PropertyStream<Integer> propertyTimes2 = property.map(value -> value * 2);
        PropertyStream<Integer> propertyTimes4 = property.map(value -> value * 4);
        
        PropertyStream<Integer> combinedProperty = combine(propertyTimes2, propertyTimes4, (a, b) -> a + b);
        
        Consumer<Integer> consumer = Mockito.mock(Consumer.class);
        InOrder inOrder = Mockito.inOrder(consumer);

        combinedProperty.onChanged(consumer);
        
        inOrder.verify(consumer).accept(0);
        
        property.setValue(2);
        
        // Assert that no '2' change was notified, since that would be a glitch.
        inOrder.verify(consumer).accept(12);
        inOrder.verifyNoMoreInteractions();
    }
    
    @Test
    public void testNoGlitchThroughPropertyBinding() {
        Property<Integer> sourceProperty = Property.create(0);
        Property<Integer> property = Property.create(0);
        Property<Integer> property2 = Property.create(0);
        
        property.bind(sourceProperty);
        property2.bind(sourceProperty);
        
        PropertyStream<Integer> combinedProperty = combine(property, property2, (a, b) -> a + b);
        Consumer<Integer> consumer = Mockito.mock(Consumer.class);
        InOrder inOrder = Mockito.inOrder(consumer);
        
        combinedProperty.onChanged(consumer);
        inOrder.verify(consumer).accept(0);


        PropertyStream<Integer> combinedTimes2 = combinedProperty.map(value -> value * 2);
        Consumer<Integer> consumer2 = Mockito.mock(Consumer.class);
        InOrder inOrder2 = Mockito.inOrder(consumer2);

        combine(combinedTimes2, property, (a, b) -> a + b).onChanged(consumer2);
        inOrder2.verify(consumer2).accept(0);
        
        
        sourceProperty.setValue(2);
        
        // Assert that no '2' change was notified, since that would be a glitch.
        inOrder.verify(consumer).accept(4);
        inOrder.verifyNoMoreInteractions();
        
        inOrder2.verify(consumer2).accept(10);
    }
}
