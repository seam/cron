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

import org.jboss.seam.cron.spi.scheduling.CronSchedulingProvider;

/**
 * <p>
 * Base class allowing scheduling providers to easily fire the
 * appropriate CDI event when required. As opposed to {#@link TriggerSupport},
 * this class is useful when you cannot pass the objects
 * necessary for firing the appropriate event (represented by #{@link TriggerSupplies})
 * directly into the worker when the schedule is first registered
 * (see #{@link CronSchedulingProvider}). Instead some providers need to
 * copy the #{@link TriggerSupplies} into some other context, and retrieve them
 * back from that context when the worker is executed. This part is done
 * inside the #{@literal fetchTriggerSupplies} method.
 * </p><p>
 * See #{@literal TriggerJob} in providers/scheduling/quartz for an example of this.
 * </p>
 *
 * @author Peter Royle
 * 
 * @see TriggerSupport
 */
public abstract class ProviderContextTriggerSupport<T> extends TriggerSupport {

    /**
     * Since scheduled jobs are typically outside the CDI context, the
     * scheduling provider will be responsible for stashing the objects required
     * by ${@link ProviderContextTriggerSupport} to fire the appropriate event, and looking
     * them up in this method's implementation using their own context.
     *
     * @param providerContext Some context provided by the underlying scheduling engine.
     * @return a new #{@link TriggerSupplies} instance.
     */
    public abstract TriggerSupplies fetchTriggerSupplies(final T providerContext);

    /**
     * Fires the appropriate trigger payload with the appropriate qualifier
     * (to in turn execute the application-specific code that observes those events).
     *
     * @param providerContext Some context provided by the underlying scheduling engine.
     */
    public void fireTrigger(final T providerContext) {
        supplies = fetchTriggerSupplies(providerContext);
        fireTrigger();
    }
}
