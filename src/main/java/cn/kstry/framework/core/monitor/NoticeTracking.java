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
@SuppressWarnings("unused")
public class NoticeTracking {

    private final String fieldName;

    private final String noticeName;

    private final ScopeTypeEnum noticeScopeType;

    private final String value;

    private NoticeTracking(String fieldName, String noticeName, ScopeTypeEnum noticeScopeType, String value) {
        this.fieldName = fieldName;
        this.noticeName = noticeName;
        this.noticeScopeType = noticeScopeType;
        this.value = value;
    }

    public static NoticeTracking build(String fieldName, String noticeName, ScopeTypeEnum noticeScopeType, Object value) {
        String v;
        if (value instanceof String) {
            v = (String) value;
        } else {
            v = JSON.toJSONString(value);
        }
        return new NoticeTracking(fieldName, noticeName, noticeScopeType, v);
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getNoticeName() {
        return noticeName;
    }

    public ScopeTypeEnum getNoticeScopeType() {
        return noticeScopeType;
    }

    public String getValue() {
        return value;
    }
}
