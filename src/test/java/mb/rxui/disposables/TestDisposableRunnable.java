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
package mb.rxui.disposables;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestDisposableRunnable {

    private Runnable runnable;
    private DisposableRunnable disposableRunnable;
    
    @Before
    public void setup() {        
        runnable = Mockito.mock(Runnable.class);
        disposableRunnable = new DisposableRunnable(runnable);
    }

    @Test
    public void testRunsIfNotDisposed() {
        disposableRunnable.run();
        Mockito.verify(runnable).run();
    }
    
    @Test
    public void testNotRunIfDisposed() {
        disposableRunnable.dispose();
        
        disposableRunnable.run();
        Mockito.verify(runnable, Mockito.never()).run();
    }
    
    @Test
    public void testRunTwice() {
        disposableRunnable.run();
        disposableRunnable.run();
        Mockito.verify(runnable, Mockito.times(2)).run();
    }
}
