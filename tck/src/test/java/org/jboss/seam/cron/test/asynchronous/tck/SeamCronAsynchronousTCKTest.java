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
package org.jboss.seam.cron.test.asynchronous.tck;

import org.jboss.seam.cron.test.asynchronous.SeamCronAsynchronousTestBase;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.cron.test.asynchronous.beans.SomeAsynchMethods;
import org.jboss.seam.cron.test.asynchronous.beans.Status;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.solder.logging.Logger;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.jboss.seam.cron.test.asynchronous.beans.SomeAsynchMethods.NUM_LOOPS;
import static org.jboss.seam.cron.test.asynchronous.beans.SomeAsynchMethods.SLEEP_PER_LOOP;

/**
 * Providers must extend this test case and append their provider-specific 
 * asynchronous support classes to the #{@link JavaArchive} returned by 
 * #{@literal createSchedulingTckTestArchive} so that these TCK tests
 * will be run against their implementation during their test phase.
 * 
 * @author Peter Royle
 */
@SuppressWarnings("serial")
@RunWith(Arquillian.class)
public class SeamCronAsynchronousTCKTest {
    
    private static final int NUM_EXECUTIONS = 3;
    private static final Logger log = Logger.getLogger(SeamCronAsynchronousTCKTest.class);

    public static JavaArchive createAsynchronousTckTestArchive() {
        return SeamCronAsynchronousTestBase.createAsynchronousTestArchive()
                .addPackage(SeamCronAsynchronousTCKTest.class.getPackage());
    }
    
    @Inject
    SomeAsynchMethods asynchBean;

    @Test
    public void testTypeLevelAsyncMethods() {
        log.info("Testing asynchronous methods on annotated bean are called asynchronously");
        for (int i = 0; i < NUM_EXECUTIONS; i++) {
            asynchBean.increment();
        }
        System.out.println("increment methods all called");

        // If executions were asynchronous then at least some of the increments
        // would have been executed by now, but not all (ie: none of those which
        // come after a sleep).
        assertTrue(SomeAsynchMethods.count.get() >= 0);
        assertTrue(SomeAsynchMethods.count.get() < NUM_EXECUTIONS * SomeAsynchMethods.NUM_LOOPS);

        // Now if we wait for long enough, all of the increments should have been completed.
        try {
            Thread.sleep(SLEEP_PER_LOOP * NUM_LOOPS + 1000);
        } catch (InterruptedException ie) {
            log.error("Interrupted while sleeping", ie);
        }

        assertTrue(SomeAsynchMethods.count.get() == NUM_EXECUTIONS * SomeAsynchMethods.NUM_LOOPS);

    }

    @Test
    public void testPostExecutionNoQualifiers() throws InterruptedException {
        log.info("Testing asynchronous methods fire observable post-execution event");
        assertNotNull(asynchBean);
        asynchBean.reset();
        assertNull(asynchBean.getStatusEvent());
        assertNull(asynchBean.getHaystackCount());
        String statusToSet = "orange";
        asynchBean.returnStatusObject(statusToSet);
        asynchBean.getStatusLatch().await(4, TimeUnit.SECONDS);
        assertNotNull(asynchBean.getStatusEvent());
        assertNull(asynchBean.getHaystackCount());
        assertEquals(statusToSet, asynchBean.getStatusEvent().getDescription());
    }

    @Test
    public void testPostExecutionHaystackQualifiers() throws InterruptedException {
        log.info("Testing asynchronous methods fire observable post-execution event");
        assertNotNull(asynchBean);
        asynchBean.reset();
        assertNull(asynchBean.getStatusEvent());
        assertNull(asynchBean.getHaystackCount());
        Integer numNeedles = 11;
        asynchBean.countNeedlesInTheHaystack(numNeedles);
        asynchBean.getHeystackLatch().await(4, TimeUnit.SECONDS);
        assertNotNull(asynchBean.getHaystackCount());
        assertNull(asynchBean.getStatusEvent());
        assertEquals(numNeedles, asynchBean.getHaystackCount());
    }

    @Test
    public void testAsynchReturningFuture() throws InterruptedException, InterruptedException, ExecutionException, TimeoutException {
        log.info("Testing asynchronous methods return a future as expected");
        assertNotNull(asynchBean);
        asynchBean.reset();
        assertNull(asynchBean.getStatusEvent());
        assertNull(asynchBean.getHaystackCount());
        String statusToSet = "blue";
        Future<Status> result = asynchBean.returnStatusInFuture(statusToSet);
        assertNotNull(result);
        Status resultStatus = result.get(4, TimeUnit.SECONDS);
        assertNotNull(resultStatus);
        assertNotNull(resultStatus.getDescription());
        assertEquals(statusToSet, resultStatus.getDescription());
        assertNull(asynchBean.getHaystackCount());
    }
    
    @Test
    public void testErrorThrownReturnsAsPerEJBSpec() {
        log.info("Testing that an error thrown during an @Asynchronous invocation which returns a Future will be delivered to the caller as per the EJB spec");
        assertNotNull(asynchBean);
        asynchBean.reset();
        Future<String> result = asynchBean.throwAnException();
        try {
            result.get(2, TimeUnit.SECONDS);
            fail("If you got here, the asynch method didn't throw an exception properly");
        } catch (ExecutionException ee) {
            log.info("The correct kind of exception was found");
            assertEquals(NullPointerException.class, ee.getCause().getClass());
        } catch (TimeoutException toe) {
            log.error("Should not have timed out here!", toe);
        } catch (InterruptedException ie) {
            log.error("Should not have been interrupted here!", ie);
        }
        
    }
}
