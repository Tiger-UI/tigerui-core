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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mockito;

public class TestPropertyObserver {
    @Test
    public void testCreateOnChangedObserver() throws Exception {
        Consumer<String> onChangeListener = Mockito.mock(Consumer.class);
        PropertyObserver<String> observer = PropertyObserver.<String>create(onChangeListener);
        assertFalse(observer.isBinding());
        
        observer.onChanged("tacos");
        Mockito.verify(onChangeListener).accept("tacos");
    }
    
    @Test
    public void testCreateOnDisposedObserver() throws Exception {
        Runnable onDisposedListener = Mockito.mock(Runnable.class);
        PropertyObserver<String> observer = PropertyObserver.<String>create(onDisposedListener);
        assertFalse(observer.isBinding());
        
        observer.onChanged("tacos");
        verify(onDisposedListener, never()).run();
        
        observer.onDisposed();
        verify(onDisposedListener).run();
    }
    
    @Test
    public void testOnChangedAndOnDisposedObserver() throws Exception {
        Consumer<String> onChangeListener = Mockito.mock(Consumer.class);
        Runnable onDisposedListener = Mockito.mock(Runnable.class);
        PropertyObserver<String> observer = PropertyObserver.create(onChangeListener, onDisposedListener);
        assertFalse(observer.isBinding());
        
        observer.onChanged("tacos");
        Mockito.verify(onChangeListener).accept("tacos");
        
        observer.onDisposed();
        verify(onDisposedListener).run();
    }
}
