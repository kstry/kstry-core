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
package cn.kstry.framework.core.resource.config;

import cn.kstry.framework.core.component.bpmn.BpmnProcessParser;
import cn.kstry.framework.core.component.jsprocess.transfer.JsonProcessParser;
import cn.kstry.framework.core.component.jsprocess.transfer.JsonSerializeProcessParser;
import cn.kstry.framework.core.enums.ResourceTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author lykan
 */
public class MultiProtocolProcessConfigResource extends BasicProcessConfigResource {

    public MultiProtocolProcessConfigResource(Resource resource) {
        super(resource);
    }

    @Override
    public void init(Resource resource, InputStream inputStream) {
        try {
            ResourceTypeEnum resourceType = getResourceType();
            if (resourceType == ResourceTypeEnum.BPMN_PROCESS) {
// xml 转 json 流程，测试使用
//                String s = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
//                processParser = new JsonProcessParser(this.getConfigName(), new JsonSerializeProcessParser().bpmnSerialize(s).orElse(null));
                processParser = new BpmnProcessParser(this, inputStream);
            } else if (resourceType == ResourceTypeEnum.JSON_PROCESS) {
                processParser = new JsonProcessParser(this, inputStream);
            } else {
                throw ExceptionUtil.buildException(null, ExceptionEnum.CONFIGURATION_RESOURCE_ERROR,
                        GlobalUtil.format("Unsupported file types exist to define the process! fileName: {}", getConfigName()));
            }
        } catch (Throwable e) {
            throw ExceptionUtil.buildException(e, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, GlobalUtil.format("Process configuration file parsing failure! fileName: {}", getConfigName()));
        }
    }
}
