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
package org.jboss.seam.cron.spi.scheduling.trigger;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Simple container for the qualifying annotation, payload type and schedule
 * definition of a scheduled event to be fired.
 * 
 * @author Peter Royle
 */
public class ScheduledTriggerDetail extends TriggerDetail {

    private final String cronScheduleSpec;

    public ScheduledTriggerDetail(final String dereferencedScheduleSpec, final Annotation qualifier, final Set<Annotation> allQualifiers) {
        super(qualifier, allQualifiers);
        this.cronScheduleSpec = dereferencedScheduleSpec;
    }

    /**
     * @return The schedule specification in full cron format
     */
    public String getCronScheduleSpec() {
        return cronScheduleSpec;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ScheduledTriggerDetail other = (ScheduledTriggerDetail) obj;
        if ((this.cronScheduleSpec == null) ? (other.cronScheduleSpec != null) : !this.cronScheduleSpec.equals(other.cronScheduleSpec)) {
            return false;
        }
        if (this.getQualifier() != other.getQualifier() && (this.getQualifier() == null || !this.getQualifier().equals(other.getQualifier()))) {
            return false;
        }
        if (this.getQualifiers() != other.getQualifiers() && (this.getQualifiers() == null || !this.getQualifiers().equals(other.getQualifiers()))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.cronScheduleSpec != null ? this.cronScheduleSpec.hashCode() : 0);
        hash = 17 * hash + (this.getQualifier() != null ? this.getQualifier().hashCode() : 0);
        hash = 17 * hash + (this.getQualifiers() != null ? this.getQualifiers().hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" + "scheduleSpec=" + cronScheduleSpec + ", qualifier=" + getQualifier() + ", qualifiers=" + getQualifiers() + '}';
    }
}
