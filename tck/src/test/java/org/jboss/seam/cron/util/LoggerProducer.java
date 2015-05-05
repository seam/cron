/**
 * JBoss, Home of Professional Open Source Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors by the @authors
 * tag. See the copyright.txt in the distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.seam.cron.util;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author peteroyle
 */
@Singleton
public class LoggerProducer {

    @Produces
    Logger getSlf4jLogger(final InjectionPoint ip) {
        return LoggerFactory.getLogger(ip.getMember().getDeclaringClass());
    }

    @Produces
    java.util.logging.Logger getJavaLogger(final InjectionPoint ip) {
        return java.util.logging.Logger.getLogger(ip.getMember().getDeclaringClass().getName());
    }

}
