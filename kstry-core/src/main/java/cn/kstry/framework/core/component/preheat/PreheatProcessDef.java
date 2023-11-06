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
package cn.kstry.framework.core.component.preheat;

import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.joinpoint.InclusiveJoinPoint;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.bpmn.link.StartProcessLink;

public class PreheatProcessDef {

    static final String preheatProcessStartId = "PREHEAT_PROCESS-0B68FAEBBB48412FAD586923E8EEDE31";

    private static final String preheatSubProcessStartId = "PREHEAT_SUB_PROCESS-B8D11CCAAFBC4FC6856F4F7C88D9B272";

    public static ProcessLink buildPreheatProcess() {
        StartProcessLink processLink = StartProcessLink.build(preheatProcessStartId);
        processLink
                .nextTask("y", StoryEnginePreheatService.class.getName(), "storyEnginePreheatReturnOne").build()
                .nextSubProcess(preheatSubProcessStartId).build()
                .end();
        return processLink;
    }

    public static SubProcessLink buildPreheatSubProcess() {
        return SubProcessLink.build(preheatSubProcessStartId, link -> {
            InclusiveJoinPoint inclusive = link.nextInclusive(link.inclusive().openAsync().build());
            link.parallel().build().joinLinks(
                    inclusive.nextTask(StoryEnginePreheatService.class.getName(), "storyEnginePreheatGetOneFromSta").build(),
                    inclusive.nextTask(StoryEnginePreheatService.class.getName(), "storyEnginePreheatGetOneFromVar").build()
            ).end();
        });
    }
}
