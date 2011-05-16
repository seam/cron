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
package org.jboss.seam.cron.beans;



import org.jboss.seam.cron.annotations.Every;
import org.jboss.seam.cron.annotations.Scheduled;

import java.util.Calendar;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import org.jboss.logging.Logger;
import org.jboss.seam.cron.events.Trigger;
import static org.jboss.seam.cron.events.TimeUnit.*;

/**
 * Test all events, but minute and hour (to shorten test time).
 * <p/>
 * By the end of the test, all boolean properties should be true.
 */
@ApplicationScoped
public class DummyBean {
    private Logger log = Logger.getLogger( DummyBean.class );
    private boolean scheduledEventObserved = false;
    private boolean namedEventObserved = false;
    private boolean typesafeEventObserved = false;
    private boolean firedCorrectly = true;
    private boolean everySecondEventObserved = false;

    public void onSchedule(@Observes
                           @Scheduled("*/5 * * ? * *")
                           Trigger event) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(event.getTimeFired());
        firedCorrectly = firedCorrectly & ((c.get(Calendar.SECOND) % 5) == 0);
        log.info("Scheduled event fired at " + c.getTime());
        this.scheduledEventObserved = true;
    }

    public void onNamedSchedule(@Observes
                                @Scheduled("test.one")
                                Trigger event) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(event.getTimeFired());
        firedCorrectly = firedCorrectly & ((c.get(Calendar.SECOND) % 5) == 0);
        log.info("Named event fired at " + c.getTime());
        this.namedEventObserved = true;
    }

    public void onTypesafeSchedule(@Observes
                                   @Frequent
                                   Trigger event) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(event.getTimeFired());
        firedCorrectly = firedCorrectly & ((c.get(Calendar.SECOND) % 5) == 0);
        log.info("Typesafe event fired at " + c.getTime());
        this.typesafeEventObserved = true;
    }

    public void onEverySecondSchedule(@Observes
                                      @Every(SECOND)
                                      Trigger event) {
        log.info("Every second event fired at " + new Date(event.getTimeFired()));
        this.everySecondEventObserved = true;
    }

    public boolean isScheduledEventObserved() {
        return scheduledEventObserved;
    }

    public boolean isNamedEventObserved() {
        return namedEventObserved;
    }

    public boolean isTypesafeEventObserved() {
        return typesafeEventObserved;
    }

    public boolean isFiredCorrectly() {
        return firedCorrectly;
    }

    public boolean isEverySecondEventObserved() {
        return everySecondEventObserved;
    }
}
