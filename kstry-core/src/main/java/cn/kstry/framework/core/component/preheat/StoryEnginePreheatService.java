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

import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.core.container.task.TaskComponentRegister;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.util.AssertUtil;

@SuppressWarnings("unused")
public class StoryEnginePreheatService implements TaskComponentRegister {

    private static final String ONE = "one";

    @TaskService(name = "storyEnginePreheatReturnOne")
    @NoticeScope(target = ONE, scope = {ScopeTypeEnum.VARIABLE, ScopeTypeEnum.STABLE, ScopeTypeEnum.RESULT})
    public int returnOne(@ReqTaskParam(reqSelf = true) int one) {
        return one;
    }

    @TaskService(name = "storyEnginePreheatGetOneFromSta")
    public void getOneFromSta(@StaTaskParam(ONE) int one) {
        AssertUtil.equals(one, 1);
    }

    @TaskService(name = "storyEnginePreheatGetOneFromVar")
    public void getOneFromVar(@VarTaskParam(ONE) int one) {
        AssertUtil.equals(one, 1);
    }

    @Override
    public String getName() {
        return StoryEnginePreheatService.class.getName();
    }
}
