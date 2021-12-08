package cn.kstry.framework.test.flow;

import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import com.alibaba.fastjson.JSON;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FlowCaseTestContextConfiguration.class)
public class FlowCase01Test {

    @Autowired
    private StoryEngine storyEngine;

    /**
     * 正常单节点
     */
    @Test
    public void testFlow001() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).trackingType(TrackingTypeEnum.SERVICE).startId("story-def-test-flow-001").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    /**
     * 多分支带条件测试
     */
    @Test
    public void testFlow02() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).startId("story-def-test-flow-002").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    /**
     * 多分支测试
     */
    @Test
    public void testFlow0201() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).trackingType(TrackingTypeEnum.SERVICE).startId("story-def-test-flow-00201").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    /**
     * 排他网关测试
     */
    @Test
    public void testFlow03() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).startId("story-def-test-flow-003").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    /**
     * 排他网关，无匹配分支测试
     */
    @Test
    public void testFlow0301() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).startId("story-def-test-flow-00301").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertNotNull(fire.getResultException());
        Assert.assertEquals(fire.getResultCode(), ExceptionEnum.STORY_FLOW_ERROR.getExceptionCode());
    }

    @Test
    public void testFlow04() {
        StoryRequest<Void> fireRequest =
                ReqBuilder.returnType(Void.class).trackingType(TrackingTypeEnum.SERVICE).timeout(4000).startId("story-def-test-flow-004").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    /**
     * allow-absent 属性测试
     * allow-absent=true 时，TaskService 未匹配到会直接跳过，默认为：true
     */
    @Test
    public void testFlow05() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).startId("story-def-test-flow-005").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertNotNull(fire.getResultException());
        Assert.assertEquals(fire.getResultCode(), ExceptionEnum.TASK_SERVICE_MATCH_ERROR.getExceptionCode());
    }

    /**
     * 并行网关测试
     */
    @Test
    public void testFlow06() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).startId("story-def-test-flow-006").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(fire.getResultCode(), GlobalProperties.STORY_SUCCESS_CODE);
    }

    /**
     * 并行网关入度有无法触及分支时，出现异常测试
     */
    @Test
    public void testFlow0601() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).startId("story-def-test-flow-00601").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertNotNull(fire.getResultException());
        Assert.assertEquals(fire.getResultCode(), ExceptionEnum.STORY_FLOW_ERROR.getExceptionCode());
    }

    /**
     * 并行网关入度有无法触及分支时，设置 strict-mode=false 关闭并行网关的严格模式。流程继续执行测试
     */
    @Test
    public void testFlow0602() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).startId("story-def-test-flow-00602").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    @Test
    public void testFlow07() {
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).startId("story-def-test-flow-007").build();
        TaskResponse<Void> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertFalse(fire.isSuccess());
        Assert.assertEquals(fire.getResultCode(), ExceptionEnum.PARAM_VERIFICATION_ERROR.getExceptionCode());
    }
}
