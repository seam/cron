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
import org.jboss.logging.Logger;
import org.jboss.seam.cron.api.asynchronous.Asynchronous;

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

    Logger log = Logger.getLogger(AsynchronousInterceptor.class);
    @Inject
    BeanManager beanMan;
    @Inject
    Instance<Invoker> iceCopies;
    @Inject 
    Instance<CronAsynchronousProvider> asyncStgyCopies;
    
    @AroundInvoke
    public Object executeAsynchronously(final InvocationContext ctx) throws Exception {
        Object result;

        if (log.isTraceEnabled()) {
            log.trace("Intercepting method invocation of " + ctx.getMethod().getName() + " to make it @Asynchronous");
        }
        
        final Invoker ice = iceCopies.get();
        ice.setInvocationContext(ctx);
        final CronAsynchronousProvider asyncStrategy = asyncStgyCopies.get();

        if (Future.class.isAssignableFrom(ctx.getMethod().getReturnType())) {
            // swap the "dummy" Future for a truly asynchronous future to return to the caller immediately
            ice.setPopResultsFromFuture(true);
            result = asyncStrategy.executeAndReturnFuture(ice);
        } else {
            asyncStrategy.executeWithoutReturn(ice);
            result = null;
        }

        // this will either be a Future, or null
        return result;
    }

}
