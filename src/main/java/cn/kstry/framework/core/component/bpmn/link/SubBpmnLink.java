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
package cn.kstry.framework.core.component.bpmn.link;

import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.exception.BusinessException;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;

/**
 * 在 BpmnLink 的基础上增加了子流程特有的功能
 */
public interface SubBpmnLink extends StartBpmnLink {

    /**
     * 获取开始事件
     *
     * @return 开始事件
     */
    StartEvent getStartEvent();

    /**
     * 不允许调用
     *
     * @param id 非空
     * @return BpmnLink
     */
    static SubBpmnLink build(String id) {
        throw new BusinessException(ExceptionEnum.BUSINESS_INVOKE_ERROR.getExceptionCode(), "Method is not allowed to be called!");
    }

    /**
     * 不允许调用
     *
     * @param id   非空
     * @param name 允许为空
     * @return BpmnLink
     */
    static SubBpmnLink build(String id, String name) {
        throw new BusinessException(ExceptionEnum.BUSINESS_INVOKE_ERROR.getExceptionCode(), "Method is not allowed to be called!");
    }

    /**
     * 与子流程相关的开始事件的Id
     *
     * @param subProcessId 子流程Id
     * @return 子流程中的开始事件的Id
     */
    static String relatedStartId(String subProcessId) {
        AssertUtil.notBlank(subProcessId);
        return subProcessId + "-start";
    }
}
