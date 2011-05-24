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

import org.jboss.seam.cron.impl.scheduling.exception.CronProviderDestructionException;
import org.jboss.seam.cron.impl.scheduling.exception.CronProviderInitialisationException;
import org.jboss.seam.cron.spi.scheduling.CronSchedulingProvider;

/**
 * <p>
 * If you are implementing a provider for scheduled or asynchronous method 
 * invocation, and you need to initialize and tear down some infrastructure
 * before and after you provider can be used, then provide an implementation of 
 * this interface. It is usually most convenient to add this interface to
 * your #{@link CronSchedulingProvider}, but it could be provided as a separate 
 * class if you wish. You could even provide multiple implementations of this
 * interface if desired, noting that the order in which they are initialized and
 * destroyed cannot be guaranteed.
 * </p>
 * 
 * @author peteroyle
 */
public interface CronProviderLifecycle {

    /**
     * Initializes the underlying provider.
     *
     */
    void initProvider() throws CronProviderInitialisationException;

    /**
     * Shutdown the underlying provider, called on application close/undeployment.
     */
    void destroyProvider() throws CronProviderDestructionException;
    
}
