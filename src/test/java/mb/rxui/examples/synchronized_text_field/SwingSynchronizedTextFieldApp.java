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
package mb.rxui.examples.synchronized_text_field;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import mb.rxui.property.Property;

/**
 * Simple Swing application to demonstrate how to use properties.
 */
public class SwingSynchronizedTextFieldApp {
    
    private final SynchronizedTextFieldView synchronizedTextView;
    
    public SwingSynchronizedTextFieldApp(String initialTextValue) {
        synchronizedTextView = new SynchronizedTextFieldView(initialTextValue);
    }
    
    public Property<String> getTextProperty() {
        return synchronizedTextView.getTextProperty();
    }

    public void show() {
        buildFrame(synchronizedTextView.getComponent());
    }

    private static void buildFrame(JComponent view) {
        
        JFrame frame = new JFrame("Synchronized Text Field Test App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setContentPane(view);
        
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingSynchronizedTextFieldApp("tacos").show());
    }
}
