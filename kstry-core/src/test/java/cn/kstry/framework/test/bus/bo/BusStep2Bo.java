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

import cn.kstry.framework.core.annotation.NoticeResult;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.engine.ParamLifecycle;
import lombok.Data;

import java.util.Optional;

/**
 *
 * @author lykan
 */
@Data
public class BusStep2Bo implements ParamLifecycle {

    @NoticeResult
    private BusTestResult busTestResult;

    @Override
    public void after(ScopeDataOperator scopeDataOperator) {
        Optional<BusTestResult> result = scopeDataOperator.getResult();
        this.busTestResult = result.orElse(null);
    }
}
