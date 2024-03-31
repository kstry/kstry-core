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

import java.util.concurrent.TimeUnit;

/**
 * 主流程 FlowFuture
 *
 * @author lykan
 */
public interface FlowFuture extends MainTaskFuture {

    /**
     * 等待任务结束，超时时间
     *
     * @param timeout 超时时间
     * @param unit    超时时间单位
     * @return 等待指定时长后，任务是否执行完成
     */
    boolean await(long timeout, TimeUnit unit) throws InterruptedException;
}
