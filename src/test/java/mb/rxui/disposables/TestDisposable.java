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

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class TestDisposable {
    @Test
    public void testDisposeComposite() {
        Disposable disposable1 = Mockito.mock(Disposable.class);
        Disposable disposable2 = Mockito.mock(Disposable.class);
        Disposable disposable3 = Mockito.mock(Disposable.class);
        
        InOrder inOrder = Mockito.inOrder(disposable1, disposable2, disposable3);
        
        Disposable disposable = Disposable.create(disposable1, disposable2, disposable3);
        disposable.dispose();
        
        inOrder.verify(disposable1).dispose();
        inOrder.verify(disposable2).dispose();
        inOrder.verify(disposable3).dispose();
        inOrder.verifyNoMoreInteractions();
        
        disposable.dispose();
        inOrder.verifyNoMoreInteractions();
    }
}
