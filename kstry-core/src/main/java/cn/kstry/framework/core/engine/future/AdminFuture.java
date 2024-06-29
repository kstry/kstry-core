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

import cn.kstry.framework.core.engine.FlowRegister;
import cn.kstry.framework.core.engine.thread.EndTaskPedometer;
import cn.kstry.framework.core.exception.KstryException;

import java.util.Optional;

/**
 * TaskFuture 管理类
 *
 * @author lykan
 */
public interface AdminFuture extends CancelableFuture {

    /**
     * 增加被管理的 Future
     *
     * @param future 被管理的 Future
     * @param startEventId 开始事件id
     */
    void addManagedFuture(FragmentFuture future, String startEventId);

    /**
     * 增加被管理的 Future
     *
     * @param parentStartEventId 父流程开始事件id
     * @param future 被管理的 Future
     * @param startEventId 开始事件id
     */
    void addManagedFuture(String parentStartEventId, FragmentFuture future, String startEventId);

    /**
     * 获取主流程 FlowFuture
     *
     * @return 主流程 FlowFuture
     */
    MainTaskFuture getMainTaskFuture();

    /**
     * 通知异常
     *
     * @param exception 异常信息
     * @param flowRegister flowRegister
     */
    void errorNotice(Throwable exception, FlowRegister flowRegister);

    /**
     * 获取异常信息
     *
     * @return 异常信息
     */
    Optional<KstryException> getException();

    /**
     * 使用开始节点id获取 EndTaskPedometer
     *
     * @param startEventId 开始节点id
     * @return EndTaskPedometer
     */
    EndTaskPedometer getEndTaskPedometer(String startEventId);
}
