package cn.kstry.framework.test.kv;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.test.kv.service.KvDynamicProcess;
import com.alibaba.fastjson.JSON;
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
@ContextConfiguration(classes = KvConfiguration.class)
public class KvCaseTest {

    @Autowired
    private StoryEngine storyEngine;

    @Test
    public void test01() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).businessId("business-channel").startId("story-def-kv-test_001").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    @Test
    public void test02() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).businessId("business-channel").startId(KvDynamicProcess.KV_PROCESS_01).build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    @Test
    public void test03() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).startId(KvDynamicProcess.KV_PROCESS_02).build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    @Test
    public void test04() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).startId(KvDynamicProcess.KV_PROCESS_03).build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    @Test
    public void test05() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).startId(KvDynamicProcess.KV_PROCESS_04).build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    @Test
    public void test06() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).businessId("business-channel").startId(KvDynamicProcess.KV_PROCESS_05).build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }
}
