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
package org.jboss.seam.cron.scheduling.test.timerservice;

import java.io.File;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.seam.cron.scheduling.timerservice.TimerScheduleProviderEjb;
import static org.jboss.seam.cron.test.SeamCronTestBase.addCronAsJar;
import static org.jboss.seam.cron.test.SeamCronTestBase.addNonCDILibraries;
import org.jboss.seam.cron.test.scheduling.tck.SeamCronSchedulingTCKTest;
import org.jboss.seam.cron.test.scheduling.tck.SeamCronSchedulingTCKTestLong;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Peter Royle
 */
public class TimerServiceSchedulingTCKTestLong extends SeamCronSchedulingTCKTestLong {

    private static Logger log = LoggerFactory.getLogger(TimerServiceSchedulingTCKTestLong.class);

    @Deployment
    public static WebArchive createDefaultArchive() {
        JavaArchive baseArchive = SeamCronSchedulingTCKTest.createSchedulingTckTestArchive(false, false)
                .addPackages(true, TimerScheduleProviderEjb.class.getPackage());
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war");
        archive.addAsLibrary(baseArchive)
                .addAsWebInfResource(new File("src/main/resources/META-INF/beans.xml"), "beans.xml");

        addNonCDILibraries(archive);
        addCronAsJar(archive);

        log.debug(archive.toString(true));
        return archive;
    }
}
