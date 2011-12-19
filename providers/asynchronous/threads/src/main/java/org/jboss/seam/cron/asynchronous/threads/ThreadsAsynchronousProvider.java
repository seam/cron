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

import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import org.jboss.seam.cron.spi.asynchronous.CronAsynchronousProvider;
import org.jboss.seam.cron.spi.asynchronous.Invoker;

/**
 * Simple asynchronous method invocation strategy which wraps the execution
 * in a new Thread and starts it.
 *
 * @author peteroyle
 */
public class ThreadsAsynchronousProvider implements CronAsynchronousProvider {

    public void executeWithoutReturn(final String queueId, final Invoker invoker) {
        // Execute the method in a background thread and return nothing of value to the caller.
        // They'll need to be observing an event if they want a return value.
        final RunnableFuture asyncResult = new FutureInvoker(invoker);
        new Thread(asyncResult).start();
    }

    public Future executeAndReturnFuture(final String queueId, final Invoker invoker) {
        final RunnableFuture asyncResult = new FutureInvoker(invoker);
        new Thread(asyncResult).start();
        return asyncResult;
    }

}
