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
package cn.kstry.framework.core.monitor;

import cn.kstry.framework.core.enums.ScopeTypeEnum;

/**
 * @author lykan
 */
public interface FieldTracking {

    /**
     * @return 参数来源字段名
     */
    String getSourceName();

    /**
     * @return 被赋值字段名
     */
    String getTargetName();

    /**
     * @return 操作字段所在的数据域
     */
    ScopeTypeEnum getScopeType();

    /**
     * @return 实际传递的值
     */
    Object getPassValue();

    /**
     * @return 实际传递值的类型
     */
    Class<?> getPassTarget();

    /**
     * @return 实际传递值的类型名称
     */
    String getTargetType();

    /**
     * @return 序列化后的实际传递的值
     */
    String getValue();

    /**
     * 使用到的类型转换器
     */
    String getConverter();
}
