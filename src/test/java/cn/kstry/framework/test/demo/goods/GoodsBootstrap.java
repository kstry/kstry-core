package cn.kstry.framework.test.demo.goods;

import cn.kstry.framework.core.annotation.EnableKstry;
import cn.kstry.framework.core.bus.BusDataBox;
import cn.kstry.framework.core.bus.DefaultDataBox;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.test.demo.goods.entity.User;
import cn.kstry.framework.test.demo.goods.facade.BuyGoodsRequest;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 *
 * @author lykan
 */
@Configuration
@ComponentScan(basePackageClasses = GoodsBootstrap.class)
@EnableKstry(configPath = "goods-config.json")
public class GoodsBootstrap {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(GoodsBootstrap.class);
        StoryEngine storyEngine = context.getBean("storyEngine", StoryEngine.class);

        BuyGoodsRequest buyGoodsRequest = new BuyGoodsRequest();
        buyGoodsRequest.setUserId(2L);
        buyGoodsRequest.setUserType(1);
        buyGoodsRequest.setGoodIds(Lists.newArrayList(1L, 2L, 3L, 4L, 5L));

        DefaultDataBox defaultDataBox = new DefaultDataBox();
        defaultDataBox.putAll(new HashMap<String, Object>() {
            {
                put("user", new User(null, null, 0));
            }
        });

        A a = new A();
        a.setUser(new User(null, null, 0));
        TaskResponse<Object> login = storyEngine.fire(buyGoodsRequest, "login", a, Object.class);
        login = storyEngine.fire(buyGoodsRequest, "login", a, Object.class);
        System.out.println(JSON.toJSONString(login));
        context.close();
    }

    public static class A implements BusDataBox {

        private User user;

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }
}
