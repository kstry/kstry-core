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
package cn.kstry.framework.core.constant;

import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author lykan
 */
public interface GlobalConstant {

    /**
     * Task Service 类型枚举
     */
    List<IdentityTypeEnum> TASK_SERVICE_ENUM_LIST = Lists.newArrayList(IdentityTypeEnum.SERVICE_TASK_ABILITY, IdentityTypeEnum.SERVICE_TASK);

    /**
     * KV 默认域名称
     */
    String VARIABLE_SCOPE_DEFAULT = "default";

    /**
     * STORY DATA SCOPE
     */
    List<ScopeTypeEnum> STORY_DATA_SCOPE = Lists.newArrayList(ScopeTypeEnum.REQUEST, ScopeTypeEnum.STABLE, ScopeTypeEnum.VARIABLE);

    /**
     * Element 引用最大次数，链路中存在循环依赖时会报错
     */
    int ELEMENT_MAX_OCCUR_NUMBER = 1000;

    /**
     * void 类型
     */
    String VOID = "void";

    /**
     * KValue Bean 注册到 Spring 容器的名字
     */
    String KV_SCOPE_DEFAULT_BEAN_NAME = "kstry-kv-scope-{}";
}
