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
package org.jboss.seam.cron.queue.queuj;

import com.workplacesystems.queuj.process.ProcessWrapper;
import com.workplacesystems.utilsj.collections.FilterableCollection;
import com.workplacesystems.utilsj.collections.IterativeCallback;

/**
 * Used to delegate to a provided IterativeCallback<Object,R> so that the
 * ProcessWrapper class isn't exposed in the Cron API's.
 * 
 * @author Dave Oxley
 */
class CronIterativeCallback<R> extends IterativeCallback<ProcessWrapper, R> {

    private final IterativeCallback<Object,R> ic;

    CronIterativeCallback(IterativeCallback<Object,R> ic) {
        this.ic = ic;
    }
    
    @Override
    public R iterate(final FilterableCollection<? extends ProcessWrapper> c) {
        return ic.iterate(c);
    }

    @Override
    protected void nextObject(ProcessWrapper t) {
        // Never called
    }
    
}
