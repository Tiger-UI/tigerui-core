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
package mb.rxui.property;

import static mb.rxui.property.javafx.JavaFxProperties.fromFxProperty;
import static mb.rxui.property.javafx.JavaFxProperties.fromObservableValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import mb.rxui.property.javafx.JavaFxProperties;

public class TestJavaFxProperties {
    
    private javafx.beans.property.Property<String> fxStringProperty;
    private Property<String> property;
    private JavaFxTestHelper javaFxTestHelper;
    
    @Before
    public void setup() {
        javaFxTestHelper = JavaFxTestHelper.instance();
        
        javaFxTestHelper.invokeAndWait(() -> {            
            fxStringProperty = Mockito.spy( new SimpleStringProperty("tacos") );
            property = JavaFxProperties.fromFxProperty(fxStringProperty);
        });
        Mockito.verify(fxStringProperty).addListener(Matchers.any(ChangeListener.class));
    }
    
    @After
    public void tearDown() {
        javaFxTestHelper.invokeAndWait(property::dispose);
        Mockito.verify(fxStringProperty).removeListener(Matchers.any(ChangeListener.class));
    }
    
    @Test
    public void testSetValueOnJavaFxProperty() throws Exception {
        javaFxTestHelper.runTest(() -> {            
            PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
            
            property.observe(observer);
            verify(observer).onChanged("tacos");
            verifyNoMoreInteractions(observer);
            
            fxStringProperty.setValue("burritos");
            verify(observer).onChanged("burritos");
            assertEquals("burritos", property.get());
            verifyNoMoreInteractions(observer);
        });
    }
    
    @Test
    public void testSetValueProperty() throws Exception {
        javaFxTestHelper.runTest(() -> { 
            PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
            
            property.observe(observer);
            verify(observer).onChanged("tacos");
            verifyNoMoreInteractions(observer);
            
            property.setValue("burritos");
            verify(fxStringProperty).setValue("burritos");
            assertEquals("burritos", fxStringProperty.getValue());
            verify(observer).onChanged("burritos");
            verifyNoMoreInteractions(observer);
        } );
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCannotCreateFxPropertyOutsideFxThread() throws Exception {
        fromFxProperty(new SimpleStringProperty("tacos"));
    }
    
    @Test(expected=NullPointerException.class)
    public void testCannotCreatePropertyIfFxPropetyHasNoValue() throws Throwable {
        javaFxTestHelper.runTestReThrowException(() -> fromFxProperty(new SimpleStringProperty()));
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCannotCreateFxPropertyObservableOutsideFxThread() throws Exception {
        fromObservableValue(new SimpleStringProperty("tacos"));
    }
    
    @Test
    public void testPropertyObservableFromObservableValue() throws Exception {
        javaFxTestHelper.runTest(() -> {            
            Task<Integer> task = new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {
                    updateMessage("tacos");
                    updateMessage("burritos");
                    return 10;
                }
            };
            
            PropertyObservable<String> observable = fromObservableValue(task.messageProperty());
            PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);

            InOrder inOrder = Mockito.inOrder(observer);
            observable.observe(observer);
            
            task.run();
            
            inOrder.verify(observer).onChanged("");
            inOrder.verify(observer).onChanged("tacos");
            inOrder.verify(observer).onChanged("burritos");
            inOrder.verifyNoMoreInteractions();
        });
    }
}
