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

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;

import org.junit.Test;
import org.mockito.Mockito;

import mb.rxui.dispatcher.Dispatcher;
import mb.rxui.dispatcher.PropertyDispatcher;

public class TestModelPropertySource {
    @Test
    public void testSetValue() throws Exception {
        
        PropertyDispatcher<String> propertyDispatcher = Dispatcher.createPropertyDispatcher();
        PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
        propertyDispatcher.subscribe(observer);
        
        PropertySource<String> source = ModelPropertySource.createFactory("tacos").apply(propertyDispatcher);
        
        assertEquals("tacos", source.get());
        
        source.setValue("burritos");
        assertEquals("burritos", source.get());
        Mockito.verify(observer).onChanged("burritos");
        Mockito.verify(observer, atLeastOnce()).isBinding();
        Mockito.verifyNoMoreInteractions(observer);
    }
}
