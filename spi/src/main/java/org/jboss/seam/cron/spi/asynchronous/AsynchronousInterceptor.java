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
package org.jboss.seam.cron.spi.asynchronous;

import java.util.concurrent.Future;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.jboss.seam.cron.api.asynchronous.Asynchronous;
import org.jboss.seam.cron.api.queue.Queue;
import org.jboss.seam.cron.impl.scheduling.exception.InternalException;
import org.jboss.seam.cron.spi.SeamCronExtension;
import org.jboss.solder.logging.Logger;

/**
 * <p>
 * Interceptor for asynchronous methods. Service providers for asynchronous 
 * method invocation should enable this interceptor in the beans.xml which ships
 * with their implementation jar. 
 * </p><p>
 * Method may be directly marked as #{@link Asynchronous} or may exist on a type 
 * marked as #{@link Asynchronous}.
 * </p>
 * 
 * @author Peter Royle
 * @Asnychronous or may exist on a type marked as @Asynchronous.
 */
@Asynchronous
@Interceptor
public class AsynchronousInterceptor {

    // We need to track where the method is being invoked from so that we can
    // handle it properly.
    protected static final String INVOKED_IN_THREAD = "INVOKED_IN_THREAD";
    public ThreadLocal<Boolean> invokedFromInterceptorInThread = new ThreadLocal<Boolean>();
    Logger log = Logger.getLogger(AsynchronousInterceptor.class);
    @Inject
    BeanManager beanMan;
    @Inject
    Instance<Invoker> iceCopies;
    @Inject SeamCronExtension cronExtension;

    public AsynchronousInterceptor() {
    }

    @AroundInvoke
    private Object executeAsynchronously(final InvocationContext ctx) throws Exception {

        if (invokedFromInterceptorInThread.get() == null) {

            if (ctx.getContextData().get(INVOKED_IN_THREAD) == null) {

                // Step 1. When the method is invoked originally, the interceptor 
                // which wraps it will land here, because the InvocationContext 
                // hasn't been fiddled with in the background thread.

                Object result = null;

                if (log.isTraceEnabled()) {
                    log.trace("Intercepting method invocation of " + ctx.getMethod().getName() + " to make it @Asynchronous");
                }

                Queue queue = ctx.getMethod().getAnnotation(Queue.class);
                String queueId = queue == null ? null : queue.value();

                final Invoker ice = iceCopies.get();
                ice.setInvocationContext(ctx);
                final CronAsynchronousProvider asyncStrategy = cronExtension.getAsynchronousProvider();

                if (Future.class.isAssignableFrom(ctx.getMethod().getReturnType())) {
                    // swap the "dummy" Future for a truly asynchronous future to return to the caller immediately
                    ice.setPopResultsFromFuture(true);
                    result = asyncStrategy.executeAndReturnFuture(queueId, ice);
                } else {
                    asyncStrategy.executeWithoutReturn(queueId, ice);
                    result = null;
                }

                // this will either be a Future, or null
                return result;

            } else {

                // Step 2. The new thread (created in Step 1 above) will assign 
                // INVOKED_IN_THREAD to TRUE in the InvocationContext and then 
                // invoke ctx.proceed(). The interceptor which wraps that invocation 
                // will land here.

                if (Boolean.TRUE.equals(ctx.getContextData().get(INVOKED_IN_THREAD))) {
                    if (log.isTraceEnabled()) {
                        log.trace("Executing original method in new thread for " + ctx.getMethod().getName());
                    }
                    invokedFromInterceptorInThread.set(Boolean.TRUE);
                    return ctx.proceed();
                } else {
                    throw new InternalException("The framework got into an illegal state while atempting to keep track of Interceptors around asynchronous method invocations. This is certainly a bug. Please file it in the SEAMCRON Jira with full stack trace");
                }
            }
        } else {

            // The interceptor around the backgrounded method invocation 
            // will set the invokedFromInterceptorInThread ThreadLocal to true
            // (see Step 2 above) and then invoke ctx.proceed(); The interceptor
            // around that invocation will land here. This simply forwards 
            // execution to the originating method.

            if (log.isTraceEnabled()) {
                log.trace("Bypassing interceptor in new thread for " + ctx.getMethod().getName());
            }
            return ctx.proceed();
        }

    }
}
