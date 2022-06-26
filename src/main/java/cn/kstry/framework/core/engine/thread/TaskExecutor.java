/*
 *
 *  * Copyright (c) 2020-2022, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.engine.thread;

import cn.kstry.framework.core.engine.future.AdminFuture;
import cn.kstry.framework.core.engine.future.InvokeFuture;
import cn.kstry.framework.core.enums.ExecutorType;

import java.util.concurrent.Executor;

/**
 * 任务执行器
 *
 * @author lykan
 */
public interface TaskExecutor extends Executor {

    /**
     * 提交流程任务，并返回结果操作入口
     *
     * @param mainFlowTask 流程任务
     * @return AdminFuture
     */
    AdminFuture submitAdminTask(MainFlowTask mainFlowTask);

    /**
     * 提交片段任务
     */
    void submitFragmentTask(FragmentTask fragmentTask);

    /**
     * 提交异步流程任务
     *
     * @param parentStartEventId 开始事件id
     * @param flowTask 流程任务
     */
    void submitMonoFlowTask(String parentStartEventId, MonoFlowTask flowTask);

    /**
     * 提交方法执行任务
     *
     * @param methodInvokeTask 方法执行任务
     * @return 方法执行任务Future
     */
    InvokeFuture submitMethodInvokeTask(MethodInvokeTask methodInvokeTask);

    /**
     * 获取线程池类型
     *
     * @return 线程池类型
     */
    ExecutorType getExecutorType();

    /**
     * 获取前缀
     *
     * @return 前缀
     */
    String getPrefix();
}
