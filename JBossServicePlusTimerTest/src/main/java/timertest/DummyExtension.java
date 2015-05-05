package timertest;

import java.lang.annotation.Annotation;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessObserverMethod;

/**
 *
 * @author peteroyle
 */
public class DummyExtension implements Extension {

    /**
     * Because "Extension classes should be public and have a public constructor for maximum portability"
     */
    public DummyExtension() {
        System.out.println("Initiailised DummyService");
    }

    public void registerDummyServiceObserver(@Observes ProcessObserverMethod pom, DummyService dummyService) {
        System.out.println("processing observer method");
        dummyService.init();
    }

    public void afterValidation(@Observes AfterDeploymentValidation afterValid, final BeanManager manager
    ) {
        DummyService dummyService = getInstanceByType(manager, DummyService.class);
        System.out.println("After validation");
            dummyService.init();
    }

    public void stopProviders(@Observes BeforeShutdown event, final BeanManager manager) {

    }
    
    
    /**
     * Utility method allowing managed instances of beans to provide entry points
     * for non-managed beans (such as {@link WeldContainer}). Should only called
     * once CDI has finished booting.
     * 
     * @param manager the BeanManager to use to access the managed instance
     * @param type the type of the Bean
     * @param bindings the bean's qualifiers
     * @return a managed instance of the bean
     * 
     */
    public static <T> T getInstanceByType(final BeanManager manager, final Class<T> type, final Annotation... bindings) {
        // TODO: (PR): fix this catch and swallow hackery
        try {
            final Bean<?> bean = manager.resolve(manager.getBeans(type));
            final CreationalContext<?> cc = manager.createCreationalContext(bean);
            return type.cast(manager.getReference(bean, type, cc));
        } catch (Throwable t) {
            return null;
        }
    }
}
