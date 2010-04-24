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
package org.jboss.seam.scheduling.quartz.jobs;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import javax.enterprise.inject.spi.BeanManager;
import org.jboss.seam.scheduling.quartz.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.scheduling.events.AbstractTimeEvent;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Base class for quartz jobs which fire scheduled events (including the built-in
 * second, minute and hourly events). Implementing classes must define type of the
 * event to be fired.
 * 
 * @author Peter Royle
 */
public abstract class AbstractTimeEventJob implements Job
{

    protected int value = 0;
    protected final GregorianCalendar gc = new GregorianCalendar();
    private Log log = LogFactory.getLog(SecondJob.class);

    /**
     * Implement this to return an instance of the appropriate event payload
     * to be used when firing the event.
     * @return an instance of the appropriate event type.
     */
    protected abstract AbstractTimeEvent createEventPayload();

    /**
     * Executes the internally scheduled job by firing the appropriate event with the
     * appropriate binding annotation (to in turn execute the application-specific jobs
     * which observe those events).
     * @param context Details about the job.
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        BeanManager manager = (BeanManager) context.getJobDetail().getJobDataMap().get(QuartzStarter.MANAGER_NAME);
        gc.setTime(new Date());
        final AbstractTimeEvent eventPayload = createEventPayload();
        for (Annotation binding : (Set<Annotation>)context.getJobDetail().getJobDataMap().get(QuartzStarter.BINDINGS)) {
            log.trace("Firing time event for " + eventPayload + " with binding " + binding);
            manager.fireEvent(eventPayload, binding);
        }
    }
}
