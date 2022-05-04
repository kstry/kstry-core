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
package cn.kstry.framework.core.resource.config;

import java.util.List;

import cn.kstry.framework.core.kv.BasicKValue;

/**
 * Properties 配置文件定义
 *
 * @author lykan
 */
public interface PropertiesConfigResource extends ConfigResource {

    /**
     * 获取 K-V 列表
     *
     * @return K-V 列表
     */
    List<BasicKValue> getKValueList();
}
