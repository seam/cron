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
package org.jboss.seam.cron.util;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 *
 * @author peteroyle
 */
public class CdiUtils {

    public static Annotation getQualifier(Annotation binding, Class qualifierType) {
        Annotation qualifier = null;
        if (qualifierType.isAssignableFrom(binding.getClass())) {
            qualifier = binding;
        } else {
            // check for meta-annotation
            qualifier = binding.annotationType().getAnnotation(qualifierType);
        }
        return qualifier;
    }

    /**
     * Utility method allowing managed instances of beans to provide entry points
     * for non-managed beans (such as {@link WeldContainer}). Should only called
     * once CDI has finished booting.
     * 
     * @param manager the BeanManager to use to access the managed instance
     * @param type the type of the Bean
     * @param bindings the bean's qualifiers
     * @return a managed instance of the bean
     * 
     */
    public static <T> T getInstanceByType(BeanManager manager, Class<T> type, Annotation... bindings) {
        // TODO: (PR): fix this catch and swallow hackery
        try {
            final Bean<?> bean = manager.resolve(manager.getBeans(type));
            CreationalContext<?> cc = manager.createCreationalContext(bean);
            return type.cast(manager.getReference(bean, type, cc));
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Utility method allowing multiple managed instances of beans to provide entry points
     * for non-managed beans (such as {@link WeldContainer}). Should only called
     * once CDI has finished booting.
     * 
     * @param manager the BeanManager to use to access the managed instance
     * @param type the type of the Bean
     * @param bindings the bean's qualifiers
     * @return a managed instance of the bean
     * 
     */
    public static <T> Set<T> getInstancesByType(BeanManager manager, Class<T> type, Annotation... bindings) {
        // TODO: (PR): fix this catch and swallow hackery
        try {
            final Set<Bean<?>> beans = manager.getBeans(type);
            final Set<T> instances = new HashSet<T>();
            for (Bean<?> bean : beans) {
                CreationalContext<?> cc = manager.createCreationalContext(bean);
                instances.add(type.cast(manager.getReference(bean, type, cc)));
            }
            return instances;
        } catch (Throwable t) {
            return null;
        }
    }
}
