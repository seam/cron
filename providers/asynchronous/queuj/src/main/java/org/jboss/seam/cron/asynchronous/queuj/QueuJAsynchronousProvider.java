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
package org.jboss.seam.cron.asynchronous.queuj;

import com.workplacesystems.queuj.Queue;
import com.workplacesystems.queuj.QueueFactory;
import com.workplacesystems.queuj.process.QueujFactory;
import com.workplacesystems.queuj.process.java.JavaProcessBuilder;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.seam.cron.impl.asynchronous.exception.AsynchronousMethodInvocationException;
import org.jboss.seam.cron.impl.scheduling.exception.CronProviderDestructionException;
import org.jboss.seam.cron.impl.scheduling.exception.CronProviderInitialisationException;
import org.jboss.seam.cron.spi.CronProviderLifecycle;
import org.jboss.seam.cron.spi.SeamCronExtension;
import org.jboss.seam.cron.spi.asynchronous.CronAsynchronousProvider;
import org.jboss.seam.cron.spi.asynchronous.Invoker;
import org.jboss.seam.cron.spi.asynchronous.support.FutureInvokerSupport;
import org.jboss.seam.cron.spi.queue.CronQueueProvider;
import org.jboss.solder.logging.Logger;

/**
 * Simple asynchronous method invocation which schedules @Asynchronous methods
 * to be executed at some point in the very near future using the QueuJ scheduler.
 *
 * @author Dave Oxley
 */
public class QueuJAsynchronousProvider implements CronProviderLifecycle, CronAsynchronousProvider {

    private static final Logger log = Logger.getLogger(QueuJAsynchronousProvider.class);
    @Inject
    BeanManager beanManager;
    @Inject
    SeamCronExtension cronExtension;

    /**
     * Initialises the scheduler.
     *
     */
    public void initProvider() throws CronProviderInitialisationException {
        try {
            QueujFactory.getProcessServer((String)null);
        } catch (Exception ex) {
            throw new CronProviderInitialisationException("Error initializing QueuJ for asynchronous method invocation", ex);
        }
    }

    /**
     * Shutdown the scheduler on application close.
     */
    public void destroyProvider() throws CronProviderDestructionException {
    }

    public void executeWithoutReturn(final String queueId, final Invoker inkover) {
        executeMethodAsScheduledJob(queueId, inkover);
    }

    public Future executeAndReturnFuture(final String queueId, final Invoker invoker) {
        FutureTask asyncResult = new FutureTask(executeMethodAsScheduledJob(queueId, invoker));
        new Thread(asyncResult).start();
        return asyncResult;
    }

    private FutureInvokerSupport executeMethodAsScheduledJob(final String queueId, final Invoker invoker) throws AsynchronousMethodInvocationException {
        Queue<JavaProcessBuilder> queue = QueueFactory.DEFAULT_QUEUE;
        if (queueId != null) {
            CronQueueProvider queueProvider = cronExtension.getQueueProvider();
            queue = (Queue)queueProvider.getQueue(queueId);
        }
        JavaProcessBuilder jpb = queue.newProcessBuilder(Locale.getDefault());
        final String jobName = UUID.randomUUID().toString();
        jpb.setProcessName(jobName);
        final FutureInvokerSupport drs = new FutureInvokerSupport(invoker);
        jpb.setProcessDetails(new AsyncMethodInvocationJob(), "execute", new Class[] { FutureInvokerSupport.class }, new Object[] { drs });
        jpb.setProcessPersistence(false);
        jpb.newProcess();
        return drs;
    }
}
