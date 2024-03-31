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
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.ZoneId;
import java.util.Date;
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

    @Qualifier("custom-mmm")
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Test
    public void test01() {
        BusTestRequest.Ar ar = new BusTestRequest.Ar();
        ar.setName("ar");
        BusTestRequest busTestRequest = new BusTestRequest();
        busTestRequest.setId(234);
        busTestRequest.setAr(ar);
        busTestRequest.setNow(new Date());
        busTestRequest.setLocalNow(busTestRequest.getNow().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

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

        Date now = new Date();
        InScopeData varScopeData = new InScopeData(ScopeTypeEnum.VARIABLE);
        varScopeData.put("step1", new BusStep1Bo());
        varScopeData.put("a1", 11);
        varScopeData.put("nowStr", now);
        varScopeData.put("localNowStr", now.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        varScopeData.put("objBool", "yes");
        varScopeData.put("oneItemList", "OneItem");
        varScopeData.put("oneItemSet", "OneItem");
        varScopeData.put("firstItemList", Lists.newArrayList("firstItem"));
        varScopeData.put("zzInt", Lists.newArrayList());

        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(busTestRequest).varScopeData(varScopeData).startId("testBusDataTaskParams").build();
        TaskResponse<Void> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void test03() {
        InScopeData varScopeData = new InScopeData(ScopeTypeEnum.VARIABLE);
        varScopeData.put("id", 23);

        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startId("testCustomParams").build();
        TaskResponse<Void> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void test04() {
        InScopeData varScopeData = new InScopeData(ScopeTypeEnum.VARIABLE);
        varScopeData.put("dateStr", "2022-02-03 11:00:00");
        varScopeData.put("arName", "AR名称");
        varScopeData.put("requests", Lists.newArrayList(
                BusTestRequest.builder().id(17).desc("描述信息17").build(),
                BusTestRequest.builder().id(18).desc("描述信息18").build()
        ));

        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).varScopeData(varScopeData).startId("testConverterMappingProcess").build();
        TaskResponse<Void> result = storyEngine.fire(fireRequest);
        Assert.assertTrue(result.isSuccess());
        System.out.println(JSON.toJSONString(result));
    }
}
