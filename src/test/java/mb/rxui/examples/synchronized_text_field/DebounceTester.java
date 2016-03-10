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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.swing.JFrame.EXIT_ON_CLOSE;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import mb.rxui.event.EventStream;

public class DebounceTester {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

            TextView<JComponent> textView = TextView.from(new JTextField(20));
            TextView<JComponent> textView2 = TextView.from(new JTextField(20));

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(2, 1));
            panel.add(textView.getView());
            panel.add(textView2.getView());

            textView.getTextProperty().synchronize(textView2.getTextProperty());

            frame.getContentPane().add(panel);

            EventStream<String> debouncedStream = textView.getTextProperty().debounce(500, MILLISECONDS);

            // this debounced stream handler should be ignored since it would be reentrant.
            debouncedStream.onEvent(value -> textView.getTextProperty().setValue("tacos"));
            
            // this debounced stream handler should not be ignored since it is not reentrant.
            debouncedStream.onEvent(event -> System.err.println("Debounced value [" + event + "]"));

            frame.setVisible(true);
        });
    }
}
