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
package cn.kstry.framework.test.mono;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.role.BasicRole;
import cn.kstry.framework.core.util.PermissionUtil;
import cn.kstry.framework.test.mono.bo.SayInfoRequest;
import cn.kstry.framework.test.util.TestUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author lykan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MonoConfiguration.class)
public class MonoResultTest {

    @Autowired
    private StoryEngine storyEngine;

    /**
     * 【正常】测试：Mono结果正常返回
     */
    @Test
    public void testMonoResult001() throws InterruptedException {
        TestUtil.repeatTest(100, () -> {
            SayInfoRequest request = new SayInfoRequest();
            request.setA(345);
            request.setB(true);
            request.setC("Mono测试");
            StoryRequest<SayInfoRequest> fireRequest = ReqBuilder.returnType(SayInfoRequest.class)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).timeout(1000).startId("story-def-mono-result-001").build();
            Mono<SayInfoRequest> sayInfoRequestMono = storyEngine.fireAsync(fireRequest);
            sayInfoRequestMono.subscribe(r -> Assert.assertEquals(345, r.getA()));
        });
        TimeUnit.SECONDS.sleep(1);
    }

    /**
     * 【异常】测试：Mono结果超时
     */
    @Test
    public void testMonoResult002() throws InterruptedException {
        TestUtil.repeatTest(10, () -> {
            SayInfoRequest request = new SayInfoRequest();
            request.setA(345);
            request.setB(true);
            request.setC("Mono测试");
            StoryRequest<SayInfoRequest> fireRequest = ReqBuilder.returnType(SayInfoRequest.class)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).timeout(200).startId("story-def-mono-result-001").build();
            Mono<SayInfoRequest> sayInfoRequestMono = storyEngine.fireAsync(fireRequest);
            sayInfoRequestMono.subscribe(r -> {
                throw new RuntimeException();
            }, e -> Assert.assertEquals(ExceptionEnum.ASYNC_TASK_TIMEOUT.getExceptionCode(), ((KstryException) e).getErrorCode()));
        });
        TimeUnit.SECONDS.sleep(1);
    }

    /**
     * 【异常】测试：Mono执行失败
     */
    @Test
    public void testMonoResult003() throws InterruptedException {
        TestUtil.repeatTest(10, () -> {
            SayInfoRequest request = new SayInfoRequest();
            request.setA(345);
            request.setB(true);
            request.setC("Mono测试");
            BasicRole basicRole = new BasicRole();
            basicRole.addPermission(PermissionUtil.permissionList("pr:mono-service@say_info2"));
            StoryRequest<SayInfoRequest> fireRequest = ReqBuilder.returnType(SayInfoRequest.class)
                    .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).timeout(200).role(basicRole).startId("story-def-mono-result-003").build();
            Mono<SayInfoRequest> sayInfoRequestMono = storyEngine.fireAsync(fireRequest);
            sayInfoRequestMono.subscribe(r -> {
                throw new RuntimeException();
            }, e -> Assert.assertEquals(ExceptionEnum.SERVICE_INVOKE_ERROR.getExceptionCode(), ((KstryException) e).getErrorCode()));
        });
        TimeUnit.SECONDS.sleep(1);
    }
}
