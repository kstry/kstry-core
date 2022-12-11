package cn.kstry.framework.test.flow;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.test.flow.bo.Goods;
import cn.kstry.framework.test.flow.bo.Te4Request;

/**
 * @author lykan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FlowCaseTestContextConfiguration.class)
public class FlowCase02Test {

    @Autowired
    private StoryEngine storyEngine;

    /**
     * 【正常】测试：复杂链路场景测试，1次
     */
    @Test
    public void testComplexFlow1() {
        Te4Request request = new Te4Request();
        request.setActivityId(24L);
        request.setGoodsId(23L);
        request.setHospitalId(22L);
        request.increase();

        StoryRequest<Goods> fireRequest = ReqBuilder.returnType(Goods.class).timeout(3000).startId("story-def-complex-flow-001").request(request).build();
        fireRequest.setTrackingType(TrackingTypeEnum.NODE);
        TaskResponse<Goods> fire = storyEngine.fire(fireRequest);
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(26, request.getCount());
    }

    /**
     * 【正常】测试：复杂链路场景测试，20000次
     */
    @Test
    public void testComplexFlow2() {
        IntStream.range(0, 30000).parallel().forEach(i -> {
            Te4Request request = new Te4Request();
            request.setActivityId(24L);
            request.setGoodsId(23L);
            request.setHospitalId(22L);
            request.increase();

            StoryRequest<Goods> fireRequest = ReqBuilder.returnType(Goods.class).timeout(3000).startId("story-def-complex-flow-001").request(request).build();
            if (i == 0) {
                fireRequest.setTrackingType(TrackingTypeEnum.NODE);
            }
            TaskResponse<Goods> fire = storyEngine.fire(fireRequest);
            Assert.assertTrue(fire.isSuccess());
            Assert.assertEquals(26, request.getCount());
        });
    }
}
