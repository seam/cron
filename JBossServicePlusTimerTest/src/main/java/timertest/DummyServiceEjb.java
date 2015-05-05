package timertest;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;

/**
 *
 * @author peteroyle
 */
@Singleton
@LocalBean
public class DummyServiceEjb implements DummyService {

    public void init() {
        System.out.println("Initialising dummy service implementation");
    }
    
}
