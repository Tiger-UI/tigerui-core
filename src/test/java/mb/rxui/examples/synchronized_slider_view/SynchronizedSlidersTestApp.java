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
package mb.rxui.examples.synchronized_slider_view;

import static mb.rxui.property.swing.SliderPropertySource.createSliderProperty;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import mb.rxui.property.Property;

public class SynchronizedSlidersTestApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Synchronized Sliders Tester");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            JSlider slider1 = new JSlider();
            JSlider slider2 = new JSlider();
            
            Property<Integer> slider1Value = createSliderProperty(slider1); 
            Property<Integer> slider2Value = createSliderProperty(slider2);
            
            slider1Value.synchronize(slider2Value);
            
            JPanel sliderPanel = new JPanel(new GridLayout(2, 1));
            
            sliderPanel.add(slider1);
            sliderPanel.add(slider2);
            
            frame.getContentPane().add(sliderPanel);
            frame.pack();
            frame.setVisible(true);
        });
    }
}
