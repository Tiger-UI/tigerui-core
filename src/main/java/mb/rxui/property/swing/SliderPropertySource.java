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

import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

import mb.rxui.dispatcher.PropertyDispatcher;
import mb.rxui.disposables.Disposable;
import mb.rxui.property.Property;

public class SliderPropertySource extends SwingPropertySource<Integer, ChangeListener, JSlider> {
    
    public SliderPropertySource(JSlider slider, PropertyDispatcher<Integer> dispatcher) {
        super(slider::getValue, slider::setValue, slider, dispatcher);
    }
    
    public static Property<Integer> createSliderProperty(JSlider slider) {
        return Property.create(dispatcher -> new SliderPropertySource(slider, dispatcher));
    }
    
    @Override
    protected ChangeListener createListener(PropertyDispatcher<Integer> dispatcher) {
        return event -> dispatcher.dispatch(((JSlider)event.getSource()).getValue());
    }

    @Override
    protected Disposable addListener(JSlider slider, ChangeListener listener) {
        slider.addChangeListener(listener);
        return () -> slider.removeChangeListener(listener);
    }
}
