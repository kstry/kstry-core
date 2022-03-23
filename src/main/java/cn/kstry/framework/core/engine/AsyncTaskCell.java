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

import cn.kstry.framework.core.bpmn.EndEvent;
import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 *
 * @author lykan
 */
public class AsyncTaskCell {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTaskCell.class);

    /**
     * 开始节点
     */
    private final StartEvent startEvent;

    /**
     * EndElement 入度集合
     */
    private final Set<FlowElement> beginFlowElementSet;

    /**
     * 削减阻塞锁
     */
    private final CountDownLatch inCountDownLatch;

    /**
     * 任务异常
     */
    private volatile KstryException exception;

    /**
     * 异步任务结果
     */
    private CompletableFuture<AsyncTaskState> resultFuture;

    /**
     * 任务是否被取消
     */
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);

    /**
     * 保存任务提交后的 Future
     */
    private final Set<Future<AsyncTaskState>> taskFutureSet = Sets.newConcurrentHashSet();

    public AsyncTaskCell(StartEvent startEvent) {
        EndEvent endEvent = startEvent.getEndEvent();
        AssertUtil.notNull(endEvent);
        this.beginFlowElementSet = Sets.newConcurrentHashSet(endEvent.comingList());
        this.inCountDownLatch = new CountDownLatch(beginFlowElementSet.size());
        this.startEvent = startEvent;
    }

    public Optional<KstryException> get(long timeout, TimeUnit unit) {
        try {
            boolean await = inCountDownLatch.await(timeout, unit);
            if (!await) {
                throw new TimeoutException(GlobalUtil.format("Async task timeout! maximum time limit: {}ms, block task count: {}, block task: {}",
                        unit.toMillis(timeout), inCountDownLatch.getCount(),
                        beginFlowElementSet.stream().map(FlowElement::getId).collect(Collectors.toList())));
            }
        } catch (InterruptedException e) {
            cancel();
            return Optional.of(KstryException.buildException(e, ExceptionEnum.ASYNC_TASK_INTERRUPTED, null));
        } catch (TimeoutException e) {
            cancel();
            return Optional.of(KstryException.buildException(e, ExceptionEnum.ASYNC_TASK_TIMEOUT, null));
        } catch (Throwable e) {
            cancel();
            return Optional.of(KstryException.buildException(e, ExceptionEnum.ASYNC_TASK_ERROR, null));
        }
        return Optional.ofNullable(exception);
    }

    public void elementCompleted(FlowElement flowElement) {
        if (!beginFlowElementSet.contains(flowElement)) {
            return;
        }

        beginFlowElementSet.remove(flowElement);
        inCountDownLatch.countDown();
        if (inCountDownLatch.getCount() > 0) {
            return;
        }
        if (resultFuture != null) {
            resultFuture.complete(AsyncTaskState.SUCCESS);
        }
    }

    public void errorNotice(Throwable exception) {
        if (exception == null) {
            return;
        }
        this.exception = KstryException.buildException(exception, ExceptionEnum.ASYNC_TASK_ERROR, null);
        beginFlowElementSet.forEach(s -> inCountDownLatch.countDown());
        if (resultFuture != null) {
            resultFuture.completeExceptionally(exception);
        }
    }

    public CompletableFuture<AsyncTaskState> initResultFuture() {
        if (resultFuture == null) {
            resultFuture = new CompletableFuture<>();
        }
        return resultFuture;
    }

    public void cancel() {
        if (isCancelled()) {
            return;
        }
        isCancelled.set(true);
        taskFutureSet.stream().filter(f -> !f.isCancelled()).forEach(f -> f.cancel(true));
        LOGGER.warn("[{}] Error occurred. Story task was cancelled! startId: {}", ExceptionEnum.TASK_CANCELLED.getExceptionCode(), startEvent.getId(), exception);
    }

    public boolean isCancelled() {
        return isCancelled.get();
    }

    public void addTaskFuture(Future<AsyncTaskState> taskFuture) {
        taskFutureSet.add(taskFuture);
    }
}
