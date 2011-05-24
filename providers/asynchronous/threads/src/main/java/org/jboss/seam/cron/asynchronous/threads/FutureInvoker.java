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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jboss.seam.cron.spi.asynchronous.support.FutureInvokerSupport;
import org.jboss.seam.cron.spi.asynchronous.Invoker;

/**
 * Handy #{@link RunnableFuture} which can be constructed with an #{@link Invoker}.
 * 
 * @author peteroyle
 */
public class FutureInvoker implements RunnableFuture {

    private final RunnableFuture delegate;

    public FutureInvoker(final Invoker invoker) {
        delegate = new FutureTask(new CallableFutureInvoker(new FutureInvokerSupport(invoker)));
    }

    public boolean isDone() {
        return delegate.isDone();
    }

    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    public Object get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.get(timeout, unit);
    }

    public Object get() throws InterruptedException, ExecutionException {
        return delegate.get();
    }

    public boolean cancel(final boolean mayInterruptIfRunning) {
        return delegate.cancel(mayInterruptIfRunning);
    }

    public void run() {
        delegate.run();
    }
}
