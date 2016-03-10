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
package mb.rxui.property.swing;

import static mb.rxui.property.swing.SliderPropertySource.createSliderProperty;
import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;

import javax.swing.JSlider;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import mb.rxui.SwingTestRunner;
import mb.rxui.property.Property;

@RunWith(SwingTestRunner.class)
public class TestSliderPropertySource {
    
    @Test
    public void testSliderPropertySource() throws Exception {
        JSlider slider = new JSlider();
        Property<Integer> valueProperty = createSliderProperty(slider);

        Consumer<Integer> onChanged = Mockito.mock(Consumer.class);
        valueProperty.onChanged(onChanged);

        Mockito.verify(onChanged).accept(50);
        assertEquals(new Integer(50), valueProperty.get());

        slider.setValue(75);
        Mockito.verify(onChanged).accept(75);
        assertEquals(new Integer(75), valueProperty.get());
    }
    
    @Test
    public void testDisposeProperty() throws Exception {
        JSlider slider = new JSlider();

        int listenerCount = slider.getChangeListeners().length;

        Property<Integer> valueProperty = createSliderProperty(slider);
        
        Consumer<Integer> onChanged = Mockito.mock(Consumer.class);
        valueProperty.onChanged(onChanged);
        
        assertEquals(listenerCount + 1, slider.getChangeListeners().length);

        valueProperty.dispose();
        assertEquals(listenerCount, slider.getChangeListeners().length);
    }
    
    @Test
    public void testSetPropertyValueUpdatesSlider() throws Exception {
        JSlider slider = new JSlider();
        Property<Integer> valueProperty = createSliderProperty(slider);

        valueProperty.setValue(25);

        Assert.assertEquals(25, slider.getValue());
    }
}
