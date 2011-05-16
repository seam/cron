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
package org.jboss.seam.cron;

import org.jboss.seam.cron.beans.DummyBean;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import junit.framework.Assert;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.seam.cron.quartz.QuartzStarter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;

/**
 * Test all kinds of events.
 */
@SuppressWarnings("serial")
@RunWith(Arquillian.class)
public class CronTest extends SeamCronTest {

    private static final int MAX_TIME_TO_WAIT = 20000;
    private static final int SLEEP_TIME = 2000;
    private static Logger log = Logger.getLogger(CronTest.class);

    @Inject
    DummyBean bean;
    @Inject
    QuartzStarter qStarter;

    @Test
    public void testScheduleDoesGetRegistered() throws SchedulerException {
        log.info("Testing scheduler gets registered.");
        List<String> jobGroupNames = Arrays.asList(qStarter.getScheduler().getJobGroupNames());
        assert jobGroupNames.contains(QuartzStarter.SCHEDULE_JOB_GROUP);
    }

    @Test
    public void testEventsGetsFired() {
        log.info("Testing schedule observer receiving events");
        assert bean.isScheduledEventObserved() == false;
        assert bean.isNamedEventObserved() == false;
        assert bean.isTypesafeEventObserved() == false;
        assert bean.isEverySecondEventObserved() == false;

        int totalTimeWaited = 0;
        while (
                !(bean.isScheduledEventObserved()
                        && bean.isNamedEventObserved()
                        && bean.isEverySecondEventObserved()
                        && bean.isTypesafeEventObserved()
                )
                        && totalTimeWaited < MAX_TIME_TO_WAIT) {
            try {
                log.info("Sleeping for a few seconds, waiting for all events to fire. Waited for " + totalTimeWaited + "ms so far ...");
                totalTimeWaited += SLEEP_TIME;
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException ex) {
                log.error("Thread was woken up while sleeping");
                ex.printStackTrace();
                Assert.fail("Thread was woken up while sleeping. Why?");
            }
        }
        assert bean.isScheduledEventObserved() == true;
        assert bean.isNamedEventObserved() == true;
        assert bean.isTypesafeEventObserved() == true;
        assert bean.isEverySecondEventObserved() == true;
        assert bean.isFiredCorrectly() == true;

    }
}
