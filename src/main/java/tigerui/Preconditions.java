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
package tigerui;

/**
 * Helper classes for various preconditions
 */
public enum Preconditions {
    ; // no instance, helper class

    /**
     * Checks that the provided condition is met and if not throws an {@link IllegalArgumentException}
     * with the provided message.
     * @param condition some condition to check
     * @param message some message to use for the thrown exception.
     * @throws IllegalArgumentException if the provided condition is not met.
     */
    public static void checkArgument(boolean condition, String message) {
        if (!condition)
            throw new IllegalArgumentException(message);
    }

    /**
     * Checks that the provided condition is met and if not throws an {@link IllegalStateException}
     * with the provided message.
     * @param condition some condition to check
     * @param message some message to use for the thrown exception.
     * @throws IllegalStateException if the provided condition is not met.
     */
    public static void checkState(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
