package cn.kstry.framework.test.demo;

import cn.kstry.framework.core.bus.InScopeData;
import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.test.demo.bo.RuleJudgeRequest;
import cn.kstry.framework.test.demo.config.ProcessConfig;
import cn.kstry.framework.test.demo.facade.CommonFields;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lykan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DemoConfiguration.class)
public class FlowDemoCase2Test {

    @Autowired
    private StoryEngine storyEngine;

    /**
     * type = 1 时执行加法
     */
    @Test
    public void testPlusCalculateByType1() {
        InScopeData varScopeData = new InScopeData(ScopeTypeEnum.VARIABLE);
        varScopeData.put(CommonFields.F.type, 1);
        varScopeData.put(CommonFields.F.a, 11);
        varScopeData.put(CommonFields.F.b, 6);

        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startId("start-event-sequence-flow-test").build();
        TaskResponse<Integer> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(17, (int) result.getResult());

        // 代码发流程定义
        fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startProcess(ProcessConfig::startEventSequenceFlowProcess).build();
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(17, (int) result.getResult());
    }

    /**
     * type = 2 时执行乘法
     */
    @Test
    public void testMultiplyCalculateByType2() {
        InScopeData varScopeData = new InScopeData(ScopeTypeEnum.VARIABLE);
        varScopeData.put(CommonFields.F.type, 2);
        varScopeData.put(CommonFields.F.a, 11);
        varScopeData.put(CommonFields.F.b, 6);

        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startId("start-event-sequence-flow-test").build();
        TaskResponse<Integer> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(66, (int) result.getResult());

        // 代码发流程定义
        fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startProcess(ProcessConfig::startEventSequenceFlowProcess2).build();
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(66, (int) result.getResult());
    }

    @Test
    public void testParallelGatewayDemo() {
        // var 域初始化数据
        InScopeData varScopeData = new InScopeData(ScopeTypeEnum.VARIABLE);
        varScopeData.put(CommonFields.F.studentId, 11);
        varScopeData.put(CommonFields.F.classId, 66);

        // 构造request
        StoryRequest<Map<String, Object>> fireRequest = ReqBuilder.<Map<String, Object>>resultType(Map.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startId("start-event-parallel-gateway-demo")
                .resultBuilder((Object r, ScopeDataQuery query) -> { // r: 流程执行完返回的结果（如果有）
                    HashMap<String, Object> objMap = Maps.newHashMap();
                    objMap.put(CommonFields.F.student, query.getVarData(CommonFields.F.student));
                    objMap.put(CommonFields.F.classInfo, query.getVarData(CommonFields.F.classInfo));
                    return objMap;
                }).build();

        // 执行
        TaskResponse<Map<String, Object>> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getResult().get(CommonFields.F.student));
        Assert.assertNotNull(result.getResult().get(CommonFields.F.classInfo));

        // 代码方式定义流程执行
        fireRequest = ReqBuilder.<Map<String, Object>>resultType(Map.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startProcess(ProcessConfig::testParallelGatewayDemoProcess)
                .resultBuilder((Object r, ScopeDataQuery query) -> { // r: 流程执行完返回的结果（如果有）
                    HashMap<String, Object> objMap = Maps.newHashMap();
                    objMap.put(CommonFields.F.student, query.getVarData(CommonFields.F.student));
                    objMap.put(CommonFields.F.classInfo, query.getVarData(CommonFields.F.classInfo));
                    return objMap;
                }).build();
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getResult().get(CommonFields.F.student));
        Assert.assertNotNull(result.getResult().get(CommonFields.F.classInfo));
    }

    @Test
    public void testExclusiveGatewayDemo() {
        // var 域初始化数据
        InScopeData varScopeData = new InScopeData(ScopeTypeEnum.VARIABLE);
        varScopeData.put(CommonFields.F.a, 11);
        varScopeData.put(CommonFields.F.b, 6);

        // 构造request
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startId("start-event-exclusive-gateway-demo")
                .build();

        // 执行 默认
        TaskResponse<Integer> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 5);

        // 执行 +
        varScopeData.put(CommonFields.F.factor, "+");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 17);

        // 执行 *
        varScopeData.put(CommonFields.F.factor, "*");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 66);

        // 代码方式定义流程
        fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startProcess(ProcessConfig::testExclusiveGatewayDemoProcess)
                .build();

        // 执行 默认
        varScopeData.remove(CommonFields.F.factor);
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 5);

        // 执行 +
        varScopeData.put(CommonFields.F.factor, "+");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 17);

        // 执行 *
        varScopeData.put(CommonFields.F.factor, "*");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 66);
    }

    @Test
    public void testInclusiveGatewayDemo() {
        // var 域初始化数据
        InScopeData varScopeData = new InScopeData(ScopeTypeEnum.VARIABLE);
        varScopeData.put(CommonFields.F.a, 11);
        varScopeData.put(CommonFields.F.b, 6);

        // 构造request
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startId("start-event-inclusive-gateway-demo")
                .build();

        // 执行 -
        varScopeData.put(CommonFields.F.factor, "-");
        TaskResponse<Integer> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 5);

        // 执行 +
        varScopeData.put(CommonFields.F.factor, "+");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 17);

        // 执行 *
        varScopeData.put(CommonFields.F.factor, "*");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 66);

        // 代码方式定义流程
        fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startProcess(ProcessConfig::testInclusiveGatewayDemoProcess)
                .build();

        // 执行 -
        varScopeData.put(CommonFields.F.factor, "-");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 5);

        // 执行 +
        varScopeData.put(CommonFields.F.factor, "+");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 17);

        // 执行 *
        varScopeData.put(CommonFields.F.factor, "*");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 66);
    }

    @Test
    public void testSubProcessDemo() {
        // 执行 *
        InScopeData varScopeData = new InScopeData(ScopeTypeEnum.VARIABLE);
        varScopeData.put(CommonFields.F.factor, "*");

        // 构造request
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startId("start-test-event-sub-process-demo")
                .build();

        TaskResponse<Integer> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 66);

        // 代码方式定义流程
        varScopeData.put(CommonFields.F.factor, "+");
        fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startProcess(ProcessConfig::testCalculateSubProcessDemo)
                .build();

        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 17);
    }

    @Test
    public void testCalculateErrorDemotion() {
        // 构造request
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).startProcess(ProcessConfig::testCalculateErrorDemotionProcess)
                .build();
        TaskResponse<Integer> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 10);
    }

    @Test
    public void testAsyncFlowDemo() {
        // 配置文件调用
        AtomicInteger atomicInteger = new AtomicInteger();
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(atomicInteger).startId("async-flow-demo")
                .build();
        TaskResponse<Void> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(8, atomicInteger.intValue());

        // 代码流程调用
        atomicInteger = new AtomicInteger();
        fireRequest = ReqBuilder.returnType(Void.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(atomicInteger).startProcess(ProcessConfig::testAsyncFlowProcess)
                .build();
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(8, atomicInteger.intValue());
    }

    @Test
    public void testRuleAndFlowDemo() {
        RuleJudgeRequest ruleJudgeRequest = new RuleJudgeRequest();
        ruleJudgeRequest.setA(10);
        ruleJudgeRequest.setB(5);
        ruleJudgeRequest.setC(15);
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(ruleJudgeRequest).startId("test-rule-and-flow-demo")
                .build();
        TaskResponse<Integer> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(15, (int) result.getResult());
    }

    @Test
    public void testRuleElseIfFlowDemo() {
        RuleJudgeRequest ruleJudgeRequest = new RuleJudgeRequest();
        ruleJudgeRequest.setA(-10);
        ruleJudgeRequest.setB(5);
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(ruleJudgeRequest).startId("test-rule-else-if-flow-demo")
                .build();
        TaskResponse<Integer> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(-15, (int) result.getResult());
    }

    @Test
    public void testRuleCompletedCountFlowDemo() {
        RuleJudgeRequest ruleJudgeRequest = new RuleJudgeRequest();
        ruleJudgeRequest.setA(15);
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(ruleJudgeRequest).startId("test-rule-completed-count-flow-demo")
                .build();
        TaskResponse<Integer> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(25, (int) result.getResult());
    }

    @Test
    public void testRbacFlowDemo() {
        InScopeData varScopeData = new InScopeData(ScopeTypeEnum.VARIABLE);
        varScopeData.put(CommonFields.F.goodsId, 10);

        StoryRequest<Long> fireRequest = ReqBuilder.returnType(Long.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startId("test-rbac-flow-demo")
                .build();
        TaskResponse<Long> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(100L, varScopeData.get(CommonFields.F.price));
        Assert.assertEquals(2987L, (long) result.getResult());

        fireRequest = ReqBuilder.returnType(Long.class)
                .businessId("external-business-id")
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startId("test-rbac-flow-demo")
                .build();
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(200L, varScopeData.get(CommonFields.F.price));
        Assert.assertEquals(2987L, (long) result.getResult());
    }


    @Test
    public void testInclusiveMidwayStart() {
        // var 域初始化数据
        InScopeData varScopeData = new InScopeData(ScopeTypeEnum.VARIABLE);
        varScopeData.put(CommonFields.F.a, 11);
        varScopeData.put(CommonFields.F.b, 6);

        // 构造request
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class).midwayStartId("9a350816bf823d30e5ca2865dd3a147f")
                .trackingType(TrackingTypeEnum.ALL).varScopeData(varScopeData).startId("start-event-inclusive-gateway-demo")
                .build();

        // 执行 -
        varScopeData.put(CommonFields.F.factor, "-");
        TaskResponse<Integer> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 5);

        // 执行 +
        varScopeData.put(CommonFields.F.factor, "+");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 17);

        // 执行 *
        varScopeData.put(CommonFields.F.factor, "*");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 66);

        // 代码方式定义流程
        fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startProcess(ProcessConfig::testInclusiveGatewayDemoProcess)
                .build();

        // 执行 -
        varScopeData.put(CommonFields.F.factor, "-");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 5);

        // 执行 +
        varScopeData.put(CommonFields.F.factor, "+");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 17);

        // 执行 *
        varScopeData.put(CommonFields.F.factor, "*");
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals((int) result.getResult(), 66);
    }

    @Test
    public void testInclusiveMidwayStart2() {
        RuleJudgeRequest ruleJudgeRequest = new RuleJudgeRequest();
        ruleJudgeRequest.setA(15);
        ruleJudgeRequest.setB(10);
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class).midwayStartId("26fb1355701a1170c0059b589aed5f14")
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(ruleJudgeRequest).startId("test-rule-completed-count-flow-demo")
                .build();
        TaskResponse<Integer> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(25, (int) result.getResult());
    }

    @Test
    public void testSimpleFlowDemo() {
        RuleJudgeRequest ruleJudgeRequest = new RuleJudgeRequest();
        ruleJudgeRequest.setA(5);

        // 流程配置执行
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(ruleJudgeRequest).startId("test-simple-flow-demo")
                .build();
        TaskResponse<Integer> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(15, (int) result.getResult());

        // 代码定义执行
        fireRequest = ReqBuilder.returnType(Integer.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(ruleJudgeRequest).startProcess(ProcessConfig::testSimpleFlowDemoProcess)
                .build();
        result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(15, (int) result.getResult());
    }
}
