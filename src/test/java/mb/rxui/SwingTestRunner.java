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
package mb.rxui;

import static mb.rxui.ThreadedTestHelper.EDT_TEST_HELPER;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * A test runner that will execute tests on the EDT
 */
public class SwingTestRunner extends BlockJUnit4ClassRunner {

    public SwingTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }
    
    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        Statement invoker = super.methodInvoker(method, test);
        return EDT_TEST_HELPER.wrapStatementToRunOnEDT(invoker);
    }
}
