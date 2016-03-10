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
package mb.rxui.property.opertator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;
import mb.rxui.property.Property;
import mb.rxui.property.PropertyObserver;
import mb.rxui.property.PropertyStream;
import mb.rxui.subscription.Subscription;

@RunWith(SwingTestRunner.class)
public class TestOperatorSwitchMap {
    
    private Property<String> propertyA;
    private Property<String> propertyB;
    private Property<String> propertyC;
    private Property<String> defaultProperty;
    private Property<String> lettersProperty;
    private PropertyStream<String> switchMapStream;
    
    @Before
    public void setup() throws Exception {
        SwingUtilities.invokeAndWait(() -> {            
            propertyA = Property.create("A");
            propertyB = Property.create("B");
            propertyC = Property.create("C");
            
            defaultProperty = Property.create("default");
            lettersProperty = Property.create("default");
            
            switchMapStream = lettersProperty.switchMap(letter -> {
                if ("A".equals(letter)) return propertyA;
                if ("B".equals(letter)) return propertyB;
                if ("C".equals(letter)) return propertyC;
                return defaultProperty;
            });
        });
    }

    @Test
    public void testSwitchMap() {
        
        assertFalse(propertyA.hasObservers());
        assertFalse(propertyB.hasObservers());
        assertFalse(propertyC.hasObservers());
        assertFalse(defaultProperty.hasObservers());
        
        Consumer<String> onChanged = Mockito.mock(Consumer.class);
        Runnable onDisposed = Mockito.mock(Runnable.class);
        switchMapStream.observe(onChanged, onDisposed);

        Mockito.verify(onChanged).accept("default");
        assertEquals("default", switchMapStream.get());
        
        assertFalse(propertyA.hasObservers());
        assertFalse(propertyB.hasObservers());
        assertFalse(propertyC.hasObservers());
        assertTrue(defaultProperty.hasObservers()); // since a property always emits a value first
        
        propertyA.setValue("Avalue");
        propertyB.setValue("Bvalue");
        propertyC.setValue("Cvalue");
        defaultProperty.setValue("Defaultvalue");
        
        Mockito.verify(onChanged).accept("Defaultvalue");
        assertEquals("Defaultvalue", switchMapStream.get());
        verifyNoMoreInteractions(onChanged, onDisposed);
        
        
        lettersProperty.setValue("A");
        Mockito.verify(onChanged).accept("Avalue"); // from the current value of property A
        assertEquals("Avalue", switchMapStream.get());
        
        assertTrue(propertyA.hasObservers());
        assertFalse(propertyB.hasObservers());
        assertFalse(propertyC.hasObservers());
        assertFalse(defaultProperty.hasObservers());
        
        propertyA.setValue("AValue");
        propertyB.setValue("BValue");
        propertyC.setValue("CValue");
        defaultProperty.setValue("DefaultValue");
        
        verify(onChanged).accept("AValue");
        assertEquals("AValue", switchMapStream.get());
        verifyNoMoreInteractions(onChanged, onDisposed);
        
        
        lettersProperty.setValue("B");
        verify(onChanged).accept("BValue");
        assertEquals("BValue", switchMapStream.get());
        
        assertFalse(propertyA.hasObservers());
        assertTrue(propertyB.hasObservers());
        assertFalse(propertyC.hasObservers());
        assertFalse(defaultProperty.hasObservers());
        
        propertyA.setValue("Anew");
        propertyB.setValue("Bnew");
        propertyC.setValue("Cnew");
        defaultProperty.setValue("Defaultnew");
        
        verify(onChanged).accept("Bnew");
        assertEquals("Bnew", switchMapStream.get());
        verifyNoMoreInteractions(onChanged, onDisposed);
        
        lettersProperty.setValue("C");
        Mockito.verify(onChanged).accept("Cnew");
        
        assertFalse(propertyA.hasObservers());
        assertFalse(propertyB.hasObservers());
        assertTrue(propertyC.hasObservers());
        assertFalse(defaultProperty.hasObservers());
        
        propertyA.setValue("ANew");
        propertyB.setValue("BNew");
        propertyC.setValue("CNew");
        defaultProperty.setValue("DefaultNew");
        
        verify(onChanged).accept("CNew");
        assertEquals("CNew", switchMapStream.get());
        Mockito.verifyNoMoreInteractions(onChanged, onDisposed);
    }
    
    @Test
    public void testDisposeUnsubscribesObserver() {
        
        Consumer<String> onChanged = Mockito.mock(Consumer.class);
        Runnable onDisposed = Mockito.mock(Runnable.class);
        
        Subscription subscription = switchMapStream.observe(onChanged, onDisposed);
        verify(onChanged).accept("default");
        assertFalse(subscription.isDisposed());
        
        lettersProperty.dispose();
        verify(onDisposed).run();
        assertTrue(subscription.isDisposed());
        verifyNoMoreInteractions(onChanged, onDisposed);
    }
    
    @Test
    public void testUnsubscribeDoesnNotStopSwitchMap() throws Exception {

        PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
        Subscription subscription = switchMapStream.observe(observer);

        assertFalse(propertyA.hasObservers());

        lettersProperty.setValue("A");

        assertTrue(lettersProperty.hasObservers());
        assertTrue(propertyA.hasObservers());

        subscription.dispose();
        assertFalse(lettersProperty.hasObservers());
        assertFalse(propertyA.hasObservers());
    }
    
    @Test
    public void testSubscribeAfterDisposed() {

        Consumer<String> onChanged = Mockito.mock(Consumer.class);
        Runnable onDisposed = Mockito.mock(Runnable.class);
        
        lettersProperty.dispose();
        Subscription subscription = switchMapStream.observe(onChanged, onDisposed);
        
        verify(onChanged).accept("default");
        verify(onDisposed).run();
        Mockito.verifyNoMoreInteractions(onChanged);
        
        assertTrue(subscription.isDisposed());
        assertFalse(lettersProperty.hasObservers());
    }
    
    @Test
    public void testReentranceBlocked() {
        
        PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);

        switchMapStream.observe(observer);
        verify(observer).onChanged("default");
        
        // this reentrant call should be blocked.
        switchMapStream.onChanged(value -> propertyA.setValue("burritos"));
        
        lettersProperty.setValue("A");
        verify(observer).onChanged("A");
        
        propertyA.setValue("tacos");
        verify(observer).onChanged("tacos");

        verify(observer, never()).onChanged("burritos");
    }
}
