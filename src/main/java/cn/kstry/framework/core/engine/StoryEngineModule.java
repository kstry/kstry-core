/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.engine;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.kstry.framework.core.container.component.ParamInjectDef;
import cn.kstry.framework.core.container.component.TaskContainer;
import cn.kstry.framework.core.container.element.StartEventContainer;
import cn.kstry.framework.core.engine.interceptor.SubProcessInterceptorRepository;
import cn.kstry.framework.core.engine.thread.TaskThreadPoolExecutor;
import cn.kstry.framework.core.enums.ExecutorType;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;

/**
 * StoryEngine 组成模块
 *
 * @author lykan
 */
public class StoryEngineModule {

    /**
     * StartEvent 容器
     */
    private final StartEventContainer startEventContainer;

    /**
     * 服务任务组件容器
     */
    private final TaskContainer taskContainer;

    /**
     * 服务节点初始化入参策略
     */
    private final Function<ParamInjectDef, Object> paramInitStrategy;

    /**
     * 子流程拦截器集合
     */
    private final SubProcessInterceptorRepository subInterceptorRepository;

    /**
     * 任务执行线程池
     */
    private final TaskThreadPoolExecutor taskThreadPool;

    /**
     * 方法执行线程池
     */
    private final TaskThreadPoolExecutor methodThreadPool;

    public StoryEngineModule(List<TaskThreadPoolExecutor> threadPoolExecutors, StartEventContainer startEventContainer, TaskContainer taskContainer,
                             Function<ParamInjectDef, Object> paramInitStrategy, SubProcessInterceptorRepository subInterceptorRepository) {
        AssertUtil.anyNotNull(threadPoolExecutors, taskContainer, paramInitStrategy, startEventContainer);

        List<TaskThreadPoolExecutor> methodThreadPoolList =
                threadPoolExecutors.stream().filter(s -> s.getExecutorType() == ExecutorType.METHOD).collect(Collectors.toList());
        List<TaskThreadPoolExecutor> taskThreadPoolList =
                threadPoolExecutors.stream().filter(s -> s.getExecutorType() == ExecutorType.TASK).collect(Collectors.toList());
        AssertUtil.oneSize(methodThreadPoolList, ExceptionEnum.COMPONENT_DUPLICATION_ERROR);
        AssertUtil.oneSize(taskThreadPoolList, ExceptionEnum.COMPONENT_DUPLICATION_ERROR);
        this.methodThreadPool = methodThreadPoolList.get(0);
        this.taskThreadPool = taskThreadPoolList.get(0);
        this.taskContainer = taskContainer;
        this.paramInitStrategy = paramInitStrategy;
        this.startEventContainer = startEventContainer;
        this.subInterceptorRepository = subInterceptorRepository;
    }

    public TaskThreadPoolExecutor getTaskThreadPool() {
        return taskThreadPool;
    }

    public StartEventContainer getStartEventContainer() {
        return startEventContainer;
    }

    public TaskContainer getTaskContainer() {
        return taskContainer;
    }

    public Function<ParamInjectDef, Object> getParamInitStrategy() {
        return paramInitStrategy;
    }

    public SubProcessInterceptorRepository getSubInterceptorRepository() {
        return subInterceptorRepository;
    }

    public TaskThreadPoolExecutor getMethodThreadPool() {
        return methodThreadPool;
    }
}
