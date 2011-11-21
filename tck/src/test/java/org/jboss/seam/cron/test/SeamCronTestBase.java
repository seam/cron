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
package org.jboss.seam.cron.test;

import java.io.File;
import java.io.Serializable;
import org.jboss.seam.cron.spi.queue.CronQueueInstaller;
import org.jboss.seam.cron.spi.scheduling.CronSchedulingInstaller;
import org.jboss.seam.cron.spi.SeamCronExtension;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.solder.logging.Logger;
import org.jboss.solder.resourceLoader.ResourceLoader;

/**
 *
 * @author Peter Royle
 */
public abstract class SeamCronTestBase implements Serializable {

    private static final Logger log = Logger.getLogger(SeamCronTestBase.class);
        
    public static JavaArchive createTestArchive() 
    {
    	final JavaArchive archive = createTestArchiveTestImpl()
    		.addAsManifestResource(
    			new File("src/main/resources/META-INF/beans.xml"), 
    			ArchivePaths.create("beans.xml"));
    	
        log.debug(archive.toString(true));
    	return archive;
    }

    public static JavaArchive createTestArchiveTestBeansXML() 
    {
    	final JavaArchive archive = createTestArchiveTestImpl()
    		.addAsManifestResource(
    			new File("src/test/resources/META-INF/beans.xml"), 
    			ArchivePaths.create("beans.xml"));
    	
        log.debug(archive.toString(true));
    	return archive;
    }

    private static JavaArchive createTestArchiveTestImpl() 
    {
    	return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackage(ResourceLoader.class.getPackage()) // arquillian needs explicit knowledge of thirdy-party producers
                .addPackage(SeamCronTestBase.class.getPackage())
                .addPackage(SeamCronExtension.class.getPackage())
                .addClasses(CronQueueInstaller.class)
                .addClasses(CronSchedulingInstaller.class);
    }

}
