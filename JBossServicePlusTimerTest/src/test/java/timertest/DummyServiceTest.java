/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package timertest;

import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author peteroyle
 */
@RunWith(Arquillian.class)
public class DummyServiceTest {

    @Inject
    private DummyService dummyService;

    @Deployment
    public static WebArchive createDefaultArchive() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war");
        archive.addAsWebInfResource("META-INF/beans.xml", "beans.xml");
        archive.addClass(DummyService.class);
        archive.addClass(DummyServiceEjb.class);

        archive.addAsResource("META-INF/services/javax.enterprise.inject.spi.Extension", "META-INF/services/javax.enterprise.inject.spi.Extension");
        archive.addClass(DummyExtension.class);

        System.out.println(archive.toString(true));
        return archive;
    }

    public DummyServiceTest() {
    }

    /**
     * Test of init method, of class DummyService.
     */
    @Test
    public void testDeployed() {
        assertNotNull(dummyService);
    }

}
