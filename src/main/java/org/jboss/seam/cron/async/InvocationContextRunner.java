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
package org.jboss.seam.cron.async;

import javax.interceptor.InvocationContext;
import org.jboss.logging.Logger;
import org.jboss.seam.cron.exception.AsynchronousMethodExecutionException;

/**
 *
 * @author Peter Royle
 */
public class InvocationContextRunner implements Runnable
{

   private InvocationContext ic;
   private Logger log = Logger.getLogger(InvocationContextRunner.class);

   public InvocationContextRunner(InvocationContext ic)
   {
      log.info("New Invocation Context");
      this.ic = ic;
   }

   public void run()
   {
      log.info("Running Invocation Context");
      try
      {
         ic.proceed();
         log.info("After proceed");
      } catch (Exception ex)
      {
         throw new AsynchronousMethodExecutionException("Error executing @Asynchronous method", ex);
      }
   }
}
