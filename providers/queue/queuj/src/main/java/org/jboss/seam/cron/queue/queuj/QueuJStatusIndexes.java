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
import com.workplacesystems.queuj.process.ProcessIndexes;
import com.workplacesystems.utilsj.collections.IterativeCallback;
import org.jboss.seam.cron.api.restriction.StatusIndexes;

/**
 * Implementation of StatusIndexes for QueuJ.
 * 
 * @author Dave Oxley
 */
public class QueuJStatusIndexes implements StatusIndexes {

    private final Queue queue;
    private final ProcessIndexes processIndexes;

    QueuJStatusIndexes(Queue queue, ProcessIndexes processIndexes) {
        this.queue = queue;
        this.processIndexes = processIndexes;
    }

    public int countOfNotRunProcesses() {
        return processIndexes.countOfNotRunProcesses(queue);
    }

    public int countOfRunningProcesses() {
        return processIndexes.countOfRunningProcesses(queue);
    }

    public int countOfWaitingToRunProcesses() {
        return processIndexes.countOfWaitingToRunProcesses(queue);
    }

    public int countOfFailedProcesses() {
        return processIndexes.countOfFailedProcesses(queue);
    }

    public <R> R iterateNotRunProcesses(IterativeCallback<Object, R> ic) {
        return processIndexes.iterateNotRunProcesses(queue, new CronIterativeCallback<R>(ic));
    }

    public <R> R iterateRunningProcesses(IterativeCallback<Object, R> ic) {
        return processIndexes.iterateRunningProcesses(queue, new CronIterativeCallback<R>(ic));
    }

    public <R> R iterateWaitingToRunProcesses(IterativeCallback<Object, R> ic) {
        return processIndexes.iterateWaitingToRunProcesses(queue, new CronIterativeCallback<R>(ic));
    }

    public <R> R iterateFailedProcesses(IterativeCallback<Object, R> ic) {
        return processIndexes.iterateFailedProcesses(queue, new CronIterativeCallback<R>(ic));
    }
}
