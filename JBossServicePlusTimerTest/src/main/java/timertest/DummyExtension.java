package timertest;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

/**
 *
 * @author peteroyle
 */
public class DummyExtension implements Extension {

    public void afterValidation(@Observes AfterDeploymentValidation afterValid, final BeanManager manager,
            AppConfig appConfig) {
        System.out.println("After validation running ...");
        appConfig.setSchedule("0 5 * * * *");
        System.out.println("After validation complete");
    }

}
