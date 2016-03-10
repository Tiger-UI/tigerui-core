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

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import mb.rxui.event.EventSequenceGenerator;

public class TestEventSequenceGenerator {
    
    private EventSequenceGenerator generator = EventSequenceGenerator.getInstance();
    
    @Before
    public void setup() {
        generator.reset();
    }
    
    @Test
    public void testNextSequenceNumber() throws InterruptedException {
        assertEquals(0, generator.nextSequenceNumber());
        Thread.sleep(2);
        assertEquals(1, generator.nextSequenceNumber());
        Thread.sleep(2);
        assertEquals(2, generator.nextSequenceNumber());
        
        Optional<Long> time0 = generator.getTimeForSequence(0);
        Optional<Long> time1 = generator.getTimeForSequence(1);
        Optional<Long> time2 = generator.getTimeForSequence(2);
        
        assertTrue(time0.isPresent());
        assertTrue(time1.isPresent());
        assertTrue(time2.isPresent());
        
        assertTrue(time1.get() - time0.get() > 0);
        assertTrue(time2.get() - time1.get() > 0);
        assertTrue(time2.get() - time0.get() > 0);
    }
    
    @Test
    public void testReset() throws Exception {
        assertEquals(0, generator.nextSequenceNumber());
        Optional<Long> time0 = generator.getTimeForSequence(0);
        assertTrue(time0.isPresent());
        
        generator.reset();
        assertFalse(generator.getTimeForSequence(0).isPresent());
        
        Thread.sleep(2);

        assertEquals(0, generator.nextSequenceNumber());
        Optional<Long> time02 = generator.getTimeForSequence(0);
        assertTrue(time02.isPresent());
        assertTrue(time02.get() - time0.get() > 0);
    }
}
