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
package org.jboss.seam.cron.provider.quartz;

import org.jboss.seam.cron.provider.spi.CronScheduleProvider;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.seam.cron.api.Every;
import org.jboss.seam.cron.api.TimeUnit;
import org.jboss.seam.cron.impl.exception.InternalException;
import org.jboss.seam.cron.impl.exception.SchedulerInitialisationException;
import org.jboss.seam.cron.provider.quartz.jobs.HourJob;
import org.jboss.seam.cron.provider.quartz.jobs.MinuteJob;
import org.jboss.seam.cron.provider.quartz.jobs.ScheduledEventJob;
import org.jboss.seam.cron.provider.quartz.jobs.SecondJob;
import org.jboss.seam.cron.provider.spi.trigger.HourTriggerHelper;
import org.jboss.seam.cron.provider.spi.trigger.IntervalTriggerDetail;
import org.jboss.seam.cron.provider.spi.trigger.MinuteTriggerHelper;
import org.jboss.seam.cron.provider.spi.trigger.ScheduledTriggerDetail;
import org.jboss.seam.cron.provider.spi.trigger.ScheduledTriggerHelper;
import org.jboss.seam.cron.provider.spi.trigger.SecondTriggerHelper;
import org.jboss.seam.cron.provider.spi.trigger.TriggerDetail;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobStore;
import org.quartz.spi.ThreadPool;

/**
 * Methods of this class are called at various stages of the JSR-299 initialisation
 * to set up and start the appropriate schedules in line with the scheduled events
 * being observed by the application.
 *
 * @author Peter Royle
 */
@ApplicationScoped
public class QuartzScheduleProvider implements CronScheduleProvider {

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
    public void initScheduler() throws Exception {
        instanceId = UUID.randomUUID();
        JobStore jobStore = new RAMJobStore();
        ThreadPool threadPool = new SimpleThreadPool(4, Thread.NORM_PRIORITY);
        threadPool.initialize();
        final DirectSchedulerFactory schedulerFactory = DirectSchedulerFactory.getInstance();
        schedulerName = SCHEDULER_NAME_PREFIX + "_" + instanceId.toString();
        schedulerFactory.createScheduler(schedulerName, instanceId.toString(), threadPool, jobStore);
        scheduler = schedulerFactory.getScheduler(schedulerName);
        scheduler.start();
    }

    public void processScheduledTrigger(final ScheduledTriggerDetail schedTriggerDetails) throws ParseException, SchedulerException, InternalError {
        Trigger schedTrigger = new CronTrigger(schedTriggerDetails.toString(), SCHEDULE_JOB_GROUP, schedTriggerDetails.getCronScheduleSpec());
        final Map jobParams = new HashMap();
        scheduleJob(schedTrigger, schedTriggerDetails, jobParams);
    }

    public void processIntervalTrigger(final IntervalTriggerDetail intervalTriggerDetails) throws ParseException, SchedulerException, InternalError {
        Trigger schedTrigger = null;
        if (TimeUnit.SECOND.equals(intervalTriggerDetails.getRepeatUnit())) {
            schedTrigger = TriggerUtils.makeSecondlyTrigger(intervalTriggerDetails.getRepeatInterval());
        } else if (TimeUnit.MINUTE.equals(intervalTriggerDetails.getRepeatUnit())) {
            schedTrigger = TriggerUtils.makeMinutelyTrigger(intervalTriggerDetails.getRepeatInterval());
        } else if (TimeUnit.HOUR.equals(intervalTriggerDetails.getRepeatUnit())) {
            schedTrigger = TriggerUtils.makeHourlyTrigger(intervalTriggerDetails.getRepeatInterval());
        } else {
            throw new InternalError("Could not work out which interval to use for the schedule of an @" + Every.class.getName() + " observer");
        }
        schedTrigger.setJobGroup(SCHEDULE_JOB_GROUP);
        final Map jobParams = new HashMap();
        scheduleJob(schedTrigger, intervalTriggerDetails, jobParams);
    }

    /**
     * Shutdown the scheduler on application close.
     */
    public void stopScheduler() {
        try {
            getScheduler().shutdown();
        } catch (SchedulerException ex) {
            log.error("Error shutting down scheduler", ex);
        }
    }

    /**
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
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
    private void scheduleJob(Trigger schedTrigger, final TriggerDetail triggerDetails,
            Map jobParams) throws SchedulerInitialisationException {

        // common Second payload sample and start time
        GregorianCalendar gc = new GregorianCalendar();
        gc.add(GregorianCalendar.SECOND, 1);
        Date startTime = new Date(gc.getTimeInMillis());
        schedTrigger.setStartTime(startTime);

        jobParams.put(QUALIFIER, triggerDetails.getQualifier());

        final String jobName = triggerDetails.toString() + "-trigger";
        schedTrigger.setName(jobName);

        // TODO: (PR): this has gone wrong. Will fix it shortly.
        Class jobKlass = null;
        if (triggerDetails.getTriggerHelper() instanceof ScheduledTriggerHelper) {
            jobKlass = ScheduledEventJob.class;
        } else if (triggerDetails.getTriggerHelper() instanceof SecondTriggerHelper) {
            jobKlass = SecondJob.class;
        } else if (triggerDetails.getTriggerHelper() instanceof MinuteTriggerHelper) {
            jobKlass = MinuteJob.class;
        } else if (triggerDetails.getTriggerHelper() instanceof HourTriggerHelper) {
            jobKlass = HourJob.class;
        } else {
            throw new InternalException("wtf");
        }

        triggerDetails.getTriggerHelper();
        JobDetail job = new JobDetail(jobName, schedTrigger.getGroup(), jobKlass);
        job.setJobDataMap(new JobDataMap());
        job.getJobDataMap().put(MANAGER_NAME, beanManager);
        job.getJobDataMap().putAll(jobParams);
        try {
            getScheduler().scheduleJob(job, schedTrigger);
        } catch (SchedulerException e) {
            throw new SchedulerInitialisationException("Error scheduling job " + jobName + " with Quartz provider", e);
        }
        log.info("Scheduler for " + jobName + " initialised");
    }
}
