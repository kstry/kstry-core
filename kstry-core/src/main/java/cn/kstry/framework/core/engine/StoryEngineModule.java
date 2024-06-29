/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
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

import cn.kstry.framework.core.component.bpmn.SerializeProcessParser;
import cn.kstry.framework.core.component.conversion.TypeConverterProcessor;
import cn.kstry.framework.core.component.limiter.RateLimiterComponent;
import cn.kstry.framework.core.container.component.ParamInjectDef;
import cn.kstry.framework.core.container.component.TaskContainer;
import cn.kstry.framework.core.container.element.StartEventContainer;
import cn.kstry.framework.core.engine.interceptor.SubProcessInterceptorRepository;
import cn.kstry.framework.core.engine.interceptor.TaskInterceptorRepository;
import cn.kstry.framework.core.engine.thread.TaskServiceExecutor;
import cn.kstry.framework.core.engine.thread.hook.ThreadSwitchHookProcessor;
import cn.kstry.framework.core.enums.ExecutorType;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.monitor.SerializeTracking;
import cn.kstry.framework.core.util.AssertUtil;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * 任务拦截器
     */
    private final TaskInterceptorRepository taskInterceptorRepository;

    /**
     * 任务执行线程池
     */
    private final TaskServiceExecutor taskThreadPool;

    /**
     * 方法执行线程池
     */
    private final TaskServiceExecutor methodThreadPool;

    /**
     * 迭代器执行线程池
     */
    private final TaskServiceExecutor iteratorThreadPool;

    /**
     * 线程切换钩子处理器
     */
    private final ThreadSwitchHookProcessor threadSwitchHookProcessor;

    /**
     * 流程序列化解析器
     */
    private final SerializeProcessParser<?> serializeProcessParser;

    /**
     * Spring上下文
     */
    private final ApplicationContext applicationContext;

    private final SerializeTracking serializeTracking;

    /**
     * 类型转换处理器
     */
    private final TypeConverterProcessor typeConverterProcessor;

    private final RateLimiterComponent rateLimiterComponent;

    public StoryEngineModule(List<TaskServiceExecutor> taskServiceExecutors, StartEventContainer startEventContainer, TaskContainer taskContainer,
                             Function<ParamInjectDef, Object> paramInitStrategy, SubProcessInterceptorRepository subInterceptorRepository,
                             TaskInterceptorRepository taskInterceptorRepository, ThreadSwitchHookProcessor threadSwitchHookProcessor, ApplicationContext applicationContext,
                             SerializeProcessParser<?> serializeProcessParser, SerializeTracking serializeTracking, TypeConverterProcessor typeConverterProcessor,
                             RateLimiterComponent rateLimiterComponent) {
        AssertUtil.anyNotNull(taskServiceExecutors, taskContainer, paramInitStrategy, startEventContainer);

        List<TaskServiceExecutor> methodThreadPoolList = taskServiceExecutors.stream().filter(s -> s.getExecutorType() == ExecutorType.METHOD).collect(Collectors.toList());
        List<TaskServiceExecutor> taskThreadPoolList = taskServiceExecutors.stream().filter(s -> s.getExecutorType() == ExecutorType.TASK).collect(Collectors.toList());
        List<TaskServiceExecutor> iteratorThreadPoolList = taskServiceExecutors.stream().filter(s -> s.getExecutorType() == ExecutorType.ITERATOR).collect(Collectors.toList());
        AssertUtil.oneSize(methodThreadPoolList, ExceptionEnum.COMPONENT_DUPLICATION_ERROR);
        AssertUtil.oneSize(taskThreadPoolList, ExceptionEnum.COMPONENT_DUPLICATION_ERROR);
        AssertUtil.oneSize(iteratorThreadPoolList, ExceptionEnum.COMPONENT_DUPLICATION_ERROR);
        this.methodThreadPool = methodThreadPoolList.get(0);
        this.taskThreadPool = taskThreadPoolList.get(0);
        this.iteratorThreadPool = iteratorThreadPoolList.get(0);
        this.taskContainer = taskContainer;
        this.paramInitStrategy = paramInitStrategy;
        this.startEventContainer = startEventContainer;
        this.subInterceptorRepository = subInterceptorRepository;
        this.threadSwitchHookProcessor = threadSwitchHookProcessor;
        this.taskInterceptorRepository = taskInterceptorRepository;
        this.applicationContext = applicationContext;
        this.serializeProcessParser = serializeProcessParser;
        this.serializeTracking = serializeTracking;
        this.typeConverterProcessor = typeConverterProcessor;
        this.rateLimiterComponent = rateLimiterComponent;
    }

    public TaskServiceExecutor getTaskThreadPool() {
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

    public TaskServiceExecutor getMethodThreadPool() {
        return methodThreadPool;
    }

    public TaskServiceExecutor getIteratorThreadPool() {
        return iteratorThreadPool;
    }

    public ThreadSwitchHookProcessor getThreadSwitchHookProcessor() {
        return threadSwitchHookProcessor;
    }

    public TaskInterceptorRepository getTaskInterceptorRepository() {
        return taskInterceptorRepository;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public SerializeProcessParser<?> getSerializeProcessParser() {
        return serializeProcessParser;
    }

    public SerializeTracking getSerializeTracking() {
        return serializeTracking;
    }

    public TypeConverterProcessor getTypeConverterProcessor() {
        return typeConverterProcessor;
    }

    public RateLimiterComponent getRateLimiterComponent() {
        return rateLimiterComponent;
    }
}
