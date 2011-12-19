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

import com.workplacesystems.queuj.Process;
import com.workplacesystems.queuj.Queue;
import com.workplacesystems.queuj.QueueRestriction;
import com.workplacesystems.queuj.process.ProcessIndexes;
import com.workplacesystems.queuj.process.ProcessIndexesCallback;
import com.workplacesystems.queuj.utils.QueujException;
import java.lang.annotation.Annotation;
import javax.enterprise.inject.spi.BeanManager;
import org.jboss.seam.cron.spi.queue.RestrictDetail;
import org.jboss.seam.cron.util.CdiUtils;

/**
 * 
 * @author Dave Oxley
 */
public class CronQueueRestriction extends QueueRestriction {

    private final BeanManager beanManager;
    private final RestrictDetail restrictDetail;

    public CronQueueRestriction(BeanManager beanManager, RestrictDetail restrictDetail) {
        this.beanManager = beanManager;
        this.restrictDetail = restrictDetail;
    }

    @Override
    protected boolean canRun(final Queue queue, final Process process) {
        final Object instance = CdiUtils.getInstanceByType(beanManager, restrictDetail.getBeanClass(), restrictDetail.getBindings().toArray(new Annotation[] {}));

        return process.getContainingServer().indexesWithReadLock(new ProcessIndexesCallback<Boolean>() {

            public Boolean readIndexes(ProcessIndexes processIndexes) {
                try {
                    QueuJStatusIndexes statusIndexes = new QueuJStatusIndexes(queue, processIndexes);
                    return (Boolean)restrictDetail.getMethod().invoke(instance, statusIndexes);
                } catch (Exception ex) {
                    throw new QueujException(ex);
                }
            }
        });
    }
    
}
