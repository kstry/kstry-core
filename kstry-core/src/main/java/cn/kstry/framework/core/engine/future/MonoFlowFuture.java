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

import cn.kstry.framework.core.enums.AsyncTaskState;
import reactor.core.publisher.Mono;

/**
 * 异步版本的主流程 FlowFuture
 *
 * @author lykan
 */
public interface MonoFlowFuture extends MainTaskFuture {

    /**
     * 获取  Mono
     *
     * @return Mono
     */
    Mono<AsyncTaskState> getMonoFuture();

    /**
     * 任务被完成
     */
    void taskCompleted();

    /**
     * 任务出现异常
     *
     * @param ex 异常信息
     */
    void taskExceptionally(Throwable ex);
}
