# Seam Cron Swing Example

## Quick Start

To run this example swing app use the following `mvn` command from this directory: 

    mvn install -Drun -Dswing-example

The example is a graph which monitors the amount of free memory available to the
running JVM. It uses Cron to update the graph every second, run garbage-collection
every minute and clears the graph data on the 20th second of every other minute.

The significant methods which perform these actions are shown below for reference.
You can find them in the SwingGrapher.java file.

    /**
     * Called every second to update the graph data and repaint the graph.
     * @param second The observed event.
     */
    public void updateChart(@Observes @Every(TimeUnit.SECOND) Trigger second)
    {
        getCatDataSet().addValue(Runtime.getRuntime().freeMemory(), FREE_MEMORY_LABEL, new Long(System.
                currentTimeMillis()).toString());
    }

    /**
     * Called every minute to request garbage collection.
     * @param second The observed event.
     */
    public void collectGarbage(@Observes @Every(TimeUnit.MINUTE) Trigger minute)
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

To use Seam Cron in your Maven project, include the following dependencies in your pom:

        <dependency>
            <groupId>org.jboss.seam.cron</groupId>
            <artifactId>seam-cron-api</artifactId>
            <version>3.0.0.Alpha1</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.seam.cron</groupId>
            <artifactId>seam-cron-scheduling-quartz</artifactId>
            <version>3.0.0.Alpha1</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.seam.cron</groupId>
            <artifactId>seam-cron-asynchronous-quartz</artifactId>
            <version>3.0.0.Alpha1</version>
            <scope>runtime</scope>
        </dependency>

## Contents of distribution

    artifacts/
 
        Provided libraries

    doc/

        API/SPI Docs and reference guide.
    
    examples/
 
        Example projects
  
    lib/
 
        Third-party dependencies for Seam Cron and examples
  
    source/
 
        Source code for this module
  
## Licensing

This distribution, as a whole, is licensed under the terms of the Apache
Software License, Version 2.0 (ASL).

## Seam Cron URLs

Seam Cron: http://sfwk.org/Seam3/Cron
Seam 3 project: http://sfwk.org/Seam3
Downloads: http://sfwk.org/Seam3/DistributionDownloads
Forums: http://sfwk.org/Community/Seam3Users
Source Code: http://github.com/seam/cron
Issue Tracking: http://issues.jboss.org/browse/SEAMCRON

## Release Notes



