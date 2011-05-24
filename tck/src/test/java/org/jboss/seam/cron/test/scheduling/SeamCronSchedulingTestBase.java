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
package org.jboss.seam.cron.test.scheduling;

import org.jboss.seam.cron.spi.scheduling.CronSchedulingInstaller;
import org.jboss.seam.cron.test.SeamCronTestBase;
import org.jboss.seam.cron.test.scheduling.beans.ScheduledBean;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 *
 * @author peteroyle
 */
public abstract class SeamCronSchedulingTestBase {

    public static JavaArchive createSchedulingTestArchive() {
        return SeamCronTestBase.createTestArchive()
                .addPackage(SeamCronSchedulingTestBase.class.getPackage())
                .addPackage(ScheduledBean.class.getPackage())
                .addPackages(true, CronSchedulingInstaller.class.getPackage());
    }
    
}
