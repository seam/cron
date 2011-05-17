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
package org.jboss.seam.cron.provider.quartz.jobs;

import java.lang.annotation.Annotation;
import org.jboss.seam.cron.api.Every;
import org.jboss.seam.cron.api.TimeUnit;

/**
 * Simple container for the qualifying annotation and payload type of
 * a scheduled event to be fired.
 * 
 * @author Peter Royle
 */
public class ScheduledQualifiedEventPayload {
   
    private final String scheduleSpec;
    private final Annotation qualifier;
    private final Class payloadType;
    private final TimeUnit repeatUnit;
    private final Integer repeatInterval;

    public ScheduledQualifiedEventPayload(String dereferencedScheduleSpec, Annotation qualifier, Class payloadType) {
        this.scheduleSpec = dereferencedScheduleSpec;
        this.qualifier = qualifier;
        this.payloadType = payloadType;
        this.repeatUnit = null;
        this.repeatInterval = null;
    }

    public ScheduledQualifiedEventPayload(Every qualifier, Class payloadType) {
        this.scheduleSpec = null;
        this.qualifier = qualifier;
        this.payloadType = payloadType;
        this.repeatUnit = qualifier.value();
        this.repeatInterval = qualifier.nth();
    }

    public String getScheduleSpec() {
        return scheduleSpec;
    }

    public Class getPayloadType() {
        return payloadType;
    }

    public Annotation getQualifier() {
        return qualifier;
    }

    public TimeUnit getRepeatUnit() {
        return repeatUnit;
    }

    public Integer getRepeatInterval() {
        return repeatInterval;
    }
    
    public boolean isInterval() {
        return repeatUnit != null && scheduleSpec == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ScheduledQualifiedEventPayload other = (ScheduledQualifiedEventPayload) obj;
        if ((this.scheduleSpec == null) ? (other.scheduleSpec != null) : !this.scheduleSpec.equals(other.scheduleSpec)) {
            return false;
        }
        if (this.qualifier != other.qualifier && (this.qualifier == null || !this.qualifier.equals(other.qualifier))) {
            return false;
        }
        if (this.payloadType != other.payloadType && (this.payloadType == null || !this.payloadType.equals(other.payloadType))) {
            return false;
        }
        if (this.repeatUnit != other.repeatUnit) {
            return false;
        }
        if (this.repeatInterval != other.repeatInterval && (this.repeatInterval == null || !this.repeatInterval.equals(other.repeatInterval))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.scheduleSpec != null ? this.scheduleSpec.hashCode() : 0);
        hash = 17 * hash + (this.qualifier != null ? this.qualifier.hashCode() : 0);
        hash = 17 * hash + (this.payloadType != null ? this.payloadType.hashCode() : 0);
        hash = 17 * hash + (this.repeatUnit != null ? this.repeatUnit.hashCode() : 0);
        hash = 17 * hash + (this.repeatInterval != null ? this.repeatInterval.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "ScheduledQualifiedEventPayload{" + "scheduleSpec=" + scheduleSpec + ", qualifier=" + qualifier + ", payloadType=" + payloadType + ", repeatUnit=" + repeatUnit + ", repeatInterval=" + repeatInterval + '}';
    }

}
