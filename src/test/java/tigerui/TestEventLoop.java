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
package tigerui;

import static tigerui.EventLoop.JAVAFX_EVENT_LOOP;
import static tigerui.EventLoop.SWING_EVENT_LOOP;
import static tigerui.ThreadedTestHelper.EDT_TEST_HELPER;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.mockito.Mockito;

import tigerui.property.javafx.JavaFxTestHelper;

public class TestEventLoop {
    
    @Test(expected=IllegalStateException.class)
    public void testEDTEventLoop() throws Exception {
        EventLoop.SWING_EVENT_LOOP.checkInEventLoop();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testPlatfromEventLoop() throws Exception {
        EventLoop.JAVAFX_EVENT_LOOP.checkInEventLoop();
    }
    
    @Test
    public void testCreateEDTEventLoop() throws Throwable {
        AtomicReference<EventLoop> eventLoop = new AtomicReference<>();
        
        EDT_TEST_HELPER.runTest(() -> {
            eventLoop.set(EventLoop.createEventLoop());
        });
        
        EDT_TEST_HELPER.runTest(() -> {
            eventLoop.get().checkInEventLoop();
        }); 
    }
    
    @Test
    public void testInvokeNowOnEDT() throws Throwable {
        EDT_TEST_HELPER.runTest(() -> {
            Runnable runnable = Mockito.mock(Runnable.class);
            
            SWING_EVENT_LOOP.invokeNow(runnable);
            
            Mockito.verify(runnable).run();
        });
    }
    
    @Test(expected=IllegalStateException.class)
    public void testInvokeNowOnEDTFromWrongThread() throws Throwable {
        JavaFxTestHelper.instance().runTest(() -> {
            Runnable runnable = Mockito.mock(Runnable.class);
            
            SWING_EVENT_LOOP.invokeNow(runnable);
            
            Mockito.verify(runnable).run();
        });
    }
    
    @Test
    public void testInvokeNowOnPlatformThread() throws Throwable {
        JavaFxTestHelper.instance().runTest(() -> {
            Runnable runnable = Mockito.mock(Runnable.class);
            
            JAVAFX_EVENT_LOOP.invokeNow(runnable);
            
            Mockito.verify(runnable).run();
        });
    }
    
    @Test(expected=IllegalStateException.class)
    public void testInvokeNowOnPlatformThreadFromWrongThread() throws Throwable {
        EDT_TEST_HELPER.runTest(() -> {
            Runnable runnable = Mockito.mock(Runnable.class);
            
            JAVAFX_EVENT_LOOP.invokeNow(runnable);
            
            Mockito.verify(runnable).run();
        });
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCreateEDTEventLoopThrows() throws Throwable {
        AtomicReference<EventLoop> eventLoop = new AtomicReference<>();
        
        EDT_TEST_HELPER.runTest(() -> {
            eventLoop.set(EventLoop.createEventLoop());
        });
        
        eventLoop.get().checkInEventLoop();
    }
    
    @Test
    public void testCreatePlatformEventLoop() throws Throwable {
        AtomicReference<EventLoop> eventLoop = new AtomicReference<>();
        
        JavaFxTestHelper.instance().runTest(() -> {
            eventLoop.set(EventLoop.createEventLoop());
        });
        
        JavaFxTestHelper.instance().runTest(() -> {
            eventLoop.get().checkInEventLoop();
        }); 
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCreatePlatformEventLoopThrows() throws Throwable {
        AtomicReference<EventLoop> eventLoop = new AtomicReference<>();
        
        JavaFxTestHelper.instance().runTest(() -> {
            eventLoop.set(EventLoop.createEventLoop());
        });
        
        eventLoop.get().checkInEventLoop();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCreateOnUnknownThread() throws Exception {
        EventLoop.createEventLoop();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testArbitraryEventLoopThrows() throws Throwable {
        EventLoop eventLoop = ThreadedTestHelper.createOnEDT(EventLoop::createEventLoop);
        
        JavaFxTestHelper.instance().runTest(eventLoop::checkInEventLoop);
    }
}
