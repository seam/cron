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
package org.jboss.seam.cron.spi.queue;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import org.jboss.seam.cron.api.queue.Queue;
import org.jboss.seam.cron.api.restriction.AsyncRestriction;
import org.jboss.seam.cron.impl.scheduling.exception.CronProviderInitialisationException;
import org.jboss.seam.cron.util.CdiUtils;
import org.jboss.solder.logging.Logger;

/**
 * Scans all queue annotations and captures the configuration as a
 * #{@link RestrictDetail}, then forwards those configurations on to the 
 * #{@link CronQueueProvider} implementation so that the underlying service 
 * can be configured appropriately. Not directly useful to providers.
 * 
 * @author Dave Oxley
 */
@ApplicationScoped
public class CronQueueInstaller {
    
    private final Logger log = Logger.getLogger(CronQueueInstaller.class);

    /**
     * Initializes schedulers for all of the observed scheduled events.
     *
     * @param manager    The JSR-299 Bean Manager.
     */
    public void initProviderQueue(final BeanManager manager, final CronQueueProvider queueProvider, 
            final Set<ProcessObserverMethod> allObservers) {
        try {
            for (ProcessObserverMethod pom : allObservers) {
                ObserverMethod<?> obsMeth = pom.getObserverMethod();
                AnnotatedMethod<?> annMeth = pom.getAnnotatedMethod();
                Queue queueQualifier = null;
                AsyncRestriction restrictQualifier = null;

                for (Object bindingObj : obsMeth.getObservedQualifiers()) {
                    final Annotation originalQualifier = (Annotation) bindingObj;

                    AsyncRestriction restrictQualifier0 = (AsyncRestriction) CdiUtils.getQualifier(originalQualifier, AsyncRestriction.class);
                    if (restrictQualifier0 != null && restrictQualifier == null)
                        restrictQualifier = restrictQualifier0;

                    Queue queueQualifier0 = (Queue) CdiUtils.getQualifier(originalQualifier, Queue.class);
                    if (queueQualifier0 != null && queueQualifier == null)
                        queueQualifier = queueQualifier0;
                }

                if (queueQualifier != null && restrictQualifier != null) {
                    RestrictDetail restrictDetail = new RestrictDetail(obsMeth.getBeanClass(), annMeth.getJavaMember(), obsMeth.getObservedQualifiers(), queueQualifier.value());
                    queueProvider.processAsynRestriction(restrictDetail);
                }
            }
            queueProvider.finaliseQueues();


        } catch (Throwable t) {
            throw new CronProviderInitialisationException("Error registering queue restrictions with underlying provider", t);
        }
    }
}
