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
package org.jboss.seam.cron.spi;

import org.jboss.seam.cron.impl.scheduling.exception.CronProviderInitialisationException;
import org.jboss.seam.cron.spi.scheduling.CronSchedulingInstaller;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import org.jboss.seam.cron.spi.asynchronous.CronAsynchronousProvider;
import org.jboss.seam.cron.spi.queue.CronQueueInstaller;
import org.jboss.seam.cron.spi.queue.CronQueueProvider;
import org.jboss.seam.cron.spi.scheduling.CronSchedulingProvider;
import org.jboss.seam.cron.util.CdiUtils;
import org.jboss.solder.logging.Logger;

/**
 * The CDI Extention implementation which bootstraps Seam Cron. Not directly 
 * useful to service providers.
 * 
 * @author Peter Royle
 */
public class SeamCronExtension implements Extension {

    private final Set<ProcessObserverMethod> allObservers = new HashSet<ProcessObserverMethod>();
    private CronQueueProvider queueProvider = null;
    private CronAsynchronousProvider asynchronousProvider = null;
    private CronSchedulingProvider schedulingProvider = null;
    private final Set<CronProviderLifecycle> providersWithLifecycles = new HashSet<CronProviderLifecycle>();
    private final Logger log = Logger.getLogger(SeamCronExtension.class);

    /**
     * Because "Extension classes should be public and have a public constructor 
     * for maximum portability"
     */
    public SeamCronExtension() {
    }

    public void registerCronEventObserver(@Observes ProcessObserverMethod pom) {
        allObservers.add(pom);
    }

    public void initProviders(@Observes AfterDeploymentValidation afterValid, final BeanManager manager,
            final CronQueueInstaller cronQueueInstaller, final CronSchedulingInstaller cronSchedInstaller) {
        // init all service providers
        log.debug("Initializing service providers");
        // process queue observers if queue provider exists
        final CronQueueProvider queueProvider = CdiUtils.getInstanceByType(manager, CronQueueProvider.class);
        if (queueProvider != null) {
            this.queueProvider = queueProvider;
            handleLifecycleInit(queueProvider);
            cronQueueInstaller.initProviderQueue(manager, queueProvider, allObservers);
        }
        // process scheduling observers if scheduling provider exists
        final CronSchedulingProvider schedProvider = CdiUtils.getInstanceByType(manager, CronSchedulingProvider.class);
        if (schedProvider != null) {
            this.schedulingProvider = schedProvider;
            handleLifecycleInit(schedProvider);
            cronSchedInstaller.initProviderScheduling(manager, schedProvider, allObservers);
        }
        // process scheduling observers if scheduling provider exists
        final CronAsynchronousProvider asyncProvider = CdiUtils.getInstanceByType(manager, CronAsynchronousProvider.class);
        if (asyncProvider != null) {
            this.asynchronousProvider = asyncProvider;
            handleLifecycleInit(asyncProvider);
        }

// TODO: (PR): If there's an asynch provider present, check if the interceptor is enabled. See https://jira.jboss.org/jira/browse/WELDX-91
//        final CronAsynchronousProvider asyncProvider = CdiUtils.getInstanceByType(manager, CronAsynchronousProvider.class);
//        if (asyncProvider != null) {
//            assert interceptors.isInterceptorEnabled(AsynchronousInterceptor.class);
//        }        

    }

    private void handleLifecycleInit(final Object asyncProvider) throws CronProviderInitialisationException {
        if (asyncProvider instanceof CronProviderLifecycle) {
            final CronProviderLifecycle schedProviderLifecycle = CronProviderLifecycle.class.cast(asyncProvider);
            schedProviderLifecycle.initProvider();
            providersWithLifecycles.add(schedProviderLifecycle);
        }
    }

    public void stopProviders(@Observes BeforeShutdown event, final BeanManager manager,
            final CronSchedulingInstaller cronSchedExt) {

        for (CronProviderLifecycle providerLifecycle : providersWithLifecycles) {
            providerLifecycle.destroyProvider();
        }
    }

    public CronAsynchronousProvider getAsynchronousProvider() {
        return asynchronousProvider;
    }

    public CronSchedulingProvider getSchedulingProvider() {
        return schedulingProvider;
    }

    public CronQueueProvider getQueueProvider() {
        return queueProvider;
    }
    
}
