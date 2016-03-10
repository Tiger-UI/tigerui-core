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

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;
import mb.rxui.event.EventSequenceGenerator;
import mb.rxui.subscription.Subscription;
import mb.rxui.event.EventObserver;

@RunWith(SwingTestRunner.class)
public class TestPropertyChangeEvents {
    
    @Before
    public void setup() {
        EventSequenceGenerator.getInstance().reset();
    }
    
    @Test
    public void testPropertyChangeEvents() throws Exception {
        Property<String> property = Property.create("tacos");
        Property<Integer> property2 = Property.create(10);
        
        // subscribing to property change events should not emit a change event until the value changes from the initial value.
        EventObserver<PropertyChangeEvent<String>> changeEventsObserver = Mockito.mock(EventObserver.class);
        property.changeEvents().observe(changeEventsObserver);
        verifyNoMoreInteractions(changeEventsObserver);

        // subscribing to property change events should not emit a change event until the value changes from the initial value.
        EventObserver<PropertyChangeEvent<Integer>> changeEventsObserver2 = Mockito.mock(EventObserver.class);
        property2.changeEvents().observe(changeEventsObserver2);
        verifyNoMoreInteractions(changeEventsObserver2);
        
        property.setValue("burritos");
        verify(changeEventsObserver).onEvent(new PropertyChangeEvent<>("tacos", "burritos", 0));
        verifyNoMoreInteractions(changeEventsObserver);
        
        property2.setValue(20);
        verify(changeEventsObserver2).onEvent(new PropertyChangeEvent<>(10, 20, 1));
        verifyNoMoreInteractions(changeEventsObserver2);
        
        property.dispose();
        property2.dispose();
        verify(changeEventsObserver).onCompleted();
        verify(changeEventsObserver2).onCompleted();
    }
    
    @Test
    public void testUnsubscribePropertyChangeEvents() throws Exception {
        Property<String> property = Property.create("tacos");
        
        EventObserver<PropertyChangeEvent<String>> changeEventsObserver = Mockito.mock(EventObserver.class);
        Subscription subscription = property.changeEvents().observe(changeEventsObserver);
        verifyNoMoreInteractions(changeEventsObserver);
        
        property.setValue("burritos");
        verify(changeEventsObserver).onEvent(new PropertyChangeEvent<>("tacos", "burritos", 0));
        
        // verify that unsubscribing and re-subscribing does not cancel the property changed event stream
        subscription.dispose();
        EventObserver<PropertyChangeEvent<String>> changeEventsObserver2 = Mockito.mock(EventObserver.class);
        property.changeEvents().observe(changeEventsObserver2);
        verifyNoMoreInteractions(changeEventsObserver2);
        
        property.setValue("tacos");
        verify(changeEventsObserver2).onEvent(new PropertyChangeEvent<>("burritos", "tacos", 1));
    }
}
