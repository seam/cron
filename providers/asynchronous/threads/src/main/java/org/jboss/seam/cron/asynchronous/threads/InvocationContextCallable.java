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
package org.jboss.seam.cron.asynchronous.threads;

import java.util.concurrent.Callable;
import org.jboss.seam.cron.asynchronous.spi.InvocationContextExecutor;
import org.jboss.seam.solder.core.Veto;

/**
 * Wraps #{@link InvocationContextExecutor} in Callable so that it can be run
 * in a separate thread.
 * @author peteroyle
 */
@Veto
public class InvocationContextCallable extends InvocationContextExecutor implements Callable {

    private InvocationContextExecutor executor;

    private InvocationContextCallable() {
    }

    public InvocationContextCallable(InvocationContextExecutor executor) {
        this.executor = executor;
    }

    public Object call() throws Exception {
        return executor.executeInvocationContext();
    }

}
