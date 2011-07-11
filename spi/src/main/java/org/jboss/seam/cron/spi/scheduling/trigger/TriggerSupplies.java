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
package org.jboss.seam.cron.spi.scheduling.trigger;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;


/**
 * Represents the objects required in order for a #{@link DefaultTriggerHelper}
 * to be able to fire the appropriate event when required to.
 *
 * @author Peter Royle
 */
public class TriggerSupplies {

    protected final BeanManager beanManager;
    protected final Annotation qualifier;
    protected final Set<Annotation> allQualifiers;

    public TriggerSupplies(final BeanManager beanManager, final Annotation qualifier, final Set<Annotation> allQualifiers) {
        this.beanManager = beanManager;
        this.qualifier = qualifier;
        this.allQualifiers = allQualifiers;
    }

    /**
     * @return the CDI #{@link BeanManager}.
     */
    public BeanManager getBeanManager() {
        return beanManager;
    }

    /**
     * @return the original qualifier on the schedule definition.
     */
    public Annotation getQualifier() {
        return qualifier;
    }

    /**
     * @return all the original qualifiers on the schedule definition.
     */
    public Set<Annotation> getQualifiers() {
        return allQualifiers;
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" + "beanManager=" + beanManager + ", qualifier=" + qualifier + '}';
    }
    
}
