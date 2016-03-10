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

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import mb.rxui.Preconditions;
import mb.rxui.annotations.VisibleForTesting;
import mb.rxui.event.EventSequenceGenerator;

/**
 * A property change event captures the change of a property value.
 * 
 * @param <M>
 *            the type of the value the change event tracks
 */
public class PropertyChangeEvent<M> {
    private final M oldValue;
    private final M newValue;
    private final long eventSequence;

    @VisibleForTesting
    PropertyChangeEvent(M oldValue, M newValue, long eventSequence) {
        Preconditions.checkArgument(!Objects.equals(oldValue, newValue), "Old value and new value must be different.!");
        this.oldValue = requireNonNull(oldValue);
        this.newValue = requireNonNull(newValue);
        this.eventSequence = eventSequence;
    }
    
    /**
     * Creates a new {@link PropertyChangeEvent}. The event sequence number will
     * be set to the next sequence number supplied by the
     * {@link EventSequenceGenerator}.
     * 
     * @param oldValue
     *            the old value for the property, can be null.
     * @param newValue
     *            the new value for the property, cannot be null.
     * @throws NullPointerException
     *             if the provided new value is null.
     */
    public PropertyChangeEvent(M oldValue, M newValue) {
        this(oldValue, newValue, EventSequenceGenerator.getInstance().nextSequenceNumber());
    }
    
    /**
     * NOTE: this value can be null
     * @return the value before the change.
     */
    public M getOldValue() {
        return oldValue;
    }
    
    /**
     * @return the new value that resulted from the change.
     */
    public M getNewValue() {
        return newValue;
    }
    
    /**
     * @return a globally consistent sequence number. This can be used to order
     *         all property change events, regardless of from which property
     *         they originate from.
     */
    public long getEventSequence() {
        return eventSequence;
    }
  
    public PropertyChangeEvent<M> next(M newValue) {
        return new PropertyChangeEvent<M>(this.newValue, newValue);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
        result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
        result = prime * result + (int) (eventSequence ^ (eventSequence >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PropertyChangeEvent<?> other = (PropertyChangeEvent<?>) obj;
        if (!newValue.equals(other.newValue))
            return false;
        if (!oldValue.equals(other.oldValue))
            return false;
        if (eventSequence != other.eventSequence)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PropertyChangeEvent [oldValue=" + oldValue + ", newValue=" + newValue + ", eventSequence=" + eventSequence + "]";
    }
}
