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
package org.jboss.seam.cron.api.asynchronous;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * This is a convenience implementation of #{@link Future} which can be used as the return 
 * value of an @#{@link Asynchronous} method. This is useful because it prompts Seam Cron
 * to re-wrap the asynchronous method execution in a legitimate #{@link FutureTask} and return
 * that immediately to the calling method.
 * </p>
 * <p>
 * @#{@link Asynchronous} methods which do not have a return type of #{@link Future} must
 * listen for the method's return value using CDI observers.
 * </p>
 * 
 * @author Peter Royle
 */
public class AsyncResult<T> implements Future<T> {

    private final T result;

    /**
     * Wraps the given result in a #{@link Future}.
     * @param result 
     */
    public AsyncResult(final T result) {
        this.result = result;
    }

    /**
     * This "dummy" Future represents a calculation which has already been performed, and thus
     * cannot be canceled.
     * @param bln Whether r not to attempt to cancel the task.
     * @return false every time
     */
    public boolean cancel(boolean bln) {
        return false;
    }

    /**
     * This "dummy" Future represents a calculation which has already been performed, and thus
     * cannot be canceled.
     * @param bln Whether r not to attempt to cancel the task.
     * @return false every time
     */
    public boolean isCancelled() {
        return false;
    }

    /**
     * This "dummy" Future represents a calculation which has already been performed.
     * @return true every time
     */
    public boolean isDone() {
        return true;
    }

    public T get() throws InterruptedException, ExecutionException {
        return result;
    }

    public T get(long l, TimeUnit tu) throws InterruptedException, ExecutionException, TimeoutException {
        return result;
    }
}
