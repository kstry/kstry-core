package cn.kstry.framework.test.flow;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.test.flow.bo.CycleRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;
import java.util.stream.IntStream;

/**
 * @author lykan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FlowCaseTestContextConfiguration.class)
public class FlowCase04Test {

    @Autowired
    private StoryEngine storyEngine;

    /**
     * 测试服务节点循环
     */
    @Test
    public void testFlow001() {
        CycleRequest request = new CycleRequest();
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                .request(request).trackingType(TrackingTypeEnum.SERVICE).startId("cycle-flow-test-001").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(20, request.getCount());
    }

    /**
     * 测试包含网关节点循环(不带指令)
     */
    @Test
    public void testFlow002() {
        IntStream.range(0, 5000).parallel().forEach(i -> {
            CycleRequest request = new CycleRequest();
            StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                    .request(request).trackingType(TrackingTypeEnum.SERVICE).startId("cycle-flow-test-002").build();
            TaskResponse<Void> fire = storyEngine.fire(fireRequest);
            Assert.assertTrue(fire.isSuccess());
            Assert.assertEquals(22, request.getCount());
        });
    }

    /**
     * 测试包含网关节点循环(带指令)
     */
    @Test
    public void testFlow003() {
        CycleRequest request = new CycleRequest();
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                .request(request).trackingType(TrackingTypeEnum.SERVICE).startId("cycle-flow-test-003").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(20, request.getCount());
    }

    /**
     * 测试包含网关中间启动(带指令)
     */
    @Test
    public void testFlow0031() {
        CycleRequest request = new CycleRequest();
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).midwayStartId("cycle-flow-test-0031")
                .request(request).trackingType(TrackingTypeEnum.SERVICE).startId("cycle-flow-test-003").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(20, request.getCount());
    }

    /**
     * 测试子流程节点循环
     */
    @Test
    public void testFlow004() {
        CycleRequest request = new CycleRequest();
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                .request(request).trackingType(TrackingTypeEnum.SERVICE).startId("cycle-flow-test-004").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(20, request.getCount());

        Optional<String> serialize = storyEngine.serialize(fireRequest);
        serialize.ifPresent(System.out::println);
    }

    /**
     * 测试并行网关节点循环
     */
    @Test
    public void testFlow005() {
        CycleRequest request = new CycleRequest();
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                .request(request).trackingType(TrackingTypeEnum.SERVICE).startId("cycle-flow-test-005").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(20, request.getCount());
    }

    /**
     * 测试排他网关循环
     */
    @Test
    public void testFlow006() {
        IntStream.range(0, 5000).parallel().forEach(i -> {
            CycleRequest request = new CycleRequest();
            StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                    .request(request).trackingType(TrackingTypeEnum.SERVICE).startId("cycle-flow-test-006").build();
            TaskResponse<Void> fire = storyEngine.fire(fireRequest);
            Assert.assertTrue(fire.isSuccess());
            int count = request.getCount();
            Assert.assertTrue(count >= 10 && count <= 19);
        });
    }

    /**
     * 测试子流程中的双重循环（并发）
     */
    @Test
    public void testFlow007() {
        IntStream.range(0, 5000).parallel().forEach(i -> {
            CycleRequest request = new CycleRequest();
            StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                    .request(request).trackingType(TrackingTypeEnum.SERVICE).startId("cycle-flow-test-007").build();
            TaskResponse<Void> fire = storyEngine.fire(fireRequest);
            Assert.assertTrue(fire.isSuccess());
            Assert.assertTrue(request.getCount() >= 20 && request.getCount() <= 22);
        });
    }

    /**
     * 测试循环的嵌套
     */
    @Test
    public void testFlow008() {
        IntStream.range(0, 5000).parallel().forEach(i -> {
            CycleRequest request = new CycleRequest();
            StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                    .request(request).trackingType(TrackingTypeEnum.SERVICE).startId("cycle-flow-test-008").build();
            TaskResponse<Void> fire = storyEngine.fire(fireRequest);
            Assert.assertTrue(fire.isSuccess());
            Assert.assertEquals(20, request.getCount());
        });
    }

    /**
     * 测试循环串联
     */
    @Test
    public void testFlow009() {
        IntStream.range(0, 5000).parallel().forEach(i -> {
            CycleRequest request = new CycleRequest();
            StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                    .request(request).trackingType(TrackingTypeEnum.SERVICE).startId("cycle-flow-test-009").build();
            TaskResponse<Void> fire = storyEngine.fire(fireRequest);
            Assert.assertTrue(fire.isSuccess());
            Assert.assertEquals(21, request.getCount());
        });
    }
}
