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
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;
import mb.rxui.property.Property;
import mb.rxui.subscription.Subscription;

@RunWith(SwingTestRunner.class)
public class TestPropertyConditionBuilder {
    @Test
    public void testConditionBuilder() throws Exception {
        Property<String> property = Property.create("tacos");
        
        Runnable action = Mockito.mock(Runnable.class);
        
        Subscription subscription = property.is("tacos").or("burritos").then(action);
        
        verify(action).run();
        verifyNoMoreInteractions(action);
        
        property.setValue("burritos");
        verifyNoMoreInteractions(action);
        
        property.setValue("fajitas");
        verifyNoMoreInteractions(action);
        
        property.dispose();
        assertTrue(subscription.isDisposed());
    }
}
