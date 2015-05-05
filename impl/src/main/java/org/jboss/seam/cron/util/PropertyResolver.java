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

import java.io.IOException;
import java.util.Properties;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author peteroyle
 */
public class PropertyResolver {

    public static final String SCHEDULE_PROPERTIES_PATH = "/cron.properties";
    private static final Logger log = LoggerFactory.getLogger(PropertyResolver.class);
    private static final Properties cronProperties = new Properties();

    static {
        try {
            cronProperties.load(PropertyResolver.class.getResourceAsStream(SCHEDULE_PROPERTIES_PATH));
        } catch (IOException ex) {
            LoggerFactory.getLogger(PropertyResolver.class).warn("Cron could not find " + SCHEDULE_PROPERTIES_PATH
                    + " on the classpath and therefore will not be using it a source of named schedules.", ex);
        }
    }

    public static String resolve(String key) {
        String value;
        try {
            // try all the power of DeltaSpike first
            Class.forName("org.apache.deltaspike.core.api.config.ConfigResolver");
            log.debug("Using DeltaSpike config resolver to resolve {0}", key);
            value = ConfigResolver.getProjectStageAwarePropertyValue(key);
        } catch (ClassNotFoundException ex) {
            log.debug("Falling back to built-in property resolution resolving property {0}", key);
            // fall back to System properties 
            value = System.getProperty(key);
            if (value == null) {
                // failing that, try cron.properties
                value = cronProperties.getProperty(key);
            }
        }
        return value;
    }

}
