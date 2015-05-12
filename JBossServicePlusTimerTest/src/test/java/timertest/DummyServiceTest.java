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
    private DummyServiceEjb dummyService;

    @Deployment
    public static WebArchive createDefaultArchive() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war");
        archive.addAsWebInfResource("META-INF/beans.xml", "beans.xml");
        archive.addClass(DummyServiceEjb.class);
        archive.addClass(AppConfig.class);

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
