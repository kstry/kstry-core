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
package cn.kstry.framework.core.container.component;

import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.util.AssertUtil;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author lykan
 */
public class ParamInjectDef {

    private final boolean needInject;
    private final Class<?> paramType;

    private final String fieldName;

    private String converter;

    private String targetName;

    private List<ParamInjectDef> fieldInjectDefList;

    private ScopeTypeEnum scopeTypeEnum;

    private boolean springInitialization;

    private boolean injectSelf;

    public ParamInjectDef(boolean needInject, Class<?> paramType, String fieldName, MethodWrapper.TaskFieldProperty taskFieldProperty) {
        AssertUtil.notNull(paramType);
        this.needInject = needInject;
        this.paramType = paramType;
        this.fieldName = fieldName;
        if (taskFieldProperty != null) {
            this.targetName = taskFieldProperty.getName();
            this.scopeTypeEnum = taskFieldProperty.getScopeDataEnum();
            this.injectSelf = taskFieldProperty.isInjectSelf();
            this.converter = taskFieldProperty.getConverter();
        }
    }

    public boolean notNeedInject() {
        return !needInject;
    }

    public Class<?> getParamType() {
        return paramType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getTargetName() {
        return targetName;
    }

    public ScopeTypeEnum getScopeDataEnum() {
        return scopeTypeEnum;
    }

    public String getConverter() {
        return converter;
    }

    public void setFieldInjectDefList(List<ParamInjectDef> fieldInjectDefList) {
        AssertUtil.isNull(this.fieldInjectDefList);
        if (fieldInjectDefList != null) {
            this.fieldInjectDefList = Collections.unmodifiableList(fieldInjectDefList);
        }
    }

    public List<ParamInjectDef> getFieldInjectDefList() {
        if (fieldInjectDefList == null) {
            return null;
        }
        return fieldInjectDefList;
    }

    public boolean isSpringInitialization() {
        return springInitialization;
    }

    public void setSpringInitialization(boolean springInitialization) {
        this.springInitialization = springInitialization;
    }

    public boolean isInjectSelf() {
        return injectSelf;
    }
}
