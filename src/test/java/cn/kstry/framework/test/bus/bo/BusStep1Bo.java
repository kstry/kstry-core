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
package cn.kstry.framework.test.bus.bo;

import cn.kstry.framework.core.annotation.NoticeScope;
import cn.kstry.framework.core.annotation.NoticeSta;
import cn.kstry.framework.core.annotation.NoticeVar;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 *
 * @author lykan
 */
@Data
@FieldNameConstants
public class BusStep1Bo {

    @NoticeScope(target = "oneId")
    private int id;

    @NoticeVar
    @NoticeSta
    private int size;

    @NoticeVar(target = "step1.br")
    private Br br;

    @Data
    public static class Br {

        private String name;
    }
}
