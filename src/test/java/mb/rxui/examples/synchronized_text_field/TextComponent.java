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

import mb.rxui.disposables.Disposable;

public class TextComponent<V> implements Disposable {
    private final V view;
    private final TextViewModel textViewModel;
    private final Disposable disposer;
    
    public TextComponent(TextView<V> view, String initialValue) {
        this.view = view.getView();
        this.textViewModel = new TextViewModel(initialValue);
        disposer = view.getTextProperty().synchronize(textViewModel.getTextProperty());
    }
    
    @Override
    public void dispose() {
        disposer.dispose();
    }
    
    V getView() {
        return view;
    }
    
    TextViewModel getModel() {
        return textViewModel;
    }
}
