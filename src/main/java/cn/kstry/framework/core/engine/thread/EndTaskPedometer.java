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

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.component.hook.Hook;
import cn.kstry.framework.core.engine.future.MonoFlowFuture;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 结束任务计步器
 *
 * @author lykan
 */
public class EndTaskPedometer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndTaskPedometer.class);

    /**
     * 任务削减锁
     */
    private final CountDownLatch taskCountDownLatch;

    /**
     * 结束节点列表
     */
    private final Set<FlowElement> endElementSet;

    /**
     * 任务完成回调
     */
    private Hook<MonoFlowFuture> completedHook;

    /**
     * 任务名
     */
    private final String taskName;

    /**
     * 开始事件ID
     */
    private final String startEventId;

    public EndTaskPedometer(String startEventId, List<FlowElement> endElementList, String taskName) {
        AssertUtil.notEmpty(endElementList);
        AssertUtil.anyNotBlank(startEventId, taskName);
        this.endElementSet = Sets.newConcurrentHashSet(endElementList);
        this.taskCountDownLatch = new CountDownLatch(endElementSet.size());
        this.taskName = taskName;
        this.startEventId = startEventId;
        LOGGER.debug("Create end task pedometer. taskName: {}, latch count: {}", taskName, this.taskCountDownLatch.getCount());
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        boolean await = taskCountDownLatch.await(timeout, unit);
        if (!await) {
            LOGGER.warn("[{}] Async task timeout! taskName: {}, maximum time limit: {}ms, block task count: {}, block task: {}",
                    ExceptionEnum.ASYNC_TASK_TIMEOUT.getExceptionCode(), taskName, timeout,
                    taskCountDownLatch.getCount(), endElementSet.stream().map(FlowElement::getId).collect(Collectors.toList()));
        }
        return await;
    }

    public void elementCompleted(FlowElement flowElement) {
        AssertUtil.notNull(flowElement);
        boolean remove = endElementSet.remove(flowElement);
        if (remove) {
            taskCountDownLatch.countDown();
        }
        LOGGER.debug("End task pedometer completed a branch. result: {}, taskName: {}, identity: {}, latch count: {}",
                remove, taskName, flowElement.identity(), taskCountDownLatch.getCount());
        if (taskCountDownLatch.getCount() > 0) {
            return;
        }
        if (this.completedHook != null) {
            LOGGER.debug("End task pedometer completed all branch. start asynchronous notification of results. taskName: {}, identity: {}, latch count: {}",
                    taskName, flowElement.identity(), taskCountDownLatch.getCount());
            this.completedHook.trigger();
        }
    }

    public String getStartEventId() {
        return startEventId;
    }

    public void forceOpenLatch() {
        endElementSet.forEach(s -> taskCountDownLatch.countDown());
    }

    public void setCompletedHook(Hook<MonoFlowFuture> completedHook) {
        this.completedHook = completedHook;
    }
}
