package cn.kstry.framework.test.iterator;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.test.iterator.bo.DataSource;
import cn.kstry.framework.test.iterator.bo.SkuBo;
import cn.kstry.framework.test.iterator.config.DynamicIteratorProcess;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author lykan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IteratorConfiguration.class)
public class IteratorTaskCaseTest {

    @Autowired
    private StoryEngine storyEngine;

    /**
     * 测试子流程迭代
     */
    @Test
    public void test01() {
        List<SkuBo> list = Lists.newArrayList();
        StoryRequest<List<SkuBo>> fireRequest = ReqBuilder.returnType(list).startId("story-def-iterate-test_100").trackingType(TrackingTypeEnum.SERVICE_DETAIL).build();
        TaskResponse<List<SkuBo>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }

    /**
     * 测试同步情况的批处理迭代
     */
    @Test
    public void test02() {
        DataSource dataSource = new DataSource();
        ArrayList<Integer> integers = Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        dataSource.setNumList(integers.toArray(new Integer[0]));
        List<Integer> list = Lists.newArrayList();
        StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list).timeout(1000).request(dataSource).trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId(DynamicIteratorProcess.ITERATE_PROCESS_01).build();

        TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(11, fire.getResult().size());
        Assert.assertEquals(fire.getResult().stream().mapToInt(i -> i).sum(), 385);
    }

    /**
     * 测试异步情况的批处理迭代
     */
    @Test
    public void test03() {
        DataSource dataSource = new DataSource();
        ArrayList<Integer> integers = Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        dataSource.setNumList(integers.toArray(new Integer[0]));
        List<Integer> list = Lists.newArrayList();
        StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list).trackingType(TrackingTypeEnum.SERVICE_DETAIL).timeout(1000).request(dataSource).startId(DynamicIteratorProcess.ITERATE_PROCESS_02).build();

        TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(11, fire.getResult().size());
        Assert.assertEquals(fire.getResult().stream().mapToInt(i -> i).sum(), 385);
    }
}
