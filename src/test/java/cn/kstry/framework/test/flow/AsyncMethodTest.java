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
package cn.kstry.framework.test.flow;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.test.flow.bo.MethodInvokeBo;
import cn.kstry.framework.test.util.TestUtil;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author lykan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FlowCaseTestContextConfiguration.class)
public class AsyncMethodTest {

    @Autowired
    private StoryEngine storyEngine;

    /**
     * 【正常】测试：同步链路中，服务节点设置超时时间调用
     */
    @Test
    public void testAsyncMethod001() {
        TestUtil.repeatTest(1000, () -> {
            MethodInvokeBo request = new MethodInvokeBo();
            request.setA(345);
            request.setB(true);
            request.setC("AsyncMethod测试");
            StoryRequest<MethodInvokeBo> fireRequest = ReqBuilder.returnType(MethodInvokeBo.class)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).startId("story-def-async-method-001").build();
            TaskResponse<MethodInvokeBo> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(346, result.getResult().getA());
        });
    }

    /**
     * 【正常】测试：异步链路中，服务节点设置超时时间调用
     */
    @Test
    public void testAsyncMethod002() {
        TestUtil.repeatTest(1000, () -> {
            MethodInvokeBo request = new MethodInvokeBo();
            request.setA(345);
            request.setB(true);
            request.setC("AsyncMethod测试");
            StoryRequest<MethodInvokeBo> fireRequest = ReqBuilder.returnType(MethodInvokeBo.class)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).startId("story-def-async-method-002").build();
            TaskResponse<MethodInvokeBo> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(346, result.getResult().getA());
        });
    }

    /**
     * 【正常】测试：异步链路中，方法执行出现异常，通过重试解决
     */
    @Test
    public void testAsyncMethod003() {
        TestUtil.repeatTest(1000, () -> {
            MethodInvokeBo request = new MethodInvokeBo();
            request.setA(345);
            request.setB(true);
            request.setC("AsyncMethod测试");
            StoryRequest<MethodInvokeBo> fireRequest = ReqBuilder.returnType(MethodInvokeBo.class)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).startId("story-def-async-method-003").build();
            TaskResponse<MethodInvokeBo> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(346, result.getResult().getA());
        });
    }

    /**
     * 【正常】测试：异步链路中，方法执行出现异常，重试失败，降级返回结果
     */
    @Test
    public void testAsyncMethod004() {
        TestUtil.repeatTest(1000, () -> {
            MethodInvokeBo request = new MethodInvokeBo();
            request.setA(345);
            request.setB(true);
            request.setC("AsyncMethod测试");
            StoryRequest<MethodInvokeBo> fireRequest = ReqBuilder.returnType(MethodInvokeBo.class)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).startId("story-def-async-method-004").build();
            TaskResponse<MethodInvokeBo> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertTrue(Lists.newArrayList(321, 322).contains(result.getResult().getA()));
            Assert.assertEquals(1, result.getResult().getD());
        });
    }

    /**
     * 【正常】测试：异步链路中，方法执行出现异常，重试失败，降级失败后设置非严格模式忽略异常
     */
    @Test
    public void testAsyncMethod005() {
        TestUtil.repeatTest(1000, () -> {
            MethodInvokeBo request = new MethodInvokeBo();
            request.setA(345);
            request.setB(true);
            request.setC("AsyncMethod测试");
            StoryRequest<MethodInvokeBo> fireRequest = ReqBuilder.returnType(MethodInvokeBo.class)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).startId("story-def-async-method-005").build();
            TaskResponse<MethodInvokeBo> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(321, result.getResult().getA());
            Assert.assertEquals(5, result.getResult().getD());
        });
    }

    /**
     * 【正常】测试：异步链路中，目标方法执行超时后，通过降级返回结果
     */
    @Test
    public void testAsyncMethod006() {
        TestUtil.repeatTest(1000, () -> {
            MethodInvokeBo request = new MethodInvokeBo();
            request.setA(345);
            request.setB(true);
            request.setC("AsyncMethod测试");
            StoryRequest<MethodInvokeBo> fireRequest = ReqBuilder.returnType(MethodInvokeBo.class)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).startId("story-def-async-method-006").build();
            TaskResponse<MethodInvokeBo> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(321, result.getResult().getA());
            Assert.assertEquals(5, result.getResult().getD());
        });
    }
}
