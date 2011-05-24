/**
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.seam.cron.api.scheduling;


/**
 * <p>
 * The payload of all scheduled events, be they arbitrarily scheduled or one of the
 * built-in @Every() schedules. Note that the presence of this superclass
 * implies that the following observation is possible:
 * <code>public void doSomething(@Observes @Every Trigger e)</code>
 * <p/><p>
 * Such an observation is valid, and will respond to every single scheduled event
 * fired (including the built in events).
 *</p>
 * 
 * @author Peter Royle
 */
public class Trigger {
    
    private final long timeFired;
    
    // TODO: (PR): This should probably go into a subclass, then we can make it final
    // the value of the second, miute or hour, as the case may be
    protected int value;

    /**
     * Create an instance of some subclass of Trigger using the given timeFired.
     *
     * @param timeFired Represents the time at which the event represented by this instance fired.
     */
    public Trigger(final long timeFired) {
        this.timeFired = timeFired;
    }

    public Trigger(final long timeFired, final int value) {
        this.timeFired = timeFired;
        this.value = value;
    }

    /**
     * @return The time at which the event was fired.
     */
    public long getTimeFired() {
        return timeFired;
    }

    /**
     * @return The value of the second, minute or hour fired.
     */
    public int getValue() {
        return value;
    }

    /**
     * @return a String representation of this instance, showing the event type and the
     *         time at which the event was fired (epoch).
     */
    @Override
    public String toString() {
        return getClass().getName() + " fired at " + Long.toString(timeFired);
    }
}
