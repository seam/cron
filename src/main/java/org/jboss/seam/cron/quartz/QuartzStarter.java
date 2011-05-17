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
package org.jboss.seam.cron.quartz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;
import org.jboss.seam.cron.api.Every;
import org.jboss.seam.cron.api.Scheduled;
import org.jboss.seam.cron.api.TimeUnit;
import static org.jboss.seam.cron.api.TimeUnit.*;
import org.jboss.seam.cron.impl.exception.SchedulerConfigurationException;
import org.jboss.seam.cron.impl.exception.SchedulerInitialisationException;
import org.jboss.seam.cron.provider.quartz.jobs.HourJob;
import org.jboss.seam.cron.provider.quartz.jobs.MinuteJob;
import org.jboss.seam.cron.provider.quartz.jobs.ScheduledQualifiedEventPayload;
import org.jboss.seam.cron.provider.quartz.jobs.ScheduledEventJob;
import org.jboss.seam.cron.provider.quartz.jobs.SecondJob;
import org.jboss.seam.cron.impl.util.SchedulePropertiesManager;
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
public class QuartzStarter
        implements Extension {

    /**
     * The name of the property containing the observer method bindings to be used
     * when storing and retrieving it from the job details.
     */
    public static final String QUALIFIER = "qualifier";
    /**
     * The name of the property containing the schedule specification (in cron format)
     * when storing and retrieving it from the job details.
     */
    public static final String CRON_SCHEDULE_SPEC = "cronScheduleSpec";
    /**
     * The name of the job group for the Second, Minute and Hour events.
     */
    public static final String TICKER_JOB_GROUP = "ticker_job_group";
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
    private final Set<ObserverMethod<? super Trigger>> allObservers = new HashSet<ObserverMethod<? super Trigger>>();
    private Scheduler scheduler;
    private UUID instanceId;
    private static final Logger log = Logger.getLogger(QuartzStarter.class);

    /**
     * Initialises the scheduler.
     *
     * @param afterDisc The initialisation event being observed.
     */
    public void initTicker(@Observes AfterBeanDiscovery afterDisc) throws Exception {
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

    /**
     * Initialises schedulers for all of the observed scheduled events.
     *
     * @param afterValid The observed event.
     * @param manager    The JSR-299 Bean Manager.
     */
    public void startJobs(@Observes AfterDeploymentValidation afterValid, BeanManager manager) {
        try {
            // common Second payload sample and start time
            GregorianCalendar gc = new GregorianCalendar();
            gc.add(GregorianCalendar.SECOND, 1);

            Date startTime = new Date(gc.getTimeInMillis());

            // arbitrarily scheduled events.
            scheduleScheduledEvents(manager, startTime);
        } catch (SchedulerException ex) {
            throw new SchedulerInitialisationException("Error setting up scheduler.", ex);
        } catch (ParseException pe) {
            throw new SchedulerInitialisationException("Error parsing schedule while initialising scheduler.", pe);
        }
    }

    /**
     * Shutdown the scheduler on application close.
     */
    @PreDestroy
    public void stopTicker(@Observes BeforeShutdown event) {
        try {
            getScheduler().shutdown();
        } catch (SchedulerException ex) {
            log.error("Error shutting down scheduler", ex);
        }
    }

    public void registerCronEventObserver(@Observes ProcessObserverMethod pom) {
        allObservers.add(pom.getObserverMethod());
    }

    /**
     * @return the scheduler
     */
    @Produces
    public Scheduler getScheduler() {
        return scheduler;
    }

    private Scheduled getScheduledBinding(Annotation binding) {
        Scheduled schedBinding = null;
        if (binding instanceof Scheduled) {
            schedBinding = (Scheduled) binding;
        } else {
            // check for a @Scheduled meta-annotation
            Scheduled scheduled = binding.annotationType().getAnnotation(Scheduled.class);

            if (scheduled != null) {
                schedBinding = scheduled;
            }
        }
        return schedBinding;
    }

    private Every getEveryBinding(Annotation binding) {
        Every everyBinding = null;
        if (binding instanceof Every) {
            everyBinding = (Every) binding;
        } else {
            // check for a @Scheduled meta-annotation
            Every every = binding.annotationType().getAnnotation(Every.class);

            if (every != null) {
                everyBinding = every;
            }
        }
        return everyBinding;
    }

    /**
     * Inspects the given @Every qualifier and extracts its settings into a new #{@link ScheduledQualifiedEventPayload}.
     * 
     * @param everyBinding
     * @return a fully populated #{@link ScheduledQualifiedEventPayload}.
     */
    public ScheduledQualifiedEventPayload createScheduledQualifiedEventPayloadFromEveryBinding(final Every everyBinding) {
        Class jobClass = null;
        switch (everyBinding.value()) {
            case SECOND:
                jobClass = SecondJob.class;
                break;
            case MINUTE:
                jobClass = MinuteJob.class;
                break;
            case HOUR:
                jobClass = HourJob.class;
        }
        return new ScheduledQualifiedEventPayload(everyBinding, jobClass);
    }
    
    /**
     * If the given String is already a schedule then just return it, otherwise check the 
     * scheduler.properties file for schedule spec with the given name and return that.
     * @param scheduleSpec
     * @return
     * @throws SchedulerConfigurationException 
     */
    private String lookupNamedScheduleIfNecessary(final String scheduleSpec)
            throws SchedulerConfigurationException {
        final String cronScheduleSpec;

        if (scheduleSpec.contains(" ")) {
            cronScheduleSpec = scheduleSpec;
        } else {
            Properties schedProperties = SchedulePropertiesManager.instance().getScheduleProperties();
            cronScheduleSpec = schedProperties.getProperty(scheduleSpec);

            if (StringUtils.isEmpty(cronScheduleSpec)) {
                throw new SchedulerConfigurationException("Found empty or missing cron definition for named scheule '"
                        + scheduleSpec + "'. Should be specified in the file "
                        + SchedulePropertiesManager.SCHEDULE_PROPERTIES_PATH
                        + " on the classpath.");
            }
        }

        return cronScheduleSpec;
    }

    /**
     * Construct the job details using the given parameter map and chedule the job
     * to be executed by the given job class using the given trigger.
     *
     * @param jobName   The name of the job to be executed.
     * @param manager   The BeanManager implementation.
     * @param trigger   The trigger representing the schedule of the job.
     * @param jobKlass  The class which will execute the job on schedule.
     * @param jobParams The parameters to be passed to the job executor.
     * @throws SchedulerException
     */
    private void scheduleJob(final String jobName, BeanManager manager, final Trigger trigger, final Class jobKlass,
            Map jobParams)
            throws SchedulerException {
        JobDetail job = new JobDetail(jobName,
                trigger.getGroup(),
                jobKlass);
        job.setJobDataMap(new JobDataMap());
        job.getJobDataMap().put(MANAGER_NAME, manager);
        job.getJobDataMap().putAll(jobParams);
        getScheduler().scheduleJob(job, trigger);
        log.info("Scheduler for " + jobName + " initialised");
    }

    /**
     * Set up schedule for an arbitrarily scheduled event. This will pass the given
     * observerBinding binding type as the QUALIFIER job parameter to be used when firing
     * the events. This binding type will usually be an instance of @Scheduled or
     * some other binding type with the @Scheduled meta-annotation.
     *
     * @param schedQualEvtPld  The schedule specification in cron format, plus the qualifier annotations and event payload type.
     * @param manager          The BeanManager implementation.
     * @param startTime        The time to start the schedule.
     * @throws SchedulerException
     */
    private void scheduleJobForEvent(final ScheduledQualifiedEventPayload schedQualEvtPld, Date startTime, BeanManager manager)
            throws ParseException, SchedulerException {
        final String name = schedQualEvtPld.toString();
        log.info("Scheduling trigger for " + name);

        Trigger schedTrigger = null;
        if (schedQualEvtPld.isInterval()) {
            if (TimeUnit.SECOND.equals(schedQualEvtPld.getRepeatUnit())) {
                schedTrigger = TriggerUtils.makeSecondlyTrigger(schedQualEvtPld.getRepeatInterval());
            } else if (TimeUnit.MINUTE.equals(schedQualEvtPld.getRepeatUnit())) {
                schedTrigger = TriggerUtils.makeMinutelyTrigger(schedQualEvtPld.getRepeatInterval());
            } else if (TimeUnit.HOUR.equals(schedQualEvtPld.getRepeatUnit())) {
                schedTrigger = TriggerUtils.makeHourlyTrigger(schedQualEvtPld.getRepeatInterval());
            } else {
                throw new InternalError("Could not work out which interval to use for the schedule of an @Every observer");
            }
            schedTrigger.setName(name);
        } else {
            schedTrigger = new CronTrigger(name, SCHEDULE_JOB_GROUP, schedQualEvtPld.getScheduleSpec());
        }
        schedTrigger.setStartTime(startTime);

        final Map jobParams = new HashMap();
        jobParams.put(CRON_SCHEDULE_SPEC, schedQualEvtPld.getScheduleSpec());
        jobParams.put(QUALIFIER, schedQualEvtPld.getQualifier());
        scheduleJob(name + "-trigger", manager, schedTrigger, schedQualEvtPld.getPayloadType(), jobParams);
    }

    /**
     * Start scheduler for Event as per observers found (if any).
     */
    private void scheduleScheduledEvents(BeanManager manager, Date startTime)
            throws SchedulerException, SchedulerInitialisationException, ParseException {
        Set<ScheduledQualifiedEventPayload> schedulesFound = new HashSet<ScheduledQualifiedEventPayload>();

        // collect the set of unique schedule specifications
        for (ObserverMethod<?> obsMeth : allObservers) {
            for (Object bindingObj : obsMeth.getObservedQualifiers()) {
                final Annotation orginalQualifier = (Annotation) bindingObj;
                final Scheduled schedBinding = getScheduledBinding(orginalQualifier);
                Every everyBinding = getEveryBinding(orginalQualifier);
                ScheduledQualifiedEventPayload payload = null;
                        
                // observer is only meaningful if the payload is the right type
                Type observedType = obsMeth.getObservedType();
                //if (org.jboss.seam.cron.events.Trigger.class.isAssignableFrom(obsMeth.getObservedType())) {

                // if we've found s scheduled event, record its bindings against
                // the cron formatted schedule specification so that it can be fired according
                // to the apropriate schedule.
                if (schedBinding != null) {
                    String cronScheduleSpec = lookupNamedScheduleIfNecessary(schedBinding.value());
                    payload = new ScheduledQualifiedEventPayload(cronScheduleSpec, orginalQualifier, ScheduledEventJob.class);
                }
                if (everyBinding != null) {
                    payload = createScheduledQualifiedEventPayloadFromEveryBinding(everyBinding);
                }
                
                if (payload != null) {
                    schedulesFound.add(payload);
                }
                    
//                } else {
//                    log.warn("Ignoring observer method " + obsMeth.toString() + " - Payload is of the wron type. "
//                            + "Is " + observedType.toString() + " but should be " + Trigger.class.toString());
//                }
            }
        }

        if (schedulesFound.size() > 0) {
            // set up a schedule for each unique schedule spec found
            for (ScheduledQualifiedEventPayload schedQualEvtPld : schedulesFound) {
                scheduleJobForEvent(schedQualEvtPld, startTime, manager);
            }
        } else {
            log.info("Skipping initilization of scheduler - No registered observers.");
        }
    }
}
