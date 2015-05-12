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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.as.quickstarts.cluster.hasingleton.service.ejb.HATimerService;
import org.jboss.as.quickstarts.cluster.hasingleton.service.ejb.HATimerServiceActivator;
import org.jboss.as.quickstarts.cluster.hasingleton.service.ejb.Scheduler;
import org.jboss.as.quickstarts.cluster.hasingleton.service.ejb.SchedulerBean;
import org.jboss.seam.cron.scheduling.timerservice.TimerScheduleProviderEjb;
import static org.jboss.seam.cron.test.SeamCronTestBase.addCronAsJar;
import static org.jboss.seam.cron.test.SeamCronTestBase.addNonCDILibraries;
import org.jboss.seam.cron.test.scheduling.tck.SeamCronSchedulingTCKTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Peter Royle
 */
@Ignore
public class TimerServiceSchedulingTCKTest extends SeamCronSchedulingTCKTest {

    private static Logger log = LoggerFactory.getLogger(TimerServiceSchedulingTCKTest.class);

    @Deployment()
    public static WebArchive createDefaultArchive() {
        JavaArchive baseArchive = SeamCronSchedulingTCKTest.createSchedulingTckTestArchive(false, false)
                .addPackages(true, TimerScheduleProviderEjb.class.getPackage());
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war");
        archive.addAsLibrary(baseArchive)
                
                
                .addAsWebInfResource(new File("src/main/resources/META-INF/beans.xml"), "beans.xml");

        addNonCDILibraries(archive);
        final JavaArchive cronJar = addCronAsJar(archive);
        cronJar.addAsManifestResource(new File("src/main/resources/META-INF/services/org.jboss.msc.service.ServiceActivator"),
                "services/org.jboss.msc.service.ServiceActivator")
                .addClass(HATimerService.class)
                .addClass(HATimerServiceActivator.class)
                .addClass(Scheduler.class)
                .addClass(SchedulerBean.class)
                .addPackages(true, org.jboss.msc.Version.class.getPackage())
                .addPackages(true, org.jboss.msc.value.Value.class.getPackage())
                .addPackages(true, org.jboss.modules.ref.Reaper.class.getPackage())
                .addPackages(true, org.jboss.as.clustering.ClusterNode.class.getPackage())
                .addPackages(true, org.jboss.as.server.Main.class.getPackage());

        log.debug(archive.toString(true));
        return archive;
    }

}
