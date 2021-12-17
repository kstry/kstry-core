package cn.kstry.framework.test.flow;

import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.test.flow.bo.Goods;
import cn.kstry.framework.test.flow.bo.Te4Request;

/**
 *
 * @author lykan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FlowCaseTestContextConfiguration.class)
public class FlowCase03Test {


    @Autowired
    private StoryEngine storyEngine;

    @Test
    public void testFlow001() {
        Te4Request request = new Te4Request();
        request.setActivityId(24L);
        request.setGoodsId(23L);
        request.setHospitalId(22L);

        StoryRequest<Goods> fireRequest = ReqBuilder.returnType(Goods.class).startId("story-def-test_001").request(request)
                .recallStoryHook(storyBus -> {
                    AssertUtil.notNull(storyBus);
                    AssertUtil.notNull(storyBus.getResult());
                })
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).build();
        TaskResponse<Goods> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    @Test
    public void testMonoFlow001() throws InterruptedException {
        Te4Request request = new Te4Request();
        request.setActivityId(24L);
        request.setGoodsId(23L);
        request.setHospitalId(22L);

        CountDownLatch clo = new CountDownLatch(1);
        StoryRequest<Goods> fireRequest = ReqBuilder.returnType(Goods.class).trackingType(TrackingTypeEnum.SERVICE_DETAIL).timeout(2000).startId("story-def-test_001")
                .monoTimeoutFallback(() -> {
                    Goods goods = new Goods();
                    goods.setName("错误");
                    return goods;
                }).recallStoryHook(storyBus -> {
                    System.out.println(JSON.toJSONString(storyBus.getResult()));
                    System.out.println(storyBus.getValue(ScopeTypeEnum.REQUEST, "activityId").orElse(null));
                    System.out.println(storyBus.getValue(ScopeTypeEnum.STABLE, "name").orElse(null));
                    System.out.println(JSON.toJSONString(storyBus.getMonitorTracking()));
                })
                .request(request).build();
        storyEngine.fireAsync(fireRequest).subscribe(fire -> System.out.println(JSON.toJSONString(fire)), exp -> {
            exp.printStackTrace();
            clo.countDown();
        }, () -> {
            System.out.println("complete consumer ~");
            clo.countDown();
        });
        clo.await();
    }


    @Test
    public void testFlow00101() {
        Te4Request request = new Te4Request();
        request.setActivityId(24L);
        request.setGoodsId(23L);
        request.setHospitalId(22L);

        StoryRequest<Goods> fireRequest = ReqBuilder.returnType(Goods.class).startId("story-def-test_00101").request(request).trackingType(TrackingTypeEnum.SERVICE_DETAIL).build();
        TaskResponse<Goods> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    @Test
    public void testFlow00102() {
        Te4Request request = new Te4Request();
        request.setActivityId(24L);
        request.setGoodsId(23L);
        request.setHospitalId(22L);

        StoryRequest<Goods> fireRequest = ReqBuilder.returnType(Goods.class).startId("story-def-test_00102").request(request).trackingType(TrackingTypeEnum.SERVICE_DETAIL).build();
        TaskResponse<Goods> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    @Test
    public void testFlow00103() {
        Te4Request request = new Te4Request();
        request.setActivityId(24L);
        request.setGoodsId(23L);
        request.setHospitalId(22L);

        StoryRequest<Goods> fireRequest = ReqBuilder.returnType(Goods.class).startId("story-def-test_00103").request(request).trackingType(TrackingTypeEnum.ALL).build();
        TaskResponse<Goods> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    @Test
    public void testFlow00104() {
        Te4Request request = new Te4Request();
        request.setActivityId(24L);
        request.setGoodsId(23L);
        request.setHospitalId(22L);

        StoryRequest<Goods> fireRequest = ReqBuilder.returnType(Goods.class).startId("story-def-test_00104").request(request).trackingType(TrackingTypeEnum.ALL).build();
        TaskResponse<Goods> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    @Test
    public void testFlow00105() {
        Te4Request request = new Te4Request();
        request.setActivityId(24L);
        request.setGoodsId(23L);
        request.setHospitalId(22L);

        StoryRequest<Goods> fireRequest = ReqBuilder.returnType(Goods.class).startId("story-def-test_00105").request(request).trackingType(TrackingTypeEnum.ALL).build();
        TaskResponse<Goods> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }
}
