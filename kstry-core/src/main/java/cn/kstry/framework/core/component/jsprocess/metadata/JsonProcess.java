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
package cn.kstry.framework.core.component.jsprocess.metadata;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * JSON 定义的流程
 *
 * @author lykan
 */
public class JsonProcess implements JsonPropertySupport {

    @JSONField(name = "process-id")
    private String processId;

    @JSONField(name = "process-name")
    private String processName;

    @JSONField(name = "start-id")
    private String startId;

    @JSONField(name = "start-name")
    private String startName;

    @JSONField(name = "sub-process")
    private boolean subProcess;

    @JSONField(name = "process-nodes")
    private List<JsonNode> jsonNodes;

    @JSONField(name = "properties")
    private JsonNodeProperties properties;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getStartId() {
        return startId;
    }

    public void setStartId(String startId) {
        this.startId = startId;
    }

    public String getStartName() {
        return startName;
    }

    public void setStartName(String startName) {
        this.startName = startName;
    }

    public boolean isSubProcess() {
        return subProcess;
    }

    public void setSubProcess(boolean subProcess) {
        this.subProcess = subProcess;
    }

    public List<JsonNode> getJsonNodes() {
        return jsonNodes;
    }

    public void setJsonNodes(List<JsonNode> jsonNodes) {
        this.jsonNodes = jsonNodes;
    }

    @Override
    @JSONField(serialize = false)
    public String getId() {
        return getProcessId();
    }

    @Override
    public JsonNodeProperties getProperties() {
        return properties;
    }

    public void setProperties(JsonNodeProperties properties) {
        this.properties = properties;
    }
}
