package cn.kstry.framework.test.iterator;

import cn.kstry.framework.core.bus.InScopeData;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.test.iterator.bo.DataSource;
import cn.kstry.framework.test.util.TestUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lykan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IteratorConfiguration.class)
public class IteratorSubProcessCaseTest {

    @Autowired
    private StoryEngine storyEngine;

    /**
     * 测试同步方法迭代，all_sucess策略（默认策略）
     */
    @Test
    public void test01() {
        DataSource dataSource = new DataSource();
        ArrayList<Integer> integers = Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        dataSource.setNumList(integers.toArray(new Integer[0]));
        List<Integer> list = Lists.newCopyOnWriteArrayList();
        InScopeData inScopeData = new InScopeData(ScopeTypeEnum.STABLE);
        inScopeData.put("squareResult", Maps.newHashMap());
        StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list)
                .timeout(3000).request(dataSource).staScopeData(inScopeData).startId("story-def-iterate-test_001").build();

        TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        for (int i = 0; i < integers.size(); i++) {
            Assert.assertEquals(integers.get(i) * integers.get(i), (int) fire.getResult().get(i));
        }
    }

    /**
     * 测试同步方法迭代，best_sucess策略
     */
    @Test
    public void test02() {
        DataSource dataSource = new DataSource();
        ArrayList<Integer> integers = Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        dataSource.setNumList(integers.toArray(new Integer[0]));
        List<Integer> list = Lists.newCopyOnWriteArrayList();
        StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list).timeout(3000).request(dataSource).startId("story-def-iterate-test_002").build();

        TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(fire.getResult().stream().mapToInt(i -> i).sum(), 369);
    }

    /**
     * 测试同步方法迭代，any_sucess策略
     */
    @Test
    public void test03() {
        DataSource dataSource = new DataSource();
        ArrayList<Integer> integers = Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        dataSource.setNumList(integers.toArray(new Integer[0]));
        List<Integer> list = Lists.newCopyOnWriteArrayList();
        StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list).timeout(1000).request(dataSource).startId("story-def-iterate-test_003").build();

        TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(fire.getResult().size(), 1);
        Assert.assertEquals((int) fire.getResult().get(0), 9);
    }

    /**
     * 测试异步方法迭代，all_sucess策略（默认策略）
     */
    @Test
    public void test04() {
        TestUtil.repeatTest(5, () -> {
            DataSource dataSource = new DataSource();
            ArrayList<Integer> integers = Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            dataSource.setNumList(integers.toArray(new Integer[0]));
            List<Integer> list = Lists.newCopyOnWriteArrayList();
            InScopeData inScopeData = new InScopeData(ScopeTypeEnum.STABLE);
            inScopeData.put("squareResult", Maps.newHashMap());
            StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list)
                    .timeout(2500).request(dataSource).staScopeData(inScopeData).startId("story-def-iterate-test_004").build();

            TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
            System.out.println(JSON.toJSONString(fire));
            Assert.assertTrue(fire.isSuccess());
            Assert.assertEquals(11, fire.getResult().size());
            Assert.assertEquals(fire.getResult().stream().mapToInt(i -> i).sum(), 385);
        });
    }

    /**
     * 测试异步方法迭代，best_sucess策略
     */
    @Test
    public void test05() {
        TestUtil.repeatTest(5, () -> {
            DataSource dataSource = new DataSource();
            ArrayList<Integer> integers = Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            dataSource.setNumList(integers.toArray(new Integer[0]));
            List<Integer> list = Lists.newCopyOnWriteArrayList();
            StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list).timeout(2500).request(dataSource).startId("story-def-iterate-test_005").build();

            TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
            System.out.println(JSON.toJSONString(fire));
            Assert.assertTrue(fire.isSuccess());
            Assert.assertEquals(10, fire.getResult().size());
            Assert.assertEquals(fire.getResult().stream().mapToInt(i -> i).sum(), 369);
        });
    }

    /**
     * 测试异步方法迭代，any_sucess策略
     */
    @Test
    public void test06() {
        DataSource dataSource = new DataSource();
        ArrayList<Integer> integers = Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        dataSource.setNumList(integers.toArray(new Integer[0]));
        List<Integer> list = Lists.newCopyOnWriteArrayList();
        StoryRequest<List<Integer>> fireRequest = ReqBuilder.returnType(list).timeout(1000).request(dataSource).startId("story-def-iterate-test_006").build();

        TaskResponse<List<Integer>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
        Assert.assertEquals(fire.getResult().size(), 1);
        Assert.assertEquals((int) fire.getResult().get(0), 9);
    }
}
