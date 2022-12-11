package cn.kstry.framework.test.diagram;

import cn.kstry.framework.core.bus.InScopeData;
import cn.kstry.framework.core.component.instruct.JsScriptProperty;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.test.diagram.bo.CalculateServiceRequest;
import cn.kstry.framework.test.diagram.constants.StoryNameConstants;
import com.alibaba.fastjson.JSON;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DiagramCaseTestContextConfiguration.class)
public class JsScriptInstructTest {


    @Autowired
    private StoryEngine storyEngine;

    @Test
    public void testJsScript001() {
        InScopeData sta = new InScopeData(ScopeTypeEnum.STABLE);
        sta.put("num", 5);

        InScopeData var = new InScopeData(ScopeTypeEnum.VARIABLE);
        var.put("num", 7);

        CalculateServiceRequest methodInvokeBo = new CalculateServiceRequest();
        methodInvokeBo.setA(13);
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class).request(methodInvokeBo)
                .staScopeData(sta).varScopeData(var).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId(StoryNameConstants.JS001).build();
        TaskResponse<Integer> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertEquals(fire.getResult(), new Integer(50));
        Assert.assertEquals(sta.get("r"), 50);
        Assert.assertEquals(var.get("r"), 50);
        Assert.assertEquals(sta.get("v"), 25.0);
    }

    @Test
    public void testJsScript002() {
        InScopeData sta = new InScopeData(ScopeTypeEnum.STABLE);
        sta.put("num", 5);

        InScopeData var = new InScopeData(ScopeTypeEnum.VARIABLE);
        var.put("num", 7);

        CalculateServiceRequest methodInvokeBo = new CalculateServiceRequest();
        methodInvokeBo.setA(13);
        StoryRequest<Integer> fireRequest = ReqBuilder.returnType(Integer.class).request(methodInvokeBo)
                .staScopeData(sta).varScopeData(var).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId(StoryNameConstants.JS002).build();
        TaskResponse<Integer> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertEquals(fire.getResult(), new Integer(50));
        Assert.assertEquals(sta.get("r"), 50);
        Assert.assertEquals(var.get("r"), 50);
        Assert.assertEquals(sta.get("v"), 25.0);
    }

    @Test
    public void testJsScript003() {
        StoryRequest<JsScriptProperty> fireRequest = ReqBuilder.returnType(JsScriptProperty.class).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId(StoryNameConstants.JS003).build();
        TaskResponse<JsScriptProperty> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertEquals(fire.getResult().getInvokeMethod(), "invoke");
        Assert.assertEquals(fire.getResult().getReturnType(), "java.lang.Integer");
    }
}
