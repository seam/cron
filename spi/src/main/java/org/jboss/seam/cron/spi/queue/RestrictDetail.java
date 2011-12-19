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
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Contains the details necessary for the queue provider to call a concurrency
 * restriction method using the BeanManager.
 * 
 * @author Dave Oxley
 */
public class RestrictDetail {

    private final Class<?> beanClass;
    private final Method method;
    private final Set<Annotation> bindings;
    private final String queueId;

    public RestrictDetail(final Class<?> beanClass, final Method method, Set<Annotation> bindings, final String queueId) {
        this.beanClass = beanClass;
        this.method = method;
        this.bindings = bindings;
        this.queueId = queueId;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Method getMethod() {
        return method;
    }

    public Set<Annotation> getBindings() {
        return bindings;
    }

    public String getQueueId() {
        return queueId;
    }
}
