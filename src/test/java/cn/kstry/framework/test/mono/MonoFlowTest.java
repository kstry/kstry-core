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
package cn.kstry.framework.test.mono;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.core.role.ServiceTaskRole;
import cn.kstry.framework.core.util.PermissionUtil;
import cn.kstry.framework.test.mono.bo.SayInfoRequest;
import cn.kstry.framework.test.util.TestUtil;
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
@ContextConfiguration(classes = MonoConfiguration.class)
public class MonoFlowTest {

    @Autowired
    private StoryEngine storyEngine;

    /**
     * 【正常】测试：Mono返回参数、Mono不返回参数
     */
    @Test
    public void testMono001() {
        TestUtil.repeatTest(100, () -> {
            SayInfoRequest request = new SayInfoRequest();
            request.setA(345);
            request.setB(true);
            request.setC("Mono测试");
            StoryRequest<SayInfoRequest> fireRequest = ReqBuilder.returnType(SayInfoRequest.class)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).startId("story-def-mono-success-001").build();
            TaskResponse<SayInfoRequest> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(346, result.getResult().getA());
        });
    }

    /**
     * 【正常】测试：异步状态下的，Mono返回参数、Mono不返回参数
     */
    @Test
    public void testMono002() {
        TestUtil.repeatTest(100, () -> {
            SayInfoRequest request = new SayInfoRequest();
            request.setA(345);
            request.setB(true);
            request.setC("Mono测试");
            StoryRequest<SayInfoRequest> fireRequest = ReqBuilder.returnType(SayInfoRequest.class)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).startId("story-def-mono-success-002").build();
            TaskResponse<SayInfoRequest> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(346, result.getResult().getA());
        });
    }

    /**
     * 【正常】测试：方法内出现异常后，经重试方法正常返回
     */
    @Test
    public void testMono003() {
        TestUtil.repeatTest(100, () -> {
            SayInfoRequest request = new SayInfoRequest();
            request.setA(345);
            request.setB(true);
            request.setC("Mono测试");
            request.setD(0);
            StoryRequest<SayInfoRequest> fireRequest = ReqBuilder
                    .returnType(SayInfoRequest.class).request(request).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId("story-def-mono-success-003").build();
            TaskResponse<SayInfoRequest> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(345, result.getResult().getA());
        });
    }

    /**
     * 【正常】测试：方法内出现异常后，经重试失败后，经降级正常返回
     */
    @Test
    public void testMono004() {
        TestUtil.repeatTest(100, () -> {
            SayInfoRequest request = new SayInfoRequest();
            request.setA(345);
            request.setB(true);
            request.setC("Mono测试");
            request.setD(0);

            ServiceTaskRole serviceTaskRole = new ServiceTaskRole();
            serviceTaskRole.addPermission(PermissionUtil.permissionList("pr:mono-service@say_empty_info3_demotion@ability"));
            StoryRequest<SayInfoRequest> fireRequest = ReqBuilder.returnType(SayInfoRequest.class)
                    .role(serviceTaskRole).request(request).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId("story-def-mono-success-004").build();
            TaskResponse<SayInfoRequest> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(345, result.getResult().getA());
        });
    }

    /**
     * 【正常】测试：方法内出现异常后，经重试失败后，降级策略策行失败后，设置非严格模式正常返回
     */
    @Test
    public void testMono005() {
        TestUtil.repeatTest(100, () -> {
            SayInfoRequest request = new SayInfoRequest();
            request.setA(345);
            request.setB(true);
            request.setC("Mono测试");
            request.setD(0);
            StoryRequest<SayInfoRequest> fireRequest = ReqBuilder.returnType(SayInfoRequest.class)
                    .request(request).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId("story-def-mono-success-005").build();
            TaskResponse<SayInfoRequest> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(345, result.getResult().getA());
        });
    }

    /**
     * 【正常】测试：Mono方法体执行失败后的降级，包括有反参、无反参
     */
    @Test
    public void testMono006() {
        TestUtil.repeatTest(100, () -> {
            SayInfoRequest request = new SayInfoRequest();
            request.setA(345);
            request.setB(true);
            request.setC("Mono测试");
            ServiceTaskRole serviceTaskRole = new ServiceTaskRole();
            serviceTaskRole.addPermission(PermissionUtil.permissionList("pr:mono-service@say_empty_info3_demotion@ability"));
            StoryRequest<SayInfoRequest> fireRequest = ReqBuilder.returnType(SayInfoRequest.class)
                    .role(serviceTaskRole).request(request).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId("story-def-mono-success-006").build();
            TaskResponse<SayInfoRequest> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(345, result.getResult().getA());
            Assert.assertEquals(100, result.getResult().getD());
        });
    }

    /**
     * 【正常】测试：Mono方法体执行超时后的降级，包括有反参、无反参
     */
    @Test
    public void testMono007() {
        TestUtil.repeatTest(100, () -> {
            SayInfoRequest request = new SayInfoRequest();
            request.setA(345);
            request.setB(true);
            request.setC("Mono测试");
            ServiceTaskRole serviceTaskRole = new ServiceTaskRole();
            serviceTaskRole.addPermission(PermissionUtil.permissionList("pr:mono-service@say_empty_info3_demotion@ability"));
            StoryRequest<SayInfoRequest> fireRequest = ReqBuilder.returnType(SayInfoRequest.class)
                    .role(serviceTaskRole).request(request).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId("story-def-mono-success-007").build();
            TaskResponse<SayInfoRequest> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(346, result.getResult().getA());
            Assert.assertEquals(100, result.getResult().getD());
        });
    }

    /**
     * 【正常】测试：非严格模式下，Mono方法体执行超时后结果被忽略
     */
    @Test
    public void testMono008() {
        TestUtil.repeatTest(100, () -> {
            SayInfoRequest request = new SayInfoRequest();
            request.setA(345);
            request.setB(true);
            request.setC("Mono测试");
            StoryRequest<SayInfoRequest> fireRequest = ReqBuilder.returnType(SayInfoRequest.class)
                    .request(request).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId("story-def-mono-success-008").build();
            TaskResponse<SayInfoRequest> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(346, result.getResult().getA());
            Assert.assertEquals(100, result.getResult().getD());
        });
    }
}
