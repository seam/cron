/**
 * JBoss, Home of Professional Open Source Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors by the @authors
 * tag. See the copyright.txt in the distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.seam.cron.scheduling.timerservice.singleton.jboss;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Timer;
import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.seam.cron.scheduling.timerservice.TimerScheduleConfig;
import org.jboss.seam.cron.scheduling.timerservice.TimerScheduleProviderBase;

/**
 * A simple example to demonstrate a implementation of a cluster-wide singleton timer.
 *
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
@Singleton
@Lock(LockType.READ) // serialise backed-up jobs. Use @AccessTimeout(value = 1, unit = TimeUnit.MINUTES) on @Observes methods to specify a finite wait time when jobs back up.
public class SchedulerBean extends TimerScheduleProviderBase implements Scheduler {

    private static Logger LOGGER = Logger.getLogger(SchedulerBean.class);
    @Inject
    private TimerScheduleConfig config;

    @Override
    public void initialize(String info) {
        LOGGER.info("Setting HA service started to true");
        config.setHaServiceStarted(true);
        super.initScheduledTriggers();
    }

    @Override
    public void stop() {
        LOGGER.info("Stop all existing HASingleton timers");
        for (Timer timer : getTimerService().getTimers()) {
            LOGGER.trace("Stop HASingleton timer: " + timer.getInfo());
            timer.cancel();
        }
    }
}
