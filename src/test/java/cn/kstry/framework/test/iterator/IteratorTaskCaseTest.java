package cn.kstry.framework.test.iterator;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.test.iterator.bo.SkuBo;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
        StoryRequest<List<SkuBo>> fireRequest = ReqBuilder.returnType(list).startId("story-def-iterate-test_100").build();
        TaskResponse<List<SkuBo>> fire = storyEngine.fire(fireRequest);
        System.out.println(JSON.toJSONString(fire));
        Assert.assertTrue(fire.isSuccess());
    }
}
