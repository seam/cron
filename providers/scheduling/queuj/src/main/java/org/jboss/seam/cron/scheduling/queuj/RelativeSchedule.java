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
package org.jboss.seam.cron.scheduling.queuj;

import com.workplacesystems.queuj.Schedule;
import com.workplacesystems.queuj.utils.QueujException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.jboss.seam.cron.spi.scheduling.trigger.IntervalTriggerDetail;

/**
 *
 * @author Dave Oxley
 */
public class RelativeSchedule extends Schedule {

    // Increase the number when an incompatible change is made
    private static final long serialVersionUID = RelativeSchedule.class.getName().hashCode() + 1;

    private final int repeatInterval;
    private final int repeatUnit;
    
    RelativeSchedule(IntervalTriggerDetail intervalTriggerDetails) {
        repeatInterval = intervalTriggerDetails.getRepeatInterval();
        switch (intervalTriggerDetails.getRepeatUnit()) {
            case SECOND:
                repeatUnit = Calendar.SECOND;
                break;
            case MINUTE:
                repeatUnit = Calendar.MINUTE;
                break;
            case HOUR:
                repeatUnit = Calendar.HOUR;
                break;
            default:
                throw new QueujException("No repeatUnit");
        }
    }

    @Override
    protected GregorianCalendar getNextRunTime(GregorianCalendar schedule_start) {
        GregorianCalendar next_run = (GregorianCalendar)schedule_start.clone();
        next_run.add(repeatUnit, repeatInterval);
        next_run.set(Calendar.MILLISECOND, 0);
        switch (repeatUnit) {
            case Calendar.MINUTE:
                next_run.set(Calendar.SECOND, 0);
                break;
            case Calendar.HOUR:
                next_run.set(Calendar.SECOND, 0);
                next_run.set(Calendar.MINUTE, 0);
                break;
        }
        return next_run;
    }

    @Override
    protected String getSelfString() {
        return ", repeatInterval = " + String.valueOf(repeatInterval) +
            ", repeatUnit = " + String.valueOf(repeatUnit);
    }
        
}