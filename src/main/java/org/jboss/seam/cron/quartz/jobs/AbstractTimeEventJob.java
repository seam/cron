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
package org.jboss.seam.cron.quartz.jobs;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.seam.cron.events.AbstractTimeEvent;
import org.jboss.seam.cron.quartz.QuartzStarter;
import org.jboss.weld.context.SessionContext;
import org.jboss.weld.context.bound.BoundLiteral;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for quartz jobs which fire scheduled events (including the built-in
 * second, minute and hourly events). Implementing classes must define type of the
 * event to be fired.
 *
 * @author Peter Royle
 */
public abstract class AbstractTimeEventJob implements Job
{
    protected int value = 0;
    protected final GregorianCalendar gc = new GregorianCalendar(  );
    private Logger log = LoggerFactory.getLogger( SecondJob.class );
	private BoundSessionContext sessionContext;
	private BoundRequestContext requestContext;
	private Map<String, Object> sessionStore = new HashMap<String, Object>();
	private Map<String, Object> requestStore = new HashMap<String, Object>();
    

    /**
     * Implement this to return an instance of the appropriate event payload
     * to be used when firing the event.
     * @return an instance of the appropriate event type.
     */
    protected abstract AbstractTimeEvent createEventPayload(  );

    /**
     * <p>Executes the internally scheduled job by firing the appropriate event with the
     * appropriate binding annotation (to in turn execute the application-specific jobs
     * which observe those events).</p>
     * 
     * <p>The methods activate new Session and Request contexts on start and destroy them at the end of the execution.</p> 
     * @param context Details about the job.
     * @throws JobExecutionException
     */
    public void execute( JobExecutionContext context ) throws JobExecutionException
    {
        BeanManager manager = (BeanManager) context.getJobDetail(  ).getJobDataMap(  ).get( QuartzStarter.MANAGER_NAME );
        gc.setTime( new Date(  ) );

        final AbstractTimeEvent eventPayload = createEventPayload(  );

        for ( Annotation binding : (Set<Annotation>) context.getJobDetail(  ).getJobDataMap(  ).get( QuartzStarter.BINDINGS ) )
        {
            log.trace( "Firing time event for " + eventPayload + " with binding " + binding );
            activateContexts(manager);
            
            try {
            	manager.fireEvent( eventPayload, binding );
            } catch (Exception e) {
				log.error("Exception when executing job {}", context.getJobDetail().getFullName(), e);
				throw new JobExecutionException(e);
			}
            
            deactivateContext(manager);
        }
    }

	private void deactivateContext(BeanManager manager) {
		sessionContext.deactivate();
		sessionContext.dissociate(sessionStore);

		requestContext.deactivate();
		requestContext.dissociate(requestStore);
	}

	private void activateContexts(BeanManager manager) {
		sessionContext = getReference(manager, BoundSessionContext.class, BoundLiteral.INSTANCE);
		sessionContext.isActive();
		sessionContext.associate(sessionStore);
		sessionContext.activate();
		
		requestContext = getReference(manager, BoundRequestContext.class, BoundLiteral.INSTANCE);
		requestContext.associate(requestStore);
		requestContext.activate();
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getReference(BeanManager manager, Class<T> type, Annotation... qualifiers) {
		Bean<?> sessionBean = manager.getBeans(type, qualifiers).iterator().next();
		CreationalContext<?> creationalContext = manager.createCreationalContext(sessionBean);
		return (T) manager.getReference(sessionBean, SessionContext.class, creationalContext);
	}
}
