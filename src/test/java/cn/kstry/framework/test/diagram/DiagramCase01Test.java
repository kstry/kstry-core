package cn.kstry.framework.test.diagram;

import cn.kstry.framework.core.bus.InScopeData;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.test.diagram.bo.CalculateServiceRequest;
import cn.kstry.framework.test.diagram.constants.StoryNameConstants;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.stream.IntStream;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DiagramCaseTestContextConfiguration.class)
public class DiagramCase01Test {

    @Autowired
    private StoryEngine storyEngine;

    @Test
    public void testDiagram001() {
        CalculateServiceRequest methodInvokeBo = new CalculateServiceRequest();
        methodInvokeBo.setA(0);
        methodInvokeBo.setD(-1);
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).request(methodInvokeBo).trackingType(TrackingTypeEnum.SERVICE).startId(StoryNameConstants.A001).build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(30, methodInvokeBo.getA());
    }

    @Test
    public void testDiagram002() {
        CalculateServiceRequest methodInvokeBo = new CalculateServiceRequest();
        methodInvokeBo.setA(0);
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).request(methodInvokeBo).trackingType(TrackingTypeEnum.SERVICE).startId(StoryNameConstants.A002).build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(4, methodInvokeBo.getA());

        CalculateServiceRequest methodInvokeBo2 = new CalculateServiceRequest();
        methodInvokeBo2.setA(20);
        StoryRequest<Void> fireRequest2 = ReqBuilder.returnType(Void.class).request(methodInvokeBo2).trackingType(TrackingTypeEnum.SERVICE).startId(StoryNameConstants.A002).build();
        TaskResponse<Void> fire2 = storyEngine.fire(fireRequest2);
        System.out.println(JSON.toJSONString(fire2));
        Assert.assertTrue(fire2.isSuccess());
        Assert.assertEquals(25, methodInvokeBo2.getA());
    }

    @Test
    public void testDiagram003() {
        CalculateServiceRequest methodInvokeBo = new CalculateServiceRequest();
        methodInvokeBo.setA(0);
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).request(methodInvokeBo).trackingType(TrackingTypeEnum.SERVICE).startId(StoryNameConstants.A003).build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(5, methodInvokeBo.getA());

        CalculateServiceRequest methodInvokeBo2 = new CalculateServiceRequest();
        methodInvokeBo2.setA(9);
        StoryRequest<Void> fireRequest2 = ReqBuilder.returnType(Void.class).request(methodInvokeBo2).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId(StoryNameConstants.A003).build();
        TaskResponse<Void> fire2 = storyEngine.fire(fireRequest2);
        System.out.println(JSON.toJSONString(fire2));
        Assert.assertTrue(fire2.isSuccess());
        Assert.assertEquals(16, methodInvokeBo2.getA());
    }

    @Test
    public void testDiagram004() {
        InScopeData sta = new InScopeData(ScopeTypeEnum.STABLE);
        sta.put("arr", IntStream.range(0, 100).toArray());

        List<Integer> list = Lists.newArrayList();
        StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list).staScopeData(sta).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId(StoryNameConstants.A004).build();
        TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(5050, fire.getResult().stream().mapToInt(i -> i).sum());
    }

    @Test
    public void testDiagram005() {
        InScopeData sta = new InScopeData(ScopeTypeEnum.STABLE);
        sta.put("arr", IntStream.range(0, 100).toArray());

        List<Integer> list = Lists.newArrayList();
        StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list).staScopeData(sta).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId(StoryNameConstants.A005).build();
        TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    @Test
    public void testDiagram006() {
        InScopeData sta = new InScopeData(ScopeTypeEnum.STABLE);
        sta.put("arr", IntStream.range(0, 100).toArray());

        List<Integer> list = Lists.newArrayList();
        StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list).staScopeData(sta).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId(StoryNameConstants.A006).build();
        TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(5050, fire.getResult().stream().mapToInt(i -> i).sum());
    }

    @Test
    public void testDiagram007() {
        CalculateServiceRequest methodInvokeBo = new CalculateServiceRequest();
        methodInvokeBo.setA(0);
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class).request(methodInvokeBo).trackingType(TrackingTypeEnum.SERVICE).startId(StoryNameConstants.A007).build();
        TaskResponse<Integer> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        AssertUtil.equals(-1, fire.getResult());
    }

    @Test
    public void testDynamicDiagram001() {
        CalculateServiceRequest methodInvokeBo = new CalculateServiceRequest();
        methodInvokeBo.setA(0);
        methodInvokeBo.setD(-1);
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).request(methodInvokeBo).trackingType(TrackingTypeEnum.SERVICE).startId(StoryNameConstants.D001).build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(30, methodInvokeBo.getA());
    }


    @Test
    public void testDynamicDiagram002() {
        CalculateServiceRequest methodInvokeBo = new CalculateServiceRequest();
        methodInvokeBo.setA(0);
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).request(methodInvokeBo).trackingType(TrackingTypeEnum.SERVICE).startId(StoryNameConstants.D002).build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(4, methodInvokeBo.getA());

        CalculateServiceRequest methodInvokeBo2 = new CalculateServiceRequest();
        methodInvokeBo2.setA(20);
        StoryRequest<Void> fireRequest2 = ReqBuilder.returnType(Void.class).request(methodInvokeBo2).trackingType(TrackingTypeEnum.SERVICE).startId(StoryNameConstants.D002).build();
        TaskResponse<Void> fire2 = storyEngine.fire(fireRequest2);
        System.out.println(JSON.toJSONString(fire2));
        Assert.assertTrue(fire2.isSuccess());
        Assert.assertEquals(25, methodInvokeBo2.getA());
    }

    @Test
    public void testDynamicDiagram003() {
        CalculateServiceRequest methodInvokeBo = new CalculateServiceRequest();
        methodInvokeBo.setA(0);
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).request(methodInvokeBo).trackingType(TrackingTypeEnum.SERVICE).startId(StoryNameConstants.D003).build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(5, methodInvokeBo.getA());

        CalculateServiceRequest methodInvokeBo2 = new CalculateServiceRequest();
        methodInvokeBo2.setA(9);
        StoryRequest<Void> fireRequest2 = ReqBuilder.returnType(Void.class).request(methodInvokeBo2).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId(StoryNameConstants.D003).build();
        TaskResponse<Void> fire2 = storyEngine.fire(fireRequest2);
        System.out.println(JSON.toJSONString(fire2));
        Assert.assertTrue(fire2.isSuccess());
        Assert.assertEquals(16, methodInvokeBo2.getA());
    }

    @Test
    public void testDynamicDiagram004() {
        InScopeData sta = new InScopeData(ScopeTypeEnum.STABLE);
        sta.put("arr", IntStream.range(0, 100).toArray());

        List<Integer> list = Lists.newArrayList();
        StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list).staScopeData(sta).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId(StoryNameConstants.D004).build();
        TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(5050, fire.getResult().stream().mapToInt(i -> i).sum());
    }

    @Test
    public void testDynamicDiagram005() {
        InScopeData sta = new InScopeData(ScopeTypeEnum.STABLE);
        sta.put("arr", IntStream.range(0, 100).toArray());

        List<Integer> list = Lists.newArrayList();
        StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list).staScopeData(sta).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId(StoryNameConstants.D005).build();
        TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    @Test
    public void testDynamicDiagram006() {
        InScopeData sta = new InScopeData(ScopeTypeEnum.STABLE);
        sta.put("arr", IntStream.range(0, 100).toArray());

        List<Integer> list = Lists.newArrayList();
        StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list).staScopeData(sta).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId(StoryNameConstants.D006).build();
        TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(5050, fire.getResult().stream().mapToInt(i -> i).sum());
    }

    @Test
    public void testDynamicDiagram007() {
        CalculateServiceRequest methodInvokeBo = new CalculateServiceRequest();
        methodInvokeBo.setA(0);
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class).request(methodInvokeBo).trackingType(TrackingTypeEnum.SERVICE).startId(StoryNameConstants.D007).build();
        TaskResponse<Integer> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        AssertUtil.equals(-1, fire.getResult());
    }

    /**
     * 测试拦截器
     * cn.kstry.framework.test.diagram.config.TaskIte
     */
    @Test
    public void testDynamicDiagram008() {
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class).trackingType(TrackingTypeEnum.SERVICE).businessId("ite").startId(StoryNameConstants.D008).build();
        TaskResponse<Integer> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(Integer.valueOf(22), fire.getResult());
    }
}
