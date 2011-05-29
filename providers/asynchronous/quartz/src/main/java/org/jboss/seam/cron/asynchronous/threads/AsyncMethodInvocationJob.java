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
package org.jboss.seam.cron.asynchronous.threads;

import org.jboss.seam.cron.spi.asynchronous.support.FutureInvokerSupport;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author peteroyle
 */
public class AsyncMethodInvocationJob implements Job {

    public void execute(final JobExecutionContext context) throws JobExecutionException {
        try {
            final FutureInvokerSupport resultCallable = (FutureInvokerSupport) context.getJobDetail().getJobDataMap().get(QuartzAsynchronousProvider.DELAYED_RESULT_SUPPORT);
            resultCallable.executeInvocationContext();
        } catch (Exception ex) {
            throw new JobExecutionException("Error invoking method inside a Quartz Job", ex);
        }
    }
}
