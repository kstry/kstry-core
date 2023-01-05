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
package cn.kstry.framework.test.bus.service;

import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.test.bus.bo.*;
import org.junit.Assert;

/**
 *
 * @author lykan
 */
@SuppressWarnings("unused")
@TaskComponent(name = "bus-test-service")
public class BusTestService {

    @NoticeScope
    @TaskService(name = "bus-step-1")
    public BusStep1Bo busStep1(long a, int b, short c, byte d, char e, double f, float g, boolean h, String i,
                               @ReqTaskParam(reqSelf = true) BusTestRequest request) {

        // 测试基础类型初始化
        Assert.assertEquals(a, 0L);
        Assert.assertEquals(b, 0);
        Assert.assertEquals(c, (short) 0);
        Assert.assertEquals(d, (byte) 0);
        Assert.assertEquals(e, (char) 0);
        Assert.assertEquals(f, 0.0, 0);
        Assert.assertEquals(g, 0.0f, 0);
        Assert.assertFalse(h);
        Assert.assertNull(i);

        Assert.assertNotNull(request);
        Assert.assertNotNull(request.getAr());

        BusStep1Bo.Br br = new BusStep1Bo.Br();
        br.setName(request.getAr().getName());

        BusStep1Bo busStep1Bo = new BusStep1Bo();
        busStep1Bo.setId(request.getId());
        busStep1Bo.setSize(1);
        busStep1Bo.setBr(br);
        return busStep1Bo;
    }

    @NoticeSta
    @NoticeVar
    @TaskService(name = "bus-step-2")
    public BusStep2Bo busStep2(BusStep2Request busStep2Request,
                               @ReqTaskParam BusTestRequest.Ar ar,
                               @StaTaskParam("busStep1Bo.br.name") String staName,
                               @VarTaskParam("step1.br.name") String varName,
                               @TaskParam(scopeEnum = ScopeTypeEnum.STABLE, value = "busStep1Bo.br.name") String scopeName) {
        Assert.assertNotNull(ar.getName());
        Assert.assertEquals(ar.getName(), staName);
        Assert.assertEquals(ar.getName(), varName);
        Assert.assertEquals(ar.getName(), scopeName);
        Assert.assertEquals(ar.getName(), busStep2Request.getBr().getName());

        BusTestResult busTestResult = new BusTestResult();
        busTestResult.setId(busStep2Request.getVarId());
        BusStep2Bo busStep2Bo = new BusStep2Bo();
        busStep2Bo.setBusTestResult(busTestResult);
        return busStep2Bo;
    }

    public int incr(int i) {
        return i + 1;
    }
}
