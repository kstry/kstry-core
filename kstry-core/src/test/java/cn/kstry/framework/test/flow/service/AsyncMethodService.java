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
package cn.kstry.framework.test.flow.service;

import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.test.flow.bo.MethodInvokeBo;
import org.junit.Assert;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author lykan
 */
@SuppressWarnings("unused")
@TaskComponent(name = "async-method-service")
public class AsyncMethodService {

    @TaskService(name = "say_empty_info")
    public void sayEmptyInfo(@ReqTaskParam(reqSelf = true) MethodInvokeBo request, @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Assert.assertEquals(345, a);
        Assert.assertEquals(true, b);
        Assert.assertEquals("AsyncMethod测试", c);
        request.setA(request.getA() + 1);
    }

    @NoticeResult
    @TaskService(name = "say_info")
    public MethodInvokeBo sayInfo(@ReqTaskParam(reqSelf = true) MethodInvokeBo request,
                                  @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Assert.assertTrue(a - 345 >= 0);
        Assert.assertEquals(true, b);
        Assert.assertEquals("AsyncMethod测试", c);
        return request;
    }

    @TaskService(name = "say_empty_info2", invoke = @Invoke(retry = 1))
    public void sayEmptyInfo2(@ReqTaskParam(reqSelf = true) MethodInvokeBo request, @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Assert.assertEquals(true, b);
        Assert.assertEquals("AsyncMethod测试", c);
        if (request.getD() == 0) {
            request.setD(1);
            throw new RuntimeException("测试异步调用中的重试");
        }
        request.setA(request.getA() + 1);
    }

    @NoticeResult
    @TaskService(name = "say_info2", invoke = @Invoke(retry = 2, demotion = "pr:async-method-service@say_info2_demotion"))
    public MethodInvokeBo sayInfo2(@ReqTaskParam(reqSelf = true) MethodInvokeBo request,
                                   @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Assert.assertTrue(a - 345 >= 0);
        Assert.assertEquals(true, b);
        Assert.assertEquals("AsyncMethod测试", c);
        if (request.getD() < 10) {
            throw new RuntimeException("测试异步调用中的降级操作");
        }
        return request;
    }

    @TaskService(name = "say_info2_demotion", invoke = @Invoke(timeout = 300))
    public MethodInvokeBo sayInfo2Demotion(@ReqTaskParam(reqSelf = true) MethodInvokeBo request,
                                           @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        request.setA(321);
        return request;
    }

    @TaskService(name = "say_empty_info3", invoke = @Invoke(retry = 2, demotion = "pr:async-method-service@say_empty_info3_demotion", strictMode = false))
    public void sayEmptyInfo3(@ReqTaskParam(reqSelf = true) MethodInvokeBo request, @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        if (request.getD() < 10) {
            throw new RuntimeException("测试节点异步中的非严格模式");
        }
    }

    @TaskService(name = "say_empty_info3_demotion")
    public void sayEmptyInfo3Demotion(@ReqTaskParam(reqSelf = true) MethodInvokeBo request,
                                      @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        request.setD(5);
        System.out.println("sayEmptyInfo3Demotion...");
        throw new RuntimeException("测试节点异步中的非严格模式...");
    }

    @NoticeResult
    @TaskService(name = "say_info3", invoke = @Invoke(demotion = "pr:async-method-service@say_info2_demotion"))
    public MethodInvokeBo sayInfo3(@ReqTaskParam(reqSelf = true) MethodInvokeBo request,
                                   @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) throws InterruptedException {
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Assert.assertTrue(a - 345 >= 0);
        Assert.assertEquals(true, b);
        Assert.assertEquals("AsyncMethod测试", c);
        TimeUnit.SECONDS.sleep(1);
        return request;
    }
}
