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
import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author lykan
 */
@SuppressWarnings("unused")
public class ParamTracking implements FieldTracking {

    private final String paramName;

    private final ScopeTypeEnum sourceScopeType;

    private final String sourceName;

    private final Object passValue;

    private final Class<?> passTarget;

    private String value;

    private ParamTracking(String paramName, String sourceName, ScopeTypeEnum sourceScopeType, Object value, Class<?> passTarget) {
        this.paramName = paramName;
        this.sourceScopeType = sourceScopeType;
        this.sourceName = sourceName;
        this.passValue = value;
        this.passTarget = passTarget;
    }

    public static ParamTracking build(String paramName, String sourceName, ScopeTypeEnum sourceScopeType, Object value, Class<?> passTarget) {
        return new ParamTracking(paramName, sourceName, sourceScopeType, value, passTarget);
    }

    @Override
    public String getSourceName() {
        return sourceName;
    }

    @Override
    public String getTargetName() {
        return paramName;
    }

    @Override
    public ScopeTypeEnum getScopeType() {
        return sourceScopeType;
    }

    @Override
    @JSONField(serialize = false)
    public Object getPassValue() {
        return passValue;
    }

    @Override
    @JSONField(serialize = false)
    public Class<?> getPassTarget() {
        return passTarget;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void valueSerialize(SerializeTracking serialize) {
        this.value = serialize.valueSerialize(this);
    }
}
