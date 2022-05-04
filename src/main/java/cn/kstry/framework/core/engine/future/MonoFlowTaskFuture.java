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
package cn.kstry.framework.core.engine.future;

import cn.kstry.framework.core.engine.thread.EndTaskPedometer;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.util.AssertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 异步版本的主流程 FlowFuture
 *
 * @author lykan
 */
public class MonoFlowTaskFuture extends FragmentTaskFuture<AsyncTaskState> implements MonoFlowFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonoFlowTaskFuture.class);

    /**
     * 削减锁
     */
    private final EndTaskPedometer endTaskPedometer;

    /**
     * 异步任务 CompletableFuture
     */
    private final CompletableFuture<AsyncTaskState> resultFuture = new CompletableFuture<>();

    /**
     * 异步任务 Mono
     */
    private final Mono<AsyncTaskState> resultMono = Mono.fromFuture(resultFuture);

    /**
     * 严格模式
     */
    private final boolean strictMode;

    public MonoFlowTaskFuture(EndTaskPedometer endTaskPedometer, Future<AsyncTaskState> future, String taskName, FlowTaskSubscriber flowTaskSubscriber) {
        super(future, taskName);
        AssertUtil.anyNotNull(endTaskPedometer, flowTaskSubscriber);
        this.strictMode = flowTaskSubscriber.isStrictMode();
        this.endTaskPedometer = endTaskPedometer;

        // 设置超时时间和超时回调策略
        Integer timeout = flowTaskSubscriber.getTimeout();
        Mono<AsyncTaskState> r = resultMono;
        if (timeout != null && timeout >= 0) {
            r = resultMono.timeout(Duration.ofMillis(timeout), Mono.fromSupplier(flowTaskSubscriber::hookTimeout)
            );
        }
        r.subscribe(flowTaskSubscriber);
    }

    @Override
    public Mono<AsyncTaskState> getMonoFuture() {
        return resultMono;
    }

    @Override
    public boolean cancel(String startEventId) {
        if (resultFuture.isDone() && !resultFuture.isCompletedExceptionally()) {
            return false;
        }
        boolean success = super.cancel(startEventId);
        if (success) {
            resultFuture.cancel(true);
        }
        return success;
    }

    @Override
    public void taskCompleted() {
        boolean result = resultFuture.complete(AsyncTaskState.SUCCESS);
        LOGGER.debug("CompletableFlowTask completes normally and exits! result: {}", result);
    }

    @Override
    public void taskExceptionally(Throwable ex) {
        boolean result = resultFuture.completeExceptionally(ex);
        LOGGER.debug("CompletableFlowTask completes exceptionally and exits! result: {}", result);
    }

    @Override
    public EndTaskPedometer getEndTaskPedometer() {
        return endTaskPedometer;
    }

    @Override
    public boolean strictMode() {
        return strictMode;
    }
}
