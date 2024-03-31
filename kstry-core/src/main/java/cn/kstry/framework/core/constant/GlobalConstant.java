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
package cn.kstry.framework.core.constant;

import cn.kstry.framework.core.enums.ScopeTypeEnum;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author lykan
 */
public interface GlobalConstant {

    /**
     * KV 默认域名称
     */
    String VARIABLE_SCOPE_DEFAULT = "default";

    /**
     * STORY DATA SCOPE
     */
    List<ScopeTypeEnum> STORY_DATA_SCOPE = Lists.newArrayList(ScopeTypeEnum.REQUEST, ScopeTypeEnum.STABLE, ScopeTypeEnum.VARIABLE, ScopeTypeEnum.RESULT);

    /**
     * void 类型
     */
    String VOID = "void";

    /**
     * KValue Bean 注册到 Spring 容器的名字
     */
    String KV_SCOPE_DEFAULT_BEAN_NAME = "kstry-kv-scope-{}";

    /**
     * 正则表达式：匹配有效的条件表达式
     */
    String VALID_DATA_EXPRESSION_PATTERN = "((req)|(sta)|(var)|(res))((\\.[\\w-]+)|(\\['[\\w-]+'\\])|(\\.\\[\\d+\\]))+";

    /**
     * fastjson序列化参数
     */
    SerializerFeature[] SERIALIZE_SF = {SerializerFeature.WriteMapNullValue, SerializerFeature.DisableCircularReferenceDetect};
}
