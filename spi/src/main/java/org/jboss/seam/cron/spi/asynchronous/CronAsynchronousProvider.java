/**
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.seam.cron.spi.asynchronous;

import java.util.concurrent.Future;

/**
 * Interface to be implemented by providers of asynchronous method invocation.
 * The provider must call the #{@literal executeInvocationContext()} method on 
 * it to cause the original method to be executed. This will also cause the
 * appropriate callback event to be fired on completion of the method invocation
 * so that the developer can respond to the result.
 * 
 * @author peteroyle
 */
public interface CronAsynchronousProvider {

    Future executeAndReturnFuture(final String queueId, final Invoker ice);

    void executeWithoutReturn(final String queueId, final Invoker ice);

}
