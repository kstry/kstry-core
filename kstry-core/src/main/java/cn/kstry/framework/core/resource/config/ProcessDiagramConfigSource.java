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

import cn.kstry.framework.core.component.bpmn.ProcessDiagramRegister;
import cn.kstry.framework.core.enums.SourceTypeEnum;
import com.google.common.collect.Lists;
import org.apache.commons.collections.MapUtils;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

/**
 *
 * @author lykan
 */
public class ProcessDiagramConfigSource implements ConfigSource {

    private final ApplicationContext applicationContext;

    public ProcessDiagramConfigSource(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public List<ConfigResource> getConfigResourceList() {
        Map<String, ProcessDiagramRegister> beanMap = applicationContext.getBeansOfType(ProcessDiagramRegister.class);
        if (MapUtils.isEmpty(beanMap)) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(beanMap.values());
    }

    @Override
    public SourceTypeEnum getSourceType() {
        return SourceTypeEnum.PROCESS_DIAGRAM;
    }
}
