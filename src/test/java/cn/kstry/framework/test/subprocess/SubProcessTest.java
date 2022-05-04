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
package cn.kstry.framework.test.subprocess;

import cn.kstry.framework.core.bus.InScopeData;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.test.subprocess.bo.SubProcessBo;
import cn.kstry.framework.test.util.TestUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author lykan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SubProcessConfiguration.class)
public class SubProcessTest {

    @Autowired
    private StoryEngine storyEngine;

    /**
     * 【正常】测试：同步情况子流程嵌套测试
     */
    @Test
    public void testSubprocess001() {

        TestUtil.repeatTest(50000, () -> {
            SubProcessBo request = new SubProcessBo();
            request.setA(1);
            request.setB(100);
            request.setC(true);
            request.setD("subProcessTest");
            request.setE(3.4d);
            request.setF(new AtomicInteger());

            InScopeData tbo = new InScopeData(ScopeTypeEnum.STABLE);
            tbo.put("tbo", request);
            StoryRequest<SubProcessBo> fireRequest = ReqBuilder.returnType(SubProcessBo.class).staScopeData(tbo)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).startId("story-def-subprocess-test-001").build();
            TaskResponse<SubProcessBo> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(1, result.getResult().getA());
            if (!Objects.equals(40, result.getResult().getF().get())) {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Assert.assertEquals(40, result.getResult().getF().get());
        });
    }


    /**
     * 【正常】测试：异步情况子流程嵌套测试
     */
    @Test
    public void testSubprocess002() {
        TestUtil.repeatTest(50000, () -> {
            SubProcessBo request = new SubProcessBo();
            request.setA(1);
            request.setB(100);
            request.setC(true);
            request.setD("subProcessTest");
            request.setE(3.4d);
            request.setF(new AtomicInteger());

            InScopeData tbo = new InScopeData(ScopeTypeEnum.STABLE);
            tbo.put("tbo", request);
            StoryRequest<SubProcessBo> fireRequest = ReqBuilder.returnType(SubProcessBo.class).staScopeData(tbo)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).startId("story-def-subprocess-test-002").build();
            TaskResponse<SubProcessBo> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(1, result.getResult().getA());
            if (!Objects.equals(40, result.getResult().getF().get())) {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Assert.assertEquals(40, result.getResult().getF().get());
        });
    }


    /**
     * 【正常】测试：子流程发生错误时将子流程设置成非严格模式继续其他的流程完成最终结果
     */
    @Test
    public void testSubprocess003() {
        TestUtil.repeatTest(50000, () -> {
            SubProcessBo request = new SubProcessBo();
            request.setA(1);
            request.setB(100);
            request.setC(true);
            request.setD("subProcessTest");
            request.setE(3.4d);
            request.setF(new AtomicInteger());

            InScopeData tbo = new InScopeData(ScopeTypeEnum.STABLE);
            tbo.put("tbo", request);
            StoryRequest<SubProcessBo> fireRequest = ReqBuilder.returnType(SubProcessBo.class).staScopeData(tbo)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).startId("story-def-subprocess-test-003").build();
            TaskResponse<SubProcessBo> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(1, result.getResult().getA());
            int i = result.getResult().getF().get();
            Assert.assertTrue(i > 2000 && i < 2050);
        });
    }


    /**
     * 【正常】测试：子流程发生超时将子流程设置成非严格模式继续其他的流程完成最终结果
     */
    @Test
    public void testSubprocess004() {
        TestUtil.repeatTest(10000, () -> {
            SubProcessBo request = new SubProcessBo();
            request.setA(1);
            request.setB(100);
            request.setC(true);
            request.setD("subProcessTest");
            request.setE(3.4d);
            request.setF(new AtomicInteger());

            InScopeData tbo = new InScopeData(ScopeTypeEnum.STABLE);
            tbo.put("tbo", request);
            StoryRequest<SubProcessBo> fireRequest = ReqBuilder.returnType(SubProcessBo.class).staScopeData(tbo)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).startId("story-def-subprocess-test-004").build();
            TaskResponse<SubProcessBo> result = storyEngine.fire(fireRequest);
            Assert.assertTrue(result.isSuccess());
            Assert.assertNotNull(result.getResult());
            Assert.assertEquals(1, result.getResult().getA());
            int i = result.getResult().getF().get();
            Assert.assertTrue(i > 2000 && i < 2050);
        });
    }
}
