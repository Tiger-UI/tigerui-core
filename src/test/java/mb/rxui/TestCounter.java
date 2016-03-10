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
package mb.rxui;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestCounter {
    
    private Counter counter;
    
    @Before
    public void setup() {
        counter = new Counter();
    }
    
    @Test
    public void testIncrement() {
        assertEquals(0, counter.getCount());
        
        counter.increment();
        
        assertEquals(1, counter.getCount());
    }
    
    @Test
    public void testIncrementAndGet() {
        assertEquals(0, counter.getCount());
        
        assertEquals(1, counter.incrementAndGet());
    }
    
    @Test
    public void testDecrement() {
        assertEquals(0, counter.getCount());
        
        counter.decrement();
        
        assertEquals(-1, counter.getCount());
    }
    
    @Test
    public void testDecrementAndGet() {
        assertEquals(0, counter.getCount());
        
        assertEquals(-1, counter.decrementAndGet());
    }
    
    @Test
    public void testReset() {
        assertEquals(0, counter.getCount());
        
        counter.increment();
        assertEquals(1, counter.getCount());
        
        counter.reset();
        assertEquals(0, counter.getCount());
    }
}
