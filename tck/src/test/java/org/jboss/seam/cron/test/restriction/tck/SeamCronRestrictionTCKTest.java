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
package org.jboss.seam.cron.test.restriction.tck;

import javax.inject.Inject;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.cron.test.restriction.SeamCronRestrictionTestBase;
import org.jboss.seam.cron.test.restriction.beans.IntervalAndAsyncBean;
import org.jboss.seam.cron.test.restriction.beans.SomeAsyncAndRestrictionMethods;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.solder.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Providers must extend this test case and append their provider-specific 
 * queue support classes to the #{@link JavaArchive} returned by 
 * #{@literal createRestrictionTckTestArchive} so that these TCK tests
 * will be run against their implementation during their test phase.
 * 
 * @author Dave Oxley
 */
@RunWith(Arquillian.class)
public class SeamCronRestrictionTCKTest {
    
    private static final int NUM_EXECUTIONS = 20;
    private static final Logger log = Logger.getLogger(SeamCronRestrictionTCKTest.class);

    public static JavaArchive createRestrictionTckTestArchive() {
        return SeamCronRestrictionTestBase.createRestrictionTestArchive()
                .addPackage(SeamCronRestrictionTCKTest.class.getPackage());
    }
    
    @Inject
    SomeAsyncAndRestrictionMethods asynchBean;

    @Inject
    IntervalAndAsyncBean intAsynchBean;

    @Test
    public void testSimpleAsyncRestrictionMethod() {
        log.info("Testing asynchronous methods on annotated bean are called asynchronously");
        for (int i = 0; i < NUM_EXECUTIONS; i++) {
            asynchBean.increment();
        }
        System.out.println("increment methods all called");

        // Now if we wait for long enough, all of the increments should have been completed.
        try {
            Thread.sleep(((SomeAsyncAndRestrictionMethods.SLEEP * NUM_EXECUTIONS) / SomeAsyncAndRestrictionMethods.MAX_CONCURRENT) + 2000);
        } catch (InterruptedException ie) {
            log.error("Interrupted while sleeping", ie);
        }

        assertTrue(SomeAsyncAndRestrictionMethods.highestRunningCount <= SomeAsyncAndRestrictionMethods.MAX_CONCURRENT);
        assertTrue(SomeAsyncAndRestrictionMethods.highestRunningCount > 1);
        assertTrue(IntervalAndAsyncBean.highestRunningCount <= IntervalAndAsyncBean.MAX_CONCURRENT);

    }
}
