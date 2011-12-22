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

import org.jboss.seam.cron.spi.scheduling.trigger.TriggerSupplies;
import org.jboss.seam.cron.spi.scheduling.CronSchedulingProvider;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.seam.cron.api.scheduling.Every;
import org.jboss.seam.cron.impl.scheduling.exception.CronProviderDestructionException;
import org.jboss.seam.cron.impl.scheduling.exception.CronProviderInitialisationException;
import org.jboss.seam.cron.spi.CronProviderLifecycle;
import org.jboss.seam.cron.spi.scheduling.trigger.IntervalTriggerDetail;
import org.jboss.seam.cron.spi.scheduling.trigger.ScheduledTriggerDetail;
import org.jboss.seam.cron.spi.scheduling.trigger.TriggerDetail;
import org.jboss.solder.logging.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobStore;
import org.quartz.spi.ThreadPool;
import static org.jboss.seam.cron.api.scheduling.Interval.*;

/**
 * Methods of this class are called at various stages of the JSR-299 initialization
 * to set up and start the appropriate schedules in line with the scheduled events
 * being observed by the application.
 *
 * @author Peter Royle
 */
public class QuartzScheduleProvider implements CronProviderLifecycle, CronSchedulingProvider {

    /**
     * The name of the property containing the observer method bindings to be used
     * when storing and retrieving it from the job details.
     */
    public static final String QUALIFIER = "qualifier";
    /**
     * The name of the job group for all arbitrarily scheduled events.
     */
    public static final String SCHEDULE_JOB_GROUP = "schedule_job_group";
    /**
     * The name of the JSR-299 BeanManager instance when a reference to it is
     * stored and retrieved from the job details.
     */
    public static final String MANAGER_NAME = "manager";
    public static final String TRIGGER_SUPPLIES = "trigger_helper";
    private static final String SCHEDULER_NAME_PREFIX = "SeamCronScheduler";
    private String schedulerName;
    private Scheduler scheduler;
    private UUID instanceId;
    private static final Logger log = Logger.getLogger(QuartzScheduleProvider.class);
    @Inject
    BeanManager beanManager;

    /**
     * Initialises the scheduler.
     *
     */
    public void initProvider() throws CronProviderInitialisationException {
        instanceId = UUID.randomUUID();
        final JobStore jobStore = new RAMJobStore();
        final ThreadPool threadPool = new SimpleThreadPool(100, Thread.NORM_PRIORITY);
        try {
            threadPool.initialize();
        } catch (SchedulerConfigException ex) {
            throw new CronProviderInitialisationException("Error initializing Quartz ThreadPool", ex);
        }
        final DirectSchedulerFactory schedulerFactory = DirectSchedulerFactory.getInstance();
        schedulerName = SCHEDULER_NAME_PREFIX + "_" + instanceId.toString();
        try {
            schedulerFactory.createScheduler(schedulerName, instanceId.toString(), threadPool, jobStore);
            scheduler = schedulerFactory.getScheduler(schedulerName);
            scheduler.start();
        } catch (SchedulerException ex) {
            throw new CronProviderInitialisationException("Error initializing Quartz scheduler", ex);
        }
    }

    public void processScheduledTrigger(final String queueId, final ScheduledTriggerDetail schedTriggerDetails) throws ParseException, SchedulerException, InternalError {
        final Trigger schedTrigger = new CronTrigger(schedTriggerDetails.toString(), SCHEDULE_JOB_GROUP, schedTriggerDetails.getCronScheduleSpec());
        startInOneSecond(schedTrigger);
        scheduleJob(schedTrigger, schedTriggerDetails);
    }

    public void processIntervalTrigger(final String queueId, final IntervalTriggerDetail intervalTriggerDetails) throws ParseException, SchedulerException, InternalError {
        Trigger schedTrigger = null;
        // start at the beginning of the next second at the earliest
        GregorianCalendar gc = getOneSecondLater();
        if (SECOND.equals(intervalTriggerDetails.getRepeatUnit())) {
            schedTrigger = TriggerUtils.makeSecondlyTrigger(intervalTriggerDetails.getRepeatInterval());
        } else if (MINUTE.equals(intervalTriggerDetails.getRepeatUnit())) {
            schedTrigger = TriggerUtils.makeMinutelyTrigger(intervalTriggerDetails.getRepeatInterval());
            // start at the beginning of the next minute
            gc.add(GregorianCalendar.MINUTE, 1);
            gc.set(GregorianCalendar.SECOND, 0);
        } else if (HOUR.equals(intervalTriggerDetails.getRepeatUnit())) {
            schedTrigger = TriggerUtils.makeHourlyTrigger(intervalTriggerDetails.getRepeatInterval());
            // start at the beginning of the next hour
            gc.add(GregorianCalendar.HOUR, 1);
            gc.set(GregorianCalendar.MINUTE, 0);
            gc.set(GregorianCalendar.SECOND, 0);
        } else {
            throw new InternalError("Could not work out which interval to use for the schedule of an @" + Every.class.getName() + " observer");
        }
        schedTrigger.setJobGroup(SCHEDULE_JOB_GROUP);
        schedTrigger.setStartTime(gc.getTime());
        scheduleJob(schedTrigger, intervalTriggerDetails);
    }

    /**
     * Shutdown the scheduler on application close.
     */
    public void destroyProvider() throws CronProviderDestructionException {
        try {
            getScheduler().shutdown();
        } catch (SchedulerException ex) {
            log.warn("Error shutting down scheduler", ex);
        }
    }

    /**
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    private GregorianCalendar startInOneSecond(final Trigger schedTrigger) {
        GregorianCalendar gc = getOneSecondLater();
        final Date startTime = new Date(gc.getTimeInMillis());
        schedTrigger.setStartTime(startTime);
        return gc;
    }

    private GregorianCalendar getOneSecondLater() {
        // common Second payload sample and start time
        final GregorianCalendar gc = new GregorianCalendar();
        gc.add(GregorianCalendar.SECOND, 1);
        gc.set(GregorianCalendar.MILLISECOND, 0);
        return gc;
    }

    /**
     * Construct the job details using the given parameter map and schedule the job
     * to be executed by the given job class using the given trigger.
     *
     * @param jobName   The name of the job to be executed.
     * @param manager   The BeanManager implementation.
     * @param trigger   The trigger representing the schedule of the job.
     * @param jobKlass  The class which will execute the job on schedule.
     * @param jobParams The parameters to be passed to the job executor.
     * @throws SchedulerException
     */
    private void scheduleJob(final Trigger schedTrigger, final TriggerDetail triggerDetails) throws CronProviderInitialisationException {

        final String jobName = triggerDetails.toString() + "-trigger";
        schedTrigger.setName(jobName);

        final JobDetail job = new JobDetail(jobName, schedTrigger.getGroup(), TriggerJob.class);
        job.setJobDataMap(new JobDataMap());
        job.getJobDataMap().put(TRIGGER_SUPPLIES, new TriggerSupplies(beanManager, triggerDetails.getQualifier(), triggerDetails.getQualifiers()));
        try {
            getScheduler().scheduleJob(job, schedTrigger);
        } catch (SchedulerException e) {
            throw new CronProviderInitialisationException("Error scheduling job " + jobName + " with Quartz provider", e);
        }
        log.info("Scheduler for " + jobName + " initialised");
    }
}
