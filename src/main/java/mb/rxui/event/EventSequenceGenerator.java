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
package mb.rxui.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A Generator that can be used to generate globally consistent event sequence
 * numbers. The sequence numbers can be used to order events from different
 * sources into a canonical ordering.
 * 
 * <p>
 * TODO: Consider adding a TimeProvider interface or something to abstract the
 * way the current time is acquired.
 */
public final class EventSequenceGenerator {
    private static final EventSequenceGenerator instance = new EventSequenceGenerator();

    private long lastSequenceNumber = 0;
    private final Map<Long, Long> sequenceMap = new HashMap<>();
    
    private EventSequenceGenerator() {}
    
    public static EventSequenceGenerator getInstance() {
        return instance;
    }
    
    /**
     * Retrieves the next event sequence number.
     * 
     * @return a sequence number that is guaranteed not to be unique.
     */
    public final long nextSequenceNumber() {
        long nextSequence = lastSequenceNumber++;
        sequenceMap.put(nextSequence, System.currentTimeMillis());
        return nextSequence;
    }
    
    /**
     * @deprecated this will be removed once an interface for Event Sequencer is
     *             created. Should only be used by tests
     */
    public final void reset() {
        lastSequenceNumber = 0;
        sequenceMap.clear();
    }
    
    /**
     * Retrieves the actual time for the provided sequence number
     * 
     * @param sequenceNumber
     *            some sequence number to get the actual time for
     * @return an {@link Optional} of the actual time retrieved for the provided
     *         sequence number. This will be empty if a time for the sequence
     *         number does not exist.
     */
    public final Optional<Long> getTimeForSequence(long sequenceNumber) {
        return Optional.ofNullable(sequenceMap.get(sequenceNumber));
    }
}