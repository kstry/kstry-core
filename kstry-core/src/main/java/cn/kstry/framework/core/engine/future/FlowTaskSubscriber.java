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
package cn.kstry.framework.core.engine.future;

import cn.kstry.framework.core.engine.FlowRegister;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.SignalType;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 自定义 Subscriber
 *
 * @author lykan
 */
public abstract class FlowTaskSubscriber extends BaseSubscriber<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowTaskSubscriber.class);

    /**
     * 流程寄存器
     */
    protected final FlowRegister flowRegister;

    /**
     * 任务名称
     */
    private final String taskName;

    /**
     * 超时时间
     */
    private final Integer timeout;

    /**
     * 严格模式，控制子任务执行失败后是否要抛出异常，默认是严格模式，子任务抛出异常后结束整个 Story 流程
     * 关闭严格模式后，子任务抛出异常时忽略继续向下执行
     */
    private final boolean strictMode;

    private final Runnable threadSwitchHook;

    private final AtomicBoolean alreadyInvoke;

    public FlowTaskSubscriber(Runnable threadSwitchHook, boolean strictMode, Integer timeout, FlowRegister flowRegister, String taskName) {
        this.threadSwitchHook = threadSwitchHook;
        this.flowRegister = flowRegister;
        this.taskName = taskName;
        this.timeout = timeout;
        this.strictMode = strictMode;
        this.alreadyInvoke = new AtomicBoolean(false);
    }

    @Override
    protected void hookOnSubscribe(@Nonnull Subscription subscription) {
        request(1);
    }

    @Override
    protected void hookOnNext(@Nonnull Object value) {
        AssertUtil.notTrue(flowRegister.getAdminFuture().isCancelled(flowRegister.getStartEventId()),
                ExceptionEnum.ASYNC_TASK_INTERRUPTED, "Task interrupted. Story task was interrupted! taskName: {}", taskName);
        if (alreadyInvoke()) {
            return;
        }
        try {
            threadSwitchHook.run();
            doNextHook(value);
        } catch (Throwable ex) {
            AdminFuture adminFuture = flowRegister.getAdminFuture();
            adminFuture.errorNotice(ex, flowRegister.getStartEventId());
        } finally {
            dispose();
        }
    }

    @Override
    protected void hookOnComplete() {
        AssertUtil.notTrue(flowRegister.getAdminFuture().isCancelled(flowRegister.getStartEventId()),
                ExceptionEnum.ASYNC_TASK_INTERRUPTED, "Task interrupted. Story task was interrupted! taskName: {}", taskName);
        if (alreadyInvoke()) {
            return;
        }
        try {
            threadSwitchHook.run();
            doCompleteHook();
        } catch (Throwable ex) {
            AdminFuture adminFuture = flowRegister.getAdminFuture();
            adminFuture.errorNotice(ex, flowRegister.getStartEventId());
        } finally {
            dispose();
        }
    }

    @Override
    protected void hookOnError(@Nonnull Throwable throwable) {
        try {
            threadSwitchHook.run();
            doErrorHook(throwable);
        } catch (Throwable ex) {
            LOGGER.warn(ex.getMessage(), ex);
        } finally {
            dispose();
        }
    }

    @Override
    protected void hookFinally(@Nonnull SignalType type) {
        try {
            threadSwitchHook.run();
            doFinallyHook();
        } catch (Throwable ex) {
            AdminFuture adminFuture = flowRegister.getAdminFuture();
            adminFuture.errorNotice(ex, flowRegister.getStartEventId());
        }
    }

    AsyncTaskState hookTimeout() {
        if (alreadyInvoke()) {
            return AsyncTaskState.TIMEOUT;
        }
        try {
            threadSwitchHook.run();
            return doTimeoutHook();
        } catch (Throwable ex) {
            LOGGER.warn(ex.getMessage(), ex);
            return AsyncTaskState.TIMEOUT;
        } finally {
            dispose();
        }
    }

    public Integer getTimeout() {
        return timeout;
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    protected void doNextHook(Object value) {
        // DO NOTHING
    }

    protected void doErrorHook(Throwable throwable) {
        // DO NOTHING
    }

    protected void doCompleteHook() {
        // DO NOTHING
    }

    protected void doFinallyHook() {
        // DO NOTHING
    }

    protected AsyncTaskState doTimeoutHook() {
        KstryException timeoutException = ExceptionUtil.buildException(null, ExceptionEnum.ASYNC_TASK_TIMEOUT, GlobalUtil.format("Async task timeout! maximum time limit: {}ms, taskName: {}", timeout, taskName));
        try {
            onError(timeoutException);
        } finally {
            flowRegister.getAdminFuture().errorNotice(timeoutException, flowRegister.getStartEventId());
        }
        return AsyncTaskState.TIMEOUT;
    }

    private boolean alreadyInvoke() {
        return !alreadyInvoke.compareAndSet(false, true);
    }
}