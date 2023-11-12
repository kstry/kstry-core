package cn.kstry.framework.test.demo;

import cn.kstry.framework.core.component.jsprocess.transfer.JsonSerializeProcessParser;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.test.demo.config.ProcessConfig;
import cn.kstry.framework.test.demo.facade.QueryScoreRequest;
import cn.kstry.framework.test.demo.facade.QueryScoreResponse;
import cn.kstry.framework.test.demo.facade.QueryScoreVarScope;
import cn.kstry.framework.test.demo.service.StudentScoreService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * @author lykan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DemoConfiguration.class)
public class FlowDemoCaseTest {

    @Autowired
    private StoryEngine storyEngine;

    /**
     * BPMN配置文件可视化图
     */
    @Test
    public void testQueryStudentScore() {
        // 查询学生信息和成绩
        QueryScoreRequest request = new QueryScoreRequest();
        request.setStudentId(66L);
        request.setNeedScore(true);

        StudentScoreService.kstryThreadLocal.set(1);
        QueryScoreVarScope varScopeData = new QueryScoreVarScope();
        StoryRequest<QueryScoreResponse> fireRequest = ReqBuilder.returnType(QueryScoreResponse.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).varScopeData(varScopeData).startId("student-score-query-process").build();
        TaskResponse<QueryScoreResponse> result = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect));
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getResult().getStudent());
        Assert.assertTrue(CollectionUtils.isNotEmpty(result.getResult().getScoreInfos()));

        // 查询学生信息
        request = new QueryScoreRequest();
        request.setStudentId(77L);

        varScopeData = new QueryScoreVarScope();
        fireRequest = ReqBuilder.returnType(QueryScoreResponse.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).varScopeData(varScopeData).startId("student-score-query-process").build();
        result = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect));
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getResult().getStudent());
    }

    /**
     * JSON配置流程图
     */
    @Test
    public void testJsonQueryStudentScore() {
        // 查询学生信息和成绩
        QueryScoreRequest request = new QueryScoreRequest();
        request.setStudentId(66L);
        request.setNeedScore(true);
        StudentScoreService.kstryThreadLocal.set(1);
        QueryScoreVarScope varScopeData = new QueryScoreVarScope();
        StoryRequest<QueryScoreResponse> fireRequest = ReqBuilder.returnType(QueryScoreResponse.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).varScopeData(varScopeData).startId("student-score-query-json-process").build();
        TaskResponse<QueryScoreResponse> result = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect));
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getResult().getStudent());
        Assert.assertTrue(CollectionUtils.isNotEmpty(result.getResult().getScoreInfos()));

        // 查询学生信息
        request = new QueryScoreRequest();
        request.setStudentId(77L);

        varScopeData = new QueryScoreVarScope();
        fireRequest = ReqBuilder.returnType(QueryScoreResponse.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).varScopeData(varScopeData).startId("student-score-query-json-process").build();
        result = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect));
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getResult().getStudent());
    }

    /**
     * 代码定义的Process
     */
    @Test
    public void testQueryStudentScoreProcess() {
        // 查询学生信息和成绩
        QueryScoreRequest request = new QueryScoreRequest();
        request.setStudentId(66L);
        request.setNeedScore(true);
        StudentScoreService.kstryThreadLocal.set(1);
        QueryScoreVarScope varScopeData = new QueryScoreVarScope();
        StoryRequest<QueryScoreResponse> fireRequest = ReqBuilder.returnType(QueryScoreResponse.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).varScopeData(varScopeData).startProcess(ProcessConfig::studentScoreQueryProcess).build();
        TaskResponse<QueryScoreResponse> result = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect));
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getResult().getStudent());
        Assert.assertTrue(CollectionUtils.isNotEmpty(result.getResult().getScoreInfos()));

        // 查询学生信息
        request = new QueryScoreRequest();
        request.setStudentId(77L);

        varScopeData = new QueryScoreVarScope();
        fireRequest = ReqBuilder.returnType(QueryScoreResponse.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).varScopeData(varScopeData).startProcess(ProcessConfig::studentScoreQueryProcess).build();
        result = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect));
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getResult().getStudent());
    }

    @Test
    public void testGetJsonProcess() throws Exception {
        InputStream inputStream = FlowDemoCaseTest.class.getClassLoader().getResourceAsStream("bpmn/demo/student-score-query-process.bpmn");
        assert inputStream != null;
        String bpmnXml = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
        Optional<String> jsonOptional = new JsonSerializeProcessParser().bpmnSerialize(bpmnXml);
        System.out.println(jsonOptional.orElse(null));
    }
}
