package cn.kstry.framework.test.demo.goods;

import cn.kstry.framework.core.annotation.EnableKstry;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.test.demo.goods.facade.BuyGoodsRequest;
import cn.kstry.framework.test.demo.goods.facade.UserLoginResponse;
import com.alibaba.fastjson.JSON;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author lykan
 */
@Configuration
@ComponentScan(basePackageClasses = GoodsBootstrap.class)
@EnableKstry(configPath = "classpath:config/*.json")
public class GoodsBootstrap {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(GoodsBootstrap.class);
        StoryEngine storyEngine = context.getBean("storyEngine", StoryEngine.class);

        BuyGoodsRequest buyGoodsRequest = new BuyGoodsRequest();
        buyGoodsRequest.setUserId(2L);
        buyGoodsRequest.setUserType(2);

        TaskResponse<UserLoginResponse> login = storyEngine.fire(buyGoodsRequest, "login", UserLoginResponse.class);
        System.out.println(JSON.toJSONString(login));
        context.close();
    }
}
