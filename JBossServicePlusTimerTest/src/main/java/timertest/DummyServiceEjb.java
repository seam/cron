package timertest;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TimerService;
import javax.inject.Inject;

/**
 *
 * @author peteroyle
 */
@Singleton
@Startup
public class DummyServiceEjb {

    @Inject
    private AppConfig appConfig;
    @Resource
    private TimerService timerService;

    @PostConstruct
    public void init() {
        System.out.println("Initialising dummy service implementation");
        System.out.println("App Config Schedule: " + appConfig.getSchedule());
    }

}
