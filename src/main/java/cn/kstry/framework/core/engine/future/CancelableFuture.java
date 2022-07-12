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
package cn.kstry.framework.core.engine.future;

/**
 * 可取消的 Future
 *
 * @author lykan
 */
public interface CancelableFuture {

    /**
     * 取消任务执行
     *
     * @param startEventId 流程开始事件ID
     */
    boolean cancel(String startEventId);

    /**
     * 判断任务执行是否被取消
     *
     * @param startEventId 流程开始事件ID
     * @return 任务执行是否被取消
     */
    boolean isCancelled(String startEventId);
}
