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
import org.jboss.seam.scheduling.events.Second;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import org.jboss.seam.scheduling.util.WebBeansManagerUtils;
import org.jboss.seam.scheduling.annotations.Every;
import org.jboss.seam.scheduling.events.Minute;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test that the Ticker is automatically initialised if there is an observer
 * of the Second event present on the classpath. Ditto for Minute.
 * 
 * @author Peter Royle
 */
@ApplicationScoped
public class TickerPresentTest extends AbstractCDITest implements Serializable
{
    private static final int MAX_TIME_TO_WAIT = 70000;
    private static final int SLEEP_TIME = 2000;

    private boolean secondTickObserved = false;
    private boolean minuteTickObserved = false;
    private Logger log = LoggerFactory.getLogger(TickerPresentTest.class);

    @Override
    public List<Class<? extends Object>> getAdditionalWebBeans()
    {
        List<Class<? extends Object>> list = new ArrayList<Class<? extends Object>>(1);
        list.add(TickerPresentTest.class);
        return list;
    }

    /**
     * Test that the TickObserverBean is registered as a WebBean.
     */
    @Test
    public void testTickObserver()
    {
        log.info("Testing tick observer exists as a WebBean");
        WebBeansManagerUtils.getInstanceByType(manager, TickerPresentTest.class);
    }

    @Test
    public void testTickDoesGetRegistered() throws SchedulerException
    {
        QuartzStarter qStarter = WebBeansManagerUtils.getInstanceByType(manager, QuartzStarter.class);
        List jobGroupNames = Arrays.asList(qStarter.getScheduler().getJobGroupNames());
        assert jobGroupNames.contains(QuartzStarter.TICKER_JOB_GROUP);
    }

    @Test
    public void testSecondTickDoesFire()
    {
        log.info("Testing tick observer receiving ticks");
        TickerPresentTest tickPresTestBean = WebBeansManagerUtils.getInstanceByType(manager, TickerPresentTest.class);
        try {
            log.debug("Sleeping for a few seconds, waiting for a Tick event or two ...");
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException ex) {
            log.error("Thread was woken up while sleeping");
            Assert.fail("Thread was woken up while sleeping. Why?");
            ex.printStackTrace();
        }
        assert tickPresTestBean.isSecondTickObserved() == true;

    }

    @Test(groups={"long-running"})
    public void testMinuteTickDoesFire()
    {
        log.info("Testing minute observer fires (could take up to a minute) ... ");
        TickerPresentTest tickPresTestBean = WebBeansManagerUtils.getInstanceByType(manager, TickerPresentTest.class);
        int totalTimeWaited = 0;
        while (!tickPresTestBean.isMinuteTickObserved() && totalTimeWaited < MAX_TIME_TO_WAIT) {
            try {
                log.debug("Sleeping for a few seconds, waiting for a Minute Tick event ...");
                totalTimeWaited += SLEEP_TIME;
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException ex) {
                log.error("Thread was woken up while sleeping");
                Assert.fail("Thread was woken up while sleeping. Why?");
                ex.printStackTrace();
            }
        }
        assert tickPresTestBean.isMinuteTickObserved() == true;

    }

    public void onTick(@Observes @Every Second second)
    {
        log.info("Tick observed (second): " + second.toString());
        this.secondTickObserved = true;
    }

    public void onTick(@Observes @Every Minute minute)
    {
        log.info("Tick observed (minute): " + minute.toString());
        this.minuteTickObserved = true;
    }

    /**
     * @return the secondTickObserved
     */
    public boolean isSecondTickObserved()
    {
        return secondTickObserved;
    }

    /**
     * @return the minuteTickObserved
     */
    public boolean isMinuteTickObserved()
    {
        return minuteTickObserved;
    }
}
