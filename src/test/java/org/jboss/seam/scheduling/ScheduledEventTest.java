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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.scheduling.util.WebBeansManagerUtils;
import org.jboss.seam.scheduling.annotations.Scheduled;
import org.jboss.seam.scheduling.events.Event;
import org.quartz.SchedulerException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author pete
 */
@ApplicationScoped
public class ScheduledEventTest extends AbstractCDITest implements Serializable
{

    private static final int MAX_TIME_TO_WAIT = 20000;
    private static final int SLEEP_TIME = 2000;
    private boolean scheduledEventObserved = false;
    private boolean namedEventObserved = false;
    private boolean typesafeEventObserved = false;
    private Log log = LogFactory.getLog(ScheduledEventTest.class);

    @Override
    public List<Class<? extends Object>> getAdditionalWebBeans()
    {
        List<Class<? extends Object>> list = new ArrayList<Class<? extends Object>>(1);
        list.add(ScheduledEventTest.class);
        return list;
    }

    /**
     * Test that the ScheduledEventTest is registered as a WebBean.
     */
    @Test
    public void testTickObserver()
    {
        log.info("Testing schedule observer exists as a WebBean");
        WebBeansManagerUtils.getInstanceByType(manager, ScheduledEventTest.class);
    }

    @Test
    public void testScheduleDoesGetRegistered() throws SchedulerException
    {
        QuartzStarter qStarter = WebBeansManagerUtils.getInstanceByType(manager, QuartzStarter.class);
        List jobGroupNames = Arrays.asList(qStarter.getScheduler().
                getJobGroupNames());
        assert jobGroupNames.contains(QuartzStarter.SCHEDULE_JOB_GROUP);
    }

    @Test
    public void testScheduleDoesFire()
    {
        log.info("Testing shedule observer receiving events");
        ScheduledEventTest schedPresBean = WebBeansManagerUtils.
                getInstanceByType(manager, ScheduledEventTest.class);
        assert schedPresBean.isScheduledEventObserved() == false;
        assert schedPresBean.isNamedEventObserved() == false;
        assert schedPresBean.isTypesafeEventObserved() == false;
        int totalTimeWaited = 0;
        while (!schedPresBean.isScheduledEventObserved() && !schedPresBean.isNamedEventObserved() &&
                !schedPresBean.isTypesafeEventObserved() && totalTimeWaited < MAX_TIME_TO_WAIT) {
            try {
                log.info("Sleeping for a few seconds, waiting for all events to fire. Waited for " + totalTimeWaited + "ms so far ...");
                totalTimeWaited += SLEEP_TIME;
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException ex) {
                log.error("Thread was woken up while sleeping");
                Assert.fail("Thread was woken up while sleeping. Why?");
                ex.printStackTrace();
            }
        }
        assert schedPresBean.isScheduledEventObserved() == true;
        assert schedPresBean.isNamedEventObserved() == true;
        assert schedPresBean.isTypesafeEventObserved() == true;

    }

    public void onSchedule(@Observes @Scheduled("*/5 * * ? * *") Event event)
    {
        log.info("Event observed: " + event.toString());
        this.scheduledEventObserved = true;
    }

    public void onNamedSchedule(@Observes @Scheduled("test.one") Event event)
    {
        log.info("Event observed: " + event.toString());
        this.namedEventObserved = true;
    }

    public void onTypesafeSchedule(@Observes @Frequent Event event)
    {
        log.info("Event observed: " + event.toString());
        this.typesafeEventObserved = true;
    }

    /**
     * @return if the unnamed, scheduled event has been observed.
     */
    public boolean isScheduledEventObserved()
    {
        return scheduledEventObserved;
    }

    /**
     * @return if the named event has been observed.
     */
    public boolean isNamedEventObserved()
    {
        return namedEventObserved;
    }

    /**
     * @return if the typesafe event has been observed.
     */
    public boolean isTypesafeEventObserved()
    {
        return typesafeEventObserved;
    }
}
