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
package cn.kstry.framework.test.role.service;

import cn.kstry.framework.core.annotation.NoticeScope;
import cn.kstry.framework.core.annotation.ReqTaskParam;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.enums.ScopeTypeEnum;

/**
 *
 * @author lykan
 */
@SuppressWarnings("unused")
@TaskComponent(name = "say_info2")
public class SayInfo {

    @NoticeScope(scope = ScopeTypeEnum.RESULT)
    @TaskService(name = "say_number")
    public Integer sayNumber(@ReqTaskParam("number") int number) {
        System.out.println("say number2: " + number);
        return number;
    }
}
