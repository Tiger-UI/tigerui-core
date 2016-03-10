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

import org.junit.Test;

public class TestCallbacks {
    @Test
    public void testCreateSafeCallback() {
        try
        {            
            Callbacks.createSafeCallback(() -> {throw new RuntimeException();}).run();
        } catch (RuntimeException exception) {
            fail();
        }
    }
    
    @Test
    public void testRunSafeCallback() throws Exception {
        try
        {            
            Callbacks.runSafeCallback(() -> {throw new RuntimeException();});
        } catch (RuntimeException exception) {
            fail();
        }
    }
}
