package cn.kstry.framework.test.bus;

import cn.kstry.framework.core.bus.InScopeData;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.test.bus.bo.BusStep1Bo;
import cn.kstry.framework.test.bus.bo.BusTestRequest;
import cn.kstry.framework.test.bus.bo.BusTestResult;
import com.alibaba.fastjson.JSON;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author lykan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BusConfiguration.class)
public class BusCaseTest {

    @Autowired
    private StoryEngine storyEngine;

    @Resource(name = "custom-mmm")
    private ThreadPoolExecutor threadPoolExecutor;

    @Test
    public void test01() {
        BusTestRequest.Ar ar = new BusTestRequest.Ar();
        ar.setName("ar");
        BusTestRequest busTestRequest = new BusTestRequest();
        busTestRequest.setId(234);
        busTestRequest.setAr(ar);

        InScopeData varScopeData = new InScopeData(ScopeTypeEnum.VARIABLE);
        varScopeData.put("step1", new BusStep1Bo());
        StoryRequest<BusTestResult> fireRequest = ReqBuilder.returnType(BusTestResult.class).storyExecutor(threadPoolExecutor)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(busTestRequest).varScopeData(varScopeData).startId("story-def-bus-test-01").build();
        TaskResponse<BusTestResult> result = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(result));
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(235, result.getResult().getId());
    }

    @Test
    public void test02() {
        BusTestRequest.Ar ar = new BusTestRequest.Ar();
        ar.setName("ar");
        BusTestRequest busTestRequest = new BusTestRequest();
        busTestRequest.setId(234);
        busTestRequest.setAr(ar);

        InScopeData varScopeData = new InScopeData(ScopeTypeEnum.VARIABLE);
        varScopeData.put("step1", new BusStep1Bo());

        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(busTestRequest).varScopeData(varScopeData).startId("testBusDataTaskParams").build();
        TaskResponse<Void> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        System.out.println(JSON.toJSONString(result));
    }
}
