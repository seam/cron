/**
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.seam.cron.examples.swinggrapher;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.jboss.seam.cron.api.scheduling.Every;
import org.jboss.seam.cron.api.scheduling.Scheduled;
import org.jboss.seam.cron.api.scheduling.Interval;
import org.jboss.seam.cron.api.scheduling.Trigger;
import org.jboss.solder.logging.Logger;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * @author Peter Royle
 */
@ApplicationScoped
public class SwingGrapher
{

    /**
     * The label for the Free Memory axis.
     */
    public static final String FREE_MEMORY_LABEL = "Free Memory";
    private final DefaultCategoryDataset catDataSet = new DefaultCategoryDataset();
    @Inject Logger log;

    /**
     *
     * @return the dataset used by the graph.
     */
    @Produces
    public DefaultCategoryDataset getCatDataSet()
    {
        return catDataSet;
    }

    /**
     * Called every second to update the graph data and repaint the graph.
     * @param second The observed event.
     */
    public void updateChart(@Observes @Every(Interval.SECOND) Trigger second)
    {
        getCatDataSet().addValue(Runtime.getRuntime().freeMemory(), FREE_MEMORY_LABEL, new Long(System.
                currentTimeMillis()).toString());
    }

    /**
     * Called every minute to request garbage collection.
     * @param second The observed event.
     */
    public void collectGarbage(@Observes @Every(Interval.MINUTE) Trigger minute)
    {
        log.info("Requesting garbage collection");
        System.gc();
    }

    /**
     * Clear the graph every 2 minutes, at 20 seconds past the minute.
     * @param e The event observed.
     */
    public void clearGraphData(@Observes @Scheduled("20 */2 * ? * *") Trigger e)
    {
        log.info("Clearing data on schedule");
        getCatDataSet().clear();
    }
}
