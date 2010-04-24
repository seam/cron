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
package org.jboss.seam.scheduling;

import org.jboss.seam.scheduling.quartz.QuartzStarter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.mock.MockBeanDeploymentArchive;
import org.jboss.weld.mock.MockDeployment;
import org.jboss.weld.mock.MockServletLifecycle;
import org.jboss.weld.test.BeanManagerLocator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author pete
 */
public abstract class AbstractCDITest
{

    MockServletLifecycle lifecycle;
    BeanManagerImpl manager;

    public AbstractCDITest()
    {
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() throws Exception
    {
        lifecycle.endRequest();
        lifecycle.endSession();
        lifecycle.endApplication();
        lifecycle = null;
    }

    @BeforeClass
    public void beforeClass() throws Throwable
    {

//        BeanManagerImpl manager = (BeanManagerImpl) new StartMain(new String[] {}).go();

        MockBeanDeploymentArchive jar = new MockBeanDeploymentArchive();
        MockDeployment deployment = new MockDeployment(jar);
        lifecycle = new MockServletLifecycle(deployment, jar);
        lifecycle.initialize();
        final List<Class<?>> allBeansList = new ArrayList<Class<?>>(getDefaultWebBeans());
        allBeansList.addAll(getAdditionalWebBeans());
        jar.setBeanClasses(allBeansList);
        lifecycle.beginApplication();
        lifecycle.beginSession();
        lifecycle.beginRequest();
        manager = getCurrentManager();
    }

    protected BeanManagerImpl getCurrentManager()
    {
        return BeanManagerLocator.INSTANCE.locate();
    }

    public List<Class<? extends Object>> getDefaultWebBeans()
    {
        return Arrays.asList(TestBean.class, AnotherTestBean.class, QuartzStarter.class);
    }

    /**
     * Override in your tests to register specific beans with the manager.
     * @return
     */
    public List<Class<? extends Object>> getAdditionalWebBeans()
    {
        return Collections.EMPTY_LIST;
    }
}
