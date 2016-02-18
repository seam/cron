/**
 * JBoss, Home of Professional Open Source Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors by the @authors
 * tag. See the copyright.txt in the distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.seam.cron.scheduling.test.timerservice;

import java.io.File;
import javax.inject.Inject;
import junit.framework.Assert;
import static junit.framework.Assert.fail;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.cron.scheduling.timerservice.TimerScheduleProviderEjb;
import static org.jboss.seam.cron.test.SeamCronTestBase.addCronAsJar;
import static org.jboss.seam.cron.test.SeamCronTestBase.addNonCDILibraries;
import org.jboss.seam.cron.test.scheduling.beans.ScheduledBean;
import org.jboss.seam.cron.test.scheduling.tck.SeamCronSchedulingTCKTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOTE: This test simply captures the fact that if "org.jboss.seam.cron.timerservice.mode=ha" is set but the server doesn't have the
 * appropriate HA singleton support deployed, the deployment WILL NOT fail, but the scheduled tasks will simply not be run.
 */
@RunWith(Arquillian.class)
public class TimerServiceSchedulingHaFailureTestLong {

    private static final int MAX_TIME_TO_WAIT = 5000;
    private static final int SLEEP_TIME = 500;
    private static Logger log = LoggerFactory.getLogger(TimerServiceSchedulingHaFailureTestLong.class);
    @Inject
    ScheduledBean bean;

    @Deployment()
    public static WebArchive createDefaultArchiveWithHaMode() {
        JavaArchive baseArchive = SeamCronSchedulingTCKTest.createSchedulingTckTestArchive(false, false)
                .addPackages(true, TimerScheduleProviderEjb.class.getPackage());
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test-long.war");
        archive.addAsLibrary(baseArchive)
                .addAsResource(new File("src/test/resources/cron-ha.properties"), "cron.properties")
                .addAsWebInfResource(new File("src/main/resources/META-INF/beans.xml"), "beans.xml");

        addNonCDILibraries(archive);
        addCronAsJar(archive);

        log.debug(archive.toString(true));
        return archive;
    }
    
    @Test
    public void testEventsDontGetFired() {
        log.info("Testing schedule observer receiving events");
        if (bean.isScheduledEventObserved()) {
            fail("Test data is not as expected prior to test");
        }

        int totalTimeWaited = 0;
        while (!(bean.isScheduledEventObserved()
                || bean.isNamedEventObserved()
                || bean.isEverySecondEventObserved()
                || bean.isTypesafeEventObserved()
                || bean.isSystemPropSchedFired())
                && totalTimeWaited < MAX_TIME_TO_WAIT) {
            try {
                log.info("Sleeping for a few seconds, waiting to ensure no events are fired. Waited for " + totalTimeWaited + "ms so far out of " + MAX_TIME_TO_WAIT + " ...");
                totalTimeWaited += SLEEP_TIME;
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException ex) {
                log.error("Thread was woken up while sleeping");
                ex.printStackTrace();
                Assert.fail("Thread was woken up while sleeping. Why?");
            }
        }

        if ((bean.isScheduledEventObserved()
                || bean.isNamedEventObserved()
                || bean.isEverySecondEventObserved()
                || bean.isTypesafeEventObserved()
                || bean.isSystemPropSchedFired())) {
            System.out.println(bean.isScheduledEventObserved());
            System.out.println(bean.isNamedEventObserved());
            System.out.println(bean.isEverySecondEventObserved());
            System.out.println(bean.isTypesafeEventObserved());
            System.out.println(bean.isSystemPropSchedFired());
            fail("Expected none of the above properties to be set to true by the configured schedules, but that wasn't the case");
        }
    }    

}
