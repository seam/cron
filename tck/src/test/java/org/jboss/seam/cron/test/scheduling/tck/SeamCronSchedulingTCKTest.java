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
package org.jboss.seam.cron.test.scheduling.tck;

import org.jboss.seam.cron.test.scheduling.SeamCronSchedulingTestBase;
import org.jboss.seam.cron.test.scheduling.beans.ScheduledBean;

import javax.inject.Inject;

import junit.framework.Assert;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.cron.spi.scheduling.CronSchedulingProvider;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.solder.logging.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Providers must extend this test case and append their provider-specific 
 * scheduling support classes to the #{@link JavaArchive} returned by 
 * #{@literal createSchedulingTckTestArchive} so that these TCK tests
 * will be run against their implementation during their test phase.
 * 
 * @author Peter Royle
 */
@SuppressWarnings("serial")
@RunWith(Arquillian.class)
public class SeamCronSchedulingTCKTest {

    private static final int MAX_TIME_TO_WAIT = 20000;
    private static final int SLEEP_TIME = 2000;
    private static final Logger log = Logger.getLogger(SeamCronSchedulingTCKTest.class);

    public static JavaArchive createSchedulingTckTestArchive() {
        return SeamCronSchedulingTestBase.createSchedulingTestArchive()
                .addPackage(SeamCronSchedulingTCKTest.class.getPackage());
    }
    
    @Inject
    ScheduledBean bean;
    @Inject
    CronSchedulingProvider cronSchedProv;

    @Test
    public void testEventsGetsFired() {
        log.info("Testing schedule observer receiving events");
        assert bean.isScheduledEventObserved() == false;
        assert bean.isNamedEventObserved() == false;
        assert bean.isTypesafeEventObserved() == false;
        assert bean.isEverySecondEventObserved() == false;

        int totalTimeWaited = 0;
        while (!(bean.isScheduledEventObserved()
                && bean.isNamedEventObserved()
                && bean.isEverySecondEventObserved()
                && bean.isTypesafeEventObserved())
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
        assert bean.isEverySecondEventObserved() == true;
        assert bean.isFiredCorrectly() == true;
        assert bean.isTypesafeEventObserved() == true;

    }
}
