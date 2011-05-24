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
package org.jboss.seam.cron.api.scheduling;

import javax.enterprise.util.AnnotationLiteral;

/**
 * @author Peter Royle
 */
public class ScheduledLiteral
        extends AnnotationLiteral<Scheduled>
        implements Scheduled {
    
    private final String value;

    /**
     * Create a new instance of ScheduledLiteral with a default 'value'. Note that
     * this defaulted 'value' is an invalid schedule/name and will cause errors
     * during setting up of the schedules if used.
     */
    public ScheduledLiteral() {
        value = "unspecified schedule";
    }

    /**
     * Create a new instance of ScheduledLiteral using the given value as the schedule
     * specification/name.
     *
     * @param value The value to be used as the schedule specification/name.
     */
    public ScheduledLiteral(final String value) {
        this.value = value;
    }

    /**
     * The schedule specification (in cron format) or name. If the value is a name,
     * then the cron-formatted schedule specification will be read from the
     * /cron.properties file on the classpath using the name as the property key.
     *
     * @return the value.
     */
    public String value() {
        return value;
    }
}
