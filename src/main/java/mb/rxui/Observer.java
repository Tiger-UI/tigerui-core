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
 * Marker interface for different kinds of Observers
 *
 * @param <T>
 *            the type of data being observed
 */
public interface Observer<T> {
    
    /**
     * @return true if this observer is represents a binding, false otherwise.
     */
    default boolean isBinding() { return false; };
}
