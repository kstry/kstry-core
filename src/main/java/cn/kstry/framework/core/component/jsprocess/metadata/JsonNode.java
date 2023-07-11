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
package cn.kstry.framework.core.component.jsprocess.metadata;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * JSON 节点
 *
 * @author lykan
 */
public class JsonNode implements JsonPropertySupport {

    @JSONField(name = "id")
    private String id;

    @JSONField(name = "name")
    private String name;

    @JSONField(name = "flow-expression")
    private String flowExpression;

    @JSONField(name = "type")
    private String type;

    @JSONField(name = "properties")
    private JsonNodeProperties properties;

    @JSONField(name = "next-nodes")
    private List<String> nextNodes;

    @JSONField(name = "call-sub-process-id")
    private String callSubProcessId;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlowExpression() {
        return flowExpression;
    }

    public void setFlowExpression(String flowExpression) {
        this.flowExpression = flowExpression;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public JsonNodeProperties getProperties() {
        return properties;
    }

    public void setProperties(JsonNodeProperties properties) {
        this.properties = properties;
    }

    public List<String> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(List<String> nextNodes) {
        this.nextNodes = nextNodes;
    }

    public String getCallSubProcessId() {
        return callSubProcessId;
    }

    public void setCallSubProcessId(String callSubProcessId) {
        this.callSubProcessId = callSubProcessId;
    }
}
