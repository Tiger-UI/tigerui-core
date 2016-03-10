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

import static org.junit.Assert.*;

import java.util.function.Consumer;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

import org.junit.Test;
import org.mockito.Mockito;

import mb.rxui.ThreadedTestHelper;
import mb.rxui.property.Property;

public class TestTextPropertySource {
    
    ThreadedTestHelper testHelper = new ThreadedTestHelper(SwingUtilities::invokeLater);
    
    @Test(expected=IllegalStateException.class)
    public void testThrowsIfUsedOutsideEDT() throws Exception {
        TextPropertySource.createTextProperty(new JTextField());
    }
    
    @Test
    public void testTextPropertySource() throws Exception {
        testHelper.runTest(() -> {            
            JTextField textField = new JTextField();
            Property<String> textProperty = TextPropertySource.createTextProperty(textField);
            
            Consumer<String> onChanged = Mockito.mock(Consumer.class);
            textProperty.onChanged(onChanged);
            
            Mockito.verify(onChanged).accept("");
            assertEquals("", textProperty.get());
            
            textField.setText("tacos");
            Mockito.verify(onChanged).accept("tacos");
            assertEquals("tacos", textProperty.get());
        });
    }
    
    @Test
    public void testDisposeProperty() throws Exception {
        testHelper.runTest(() -> {            
            JTextField textField = new JTextField();
            
            AbstractDocument document = (AbstractDocument)textField.getDocument();
            
            int listenerCount = document.getDocumentListeners().length;
            
            Property<String> textProperty = TextPropertySource.createTextProperty(textField);
            assertEquals(listenerCount + 1, document.getDocumentListeners().length);
            
            textProperty.dispose();
            assertEquals(listenerCount, document.getDocumentListeners().length);
        });
    }
    
    @Test
    public void testSetPropertyValueUpdatesTextField() throws Exception {
        testHelper.runTest(() -> {            
            JTextField textField = new JTextField();
            Property<String> textProperty = TextPropertySource.createTextProperty(textField);
            
            textProperty.setValue("tacos");
            
            assertEquals("tacos", textField.getText());
        });
    }
    
    @Test
    public void testDocumentRemoveUpdate() throws Exception {
        testHelper.runTest(() -> {            
            JTextField textField = new JTextField("tacos");
            Document document = textField.getDocument();
            Property<String> textProperty = TextPropertySource.createTextProperty(textField);
            
            Consumer<String> onChanged = Mockito.mock(Consumer.class);
            textProperty.onChanged(onChanged);
            
            try {
                document.remove(0, 1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            Mockito.verify(onChanged).accept("acos");
        });
    }
}
