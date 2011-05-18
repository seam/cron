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
package org.jboss.seam.cron.provider.quartz;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.seam.cron.impl.SeamCronTestBase;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;

/**
 * Test all kinds of events.
 */
@SuppressWarnings("serial")
@RunWith(Arquillian.class)
public class QuartzScheduleProviderTest extends SeamCronTestBase {

    private static final int MAX_TIME_TO_WAIT = 20000;
    private static final int SLEEP_TIME = 2000;
    private static Logger log = Logger.getLogger(QuartzScheduleProviderTest.class);

    @Inject
    QuartzScheduleProvider qStarter;

    @Deployment
    public static JavaArchive createTestArchive() {
        return createDefaultArchive().addPackage(QuartzScheduleProvider.class.getPackage());
    }

    @Test
    public void testScheduleDoesGetRegistered() throws SchedulerException {
        log.info("Testing scheduler gets registered.");
        List<String> jobGroupNames = Arrays.asList(qStarter.getScheduler().getJobGroupNames());
        assert jobGroupNames.contains(QuartzScheduleProvider.SCHEDULE_JOB_GROUP);
    }

}
