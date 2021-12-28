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
package cn.kstry.framework.core.monitor;

import cn.kstry.framework.core.enums.ScopeTypeEnum;
import com.alibaba.fastjson.JSON;

/**
 *
 * @author lykan
 */
public class ParamTracking {

    private final String paramName;

    private final ScopeTypeEnum sourceScopeType;

    private final String sourceName;

    private final String value;

    private ParamTracking(String paramName, String value, ScopeTypeEnum sourceScopeType, String sourceName) {
        this.paramName = paramName;
        this.sourceScopeType = sourceScopeType;
        this.sourceName = sourceName;
        this.value = value;
    }

    public static ParamTracking build(String paramName, Object value, ScopeTypeEnum sourceScopeType, String sourceName) {
        String v;
        if (value instanceof String) {
            v = (String) value;
        } else {
            v = JSON.toJSONString(value);
        }
        return new ParamTracking(paramName, v, sourceScopeType, sourceName);
    }

    public String getParamName() {
        return paramName;
    }

    public ScopeTypeEnum getSourceScopeType() {
        return sourceScopeType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getValue() {
        return value;
    }
}
