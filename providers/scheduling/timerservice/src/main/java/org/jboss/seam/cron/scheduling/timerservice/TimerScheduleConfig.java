/**
 * JBoss, Home of Professional Open Source Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors by the @authors
 * tag. See the copyright.txt in the distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.seam.cron.scheduling.timerservice;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.seam.cron.impl.scheduling.exception.CronProviderDestructionException;
import org.jboss.seam.cron.impl.scheduling.exception.CronProviderInitialisationException;
import org.jboss.seam.cron.spi.CronProviderLifecycle;
import org.jboss.seam.cron.spi.scheduling.CronSchedulingProvider;
import org.jboss.seam.cron.spi.scheduling.trigger.IntervalTriggerDetail;
import org.jboss.seam.cron.spi.scheduling.trigger.ScheduledTriggerDetail;
import org.slf4j.Logger;

/**
 *
 * @author peteroyle
 */
@ApplicationScoped
public class TimerScheduleConfig implements CronProviderLifecycle, CronSchedulingProvider {

    private List<ScheduledTriggerDetail> scheduleTriggers = new ArrayList<ScheduledTriggerDetail>();
    private List<IntervalTriggerDetail> intervalTriggers = new ArrayList<IntervalTriggerDetail>();
//    @Inject
//    private Logger log;

    public void initProvider() throws CronProviderInitialisationException {
//        log.debug("Initialising Cron EJB Timer Scheduling Config");
    }

    public void destroyProvider() throws CronProviderDestructionException {
//        log.debug("Destroying Cron EJB Timer Scheduling Config");
    }

    public void processScheduledTrigger(String queueId, ScheduledTriggerDetail schedTriggerDetails) throws Exception {
//        log.debug("TimerScheduleConfig.processScheduledTrigger: " + schedTriggerDetails);
        scheduleTriggers.add(schedTriggerDetails);
    }

    public void processIntervalTrigger(String queueId, IntervalTriggerDetail intervalTriggerDetails) throws Exception {
//        log.debug("TimerScheduleConfig.processIntervalTrigger: " + intervalTriggerDetails);
        intervalTriggers.add(intervalTriggerDetails);
    }

    public List<ScheduledTriggerDetail> getScheduleTriggers() {
        return scheduleTriggers;
    }

    public List<IntervalTriggerDetail> getIntervalTriggers() {
        return intervalTriggers;
    }

}
