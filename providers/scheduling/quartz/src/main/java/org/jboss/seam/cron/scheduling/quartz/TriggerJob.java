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
package org.jboss.seam.cron.scheduling.quartz;

import org.jboss.seam.cron.spi.scheduling.trigger.ProviderContextTriggerSupport;


import org.jboss.seam.cron.scheduling.quartz.QuartzScheduleProvider;
import org.jboss.seam.cron.spi.scheduling.trigger.TriggerSupplies;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Base class for firing a Trigger via a Quartz Job via the extended #{@link ProviderContextTriggerSupport}.
 *
 * @author Peter Royle
 */
public class TriggerJob extends ProviderContextTriggerSupport<JobExecutionContext>
        implements Job {

    @Override
    public TriggerSupplies fetchTriggerSupplies(final JobExecutionContext context) {
        return (TriggerSupplies) context.getJobDetail().getJobDataMap().get(QuartzScheduleProvider.TRIGGER_SUPPLIES);
    }

    /**
     * Executes the firing of the trigger payload via the delegate #{@link ProviderContextTriggerSupport}
     * when told to do so by the Quartz scheduler.
     * 
     * @param context
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        fireTrigger(context);
    }


}
