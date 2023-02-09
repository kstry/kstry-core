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
package cn.kstry.framework.core.bpmn;

import cn.kstry.framework.core.component.bpmn.builder.ServiceTaskBuilder;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.util.GlobalUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * ServiceTask
 */
public interface ServiceTask extends Task {

    /**
     * 获取 TaskComponent str
     *
     * @return TaskComponent
     */
    String getTaskComponent();

    /**
     * 获取 TaskService str
     *
     * @return TaskService
     */
    String getTaskService();

    /**
     * @return 指定自定义角色模块
     */
    ServiceNodeResource getCustomRoleInfo();

    /**
     * 是否是有效的 ServiceTask
     *
     * @return 判断结果
     */
    boolean validTask();

    /**
     * 角色匹配失败后，是否允许跳过当前节点，继续向下执行
     *
     * @return allowAbsent 默认不跳过
     */
    boolean allowAbsent();

    /**
     * 获取任务属性
     *
     * @return 任务属性
     */
    String getTaskProperty();

    /**
     * 指定任务参数
     *
     * @return 指定参数
     */
    Map<String, Object> getTaskParams();

    /**
     * 获取任务指令
     *
     * @return 指令名称
     */
    String getTaskInstruct();

    /**
     * 获取指令内容
     *
     * @return 指令内容
     */
    String getTaskInstructContent();

    /**
     * 获取Service Task 构造器
     *
     * @return ServiceTaskBuilder
     */
    static ServiceTaskBuilder builder() {
        return builder(null);
    }

    /**
     * 获取Service Task 构造器
     *
     * @param id service task id
     * @return ServiceTaskBuilder
     */
    static ServiceTaskBuilder builder(String id) {
        ServiceTaskBuilder serviceTaskBuilder = new ServiceTaskBuilder();
        if (StringUtils.isBlank(id)) {
            id = GlobalUtil.uuid();
        }
        return serviceTaskBuilder.id(id);
    }
}
