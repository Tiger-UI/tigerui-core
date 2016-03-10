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

/**
 * A counter can be used in lambdas to increment and decrement a count
 */
public class Counter {
    private int count = 0;
    
    public void increment() {
        count++;
    }
    
    public int incrementAndGet() {
        return ++count;
    }
    
    public void decrement() {
        count--;
    }
    
    public int decrementAndGet() {
        return --count;
    }
    
    public int getCount() {
        return count;
    }
    
    public void reset() {
        count = 0;
    }
}
