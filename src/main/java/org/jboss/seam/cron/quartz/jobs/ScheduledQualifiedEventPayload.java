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
package org.jboss.seam.cron.quartz.jobs;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple container for the qualifying annotation and payload type of
 * a scheduled event to be fired.
 * 
 * @author Peter Royle
 */
public class ScheduledQualifiedEventPayload {
   
    private final String scheduleSpec;
    private final Set<Annotation> qualifiers;
    private final Class payloadType;

    public ScheduledQualifiedEventPayload(String scheduleSpec, Annotation qualifier, Class payloadType) {
        this.scheduleSpec = scheduleSpec;
        this.qualifiers = new HashSet<Annotation>();
        this.qualifiers.add(qualifier);
        this.payloadType = payloadType;
    }

    public ScheduledQualifiedEventPayload(String scheduleSpec, Set<Annotation> qualifiers, Class payloadType) {
        this.scheduleSpec = scheduleSpec;
        this.qualifiers = qualifiers;
        this.payloadType = payloadType;
    }

    public String getScheduleSpec() {
        return scheduleSpec;
    }

    public Class getPayloadType() {
        return payloadType;
    }

    public Set<Annotation> getQualifiers() {
        return qualifiers;
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
        if (this.qualifiers != other.qualifiers && (this.qualifiers == null || !this.qualifiers.equals(other.qualifiers))) {
            return false;
        }
        if (this.payloadType != other.payloadType && (this.payloadType == null || !this.payloadType.equals(other.payloadType))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.scheduleSpec != null ? this.scheduleSpec.hashCode() : 0);
        hash = 59 * hash + (this.qualifiers != null ? this.qualifiers.hashCode() : 0);
        hash = 59 * hash + (this.payloadType != null ? this.payloadType.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "SeamCronSchedule{" + "scheduleSpec=" + scheduleSpec + ", qualifiers=" + qualifiers + ", payloadType=" + payloadType + '}';
    }

}
