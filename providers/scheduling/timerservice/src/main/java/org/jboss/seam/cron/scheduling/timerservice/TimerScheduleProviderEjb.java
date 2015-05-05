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

import java.io.Serializable;
import java.util.GregorianCalendar;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import org.jboss.seam.cron.api.scheduling.Every;
import static org.jboss.seam.cron.api.scheduling.Interval.HOUR;
import static org.jboss.seam.cron.api.scheduling.Interval.MINUTE;
import static org.jboss.seam.cron.api.scheduling.Interval.SECOND;
import org.jboss.seam.cron.spi.scheduling.trigger.IntervalTriggerDetail;
import org.jboss.seam.cron.spi.scheduling.trigger.ProviderContextTriggerSupport;
import org.jboss.seam.cron.spi.scheduling.trigger.ScheduledTriggerDetail;
import org.jboss.seam.cron.spi.scheduling.trigger.TriggerSupplies;
import static org.jboss.seam.cron.util.TimeUtils.getOneSecondLater;
import org.slf4j.Logger;

/**
 *
 * @author peteroyle
 */
@Startup
@Singleton
//@Stateless
//@Singleton // Can't use javax.ejb.@Singleton yet as it causes JBoss AS to hang on deployment.
//@LocalBean
@Lock(LockType.READ) // serialise backed-up jobs. Use @AccessTimeout(value = 1, unit = TimeUnit.MINUTES) on @Observes methods to specify a finite wait time when jobs back up.
public class TimerScheduleProviderEjb {

    @Inject
    private TimerScheduleConfig scheduleConfigs;
    @Inject
    private BeanManager beanManager;
    @Resource
    private TimerService timerService;
//    @Inject
//    private Logger log;

    @Timeout
    public void fireScheduledEvent(Timer timer) {
//        log.debug("Cron EJB Timer timeout firing");
        final Serializable info = timer.getInfo();
        new ProviderContextTriggerSupport() {
            @Override
            public TriggerSupplies fetchTriggerSupplies() {
                final TriggerSupplies triggerSupplies = (TriggerSupplies) info;
                return triggerSupplies;
            }
        }.fireTrigger();
    }

    @PostConstruct
    public void initScheduledTriggers() {
//        log.debug("Initiailising schedule configs found during extension initialisation");
        for (ScheduledTriggerDetail schedTrigger : scheduleConfigs.getScheduleTriggers()) {
            processScheduledTrigger(schedTrigger);
        }
        for (IntervalTriggerDetail intervalTrigger : scheduleConfigs.getIntervalTriggers()) {
            processIntervalTrigger(intervalTrigger);
        }
    }

    public void processScheduledTrigger(ScheduledTriggerDetail schedTriggerDetails) {
//        log.debug("TimerScheduleProviderEjb.processScheduledTrigger: " + schedTriggerDetails);
        // TODO: shouldn't be passing beanMnager here because it's not Serializable. Might be OK since we're not using persistant schedules, but who knows.
        // TODO: .. Ultimately we want to refactor TriggerSupplies to remove beanManager but not sure we can because beanManager is needed in TriggerSupport,
        // TODO: .. and in non-CDI environments there's no other way to get a reference to it.
        TriggerSupplies observerDetails = new TriggerSupplies(beanManager, schedTriggerDetails.getQualifier(), schedTriggerDetails.
                getQualifiers());
        TimerConfig timerConfig = new TimerConfig(observerDetails, false);
        ScheduleExpression scheduleExpression = new ScheduleExpression();
        final String cronStr = schedTriggerDetails.getCronScheduleSpec();
        if (cronStr.contains(":")) {
            final String[] parts = cronStr.split(":");
            String hoursStr = parts[0];
            String minsStr = parts[1];
            scheduleExpression.hour(hoursStr);
            scheduleExpression.minute(minsStr);
        } else {
            BasicCronParser cronParser = new BasicCronParser(cronStr);
            scheduleExpression.dayOfMonth(cronParser.getDaysOfMonthExpr());
            scheduleExpression.dayOfWeek(cronParser.getDaysOfWeekExpr());
            scheduleExpression.hour(cronParser.getHoursExpr());
            scheduleExpression.minute(cronParser.getMinutesExpr());
            scheduleExpression.second(cronParser.getSecondsExpr());
            scheduleExpression.month(cronParser.getMonthsExpr());
            scheduleExpression.year(cronParser.getYearsExpr());
        }
        Timer timer = timerService.createCalendarTimer(scheduleExpression, timerConfig);
    }

    public void processIntervalTrigger(IntervalTriggerDetail intervalTriggerDetails) {
//        log.debug("TimerScheduleProviderEjb.processIntervalTrigger: " + intervalTriggerDetails);
        GregorianCalendar gc = getOneSecondLater();
        TriggerSupplies observerDetails = new TriggerSupplies(beanManager, intervalTriggerDetails.getQualifier(), intervalTriggerDetails.
                getQualifiers());
        TimerConfig timerConfig = new TimerConfig(observerDetails, false);
        if (SECOND.equals(intervalTriggerDetails.getRepeatUnit())) {
            final Timer timer = timerService.createIntervalTimer(gc.getTime(), 1000 * intervalTriggerDetails.getRepeatInterval(), timerConfig);
        } else if (MINUTE.equals(intervalTriggerDetails.getRepeatUnit())) {
            // start at the beginning of the next minute
            gc.add(GregorianCalendar.MINUTE, 1);
            gc.set(GregorianCalendar.SECOND, 0);
            final Timer timer = timerService.createIntervalTimer(gc.getTime(), 1000 * 60 * intervalTriggerDetails.getRepeatInterval(), timerConfig);
        } else if (HOUR.equals(intervalTriggerDetails.getRepeatUnit())) {
            // start at the beginning of the next hour
            gc.add(GregorianCalendar.HOUR, 1);
            gc.set(GregorianCalendar.MINUTE, 0);
            gc.set(GregorianCalendar.SECOND, 0);
            final Timer timer = timerService.createIntervalTimer(gc.getTime(), 1000 * 60 * 60 * intervalTriggerDetails.getRepeatInterval(),
                    timerConfig);
        } else {
            throw new InternalError(
                    "Could not work out which interval to use for the schedule of an @" + Every.class.getName() + " observer");
        }
    }

}
