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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.bpmn.AggregationFlowElement;
import cn.kstry.framework.core.bpmn.AsyncFlowElement;
import cn.kstry.framework.core.bpmn.BpmnElement;
import cn.kstry.framework.core.bpmn.FlowElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * ElementPropertyUtil
 *
 * @author lykan
 */
public class ElementPropertyUtil {

    /**
     *
     * @param element element
     * @return true：是
     */
    public static boolean isSupportAggregation(BpmnElement element) {
        if (element == null) {
            return false;
        }
        return element instanceof AggregationFlowElement;
    }

    /**
     * 是否需要开启异步
     *
     * @param flowElement flowElement
     * @return boolean
     */
    public static boolean needOpenAsync(FlowElement flowElement) {
        if (!(flowElement instanceof AsyncFlowElement)) {
            return false;
        }
        AsyncFlowElement asyncFlowElement = GlobalUtil.transferNotEmpty(flowElement, AsyncFlowElement.class);
        return asyncFlowElement.openAsync();
    }

    public static Optional<String> getNodeProperty(FlowNode flowNode, String name) {
        AssertUtil.notBlank(name);
        if (flowNode == null) {
            return Optional.empty();
        }

        ExtensionElements extensionElements = flowNode.getExtensionElements();
        if (extensionElements == null) {
            return Optional.empty();
        }

        Collection<CamundaProperties> camundaProperties = extensionElements.getChildElementsByType(CamundaProperties.class);
        if (CollectionUtils.isEmpty(camundaProperties)) {
            return Optional.empty();
        }

        for (CamundaProperties camundaProperty : camundaProperties) {
            Collection<CamundaProperty> ps = camundaProperty.getCamundaProperties();
            if (CollectionUtils.isEmpty(ps)) {
                continue;
            }
            Optional<String> rOptional = ps.stream()
                    .filter(p -> Objects.equals(p.getCamundaName(), name)).map(CamundaProperty::getCamundaValue).filter(StringUtils::isNotBlank).findFirst();
            if (rOptional.isPresent()) {
                return rOptional;
            }
        }
        return Optional.empty();
    }
}
