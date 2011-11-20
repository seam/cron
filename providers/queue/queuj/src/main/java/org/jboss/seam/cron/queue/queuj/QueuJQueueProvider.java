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
package org.jboss.seam.cron.queue.queuj;

import com.workplacesystems.queuj.Queue;
import com.workplacesystems.queuj.QueueBuilder;
import com.workplacesystems.queuj.QueueFactory;
import com.workplacesystems.queuj.process.QueujFactory;
import com.workplacesystems.queuj.process.java.JavaProcessBuilder;
import java.util.HashMap;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.seam.cron.impl.scheduling.exception.CronProviderDestructionException;
import org.jboss.seam.cron.impl.scheduling.exception.CronProviderInitialisationException;
import org.jboss.seam.cron.spi.CronProviderLifecycle;
import org.jboss.seam.cron.spi.queue.CronQueueProvider;
import org.jboss.seam.cron.spi.queue.RestrictDetail;
import org.jboss.solder.logging.Logger;

/**
 * Queue provider allows properties to be set against Asyncrhonous and Scheduled
 * methods. Currently only supports QueueRestrictions for managing concurrency.
 *
 * @author Dave Oxley
 */
public class QueuJQueueProvider implements CronProviderLifecycle, CronQueueProvider {

    private static final Logger log = Logger.getLogger(QueuJQueueProvider.class);
    @Inject
    BeanManager beanManager;
    private final HashMap<String,QueueBuilder> queueBuilders = new HashMap<String, QueueBuilder> ();
    private final HashMap<String,Queue> queues = new HashMap<String, Queue> ();

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

    public void processAsynRestriction(RestrictDetail restrictDetail) {
        QueueBuilder<JavaProcessBuilder> queueBuilder = QueueFactory.DEFAULT_QUEUE.newQueueBuilder();
        queueBuilder.setQueueRestriction(new CronQueueRestriction(beanManager, restrictDetail));
        queueBuilders.put(restrictDetail.getQueueId(), queueBuilder);
    }

    public void finaliseQueues() {
        for (String queueId : queueBuilders.keySet()) {
            QueueBuilder queueBuilder = queueBuilders.get(queueId);
            queues.put(queueId, queueBuilder.newQueue());
        }
        queueBuilders.clear();
    }

    public Object getQueue(String queueId) {
        return queues.get(queueId);
    }
}
