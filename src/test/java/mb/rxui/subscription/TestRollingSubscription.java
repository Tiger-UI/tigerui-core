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
package mb.rxui.subscription;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.mockito.Mockito;

public class TestRollingSubscription {

    @Test
    public void testUnsubscribesPrevious() {
        RollingSubscription subscription = new RollingSubscription();
        
        Subscription sub1 = Mockito.mock(Subscription.class);
        
        subscription.set(sub1);
        verifyNoMoreInteractions(sub1);
        
        Subscription sub2 = Mockito.mock(Subscription.class);
        subscription.set(sub2);
        Mockito.verify(sub1).dispose();
        verifyNoMoreInteractions(sub2);
    }
    
    @Test
    public void testAddSubAfterDispose() {
        RollingSubscription subscription = new RollingSubscription();
        
        subscription.dispose();
        
        Subscription sub1 = Mockito.mock(Subscription.class);
        
        subscription.set(sub1);
        Mockito.verify(sub1).dispose();
    }
}
