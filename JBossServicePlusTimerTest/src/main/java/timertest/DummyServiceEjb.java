package timertest;

import javax.ejb.LocalBean;
//import javax.ejb.Singleton; // <!-- this causes the hang!
import javax.inject.Singleton;


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
