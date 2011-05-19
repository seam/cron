/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.seam.cron.asynchronous.impl;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.jboss.logging.Logger;
import org.jboss.seam.cron.asynchronous.api.Asynchronous;

/**
 * Interceptor for asynchronous methods. Method may be directly marked as
 * #{@link Asynchronous} or may exist on a type marked as #{@link Asynchronous}.
 * 
 * @author Peter Royle
 * @Asnychronous or may exist on a type marked as @Asynchronous.
 */
@Asynchronous
@Interceptor
public class AsynchronousInterceptor {

    Logger log = Logger.getLogger(AsynchronousInterceptor.class);
    @Inject
    BeanManager beanMan;
    @Inject
    Instance<InvocationCallable> icrs;
    
    @AroundInvoke
    public Object executeAsynchronously(final InvocationContext ctx) throws Exception {
        Object result;

        log.trace("Intercepting method invocation of " + ctx.getMethod().getName() + " to make it @Asynchronous");

        final InvocationCallable icr = icrs.get();
        icr.setInvocationContext(ctx);

        if (Future.class.isAssignableFrom(ctx.getMethod().getReturnType())) {
            // swap the "dummy" Future for a truly asynchronous future to return to the caller immediately
            icr.setPopResultsFromFuture(true);
            // use of FutureTask here provides the exception behaviour described by EJB
            FutureTask asyncResult = new FutureTask(icr);
            new Thread(asyncResult).start();
            result = asyncResult;
        } else {
            // Execute the method in a background thread and return nothing of value to the caller.
            // They'll need to be observing an event if they want a return value.
            new CallableAsThread(icr).start();
            result = null;
        }

        // this will either be a Future, or null
        return result;
    }
    
}
