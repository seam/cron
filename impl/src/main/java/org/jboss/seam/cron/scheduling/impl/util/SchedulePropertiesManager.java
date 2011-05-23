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
package org.jboss.seam.cron.scheduling.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.logging.Logger;

/**
 * A singleton instance of the properties read from the /schedule.properties
 * file on the classpath.
 *
 * @author Peter Royle
 */
public class SchedulePropertiesManager {
    /**
     * The path on the classpath at which the properties file cotaining named schedules is expected to be found.
     */
    public static final String SCHEDULE_PROPERTIES_PATH = "/cron.properties";
    private Properties scheduleProperties = null;
    private static SchedulePropertiesManager instance = null;
    private Logger log = Logger.getLogger(SchedulePropertiesManager.class);

    /**
     * Return a singleton instance of this class, creating the instance if necessary.
     *
     * @return the singleton instance of this class.
     */
    public static SchedulePropertiesManager instance() {
        if (instance == null) {
            synchronized (SCHEDULE_PROPERTIES_PATH) {
                if (instance == null) {
                    instance = new SchedulePropertiesManager();
                }
            }
        }

        return instance;
    }

    /**
     * Creates a new instance of SchedulePropertiesManager, reading the named schedules
     * properties file if found.
     */
    public SchedulePropertiesManager() {
        scheduleProperties = new Properties();

        final InputStream schedPropsIS = this.getClass().getResourceAsStream(SCHEDULE_PROPERTIES_PATH);

        if (schedPropsIS != null) {
            try {
                scheduleProperties.load(schedPropsIS);
            } catch (IOException ex) {
                log.error("Error loading properties file for named schedules at " + SCHEDULE_PROPERTIES_PATH, ex);
            }
        } else {
            log.warn("No named schedule configurations found (" + SCHEDULE_PROPERTIES_PATH +
                    " not found on classpath).");
        }
    }

    /**
     * @return the scheduleProperties.
     */
    public Properties getScheduleProperties() {
        return scheduleProperties;
    }
}
