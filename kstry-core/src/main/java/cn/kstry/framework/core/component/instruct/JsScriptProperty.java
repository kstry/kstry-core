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
package cn.kstry.framework.core.component.instruct;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class JsScriptProperty {

    @JSONField(name = "return-type")
    private String returnType;

    @JSONField(name = "return-target")
    private List<String> returnTarget;

    @JSONField(name = "invoke-method")
    private String invokeMethod;

    @JSONField(name = "result-converter")
    private String resultConverter;

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<String> getReturnTarget() {
        return returnTarget;
    }

    public void setReturnTarget(List<String> returnTarget) {
        this.returnTarget = returnTarget;
    }

    public String getInvokeMethod() {
        return invokeMethod;
    }

    public void setInvokeMethod(String invokeMethod) {
        this.invokeMethod = invokeMethod;
    }

    public String getResultConverter() {
        return resultConverter;
    }

    public void setResultConverter(String resultConverter) {
        this.resultConverter = resultConverter;
    }
}
