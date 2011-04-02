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
package org.jboss.seam.scheduling;

import org.jboss.seam.scheduling.quartz.QuartzStarter;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import org.jboss.seam.scheduling.util.WebBeansManagerUtils;
import org.quartz.SchedulerException;
import org.testng.annotations.Test;

/**
 * Test that the Ticker is NOT automatically initialised if there is NOT an observer
 * of the Tick event present on the claspath.
 * @author Peter Royle
 */
public class TickerAbsentTest extends AbstractCDITest {

    /**
     * Test that the TickObserverBean is not registered with the test container
     * by default (ie: unless there are observers found to be listening).
     */
    @Test(expectedExceptions={NoSuchElementException.class})
    public void testTickObserver() throws Exception {
        WebBeansManagerUtils.getInstanceByType(manager, TickerPresentTest.class);
    }

    @Test
    public void testTickDoesntGetRegistered() throws SchedulerException {
        QuartzStarter qStarter = WebBeansManagerUtils.getInstanceByType(manager, QuartzStarter.class);
        List jobGroupNames = Arrays.asList(qStarter.getScheduler().getJobGroupNames());
        assert !jobGroupNames.contains(QuartzStarter.TICKER_JOB_GROUP);
    }

}
