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

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.cron.test.scheduling.beans.IncrementalScheduledBean;
import org.jboss.seam.cron.impl.scheduling.exception.InternalException;
import org.jboss.seam.cron.test.SeamCronTestBase;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.solder.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test @Every schedules
 */
@RunWith(Arquillian.class)
public class IntervalScheduleTCKLongTest extends SeamCronTestBase {
    
    public static final int SLEEP_TIME = 100000;
    private static final Logger log = Logger.getLogger(IntervalScheduleTCKLongTest.class);
    
    @Inject
    IncrementalScheduledBean everyTestBean;

    @Deployment
    public static JavaArchive createTestArchive() {
        return SeamCronTestBase.createTestArchive();
    }

    @Test 
    public void testEvery40thSecond() throws Exception {
        try {
            // just fire up the bean and give it enough time to time itself and record any anomolies.
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException ex) {
            throw new InternalException("Should not have been woken up here");
        }
        Assert.assertTrue(everyTestBean.isWasEventObserved());
        if (everyTestBean.getErrorDetected() != null) {
            throw everyTestBean.getErrorDetected();
        }
    }
}
