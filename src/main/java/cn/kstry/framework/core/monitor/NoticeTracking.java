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
public class NoticeTracking implements FieldTracking {

    private final String fieldName;

    private final String noticeName;

    private final ScopeTypeEnum noticeScopeType;

    private final Object passValue;

    private final Class<?> passTarget;

    private String value;

    private NoticeTracking(String fieldName, String noticeName, ScopeTypeEnum noticeScopeType, Object value, Class<?> passTarget) {
        this.fieldName = fieldName;
        this.noticeName = noticeName;
        this.noticeScopeType = noticeScopeType;
        this.passValue = value;
        this.passTarget = passTarget;
    }

    public static NoticeTracking build(String fieldName, String noticeName, ScopeTypeEnum noticeScopeType, Object value, Class<?> passTarget) {
        return new NoticeTracking(fieldName, noticeName, noticeScopeType, value, passTarget);
    }

    @Override
    public String getSourceName() {
        return fieldName;
    }

    @Override
    public String getTargetName() {
        return noticeName;
    }

    @Override
    public ScopeTypeEnum getScopeType() {
        return noticeScopeType;
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
