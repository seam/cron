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
@Lock(LockType.READ) // serialise backed-up jobs. Use @AccessTimeout(value = 1, unit = TimeUnit.MINUTES) on @Observes methods to specify a finite wait time when jobs back up.
public class TimerScheduleProviderEjb extends TimerScheduleProviderBase {


}
