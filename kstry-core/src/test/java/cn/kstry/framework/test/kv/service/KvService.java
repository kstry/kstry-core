package cn.kstry.framework.test.kv.service;

import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.kv.KvAbility;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 *
 * @author lykan
 */
@SuppressWarnings("unused")
@TaskComponent(name = "kv-test")
public class KvService {

    @Autowired
    private KvAbility kvAbility;

    @TaskService(name = "kv-service-business")
    public void kvServiceBusiness() {
        eq(kvAbility.getValueByScope("new-per-scope", "name1").orElse(null), "name-1");
        eq(kvAbility.getValueByScope("new-per-scope", "name2").orElse(null), "name-2");
        eq(kvAbility.getValueByScope("new-per-scope", "name3").orElse(null), "name-3");
        eq(kvAbility.getValueByScope("new-per-scope", "name4").orElse(null), "name-4");
        eq(kvAbility.getValueByScope("new-per-scope", "name5").orElse(null), "name-5");
        eq(kvAbility.getValueByScope("new-per-scope", "name6").orElse(null), "name-6");
        eq(kvAbility.getValueByScope("new-per-scope", "name7").orElse(null), "name-7");
        eq(kvAbility.getValueByScope("new-per-scope", "name8").orElse(null), "name-8");
    }

    @TaskService(name = "kv-channel-scope", kvScope = "new-per-scope")
    public void kvChannelScope() {
        eq(kvAbility.getObject("name1", String.class).orElse(null), "name-1");
        eq(kvAbility.getObject("name2", String.class).orElse(null), "name-2");
        eq(kvAbility.getObject("name3", String.class).orElse(null), "name-3");
        eq(kvAbility.getObject("name4", String.class).orElse(null), "name-4");
        eq(kvAbility.getObject("name5", String.class).orElse(null), "name-5");
        eq(kvAbility.getObject("name6", String.class).orElse(null), "name-6");
        eq(kvAbility.getObject("name7", String.class).orElse(null), "name-7");
        eq(kvAbility.getObject("name8", String.class).orElse(null), "name-8");
        neq(kvAbility.getString("bust").orElse(null), "bust");
    }

    @TaskService(name = "kv-scope-env", kvScope = "new-per-scope")
    public void kvScopeEnv() {
        eq(kvAbility.getString("name5").orElse(null), "name-5");
        eq(kvAbility.getString("name6").orElse(null), "name-6");
        eq(kvAbility.getString("name7").orElse(null), "name-7");
        eq(kvAbility.getString("name8").orElse(null), "name-8");
    }

    @TaskService(name = "kv-default-env")
    public void kvDefaultEnv() {
        eq(kvAbility.getString("name7").orElse(null), "name-7");
        eq(kvAbility.getString("name8").orElse(null), "name-8");
        eq(kvAbility.getList("ids", Integer.class), Lists.newArrayList(1, 2, 3, 4));
    }

    @TaskService(name = "kv-empty-get")
    public void kvEmptyGet() {
        neq(kvAbility.getString("fir").orElse(null), "fir");
        neq(kvAbility.getValueByScope("new-per-scope", "ate").orElse(null), "ate");
    }

    @TaskService(name = "kv-dynamic-get", kvScope = "inx-scope")
    public void kvDynamicGet() {
        eq(kvAbility.getValueByScope("new-per-scope", "dynamic").orElse(null), "dynamic");
    }

    private void eq(Object left, Object right) {
        if (left instanceof List) {
            System.out.println(JSON.toJSONString(left));
            for (int i = 0; i < ((List<?>) left).size(); i++) {
                Assert.assertEquals(((List<?>) left).get(i), ((List<?>) right).get(i));
            }
            return;
        }

        System.out.println(left);
        Assert.assertEquals(left, right);
    }

    private void neq(Object left, Object right) {
        System.out.println(left);
        Assert.assertNotEquals(left, right);
    }
}
