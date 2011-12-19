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
package org.jboss.seam.cron.spi.scheduling;

import org.jboss.seam.cron.spi.scheduling.trigger.IntervalTriggerDetail;
import org.jboss.seam.cron.spi.scheduling.trigger.ScheduledTriggerDetail;

/**
 * The service contract for underlying providers of scheduled events.
 * 
 * @author peteroyle
 */
public interface CronSchedulingProvider {

    /**
     * Given the details of a scheduled trigger, set up the underlying scheduling
     * engine to fire the appropriate trigger at the scheduled times.
     * 
     * @param schedTriggerDetails Details of the schedule and qualified trigger to fire.
     * @throws Exception Anything that the underlying provider might throw.
     */
    public void processScheduledTrigger(final String queueId, final ScheduledTriggerDetail schedTriggerDetails) throws Exception;

    /**
     * Given the details of a repeating-interval trigger, set up the underlying scheduling
     * engine to fire the appropriate trigger at the appropriate times.
     * 
     * @param schedTriggerDetails Details of the interval and qualified trigger to fire.
     * @throws Exception Anything that the underlying provider might throw.
     */
    public void processIntervalTrigger(final String queueId, final IntervalTriggerDetail intervalTriggerDetails) throws Exception;

}
