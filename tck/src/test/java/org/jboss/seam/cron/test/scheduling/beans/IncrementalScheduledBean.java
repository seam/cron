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
package org.jboss.seam.cron.test.scheduling.beans;

import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.time.Instant;
import org.jboss.seam.cron.api.scheduling.Every;
import org.jboss.seam.cron.api.scheduling.Interval;
import org.jboss.seam.cron.api.scheduling.Trigger;
import org.jboss.seam.cron.impl.scheduling.exception.InternalException;

/**
 *
 * @author Peter Royle
 */
@ApplicationScoped
public class IncrementalScheduledBean {

    private Instant lastTriggerSecond = null;
    private final int tolleranceSeconds = 1; // tollerance of 1 second
    private Exception errorDetected = null;
    private boolean was40SecondEventObserved = false;
    private boolean wasMinuteEventObserved = false;

    public void every40Seconds(@Observes @Every(nth = 40, value = Interval.SECOND) Trigger t) throws Exception {
        was40SecondEventObserved = true;
        final Instant newInstant = Instant.millis(t.getTimeFired());
        if (lastTriggerSecond != null) {
            if (Math.abs(lastTriggerSecond.getEpochSeconds() - (newInstant.getEpochSeconds() - 40)) > tolleranceSeconds) {
                final String errorMessage = "Tick interval was not as per configuration. Previous tick was at " + lastTriggerSecond.getEpochSeconds()
                        + " and this one was at " + newInstant.getEpochSeconds();
                System.out.println("ERROR: " + errorMessage);
                errorDetected = new InternalException(errorMessage);
            }
        }
        lastTriggerSecond = newInstant;
    }

    public void everyMinute(@Observes @Every(Interval.MINUTE) Trigger t) throws Exception {
        wasMinuteEventObserved = true;
        GregorianCalendar gc = new GregorianCalendar();
        int second = gc.get(Calendar.SECOND);
        System.out.println("Every Minute fired on second: " + second);
        if (second > tolleranceSeconds && second < (60 - tolleranceSeconds)) {
            final String errorMessage = "Minute tick did not fire on the minute (ie: at zero seconds). Instead it was fired at " + second + " seconds past the minute.";
            System.out.println("ERROR: " + errorMessage);
            errorDetected = new InternalException(errorMessage);
        }
    }

    public Exception getErrorDetected() {
        return errorDetected;
    }

    public boolean isWas40SecondEventObserved() {
        return was40SecondEventObserved;
    }

    public boolean isWasMinuteEventObserved() {
        return wasMinuteEventObserved;
    }
    
}
