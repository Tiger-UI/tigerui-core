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
package mb.rxui.event;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mockito;

public class TestEventObserver {
    @Test
    public void testOnEventObserver() {
        Consumer<String> eventHandler = Mockito.mock(Consumer.class);
        EventObserver<String> observer = EventObserver.create(eventHandler);
        
        observer.onEvent("tacos");
        Mockito.verify(eventHandler).accept("tacos");
    }
    
    @Test
    public void testOnCompletedObserver() {
        Runnable onCompleted = Mockito.mock(Runnable.class);
        EventObserver<String> observer = EventObserver.create(onCompleted);
        
        observer.onCompleted();
        Mockito.verify(onCompleted).run();
    }
    
    @Test
    public void testOnEventAndOnCompleted() {
        Consumer<String> eventHandler = Mockito.mock(Consumer.class);
        Runnable onCompleted = Mockito.mock(Runnable.class);
        EventObserver<String> observer = EventObserver.create(eventHandler, onCompleted);
        
        observer.onEvent("tacos");
        Mockito.verify(eventHandler).accept("tacos");
        
        observer.onCompleted();
        Mockito.verify(onCompleted).run();
    }
    
    @Test(expected=NullPointerException.class)
    public void testOnEventHandlerCannotBeNull() {
        EventObserver.create((Consumer<Object>) null);
    }
    
    @Test(expected=NullPointerException.class)
    public void testOnCompletedHandlerCannotBeNull() {
        EventObserver.create(value -> {}, (Runnable) null);
    }
}
