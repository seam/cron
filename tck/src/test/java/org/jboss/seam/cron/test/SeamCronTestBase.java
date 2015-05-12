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
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.cron.api.asynchronous.Asynchronous;
import org.jboss.seam.cron.api.queue.Queue;
import org.jboss.seam.cron.api.scheduling.Every;
import org.jboss.seam.cron.impl.scheduling.exception.CronProviderInitialisationException;
import org.jboss.seam.cron.spi.SeamCronExtension;
import org.jboss.seam.cron.util.LoggerProducer;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

/**
 *
 * @author Peter Royle
 */
public abstract class SeamCronTestBase implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(SeamCronTestBase.class);

    /**
     *
     * @param useTestBeansXml Typically false, only true if you need to create a custom beans.xml in src/test/resources/META-INF to use during
     * testing. Otherwise the default beans.xml included at runtime will be used during testing as well, which is ideal.
     * @param includeCron Typically true, unless testing in a EE environment where you would want to deploy cron in a separate jar, which
     * you can achieve by calling addCronAsJar(archive) and passing it your war archive.
     * @return
     */
    public static JavaArchive createTestArchive(boolean useTestBeansXml, boolean includeCron) {
        final String testOrMain = useTestBeansXml ? "test" : "main";
        final JavaArchive archive = createTestArchiveTestImpl(includeCron)
                .addAsManifestResource(
                        new File("src/" + testOrMain + "/resources/META-INF/beans.xml"),
                        ArchivePaths.create("beans.xml"));

        log.debug(archive.toString(true));
        return archive;
    }

    private static JavaArchive createTestArchiveTestImpl(boolean includeCron) {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackage(LoggerProducer.class.getPackage()) // arquillian needs explicit knowledge of third-party producers
                .addPackage(SeamCronTestBase.class.getPackage());
        if (includeCron == true) {
            addCronAsClasses(archive);
        }
        return archive;
    }

    public static void addNonCDILibraries(WebArchive webArchive) throws IllegalArgumentException {
        // add library dependencies
        // TODO: when we upgrade shrinkwrap, resolve jars via maven?
        final JavaArchive libs = ShrinkWrap.create(JavaArchive.class, "libs.jar");
        libs.addPackages(true, Logger.class.getPackage());
        libs.addPackages(true, org.jboss.logmanager.Logger.class.getPackage());
        libs.addPackages(true, SimpleLogger.class.getPackage());
        libs.addPackages(true, StringUtils.class.getPackage());
        libs.addPackages(true, javax.ejb.Singleton.class.getPackage());
        libs.addPackages(true, javax.time.Instant.class.getPackage());
        webArchive.addAsLibraries(libs);
    }

    public static void addCronAsClasses(ClassContainer archive) throws IllegalArgumentException {
        archive.addPackages(true, Asynchronous.class.getPackage());
        archive.addPackages(true, SeamCronExtension.class.getPackage());
        archive.addPackages(true, Every.class.getPackage());
        archive.addPackages(true, Queue.class.getPackage());
        archive.addPackages(true, CronProviderInitialisationException.class.getPackage());
    }

    public static void addCronAsJar(WebArchive webArchive) throws IllegalArgumentException {
        // add library dependencies
        // TODO: when we upgrade shrinkwrap, resolve jars via maven?
        final JavaArchive libs = ShrinkWrap.create(JavaArchive.class, "cdilibs.jar");
        libs.addAsManifestResource("META-INF/beans.xml", "beans.xml");
        libs.addAsManifestResource("META-INF/services/javax.enterprise.inject.spi.Extension",
                "services/javax.enterprise.inject.spi.Extension");
        addCronAsClasses(libs);
        webArchive.addAsLibraries(libs);
    }
}
