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
package org.jboss.seam.cron.scheduling.queuj;

import com.workplacesystems.queuj.Occurrence;
import com.workplacesystems.queuj.Queue;
import com.workplacesystems.queuj.QueueFactory;
import com.workplacesystems.queuj.process.QueujFactory;
import com.workplacesystems.queuj.process.java.JavaProcessBuilder;
import org.jboss.seam.cron.spi.SeamCronExtension;
import org.jboss.seam.cron.spi.queue.CronQueueProvider;
import org.jboss.seam.cron.spi.scheduling.CronSchedulingProvider;
import java.text.ParseException;
import java.util.Locale;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.seam.cron.impl.scheduling.exception.CronProviderDestructionException;
import org.jboss.seam.cron.impl.scheduling.exception.CronProviderInitialisationException;
import org.jboss.seam.cron.spi.CronProviderLifecycle;
import org.jboss.seam.cron.spi.scheduling.trigger.IntervalTriggerDetail;
import org.jboss.seam.cron.spi.scheduling.trigger.ScheduledTriggerDetail;
import org.jboss.seam.cron.spi.scheduling.trigger.TriggerDetail;
import org.jboss.seam.cron.spi.scheduling.trigger.TriggerSupplies;
import org.jboss.solder.logging.Logger;

/**
 * Methods of this class are called at various stages of the JSR-299 initialization
 * to set up and start the appropriate schedules in line with the scheduled events
 * being observed by the application.
 *
 * @author Dave Oxley
 */
public class QueuJScheduleProvider implements CronProviderLifecycle, CronSchedulingProvider {

    private static final Logger log = Logger.getLogger(QueuJScheduleProvider.class);
    @Inject
    BeanManager beanManager;
    @Inject
    SeamCronExtension cronExtension;

    /**
     * Initialises the scheduler.
     *
     */
    public void initProvider() throws CronProviderInitialisationException {
        try {
            QueujFactory.getProcessServer((String)null);
        } catch (Exception ex) {
            throw new CronProviderInitialisationException("Error initializing QueuJ scheduler", ex);
        }
    }

    public void processScheduledTrigger(final String queueId, final ScheduledTriggerDetail schedTriggerDetails) throws ParseException, InternalError {
        Occurrence runRelatively = new RunRelatively(schedTriggerDetails);
        scheduleJob(queueId, schedTriggerDetails, runRelatively);
    }

    public void processIntervalTrigger(final String queueId, final IntervalTriggerDetail intervalTriggerDetails) throws ParseException, InternalError {
        Occurrence runRelatively = new RunRelatively(intervalTriggerDetails);
        scheduleJob(queueId, intervalTriggerDetails, runRelatively);
    }

    private void scheduleJob(final String queueId, TriggerDetail triggerDetails, Occurrence occurence) {
        Queue<JavaProcessBuilder> queue = QueueFactory.DEFAULT_QUEUE;
        if (queueId != null) {
            CronQueueProvider queueProvider = cronExtension.getQueueProvider();
            queue = (Queue)queueProvider.getQueue(queueId);
        }
        JavaProcessBuilder jpb = queue.newProcessBuilder(Locale.getDefault());
        final String jobName = triggerDetails.toString() + "-trigger";
        jpb.setProcessName(jobName);
        jpb.setProcessOccurrence(occurence);
        TriggerSupplies triggerSupplies = new TriggerSupplies(beanManager, triggerDetails.getQualifier(), triggerDetails.getQualifiers());
        jpb.setProcessDetails(new TriggerRunner(), "execute", new Class[] { TriggerSupplies.class }, new Object[] { triggerSupplies });
        jpb.setProcessPersistence(false);
        jpb.newProcess();
    }

    /**
     * Shutdown the scheduler on application close.
     */
    public void destroyProvider() throws CronProviderDestructionException {
    }
}
