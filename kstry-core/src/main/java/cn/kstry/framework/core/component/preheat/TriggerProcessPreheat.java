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
package cn.kstry.framework.core.component.preheat;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * 框架资源加载完成后，触发一次流程调用，对引擎进行预热
 */
public class TriggerProcessPreheat implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(TriggerProcessPreheat.class);

    private final StoryEngine storyEngine;

    public TriggerProcessPreheat(StoryEngine storyEngine) {
        this.storyEngine = storyEngine;
    }

    @Override
    public void afterPropertiesSet() {
        StoryRequest<Integer> request = ReqBuilder.returnType(Integer.class).trackingType(TrackingTypeEnum.NONE).request(1).startId(PreheatProcessDef.preheatProcessStartId).build();
        storyEngine.fireAsync(request).subscribe();
        TaskResponse<Integer> response = storyEngine.fire(request);
        if (response.getResultException() != null) {
            LOGGER.warn("[{}] StoryEngine preheat fail!", ExceptionEnum.STORY_ERROR.getExceptionCode(), response.getResultException());
        }
    }
}
