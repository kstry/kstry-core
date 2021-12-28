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
package cn.kstry.framework.test.role;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.role.BasicRole;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.PermissionUtil;
import cn.kstry.framework.test.flow.FlowCaseTestContextConfiguration;
import cn.kstry.framework.test.role.bo.SayInfoRequest;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author lykan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FlowCaseTestContextConfiguration.class)
public class RoleCaseTest {

    @Autowired
    private StoryEngine storyEngine;

    /**
     * 无角色
     */
    @Test
    public void testRole00101() {
        SayInfoRequest request = new SayInfoRequest();
        request.setNumber(8);
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class).request(request).startId("story-def-test-role-000").build();
        TaskResponse<Integer> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(fire.getResult(), request.getNumber());
    }

    /**
     * 匹配子能力
     */
    @Test
    public void testRole00102() {
        SayInfoRequest request = new SayInfoRequest();
        request.setNumber(8);

        Role role1 = new BasicRole();
        role1.addPermission(PermissionUtil.permissionList("r:say_number@say_number_increase"));

        Role role2 = new BasicRole();
        role2.addPermission(PermissionUtil.permissionList("r:say_number@say_number_square"));

        Role role3 = new BasicRole();
        role3.addPermission(PermissionUtil.permissionList("r:say_number@say_number_cube"));

        List<Role> roles = Lists.newArrayList(role1, role2, role3);
        List<Consumer<Integer>> consumers = Lists.newArrayList(
                x -> Assert.assertEquals((int) x, request.getNumber() + 1),
                x -> Assert.assertEquals((int) x, request.getNumber() * request.getNumber()),
                x -> Assert.assertEquals((int) x, request.getNumber() * request.getNumber() * request.getNumber())
        );
        for (int i = 0; i < roles.size(); i++) {
            StoryRequest<Integer> sReq = ReqBuilder.returnType(Integer.class).role(roles.get(i))
                    .trackingType(TrackingTypeEnum.SERVICE).request(request).startId("story-def-test-role-001").build();
            TaskResponse<Integer> fire = storyEngine.fire(sReq);
            System.out.println(JSON.toJSONString(fire));
            Assert.assertTrue(fire.isSuccess());
            consumers.get(i).accept(fire.getResult());
        }
    }

    /**
     * 匹配多个子能力异常
     */
    @Test
    public void testRole00103() {
        SayInfoRequest request = new SayInfoRequest();
        request.setNumber(8);
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class).request(request).startId("story-def-test-role-001").build();
        TaskResponse<Integer> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertFalse(fire.isSuccess());
        Assert.assertEquals(fire.getResultCode(), ExceptionEnum.EXECUTION_ONE_RESULT.getExceptionCode());
    }

    @Test
    public void testRole00104() {
        Role role = new BasicRole();
        SayInfoRequest request = new SayInfoRequest();
        request.setNumber(8);

        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class).role(role).request(request).startId("story-def-test-role-003").build();
        TaskResponse<Integer> fire = storyEngine.fire(fireRequest);
        Assert.assertTrue(fire.isSuccess());
        System.out.println(JSON.toJSONString(fire));
    }

    @Test
    public void testRole004() {
        SayInfoRequest request = new SayInfoRequest();
        request.setNumber(8);
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).businessId("aq-110").request(request).startId("story-def-test-role-004").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        Assert.assertTrue(fire.isSuccess());
        System.out.println(JSON.toJSONString(fire));
    }

    @Test
    public void testRole00401() {
        SayInfoRequest request = new SayInfoRequest();
        request.setNumber(8);
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).businessId("aq-110").request(request).startId("story-def-test-role-00401").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        Assert.assertTrue(fire.isSuccess());
        System.out.println(JSON.toJSONString(fire));
    }

    /**
     * 测试 TaskComponentPermission
     */
    @Test
    public void testRole002() {
        BasicRole parentRole = new BasicRole();

        parentRole.addPermission(PermissionUtil.permissionList("pr:say_info2@say_number", null));
        Role role = new BasicRole();
        role.addPermission(PermissionUtil.permissionList("pr:say_info@say_number", null));
        role.addParentRole(Sets.newHashSet(parentRole));

        SayInfoRequest request = new SayInfoRequest();
        request.setNumber(8);
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class).role(role).request(request).startId("story-def-test-role-002").build();
        TaskResponse<Integer> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals((int) fire.getResult(), (int) request.getNumber());
    }
}
