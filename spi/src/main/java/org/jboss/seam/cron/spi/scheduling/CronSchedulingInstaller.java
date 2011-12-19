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
package org.jboss.seam.cron.spi.scheduling;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.cron.api.queue.Queue;
import org.jboss.seam.cron.api.scheduling.Every;
import org.jboss.seam.cron.api.scheduling.Scheduled;
import org.jboss.seam.cron.impl.scheduling.exception.SchedulerConfigurationException;
import org.jboss.seam.cron.impl.scheduling.exception.CronProviderInitialisationException;
import org.jboss.seam.cron.util.CdiUtils;
import org.jboss.seam.cron.spi.scheduling.trigger.IntervalTriggerDetail;
import org.jboss.seam.cron.spi.scheduling.trigger.ScheduledTriggerDetail;
import org.jboss.seam.cron.spi.scheduling.trigger.TriggerDetail;
import org.jboss.solder.logging.Logger;
import org.jboss.solder.resourceLoader.Resource;

/**
 * Scans all scheduling annotations and captures the configuration as a #{@link Set}
 * of #{@link TriggerDetail}s, then forwards those configurations on to the 
 * #{@link CronSchedulingProvider} implementation so that the underlying service 
 * can be configured appropriately. Not directly useful to providers.
 * 
 * @author peteroyle
 */
@ApplicationScoped
public class CronSchedulingInstaller {

    public static final String SCHEDULE_PROPERTIES_PATH = "/cron.properties";
    
    @Inject
    @Resource(SCHEDULE_PROPERTIES_PATH)
    private Properties schedProperties;
    private final Logger log = Logger.getLogger(CronSchedulingInstaller.class);

    /**
     * Initializes schedulers for all of the observed scheduled events.
     *
     * @param manager    The JSR-299 Bean Manager.
     */
    public void initProviderScheduling(final BeanManager manager, final CronSchedulingProvider scheduleProvider, 
            final Set<ProcessObserverMethod> allObservers) {
        try {
            // process the set of unique schedule specifications
            Set<TriggerDetail> configuredTriggers = new HashSet<TriggerDetail>();
            for (ProcessObserverMethod pom : allObservers) {
                ObserverMethod<?> obsMeth = pom.getObserverMethod();

                String queueId = null;
                for (Object bindingObj : obsMeth.getObservedQualifiers()) {
                    final Annotation orginalQualifier = (Annotation) bindingObj;
                    final Queue queue = (Queue) CdiUtils.getQualifier(orginalQualifier, Queue.class);
                    if (queue != null) {
                        queueId = queue.value();
                        break;
                    }
                }

                for (Object bindingObj : obsMeth.getObservedQualifiers()) {
                    final Annotation orginalQualifier = (Annotation) bindingObj;
                    final Scheduled schedQualifier = (Scheduled) CdiUtils.getQualifier(orginalQualifier, Scheduled.class);
                    final Every everyQualifier = (Every) CdiUtils.getQualifier(orginalQualifier, Every.class);
                    // gather the details of all @Scheduled and @Every triggers
                    if (schedQualifier != null) {
                        String cronScheduleSpec = lookupNamedScheduleIfNecessary(schedQualifier.value());
                        ScheduledTriggerDetail payload = new ScheduledTriggerDetail(cronScheduleSpec, orginalQualifier, obsMeth.getObservedQualifiers());
                        if (!configuredTriggers.contains(payload)) {
                            scheduleProvider.processScheduledTrigger(queueId, payload);
                            configuredTriggers.add(payload);
                        }
                    }
                    if (everyQualifier != null) {
                        IntervalTriggerDetail payload = createEventPayloadFromEveryBinding(everyQualifier, obsMeth.getObservedQualifiers());
                        if (!configuredTriggers.contains(payload)) {
                            scheduleProvider.processIntervalTrigger(queueId, payload);
                            configuredTriggers.add(payload);
                            System.out.println("Adding payload: " + payload);
                        }
                    }
                }
            }


        } catch (Throwable t) {
            throw new CronProviderInitialisationException("Error registering schedules with underlying provider", t);
        }
    }

    /**
     * Inspects the given @Every qualifier and extracts its settings into a new #{@link ScheduledTriggerDetail}.
     *
     * @param everyBinding
     * @return a fully populated #{@link ScheduledTriggerDetail}.
     */
    public IntervalTriggerDetail createEventPayloadFromEveryBinding(final Every everyBinding, final Set<Annotation> allQualifiers) {
        return new IntervalTriggerDetail(everyBinding, allQualifiers);
    }

    /**
     * If the given String is already a schedule then just return it, otherwise check the
     * cron.properties file for schedule spec with the given name and return that.
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
            cronScheduleSpec = schedProperties.getProperty(scheduleSpec);

            if (StringUtils.isEmpty(cronScheduleSpec)) {
                throw new SchedulerConfigurationException(
                        "Found empty or missing cron definition for named schedule '"
                        + scheduleSpec + "'. Should be specified in the file "
                        + SCHEDULE_PROPERTIES_PATH + " on the classpath.");
            }
        }

        return cronScheduleSpec;
    }
}
