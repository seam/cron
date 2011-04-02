/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.seam.cron.quartz.jobs;

import org.jboss.seam.cron.events.AbstractTimeEvent;
import org.jboss.seam.cron.events.Second;

import java.util.GregorianCalendar;

/**
 * Fires the Second event with the @Every binding.
 * @author Peter Royle
 */
public class SecondJob
    extends AbstractTimeEventJob
{
    /**
     * Create an instance of the Event payload using the current system time.
     * @return an instance of Event.
     */
    protected AbstractTimeEvent createEventPayload(  )
    {
        return new Second( System.currentTimeMillis(  ),
                           gc.get( GregorianCalendar.SECOND ) );
    }
}
