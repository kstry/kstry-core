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
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.test.bus.bo.*;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;

import java.util.List;
import java.util.Map;

/**
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
                               @StaTaskParam({"$", "busStep1Bo", "br", "name"}) String staName,
                               @VarTaskParam({"$", "step1", "br", "name"}) String varName,
                               @TaskParam(scopeEnum = ScopeTypeEnum.STABLE, value = {"busStep1Bo", "br", "name"}) String scopeName) {
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

    @TaskService(name = "bus-step-3")
    public void busStep3(@TaskParam(scopeEnum = ScopeTypeEnum.RESULT) BusTestResult busTestResult, BusStep2Bo busStep2Bo) {
        AssertUtil.notNull(busTestResult);
        AssertUtil.notNull(busStep2Bo.getBusTestResult());
        AssertUtil.isTrue(busTestResult == busStep2Bo.getBusTestResult());
    }

    @TaskService(invoke = @Invoke(executor = "custom-mmm"))
    public void testBusDataTaskParams(int a, long b, String c, double d, @TaskParam("reqId") int id, BusStep1Bo bo, @VarTaskParam("a1") int e, ConvTBo convTBo) {
        Assert.assertEquals(0, a);
        Assert.assertEquals(0L, b);
        Assert.assertEquals(111, e);
        Assert.assertNull(c);
        Assert.assertEquals(3.24D, d, 0);
        Assert.assertEquals("张三", bo.getBr().getName());
        Assert.assertEquals(id, bo.getId());
        Assert.assertNotNull(convTBo.getNowStr());
        Assert.assertEquals(convTBo.getNowStr(), convTBo.getLocalNowStr());
        Assert.assertTrue(convTBo.isObjBool());
        Assert.assertEquals(1, CollectionUtils.size(convTBo.getOneItemList()));
        Assert.assertEquals(1, CollectionUtils.size(convTBo.getOneItemSet()));
        Assert.assertEquals("firstItem", convTBo.getFirstItemList());
        Assert.assertEquals(0, convTBo.getZzInt());
    }

    @TaskService(invoke = @Invoke(executor = "custom-mmm"))
    public void testCustomParams(BusTestRequest busTestRequest) {
        System.out.println(JSON.toJSONString(busTestRequest));
    }

    @TaskService
    @SuppressWarnings("all")
    public void testConverterMapping(Map<String, Object> params, String reqDescList, List<BusTestRequest> requests) {
        Assert.assertNotNull(params.get("busTest"));
        Assert.assertTrue(params.get("busTest") instanceof BusTestRequest);
        Assert.assertNotNull(((BusTestRequest) params.get("busTest")).getLocalNow());
        Assert.assertEquals(((BusTestRequest) params.get("busTest")).getAr().getName(), "AR名称");
        Assert.assertEquals(3, ((List<?>) params.get("requests")).size());
        Assert.assertNotNull(((Map<?, ?>) ((List<?>) params.get("requests")).get(1)).get("now"));
        Assert.assertEquals(54, ((List<?>) params.get("requests")).stream().mapToInt(m -> (Integer) ((Map<?, ?>) m).get("id")).sum());
        Assert.assertNotNull(reqDescList);
        Assert.assertTrue(requests != null && requests.get(0) != null && (requests.get(0) instanceof BusTestRequest));
        Assert.assertEquals(2, ((List<?>) params.get("reqDescList2")).size());
        Assert.assertEquals(4, ((List<?>) ((List<?>) params.get("reqDescList2")).get(0)).size());
        Assert.assertEquals(2, ((List<?>) ((List<?>) params.get("reqDescList2")).get(1)).size());
        Assert.assertEquals(1, ((List<?>) ((List<?>) params.get("reqDescList2")).get(0)).get(1));
        Assert.assertTrue(((List<?>) ((List<?>) ((List<?>) params.get("requests2")).get(0)).get(0)).get(0) instanceof Map);
        System.out.println(JSON.toJSONString(params));
    }

    public int incr(int i) {
        return i + 1;
    }
}
