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
package cn.kstry.framework.test.diagram.config;

import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.bpmn.link.StartProcessLink;
import cn.kstry.framework.core.component.instruct.JsScriptProperty;
import cn.kstry.framework.test.diagram.constants.StoryNameConstants;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lykan
 */
@Configuration
public class JsScriptDiagramConfiguration {

    @Bean
    public ProcessLink jsScriptBpmnLink1() {
        JsScriptProperty property = new JsScriptProperty();
        property.setInvokeMethod("inv");
        property.setReturnType("java.lang.Integer");
        property.setReturnTarget(Lists.newArrayList("res", "sta.r", "var.r"));

        StartProcessLink bpmnLink = StartProcessLink.build(StoryNameConstants.JS001);
        bpmnLink.nextInstruct("c-jscript", "function inv(){ ksta.v = kreq.a + kvar.num + ksta.num; return ksta.v * 2;}").property(JSON.toJSONString(property)).build().end();
        return bpmnLink;
    }

    @Bean
    public ProcessLink jsScriptBpmnLink2() {
        JsScriptProperty property = new JsScriptProperty();
        property.setReturnType("java.lang.Integer");
        property.setReturnTarget(Lists.newArrayList("res", "sta.r", "var.r"));

        StartProcessLink bpmnLink = StartProcessLink.build(StoryNameConstants.JS002);
        bpmnLink.nextInstruct("c-jscript", "ksta.v = kreq.a + kvar.num + ksta.num; return ksta.v * 2;").property(JSON.toJSONString(property)).build().end();
        return bpmnLink;
    }

    @Bean
    public ProcessLink jsScriptBpmnLink3() {
        JsScriptProperty property = new JsScriptProperty();
        property.setReturnType("cn.kstry.framework.core.component.instruct.JsScriptProperty");
        property.setReturnTarget(Lists.newArrayList("res"));

        StartProcessLink bpmnLink = StartProcessLink.build(StoryNameConstants.JS003);
        bpmnLink.nextInstruct("c-jscript", "return {'invoke-method':'invoke', 'return-type':'java.lang.Integer'}").property(JSON.toJSONString(property)).build().end();
        return bpmnLink;
    }
}
