/*
 *
 *  * Copyright (c) 2020-2023, Lykan (jiashuomeng@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.test.flow.service;

import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.container.task.TaskComponentRegister;
import cn.kstry.framework.test.flow.bo.MethodInvokeBo;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author lykan
 */
@SuppressWarnings("unused")
@Component
public class MethodInvokeService implements TaskComponentRegister {

    @TaskService(name = "increase-one")
    public void increaseOne(ScopeDataOperator operator) {
        ReentrantReadWriteLock.WriteLock writeLock = operator.writeLock();
        writeLock.lock();
        try {
            MethodInvokeBo reqScope = operator.getReqScope();
            reqScope.setA(reqScope.getA() + 1);
        } finally {
            writeLock.unlock();
        }
    }

    @TaskService(name = "increase-two")
    public void increaseTwo(ScopeDataOperator operator) {
        ReentrantReadWriteLock.WriteLock writeLock = operator.writeLock();
        writeLock.lock();
        try {
            MethodInvokeBo reqScope = operator.getReqScope();
            reqScope.setA(reqScope.getA() + 2);
        } finally {
            writeLock.unlock();
        }
    }

    @TaskService(name = "notice-result")
    public void noticeResult(ScopeDataOperator operator) {
        MethodInvokeBo reqScope;
        ReentrantReadWriteLock.ReadLock readLock = operator.readLock();
        readLock.lock();
        try {
            reqScope = operator.getReqScope();
        } finally {
            readLock.unlock();
        }
        operator.setResult(reqScope);
    }

    @Override
    public String getName() {
        return "method-invoke-service";
    }
}
