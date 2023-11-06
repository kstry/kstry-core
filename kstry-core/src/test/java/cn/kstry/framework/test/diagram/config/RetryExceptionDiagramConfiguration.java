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
package cn.kstry.framework.test.diagram.config;

import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.bpmn.link.StartProcessLink;
import cn.kstry.framework.test.diagram.service.RetryExceptionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lykan
 */
@Configuration
public class RetryExceptionDiagramConfiguration {

    @Bean
    public ProcessLink retryExceptionProcess1() {
        StartProcessLink processLink = StartProcessLink.build(RetryExceptionDiagramConfiguration::retryExceptionProcess1);
        processLink.nextService(RetryExceptionService::simpleException).build().end();
        return processLink;
    }

    @Bean
    public ProcessLink retryExceptionProcess2() {
        StartProcessLink processLink = StartProcessLink.build(RetryExceptionDiagramConfiguration::retryExceptionProcess2);
        processLink.nextService(RetryExceptionService::simpleExceptionAsync).retryTimes(3).build().end();
        return processLink;
    }

    @Bean
    public ProcessLink retryExceptionProcess3() {
        StartProcessLink processLink = StartProcessLink.build(RetryExceptionDiagramConfiguration::retryExceptionProcess3);
        processLink.nextService(RetryExceptionService::simpleException1).build().end();
        return processLink;
    }

    @Bean
    public ProcessLink retryExceptionProcess4() {
        StartProcessLink processLink = StartProcessLink.build(RetryExceptionDiagramConfiguration::retryExceptionProcess4);
        processLink.nextService(RetryExceptionService::simpleExceptionAsync1).build().end();
        return processLink;
    }

    @Bean
    public ProcessLink retryExceptionProcess5() {
        StartProcessLink processLink = StartProcessLink.build(RetryExceptionDiagramConfiguration::retryExceptionProcess5);
        processLink.nextService(RetryExceptionService::simpleException2).build().end();
        return processLink;
    }

    @Bean
    public ProcessLink retryExceptionProcess6() {
        StartProcessLink processLink = StartProcessLink.build(RetryExceptionDiagramConfiguration::retryExceptionProcess6);
        processLink.nextService(RetryExceptionService::simpleExceptionAsync2).build().end();
        return processLink;
    }
}
