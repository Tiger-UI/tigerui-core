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
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;

@RunWith(SwingTestRunner.class)
public class TestBinding {
    @Test
    public void testBinding() throws Exception {
        Property<String> property = Property.create("tacos");
        
        PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
        property.observe(observer);
        verify(observer).onChanged("tacos");
        
        PropertyObserver<String> binding = new PropertyBinding<>(property);
        assertTrue(binding.isBinding());
        
        binding.onChanged("burritos");
        verify(observer).onChanged("burritos");
        
        binding.onDisposed();
        Mockito.verify(observer, atLeastOnce()).isBinding();
        Mockito.verifyNoMoreInteractions(observer);
    }
}
