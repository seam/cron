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
package org.jboss.seam.cron.scheduling.spi.trigger;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.logging.Logger;
import org.jboss.seam.cron.scheduling.api.Every;
import org.jboss.seam.cron.scheduling.api.Scheduled;
import org.jboss.seam.cron.scheduling.api.Trigger;
import org.jboss.seam.cron.scheduling.impl.exception.InternalException;
import org.jboss.seam.cron.util.AnnotaitonUtils;

/**
 * Base class for firing a Trigger. Implementing classes must define type of the
 * event to be fired.
 *
 * @author Peter Royle
 */
public class DefaultTriggerHelper implements TriggerHelper {

    protected int value = 0;
    protected final GregorianCalendar gc = new GregorianCalendar();
    protected BeanManager beanManager;
    protected Annotation qualifier;
    private boolean configured = false;
    private Logger log = Logger.getLogger(DefaultTriggerHelper.class);

    /**
     * Must call configure before calling fireTrigger.
     */
    public void configure(BeanManager beanManager, Annotation qualifier) {
        this.beanManager = beanManager;
        this.qualifier = qualifier;
        this.configured = true;
    }

    /**
     * Fires the appropriate trigger payload with the appropriate qualifier
     * (to in turn execute the application-specific code that observes those events).
     *
     */
    public void fireTrigger() {
        if (!configured) {
            throw new InternalException(getClass().getName() + " was not configured properly before calling this method.");
        }

        gc.setTime(new Date());

        Trigger eventPayload = null;
        final Scheduled schedQualifier = (Scheduled) AnnotaitonUtils.getQualifier(qualifier, Scheduled.class);
        if (schedQualifier != null) {
            eventPayload = createScheduledEventPayload();
        } else {
            final Every everyQualifier = (Every) AnnotaitonUtils.getQualifier(qualifier, Every.class);
            if (everyQualifier != null) {
                switch (everyQualifier.value()) {
                    case SECOND: eventPayload = createSecondEventPayload(); break;
                    case MINUTE: eventPayload = createMinuteEventPayload(); break;
                    case HOUR: eventPayload = createHourEventPayload(); break;
                }
            }
        }
        log.trace("Firing time event for " + eventPayload + " with binding " + qualifier);
        beanManager.fireEvent(eventPayload, qualifier);
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
