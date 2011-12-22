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

import javax.inject.Inject;

import junit.framework.Assert;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.cron.impl.scheduling.exception.InternalException;
import org.jboss.seam.cron.test.scheduling.beans.IncrementalScheduledBean;
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
public class SeamCronSchedulingTCKTestLong {

    public static final int LONG_SLEEP_TIME = 100000;
    private static final Logger log = Logger.getLogger(SeamCronSchedulingTCKTestLong.class);

    public static JavaArchive createSchedulingTckTestArchive() {
        return SeamCronSchedulingTestBase.createSchedulingTestArchive()
                .addPackage(SeamCronSchedulingTCKTestLong.class.getPackage());
    }
    @Inject
    IncrementalScheduledBean everyTestBean;

    @Test
    public void testEvery40thSecond() throws Exception {
        try {
            // just fire up the bean and give it enough time to time itself and record any anomolies.
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException ex) {
            throw new InternalException("Should not have been woken up here");
        }
        Assert.assertTrue(everyTestBean.isWas40SecondEventObserved());
        Assert.assertTrue(everyTestBean.isWasMinuteEventObserved());
        if (everyTestBean.getErrorDetected() != null) {
            throw everyTestBean.getErrorDetected();
        }
    }
}
