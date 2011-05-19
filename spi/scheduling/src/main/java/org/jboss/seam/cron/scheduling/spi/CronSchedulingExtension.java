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
package org.jboss.seam.cron.scheduling.spi;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;
import org.jboss.seam.cron.scheduling.api.Every;
import org.jboss.seam.cron.scheduling.api.Scheduled;
import org.jboss.seam.cron.scheduling.api.Trigger;
import org.jboss.seam.cron.scheduling.impl.exception.SchedulerConfigurationException;
import org.jboss.seam.cron.scheduling.impl.exception.SchedulerInitialisationException;
import org.jboss.seam.cron.util.AnnotaitonUtils;
import org.jboss.seam.cron.scheduling.impl.util.SchedulePropertiesManager;
import org.jboss.seam.cron.scheduling.spi.trigger.IntervalTriggerDetail;
import org.jboss.seam.cron.scheduling.spi.trigger.ScheduledTriggerDetail;

/**
 *
 * @author peteroyle
 */
@ApplicationScoped
public class CronSchedulingExtension
        implements Extension {

    private final Set<ObserverMethod<? super Trigger>> allObservers = new HashSet<ObserverMethod<? super Trigger>>();
    private Logger log = Logger.getLogger(CronSchedulingExtension.class);

    public void registerCronEventObserver(@Observes ProcessObserverMethod pom) {
        allObservers.add(pom.getObserverMethod());
    }

    /**
     * Initialises the scheduler.
     *
     */
    public void initProviderScheduler(@Observes AfterBeanDiscovery afterDisc, BeanManager beanManager, CronScheduleProvider scheduleProvider) throws Exception {
        scheduleProvider.initScheduler();
    }

    /**
     * Initialises schedulers for all of the observed scheduled events.
     *
     * @param afterValid The observed event.
     * @param manager    The JSR-299 Bean Manager.
     */
    public void processScheduledTriggers(@Observes AfterDeploymentValidation afterValid, BeanManager manager, CronScheduleProvider scheduleProvider) {
        try {
            // collect the set of unique schedule specifications
            for (ObserverMethod<?> obsMeth : allObservers) {
                for (Object bindingObj : obsMeth.getObservedQualifiers()) {
                    final Annotation orginalQualifier = (Annotation) bindingObj;
                    final Scheduled schedQualifier = (Scheduled) AnnotaitonUtils.getQualifier(orginalQualifier, Scheduled.class);
                    final Every everyQualifier = (Every) AnnotaitonUtils.getQualifier(orginalQualifier, Every.class);
                    // gather the details of all @Scheduled and @Every triggers
                    if (schedQualifier != null) {
                        String cronScheduleSpec = lookupNamedScheduleIfNecessary(schedQualifier.value());
                        ScheduledTriggerDetail payload = new ScheduledTriggerDetail(cronScheduleSpec, orginalQualifier);
                        scheduleProvider.processScheduledTrigger(payload);
                    }
                    if (everyQualifier != null) {
                        IntervalTriggerDetail payload = createEventPayloadFromEveryBinding(everyQualifier);
                        scheduleProvider.processIntervalTrigger(payload);
                    }
                }
            }


        } catch (Throwable t) {
            throw new SchedulerInitialisationException("Error registering schedules with underlying provider", t);
        }
    }

    /**
     * Shutdown the scheduler on application close.
     */
    @PreDestroy
    public void stopProvidersScheduler(@Observes BeforeShutdown event, CronScheduleProvider scheduleProvider) {
        scheduleProvider.stopScheduler();
    }

    /**
     * Inspects the given @Every qualifier and extracts its settings into a new #{@link ScheduledTriggerDetail}.
     *
     * @param everyBinding
     * @return a fully populated #{@link ScheduledTriggerDetail}.
     */
    public IntervalTriggerDetail createEventPayloadFromEveryBinding(final Every everyBinding) {
        return new IntervalTriggerDetail(everyBinding);
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
                throw new SchedulerConfigurationException("Found empty or missing cron definition for named schedule '"
                        + scheduleSpec + "'. Should be specified in the file "
                        + SchedulePropertiesManager.SCHEDULE_PROPERTIES_PATH
                        + " on the classpath.");
            }
        }

        return cronScheduleSpec;
    }
}
