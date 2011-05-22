/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import org.jboss.seam.cron.spi.asynchronous.AsynchronousStrategy;

/**
 * @author Peter Royle
 */
@ApplicationScoped
public class CronAsyncMethodInvocationExtension {

//    public void registerTypes(@Observes BeforeBeanDiscovery event, BeanManager manager) {
//        event.addAnnotatedType(manager.createAnnotatedType(AsynchronousInterceptor.class));
//    }

    /**
     * Initialises the asynchronous method invoker, if necessary.
     *
     */
    public void initProviderAsynchronous(AsynchronousStrategy asyncStrategy) {
        asyncStrategy.initMethodInvoker();
    }

    /**
     * Shutdown the asynchronous method invoker, if necessary.
     */
    public void stopProviderAsynchronous(AsynchronousStrategy asyncStratey) {
        asyncStratey.shutdownMethodInvoker();
    }

}
