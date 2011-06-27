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

import com.workplacesystems.queuj.process.java.JavaProcessRunner;


import org.jboss.seam.cron.spi.scheduling.trigger.TriggerSupplies;
import org.jboss.seam.cron.spi.scheduling.trigger.TriggerSupport;

/**
 * Base class for firing a Trigger via a QueuJ Job via a new #{@link TriggerSupport}.
 *
 * @author Dave Oxley
 */
public class TriggerRunner extends JavaProcessRunner {

    public TriggerRunner() {
    }
    
    /**
     * Executes the firing of the trigger payload via a new #{@link TriggerSupport}
     * when told to do so by the QueuJ scheduler.
     * 
     * @param context
     * @throws JobExecutionException
     */
    public void execute(final TriggerSupplies triggerSupplies) {
        (new TriggerSupport(triggerSupplies) {}).fireTrigger();
    }
}
