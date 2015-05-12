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
package org.jboss.seam.cron.test.restriction.beans;

import com.workplacesystems.utilsj.collections.helpers.HasLessThan;
import java.util.concurrent.atomic.AtomicInteger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import org.jboss.seam.cron.api.asynchronous.Asynchronous;
import org.jboss.seam.cron.api.queue.Queue;
import org.jboss.seam.cron.api.restriction.AsyncRestriction;
import org.jboss.seam.cron.api.restriction.StatusIndexes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dave Oxley
 */
@ApplicationScoped
public class SomeAsyncAndRestrictionMethods {
    
    public static final AtomicInteger runningCount = new AtomicInteger(0);
    public static int highestRunningCount = 0;
    public static final String GROUP = "TEST_GROUP";
    public static final int SLEEP = 1000;
    public static final int MAX_CONCURRENT = 3;
    private static final Object MUTEX = new Object();
    private static final Logger log = LoggerFactory.getLogger(SomeAsyncAndRestrictionMethods.class);

    
    
    public boolean restrict(@Observes
                            @AsyncRestriction()
                            @Queue(GROUP)
                            StatusIndexes indexes) {
        HasLessThan<Object> max = new HasLessThan<Object>(MAX_CONCURRENT);
        max = indexes.iterateRunningProcesses(max);
        indexes.iterateWaitingToRunProcesses(max);
        return max.hasLess();
    }

    @Queue(GROUP)
    @Asynchronous()
    public void increment() {
        int c = runningCount.incrementAndGet();
        synchronized (MUTEX) {
            if (c > highestRunningCount)
                highestRunningCount = c;
        }
        System.out.println("Running count: " + c);
        try {
            Thread.sleep((long)(Math.random() * SLEEP));
        } catch (InterruptedException ex) {
            log.error("Interrupted while sleeping", ex);
        }
        runningCount.decrementAndGet();
    }
}
