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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * <p>
 * A binding annotation for use observing the Event.class event.
 * <p/><p>
 * The value can either be a schedule in Cron format, or a 'named schedule', whereby
 * the cron-formatted schedule appears in the /cron.properties file on the classpath
 * as the value of the property with the given name.
 * <p/><p>
 * Example scheduled event observer:
 * <code>public void doSomething(@Observes @Scheduled("0 * * ? * *") Trigger t) { ... }</code>
 * <p/><p>
 * Example named, scheduled event observer:
 * <code>public void doSomething(@Observes @Scheduled("after.hours") Trigger t) { ... }</code>
 * <p/><p>
 * Where the 'after.hours' schedule would be specified in /cron.properties like so:
 * <code>after.hours=0 0 * ? * *
 * <p/><p>
 * Scheduled bindings may be made typesafe using the following pattern:
 * <code>
 *

 * @Scheduled("after.hours")
 * @BindingType
 * @Retention(RetentionPolicy.RUNTIME) public @interface AfterHours { .. }
 * </code>
 * </p><p>
 * The schedule can then be observed like so:
 * <code>public void doSomething(@Observes @AfterHours Trigger t) { ... }</code>
 * </p>
 * 
 * @author Peter Royle 
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.TYPE
})
public @interface Scheduled {
    /**
     * The schedule specification (in cron format) or name.
     *
     * @return the value.
     */
    String value();
}
