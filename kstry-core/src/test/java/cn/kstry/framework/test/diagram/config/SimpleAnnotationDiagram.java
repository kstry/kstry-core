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
package cn.kstry.framework.test.diagram.config;

import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.bpmn.link.StartProcessLink;
import cn.kstry.framework.test.diagram.service.SimpleAnnotationComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author lykan
 */
@Configuration
public class SimpleAnnotationDiagram {

    @Bean
    public ProcessLink simpleAnnotationProcess() {

        StartProcessLink build = StartProcessLink.build(SimpleAnnotationDiagram::simpleAnnotationProcess);
        ProcessLink service1 = build
                .nextService(SimpleAnnotationComponent::method0).build()
                .nextService(SimpleAnnotationComponent::method1).build()
                .nextService(SimpleAnnotationComponent::method2).build()
                .nextService(SimpleAnnotationComponent::method3).build()
                .nextService(SimpleAnnotationComponent::method4).build()
                .nextService(SimpleAnnotationComponent::method5).build()
                .nextService(SimpleAnnotationComponent::method6).build()
                .nextService(SimpleAnnotationComponent::method7).build()
                .nextService(SimpleAnnotationComponent::method8).build()
                .nextService(SimpleAnnotationComponent::method9).build()
                .nextService(SimpleAnnotationComponent::method10).build();

        build.inclusive().build().joinLinks(
                        service1.nextService("O10: true", SimpleAnnotationComponent::method0).build(),
                        service1.nextService("O9: true", SimpleAnnotationComponent::method1).build(),
                        service1.nextService("O8: true", SimpleAnnotationComponent::method2).build(),
                        service1.nextService("O7: true", SimpleAnnotationComponent::method3).build(),
                        service1.nextService("O6: @notBlank('')", SimpleAnnotationComponent::method4).build(),
                        service1.nextService("O5: true", SimpleAnnotationComponent::method5).build(),
                        service1.nextService("O0: true", SimpleAnnotationComponent::method6).build(),
                        service1.nextService("true", SimpleAnnotationComponent::method7).build(),
                        service1.nextService("O2: true", SimpleAnnotationComponent::method8).build(),
                        service1.nextService("O3: true", SimpleAnnotationComponent::method9).build(),
                        service1.nextService("O4: true", SimpleAnnotationComponent::method10).build()
                )
                .nextService(SimpleAnnotationComponent::methodR0).build()
                .nextService(SimpleAnnotationComponent::methodR1).build()
                .nextService(SimpleAnnotationComponent::methodR2).build()
                .nextService(SimpleAnnotationComponent::methodR3).build()
                .nextService(SimpleAnnotationComponent::methodR4).build()
                .nextService(SimpleAnnotationComponent::methodR5).build()
                .nextService(SimpleAnnotationComponent::methodR6).build()
                .nextService(SimpleAnnotationComponent::methodR7).build()
                .nextService(SimpleAnnotationComponent::methodR8).build()
                .nextService(SimpleAnnotationComponent::methodR9).build()
                .nextService(SimpleAnnotationComponent::methodR10).build()
                .nextService("true", SimpleAnnotationComponent::methodR0).build()
                .nextService("true", SimpleAnnotationComponent::methodR1).build()
                .nextService("true", SimpleAnnotationComponent::methodR2).build()
                .nextService("true", SimpleAnnotationComponent::methodR3).build()
                .nextService("true", SimpleAnnotationComponent::methodR4).build()
                .nextService("true", SimpleAnnotationComponent::methodR5).build()
                .nextService("true", SimpleAnnotationComponent::methodR6).build()
                .nextService("true", SimpleAnnotationComponent::methodR7).build()
                .nextService("true", SimpleAnnotationComponent::methodR8).build()
                .nextService("true", SimpleAnnotationComponent::methodR9).build()
                .nextService("true", SimpleAnnotationComponent::methodR10).build()
                .nextService(SimpleAnnotationComponent::methodR0).build()
                .end();

        return build;
    }
}
