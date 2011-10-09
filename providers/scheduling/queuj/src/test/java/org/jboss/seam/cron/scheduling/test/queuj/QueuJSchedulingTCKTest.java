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
package org.jboss.seam.cron.scheduling.test.queuj;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.seam.cron.scheduling.queuj.QueuJScheduleProvider;
import org.jboss.seam.cron.test.scheduling.tck.SeamCronSchedulingTCKTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.solder.logging.Logger;

/**
 *
 * @author Dave Oxley
 */
public class QueuJSchedulingTCKTest extends SeamCronSchedulingTCKTest {

    private static Logger log = Logger.getLogger(QueuJSchedulingTCKTest.class);

    @Deployment
    public static JavaArchive createDefaultArchive() {
        JavaArchive archive = SeamCronSchedulingTCKTest.createSchedulingTckTestArchive().addPackages(true, QueuJScheduleProvider.class.getPackage());

        log.debug(archive.toString(true));
        return archive;
    }
}
