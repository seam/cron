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
package org.jboss.seam.cron.test.asynchronous.beans;

import org.jboss.seam.cron.api.asynchronous.AsyncResult;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import org.jboss.seam.cron.api.asynchronous.Asynchronous;
import org.jboss.solder.logging.Logger;

/**
 * @author Peter Royle
 */
@ApplicationScoped
public class SomeAsynchMethods {

    public static final AtomicInteger count = new AtomicInteger(0);
    public static final int NUM_LOOPS = 2;
    public static final int SLEEP_PER_LOOP = 1000;
    private CountDownLatch statusLatch;
    private CountDownLatch heystackLatch;
    private Status statusEvent;
    private Integer haystackCount;
    private static final Logger log = Logger.getLogger(SomeAsynchMethods.class);

    public void reset() {
        statusEvent = null;
        haystackCount = null;
        statusLatch = new CountDownLatch(1);
        heystackLatch = new CountDownLatch(1);
    }
    
    @Asynchronous
    public void increment() {
        for (int i = 0; i < NUM_LOOPS; i++) {
            int c = count.incrementAndGet();
            System.out.println("Count: " + c);
            try {
                Thread.sleep(SLEEP_PER_LOOP);
            } catch (InterruptedException ex) {
                log.error("Interrupted while sleeping", ex);
            }
        }
    }

    @Asynchronous
    public Status returnStatusObject(final String statusToSet) {
        return new Status(statusToSet);
    }

    public void reportStatusForBoth(@Observes Status status) {
        statusEvent = status;
        statusLatch.countDown();
        System.out.println("The future is " + status.getDescription());
    }

    @Asynchronous
    public Future<Status> returnStatusInFuture(String statusToSet) {
        return new AsyncResult<Status>(new Status(statusToSet));
    }

    @Asynchronous
    @HaystackCount
    public Integer countNeedlesInTheHaystack(final int numToReturn) {
        return numToReturn;
    }
    
    @Asynchronous
    public Future<String> throwAnException() {
        String result = null;
        result.toString();
        return new AsyncResult<String>("You won't get this far");
    }

    public void reportHaystackCount(@Observes @HaystackCount Integer count) {
        System.out.println("Needles in haystack: " + count);
        haystackCount = count;
        heystackLatch.countDown();
    }

    public CountDownLatch getStatusLatch() {
        return statusLatch;
    }

    public Status getStatusEvent() {
        return statusEvent;
    }

    public Integer getHaystackCount() {
        return haystackCount;
    }

    public CountDownLatch getHeystackLatch() {
        return heystackLatch;
    }
    
}
