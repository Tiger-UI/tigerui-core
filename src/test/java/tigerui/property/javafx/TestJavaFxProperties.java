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
package tigerui.property.javafx;

import static org.junit.Assert.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static tigerui.property.javafx.JavaFxProperties.fromFxProperty;
import static tigerui.property.javafx.JavaFxProperties.fromObservableValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import tigerui.property.Property;
import tigerui.property.PropertyObserver;
import tigerui.property.PropertyStream;
import tigerui.property.javafx.JavaFxProperties;
import tigerui.subscription.Subscription;

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
    public void testSetValueOnJavaFxProperty() throws Throwable {
        javaFxTestHelper.runTest(() -> {            
            PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
            
            property.observe(observer);
            verify(observer).onChanged("tacos");
            verify(observer, never()).onDisposed();
            
            fxStringProperty.setValue("burritos");
            verify(observer).onChanged("burritos");
            assertEquals("burritos", property.get());
            verify(observer, never()).onDisposed();
        });
    }
    
    @Test
    public void testSetValueProperty() throws Throwable {
        javaFxTestHelper.runTest(() -> { 
            PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);
            
            property.observe(observer);
            verify(observer).onChanged("tacos");
            verify(observer, never()).onDisposed();
            
            property.setValue("burritos");
            verify(fxStringProperty).setValue("burritos");
            assertEquals("burritos", fxStringProperty.getValue());
            verify(observer).onChanged("burritos");
            verify(observer, never()).onDisposed();
        } );
    }
    
    @Test
    public void testUnsubscribeRemovesListener() throws Throwable {
        javaFxTestHelper.runTest(() -> {
            SimpleStringProperty observableValue = Mockito.spy(new SimpleStringProperty("tacos"));
            PropertyStream<String> stream = fromObservableValue(observableValue);
            
            PropertyObserver<String> mock = Mockito.mock(PropertyObserver.class);
            
            Subscription subscription = stream.observe(mock);
            Mockito.verify(observableValue).addListener(Matchers.any(ChangeListener.class));
            
            subscription.dispose();            
            Mockito.verify(observableValue).removeListener(Matchers.any(ChangeListener.class));
        });
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCannotCreateFxPropertyOutsideFxThread() throws Exception {
        fromFxProperty(new SimpleStringProperty("tacos"));
    }
    
    @Test(expected=NullPointerException.class)
    public void testCannotCreatePropertyIfFxPropetyHasNoValue() throws Throwable {
        javaFxTestHelper.runTest(() -> fromFxProperty(new SimpleStringProperty()));
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCannotCreateFxPropertyStreamOutsideFxThread() throws Exception {
        fromObservableValue(new SimpleStringProperty("tacos"));
    }
    
    @Test
    public void testPropertyStreamFromObservableValue() throws Throwable {
        javaFxTestHelper.runTest(() -> {            
            Task<Integer> task = new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {
                    updateMessage("tacos");
                    updateMessage("burritos");
                    return 10;
                }
            };
            
            PropertyStream<String> stream = fromObservableValue(task.messageProperty());
            PropertyObserver<String> observer = Mockito.mock(PropertyObserver.class);

            InOrder inOrder = Mockito.inOrder(observer);
            stream.observe(observer);
            
            task.run();
            
            inOrder.verify(observer).onChanged("");
            inOrder.verify(observer).onChanged("tacos");
            inOrder.verify(observer).onChanged("burritos");
            inOrder.verifyNoMoreInteractions();
        });
    }
}
