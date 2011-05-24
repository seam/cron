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
package org.jboss.seam.cron.spi.asynchronous.support;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import org.jboss.seam.cron.spi.asynchronous.Invoker;

/**
 * <p>
 * Creates a #{@link Callable} whose #{@literal call} method will block until 
 * the #{@literal executeInvocationContext()} method is called. This comes in
 * handy when returning Futures (eg: #{@literal new FutureTask(
 * new FutureInvokerSupport(invoker))}).
 * </p><p>
 * In the simplest case #{@literal executeInvocationContext()} could be called 
 * in the same thread prior to calling #{@literal call()}. This is useful when 
 * your method of executing in the background is also #{@link Runnable} or 
 * #{@link Callable} and can thus be used to return a #{@link RunnableFuture}.
 * </p><p>
 * In more complex cases #{@literal executeInvocationContext()} could be called 
 * by a separate thread at any time before or after the current thread calls 
 * #{@literal call()}. This is useful when your method of executing in the background is 
 * fairly arbitrary (eg a Quartz Job scheduled for the near future), and you need
 * to be able to return a #{@literal new FutureTask(callable)} immediately
 * but trigger the processing on that callable some arbitrary way (ie: inside the
 * Quartz Job implementation).
 * </p>
 * <p>
 * See the Seam Cron Asynchronous Provider projects for the Threads and Quartz providers
 * to see this in action (providers/scheduling/quartz and providers/asynchronous/threads).
 * </p>
 *
 * @author peteroyle
 */
public class FutureInvokerSupport implements Callable {

    private Invoker executor;
    private final BlockingQueue queue = new ArrayBlockingQueue(1);
    // can't add null to a BlockingQueue, so we might have to add a marker instead
    private static final Boolean RESULT_AS_NULL = true;

    public FutureInvokerSupport(final Invoker executor) {
        this.executor = executor;
    }

    public void executeInvocationContext() throws Exception {
        Object result = executor.executeInvocationContext();
        if (result == null) {
            result = RESULT_AS_NULL;
        }
        queue.put(result);
    }

    public Object call() throws Exception {
        Object result = queue.take();
        if (result == RESULT_AS_NULL) {
            result = null;
        }
        return result;
    }
}
