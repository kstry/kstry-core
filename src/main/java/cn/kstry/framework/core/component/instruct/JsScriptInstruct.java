/*
 *
 *  * Copyright (c) 2020-2022, Lykan (jiashuomeng@gmail.com).
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

import cn.kstry.framework.core.annotation.TaskInstruct;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.bus.InstructContent;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.constant.BpmnElementProperties;
import cn.kstry.framework.core.container.task.TaskComponentRegister;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.PropertyUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;

@SuppressWarnings("unused")
public class JsScriptInstruct implements TaskComponentRegister {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtil.class);

    private static final String DEFAULT_FUNCTION = "function {}() \n{\n{}\n}";

    @TaskInstruct(name = "jscript")
    @TaskService(name = "js-script-instruct")
    public void instruct(InstructContent instructContent, ScopeDataOperator scopeDataOperator) {
        if (StringUtils.isBlank(instructContent.getContent())) {
            return;
        }

        JsScriptProperty property = null;
        if (scopeDataOperator.getTaskProperty().isPresent()) {
            try {
                property = JSON.parseObject(scopeDataOperator.getTaskProperty().get(), JsScriptProperty.class);
            } catch (Exception e) {
                LOGGER.warn("[{}] js script property parsing exception. instruct: '{}{}', property: {}", ExceptionEnum.SCRIPT_PROPERTY_PARSER_ERROR.getExceptionCode(),
                        BpmnElementProperties.SERVICE_TASK_TASK_INSTRUCT, instructContent.getInstruct(), scopeDataOperator.getTaskProperty().orElse(StringUtils.EMPTY), e);
            }
        }

        String invokeMethodName = "invoke";
        String script = instructContent.getContent();
        try {
            if (property != null && StringUtils.isNotBlank(property.getInvokeMethod())) {
                invokeMethodName = property.getInvokeMethod();
            } else {
                script = GlobalUtil.format(DEFAULT_FUNCTION, invokeMethodName, script);
            }
            LOGGER.debug("invoke js script. instruct: '{}{}', script: {}, property: {}",
                    BpmnElementProperties.SERVICE_TASK_TASK_INSTRUCT, instructContent.getInstruct(), script, scopeDataOperator.getTaskProperty().orElse(StringUtils.EMPTY));
            ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("js");
            Bindings bind = jsEngine.createBindings();
            bind.put("ksta", scopeDataOperator.getStaScope());
            bind.put("kvar", scopeDataOperator.getVarScope());
            bind.put("kreq", scopeDataOperator.getReqScope());
            bind.put("kres", scopeDataOperator.getResult().orElse(null));
            jsEngine.setBindings(bind, ScriptContext.ENGINE_SCOPE);

            jsEngine.eval(script);
            Object result = ((Invocable) jsEngine).invokeFunction(invokeMethodName);
            if (result != null && property != null && StringUtils.isNotBlank(property.getReturnType())) {
                result = JSON.parseObject(JSON.toJSONString(result), Class.forName(property.getReturnType()));
            }
            if (property != null && CollectionUtils.isNotEmpty(property.getReturnTarget())) {
                for (String target : property.getReturnTarget()) {
                    boolean setRes = scopeDataOperator.setData(target, result);
                    LOGGER.debug("invoke js script set result. instruct: '{}{}', target: {}, set result: {}, result: {}",
                            BpmnElementProperties.SERVICE_TASK_TASK_INSTRUCT, instructContent.getInstruct(), target, setRes ? "success" : "fail", result);
                }
            }
            LOGGER.debug("invoke js script success. instruct: '{}{}', result: {}", BpmnElementProperties.SERVICE_TASK_TASK_INSTRUCT, instructContent.getInstruct(), result);
        } catch (Throwable e) {
            throw ExceptionUtil.buildException(e, ExceptionEnum.SCRIPT_EXECUTE_ERROR, GlobalUtil.format("js script execution exception! instruct: '{}{}', property: {}, script: \n{}",
                    BpmnElementProperties.SERVICE_TASK_TASK_INSTRUCT, instructContent.getInstruct(), scopeDataOperator.getTaskProperty().orElse(StringUtils.EMPTY), script));
        }
    }

    @Override
    public String getName() {
        return "js-script-instruct-component";
    }
}
