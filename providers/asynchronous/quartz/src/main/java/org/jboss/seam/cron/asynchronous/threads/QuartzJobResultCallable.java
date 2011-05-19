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

/**
 *
 * @author peteroyle
 */
public class QuartzJobResultCallable<T> implements Callable<T> {

    private T result;
    private boolean resultReturned = false;

    public void setResult(T result) {
        this.result = result;
        this.resultReturned = true;
    }

    public T call() throws Exception {
        while (!resultReturned) {
            // TODO: (PR): Horrible, i know!! Just testing a theory.
            Thread.sleep(500);
        }
        return result;
    }
}
