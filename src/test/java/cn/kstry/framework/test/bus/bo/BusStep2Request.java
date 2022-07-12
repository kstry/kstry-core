/*
 *
 *  * Copyright (c) 2020-2022, Lykan (jiashuomeng@gmail.com).
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

import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.core.engine.ParamLifecycle;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.test.bus.service.BusTestService;
import lombok.Data;
import org.junit.Assert;

import javax.annotation.Resource;

/**
 *
 * @author lykan
 */
@Data
@SpringInitialization
public class BusStep2Request implements ParamLifecycle {

    @VarTaskField("busStep1Bo." + BusStep1Bo.Fields.id)
    private int varId;

    @StaTaskField("busStep1Bo." + BusStep1Bo.Fields.id)
    private int staId;

    @ReqTaskField("id")
    private int reqId;

    @StaTaskField("oneId")
    private int oneId;

    @StaTaskField("size")
    private int size;

    @TaskField(scopeEnum = ScopeTypeEnum.STABLE, value = "busStep1Bo.br.name")
    private String name;

    private Br br;

    @Resource
    private BusTestService busTestService;

    @Override
    public void before() {
        ParamLifecycle.super.before();
        br = new Br();
    }

    @Override
    public void after() {
        Assert.assertTrue(reqId > 0);
        Assert.assertEquals(reqId, varId);
        Assert.assertEquals(reqId, staId);
        Assert.assertEquals(reqId, oneId);
        Assert.assertEquals(1, size);
        Assert.assertNotNull(name);
        br.setName(name);
        varId = busTestService.incr(varId);
    }

    @Data
    public static class Br {
        private String name;
    }
}
