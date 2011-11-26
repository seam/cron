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
package org.jboss.seam.cron.spi.queue;

/**
 * Interface to be implemented by providers of queue.
 * 
 * @author Dave Oxley
 */
public interface CronQueueProvider {

    /**
     * Given the details of a restriction, set up the underlying queue
     * engine to configure an appopriate queue.
     * 
     * @param restrictDetail Details of the concurrency restriction.
     */
    public void processAsynRestriction(RestrictDetail restrictDetail);

    /**
     * Finalises or commits the queue once all properties have been configured
     * for each queue.
     */
    public void finaliseQueues();

    /**
     * Retrieve the Queue object (provider specific Object) for the given
     * queueId.
     * 
     * @param queueId The id of the queue.
     * @return the queue Object.
     */
    public Object getQueue(String queueId);    
}
