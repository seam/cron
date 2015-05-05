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
package org.jboss.seam.cron.scheduling.timerservice;

import org.apache.commons.lang.StringUtils;

/**
 * Performs rudimentary cron-expression parsing functions, for example simply splitting the expression into it's various parts as strings
 * separated by whitespace.
 *
 * @author peteroyle
 */
public class BasicCronParser {

    private final String cronExpression;
    private String secondsExpr;
    private String minutesExpr;
    private String hoursExpr;
    private String daysOfMonthExpr;
    private String monthsExpr;
    private String daysOfWeekExpr;
    private String yearsExpr;

    public BasicCronParser(String cronExpression) {
        this.cronExpression = cronExpression;
        this.parseCronExpression();
    }

    public String getCronExpression() {
        return cronExpression;
    }

    private final void parseCronExpression() {
        final String[] parts = StringUtils.split(this.cronExpression, " \t");
        secondsExpr = parts[0];
        minutesExpr = parts[1];
        hoursExpr = parts[2];
        daysOfMonthExpr = parts[3];
        if ("?".equals(daysOfMonthExpr)) {
            daysOfMonthExpr = "*";
        }
        monthsExpr = parts[4];
        daysOfWeekExpr = parts[5];
        if ("?".equals(daysOfWeekExpr)) {
            daysOfWeekExpr = "*";
        }
        if (parts.length > 6) {
            yearsExpr = parts[6];
        } else {
            yearsExpr = "*";
        }
    }

    public String getSecondsExpr() {
        return secondsExpr;
    }

    public String getMinutesExpr() {
        return minutesExpr;
    }

    public String getHoursExpr() {
        return hoursExpr;
    }

    public String getDaysOfMonthExpr() {
        return daysOfMonthExpr;
    }

    public String getMonthsExpr() {
        return monthsExpr;
    }

    public String getDaysOfWeekExpr() {
        return daysOfWeekExpr;
    }

    public String getYearsExpr() {
        return yearsExpr;
    }

}
