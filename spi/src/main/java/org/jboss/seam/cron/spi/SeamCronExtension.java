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
package org.jboss.seam.cron.spi;

import org.jboss.seam.cron.spi.scheduling.CronSchedulingInstaller;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import org.jboss.logging.Logger;
import org.jboss.seam.cron.scheduling.impl.exception.SchedulerConfigurationException;
import org.jboss.seam.cron.spi.scheduling.CronScheduleProvider;
import org.jboss.seam.cron.util.CdiUtils;

/**
 *
 * @author Peter Royle
 */
@ApplicationScoped
public class SeamCronExtension implements Extension {

    private final Set<ObserverMethod> allObservers = new HashSet<ObserverMethod>();
    private Logger log = Logger.getLogger(SeamCronExtension.class);

    public void registerCronEventObserver(@Observes ProcessObserverMethod pom) {
        allObservers.add(pom.getObserverMethod());
    }

    public void initProviders(@Observes AfterDeploymentValidation afterValid, BeanManager manager,
            CronSchedulingInstaller cronSchedExt) {
        // init all service providers
        log.debug("Initialising service providers");
        Set<CronProviderLifecycle> providerLifecycles = getProviderLifecycles(manager);
        for (CronProviderLifecycle providerLifecycle : providerLifecycles) {
            log.info("Initialising service provider: " + providerLifecycle.toString());
            providerLifecycle.initProvider();
        }
        // process scheduling observers if scheduling provider exists
        CronScheduleProvider schedulingProvider = CdiUtils.getInstanceByType(manager, CronScheduleProvider.class);
        if (schedulingProvider != null) {
            cronSchedExt.initProviderScheduling(manager, schedulingProvider, allObservers);
        }
    }

    public void stopProviders(@Observes BeforeShutdown event, BeanManager manager,
            CronSchedulingInstaller cronSchedExt) {

        Set<CronProviderLifecycle> providerLifecycles = getProviderLifecycles(manager);
        for (CronProviderLifecycle providerLifecycle : providerLifecycles) {
            providerLifecycle.destroyProvider();
        }
    }
    
    /**
     * 
     * @param manager
     * @param cronSchedExt
     * @param cronAsyncExt
     * @return Not null
     * @throws SchedulerConfigurationException 
     */
    private Set<CronProviderLifecycle> getProviderLifecycles(BeanManager manager) throws SchedulerConfigurationException {
        Set<CronProviderLifecycle> providerLifecycles = CdiUtils.getInstancesByType(manager, CronProviderLifecycle.class);
        return providerLifecycles;
    }

}
