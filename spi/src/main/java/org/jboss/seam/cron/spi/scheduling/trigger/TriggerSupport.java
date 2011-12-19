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
package org.jboss.seam.cron.spi.scheduling.trigger;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.GregorianCalendar;

import org.jboss.seam.cron.api.scheduling.Every;
import org.jboss.seam.cron.api.scheduling.Scheduled;
import org.jboss.seam.cron.api.scheduling.Trigger;
import org.jboss.seam.cron.spi.scheduling.CronSchedulingProvider;
import org.jboss.seam.cron.util.CdiUtils;
import org.jboss.solder.logging.Logger;

/**
 * <p>
 * Base class allowing scheduling providers to easily fire the
 * appropriate CDI event when required. Simply provide the necessary
 * objects (represented by #{@link TriggerSupplies} to the constructor when the
 * schedule is first registered (see #{@link CronSchedulingProvider}). Then call
 * #{@literal fireTrigger()} at the scheduled time(s).
 * </p>
 * <p>
 * If the scheduling provider does not allow context to be passed directly to
 * a new worker instance, but instead requires it to be passed via some
 * other context, you will need to use #{@link ProviderContextTriggerSupport}
 * instead.
 * </p>
 * 
 * @author Peter Royle
 * 
 * @see ProviderContextTriggerSupport
 */
public abstract class TriggerSupport {

    protected int value = 0;
    protected final GregorianCalendar gc = new GregorianCalendar();
    protected TriggerSupplies supplies = null;
    private final Logger log = Logger.getLogger(TriggerSupport.class);

    protected TriggerSupport() {
    }

    public TriggerSupport(final TriggerSupplies supplies) {
        this.supplies = supplies;
    }

    /**
     * Fires the appropriate trigger payload with the appropriate qualifier
     * (to in turn execute the application-specific code that observes those events).
     *
     */
    public void fireTrigger() {

        gc.setTime(new Date());

        if (log.isTraceEnabled()) {
            log.trace("Firing scheduled trigger with these supplies: " + (supplies != null ? supplies.toString() : supplies));
        }
        
        Trigger eventPayload = null;
        final Scheduled schedQualifier = (Scheduled) CdiUtils.getQualifier(supplies.getQualifier(), Scheduled.class);
        if (schedQualifier != null) {
            eventPayload = createScheduledEventPayload();
        } else {
            final Every everyQualifier = (Every) CdiUtils.getQualifier(supplies.getQualifier(), Every.class);
            if (everyQualifier != null) {
                switch (everyQualifier.value()) {
                    case SECOND:
                        eventPayload = createSecondEventPayload();
                        break;
                    case MINUTE:
                        eventPayload = createMinuteEventPayload();
                        break;
                    case HOUR:
                        eventPayload = createHourEventPayload();
                        break;
                }
            }
        }
        log.trace("Firing time event for " + eventPayload + " with qualifier " + supplies.getQualifiers());
        supplies.getBeanManager().fireEvent(eventPayload, supplies.getQualifiers().toArray(new Annotation[] {}));
    }

    /**
     * Create an instance of the Event payload using the current system time.
     *
     * @return an instance of Event.
     */
    protected Trigger createScheduledEventPayload() {
        return new Trigger(System.currentTimeMillis());
    }

    protected Trigger createSecondEventPayload() {
        return new Trigger(System.currentTimeMillis(),
                gc.get(GregorianCalendar.SECOND));
    }

    protected Trigger createMinuteEventPayload() {
        return new Trigger(System.currentTimeMillis(),
                gc.get(GregorianCalendar.MINUTE));
    }

    protected Trigger createHourEventPayload() {
        return new Trigger(System.currentTimeMillis(),
                gc.get(GregorianCalendar.HOUR_OF_DAY));
    }
}
