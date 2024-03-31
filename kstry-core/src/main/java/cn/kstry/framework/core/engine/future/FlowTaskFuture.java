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
package cn.kstry.framework.core.engine.future;

import cn.kstry.framework.core.engine.thread.EndTaskPedometer;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.util.AssertUtil;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 主流程 FlowFuture
 *
 * @author lykan
 */
public class FlowTaskFuture extends FragmentTaskFuture<AsyncTaskState> implements FlowFuture {

    /**
     * 削减锁
     */
    private final EndTaskPedometer endTaskPedometer;

    public FlowTaskFuture(EndTaskPedometer endTaskPedometer, Future<AsyncTaskState> future, String taskName) {
        super(future, taskName);
        AssertUtil.notNull(endTaskPedometer);
        this.endTaskPedometer = endTaskPedometer;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return endTaskPedometer.await(timeout, unit);
    }

    @Override
    public EndTaskPedometer getEndTaskPedometer() {
        return endTaskPedometer;
    }

    @Override
    public boolean strictMode() {
        return true;
    }
}
