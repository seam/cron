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

import org.apache.commons.lang.StringUtils;

import org.jboss.seam.cron.annotations.EveryBinding;
import org.jboss.seam.cron.annotations.Scheduled;
import org.jboss.seam.cron.events.Hour;
import org.jboss.seam.cron.events.Minute;
import org.jboss.seam.cron.events.Second;
import org.jboss.seam.cron.exception.SchedulerConfigurationException;
import org.jboss.seam.cron.exception.SchedulerInitialisationException;
import org.jboss.seam.cron.quartz.jobs.HourJob;
import org.jboss.seam.cron.quartz.jobs.MinuteJob;
import org.jboss.seam.cron.quartz.jobs.ScheduledEventJob;
import org.jboss.seam.cron.quartz.jobs.SecondJob;
import org.jboss.seam.cron.util.SchedulePropertiesManager;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import org.quartz.impl.StdSchedulerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;

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

/**
 * Methods of this class are called at various stages of the JSR-299 initialisation
 * to set up and start the appropriate schedules in line with the scheduled events
 * being observed by the application.
 *
 * @author Peter Royle
 */
@ApplicationScoped
public class QuartzStarter
    implements Extension
{
    /**
     * The name of the property containing the observer method bindings to be used
     * when storing and retreiving it from the job details.
     */
    public static final String BINDINGS = "binding";

    /**
     * The name of the property containing the schedule specification (in cron format)
     * when storing and retreiving it from the job details.
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
    private static final String BEANMAN_CLASSNAME_WEBBEANS = "org.jboss.weld.manager.BeanManagerImpl";
    private Scheduler scheduler;
    private Logger log = LoggerFactory.getLogger( QuartzStarter.class );

    /**
     * Initialises the scheduler.
     * @param afterDisc The initialisation event being observed.
     */
    public void initTicker( @Observes
    AfterBeanDiscovery afterDisc )
    {
        try
        {
            scheduler = StdSchedulerFactory.getDefaultScheduler(  );
            getScheduler(  ).start(  );
        } catch ( SchedulerException ex )
        {
            log.error( "Error initialising/starting scheduler", ex );
        }
    }

    /**
     * Initialises schedulers for all of the observed scheduled events.
     * @param afterValid The observed event.
     * @param manager The JSR-299 Bean Manager.
     */
    public void startJobs( @Observes
    AfterDeploymentValidation afterValid, BeanManager manager )
    {
        try
        {
            // common Second payload sample and start time
            GregorianCalendar gc = new GregorianCalendar(  );
            gc.add( GregorianCalendar.SECOND, 1 );

            Date startTime = new Date( gc.getTimeInMillis(  ) );

            // Start scheduler for Second every second
            final Trigger secondTrigger =
                new SimpleTrigger( "jcdiTickerTrigger", TICKER_JOB_GROUP, startTime, null,
                                   SimpleTrigger.REPEAT_INDEFINITELY, 1000 );
            Set tickObservers = manager.resolveObserverMethods( new Second( 0, 0 ),
                                                                new EveryBinding(  ) );
            scheduleTicks( tickObservers, "second-trigger", manager, secondTrigger, SecondJob.class );

            // Start scheduler for Minute every minute
            Set minTickObservers = manager.resolveObserverMethods( new Minute( 0, 0 ),
                                                                   new EveryBinding(  ) );
            final CronTrigger minTrigger =
                new CronTrigger( "jcdiMinutelyTickerTrigger", TICKER_JOB_GROUP, "0 * * ? * *" );
            minTrigger.setStartTime( startTime );
            scheduleTicks( minTickObservers, "minute-trigger", manager, minTrigger, MinuteJob.class );

            // Start scheduler for Hour every hour
            Set hrlyTickObservers = manager.resolveObserverMethods( new Hour( 0, 0 ),
                                                                    new EveryBinding(  ) );
            final CronTrigger hrTrigger = new CronTrigger( "jcdiHourlyTickerTrigger", TICKER_JOB_GROUP, "0 0 * ? * *" );
            hrTrigger.setStartTime( startTime );
            scheduleTicks( hrlyTickObservers, "hour-trigger", manager, hrTrigger, HourJob.class );

            // arbitrarily scheduled events.
            scheduleScheduledEvents( manager, startTime );
        } catch ( SchedulerException ex )
        {
            throw new SchedulerInitialisationException( "Error setting up scheduler.", ex );
        } catch ( ParseException pe )
        {
            throw new SchedulerInitialisationException( "Error parsing schedule while initialising scheduler.", pe );
        }
    }

    /**
     * Shutdown the scheduler on application close.
     */
    @PreDestroy
    public void stopTicker( @Observes
    BeforeShutdown event )
    {
        try
        {
            getScheduler(  ).shutdown(  );
        } catch ( SchedulerException ex )
        {
            log.error( "Error shutting down scheduler", ex );
        }
    }

    /**
     * @return the scheduler
     */
    @Produces
    public Scheduler getScheduler(  )
    {
        return scheduler;
    }

    private List<ObserverMethod<?>> getRegisteredObservers( BeanManager manager )
                                                    throws SchedulerInitialisationException
    {
        // TODO(PR): can we remove this compile-time dependency on WebBeans' EventObserver<?> using reflection? Also if we could loose ConcurrentSetMultiMap that would be good.
        final List<ObserverMethod<?>> registeredObservers;

        if ( manager.getClass(  ).getName(  ).equals( BEANMAN_CLASSNAME_WEBBEANS ) )
        {
            try
            {
                registeredObservers = (List<ObserverMethod<?>>) manager.getClass(  ).getDeclaredMethod( "getObservers" )
                                                                       .invoke( manager );
            } catch ( Exception ex )
            {
                throw new SchedulerInitialisationException( "Error gettng list of observers from BeanManager (Web Beans implementstion)",
                                                            ex );
            }
        } else
        {
            throw new SchedulerInitialisationException( "Unknown BeanManager implementation: " + manager.getClass(  ) +
                                                        ". Please raise this as a feature request, stating the JSR-299 implementation in use if possible." );
        }

        return registeredObservers;
    }

    private String lookupNamedScheduleIfNecessary( final String scheduleSpec )
                                           throws SchedulerConfigurationException
    {
        final String cronScheduleSpec;

        if ( scheduleSpec.contains( " " ) )
        {
            cronScheduleSpec = scheduleSpec;
        } else
        {
            Properties schedProperties = SchedulePropertiesManager.instance(  ).getScheduleProperties(  );
            cronScheduleSpec = schedProperties.getProperty( scheduleSpec );

            if ( StringUtils.isEmpty( cronScheduleSpec ) )
            {
                throw new SchedulerConfigurationException( "Found empty or missing cron definition for named scheule '" +
                                                           scheduleSpec + "'. Should be specified in the file " +
                                                           SchedulePropertiesManager.SCHEDULE_PROPERTIES_PATH +
                                                           " on the classpath." );
            }
        }

        return cronScheduleSpec;
    }

    /**
     * Construct the job details using the given parameter map and chedule the job
     * to be executed by the given job class using the given trigger.
     * @param jobName The name of the job to be executed.
     * @param manager The BeanManager implementation.
     * @param trigger The trigger representing the schedule of the job.
     * @param jobKlass The class which will execute the job on schedule.
     * @param jobParams The parameters to be passed to the job executor.
     * @throws SchedulerException
     */
    private void scheduleJob( final String jobName, BeanManager manager, final Trigger trigger, final Class jobKlass,
                              Map jobParams )
                      throws SchedulerException
    {
        JobDetail job = new JobDetail( jobName,
                                       trigger.getGroup(  ),
                                       jobKlass );
        job.setJobDataMap( new JobDataMap(  ) );
        job.getJobDataMap(  ).put( MANAGER_NAME, manager );
        job.getJobDataMap(  ).putAll( jobParams );
        getScheduler(  ).scheduleJob( job, trigger );
        log.info( "Scheduler for " + jobName + " initialised" );
    }

    /**
     * Set up schedule for a given type of tick event (second, minute or hourly) but only if
     * the given set of observers of that tick is not empty. This will pass an instance
     * of the @Every binding type as the BINDINGS job parameter to be used when firing
     * the events.
     * @param tickObservers The set of observers of this event type (second, minute or hourly).
     * @param jobName The name of the job.
     * @param manager The BeanManager implementation.
     * @param trigger The trigger representing the schedule of the job.
     * @param jobKlass The class which will handle execution of the job on schedule.
     * @throws SchedulerException
     */
    private void scheduleTicks( Set<Observer> tickObservers, final String jobName, BeanManager manager,
                                final Trigger trigger, final Class jobKlass )
                        throws SchedulerException
    {
        if ( ! tickObservers.isEmpty(  ) )
        {
            Map jobParams = new HashMap( 1 );
            Set<Annotation> oneEveryBinding = new HashSet<Annotation>( 1 );
            oneEveryBinding.add( new EveryBinding(  ) );
            jobParams.put( BINDINGS, oneEveryBinding );
            scheduleJob( jobName, manager, trigger, jobKlass, jobParams );
        } else
        {
            log.info( "Skipping initilization of scheduler for " + jobName + " - No registered observers." );
        }
    }

    /**
     * Set up schedule for an arbitrarily scheduled event. This will pass the given
     * observerBinding binding type as the BINDINGS job parameter to be used when firing
     * the events. This binding type will usually be an instance of @Scheduled or
     * some other binding type with the @Scheduled meta-annotation.
     * @param scheduleSpec The schedule specification in cron format or the name of a named schedule.
     * @param observerBindings The set of bindings found on the observers which will therefore be used when firing the events.
     * @param manager The BeanManager implementation.
     * @param startTime The time to start the schedule.
     * @throws SchedulerException
     */
    private void scheduleJobForEvent( final String cronScheduleSpec, final Set<Annotation> observerBindings,
                                      Date startTime, BeanManager manager )
                              throws ParseException, SchedulerException
    {
        final String name = "jcdiScheduledEventTrigger(" + cronScheduleSpec + ")";
        log.info( "Scheduling trigger for " + name );

        final Trigger schedTrigger = new CronTrigger( name, SCHEDULE_JOB_GROUP, cronScheduleSpec );
        schedTrigger.setStartTime( startTime );

        final Map jobParams = new HashMap(  );
        jobParams.put( CRON_SCHEDULE_SPEC, cronScheduleSpec );
        jobParams.put( BINDINGS, observerBindings );
        scheduleJob( name + "-trigger", manager, schedTrigger, ScheduledEventJob.class, jobParams );
    }

    /**
     * Start scheduler for Event as per observers found (if any).
     */
    private void scheduleScheduledEvents( BeanManager manager, Date startTime )
                                  throws SchedulerException, SchedulerInitialisationException, ParseException
    {
        final List<ObserverMethod<?>> registeredObservers = getRegisteredObservers( manager );
        Map<String, Set<Annotation>> schedulesFound = new HashMap<String, Set<Annotation>>(  );

        // collect the set of unique schedule specifications
        for ( ObserverMethod<?> obsMeth : registeredObservers )
        {
            for ( Object bindingObj : obsMeth.getObservedQualifiers(  ) )
            {
                Annotation binding = (Annotation) bindingObj;
                Scheduled schedBinding = null;

                if ( binding instanceof Scheduled )
                {
                    schedBinding = (Scheduled) binding;
                } else
                {
                    // check for a @Scheduled meta-annotation
                    Scheduled scheduled = binding.annotationType(  ).getAnnotation( Scheduled.class );

                    if ( scheduled != null )
                    {
                        schedBinding = scheduled;
                    }
                }

                // if we've found an arbitrarily scheduled event, record it's bindings against
                // the cron formatted schedule specification so that it can be fired according
                // to the apropriate schedule.
                if ( schedBinding != null )
                {
                    final String cronScheduleSpec = lookupNamedScheduleIfNecessary( schedBinding.value(  ) );
                    Set<Annotation> existingAnnotations = schedulesFound.get( cronScheduleSpec );

                    if ( existingAnnotations == null )
                    {
                        existingAnnotations = new HashSet<Annotation>(  );
                    }

                    existingAnnotations.add( binding );
                    schedulesFound.put( cronScheduleSpec, existingAnnotations );
                }
            }
        }

        if ( schedulesFound.size(  ) > 0 )
        {
            // set up a schedule for each unique schedule spec found
            for ( String cronScheduleSpec : schedulesFound.keySet(  ) )
            {
                scheduleJobForEvent( cronScheduleSpec,
                                     schedulesFound.get( cronScheduleSpec ),
                                     startTime,
                                     manager );
            }
        } else
        {
            log.info( "Skipping initilization of scheduler for arbitrarily scheduled events - No registered observers." );
        }
    }
}
