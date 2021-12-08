package cn.kstry.framework.test.common.service;

import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.kv.KvAbility;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 *
 * @author lykan
 */
@SuppressWarnings("unused")
@TaskComponent(name = "kv-test")
public class KvService {

    @Autowired
    private KvAbility kvAbility;

    @TaskService(name = "kv-service")
    public void kvService() {
        System.out.println("kv service ~");
        System.out.println("kv newScope name: " + kvAbility.getValue("name").orElse(null));
        System.out.println("kv default name: " + kvAbility.getValueByScope("default", "name").orElse(null));
        System.out.println("kv year: " + kvAbility.getString("year").orElse(null));
        System.out.println("kv user: " + kvAbility.getObject("user", Map.class).map(t -> JSON.toJSONString(t, SerializerFeature.WriteMapNullValue)).orElse(null));
        System.out.println("kv user.name: " + kvAbility.getString("user.name").orElse(null));
        System.out.println("kv ids: " + kvAbility.getObject("ids", List.class).map(JSON::toJSONString).orElse(null));
    }
}
