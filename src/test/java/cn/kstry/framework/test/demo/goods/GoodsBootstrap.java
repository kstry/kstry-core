package cn.kstry.framework.test.demo.goods;

import cn.kstry.framework.core.annotation.EnableKstry;
import cn.kstry.framework.core.annotation.EnableKstryRegister;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.test.demo.goods.facade.BuyGoodsRequest;
import cn.kstry.framework.test.demo.goods.facade.BuyGoodsResponse;
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
@EnableKstry(configPath = "goods-config.json")
public class GoodsBootstrap {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(GoodsBootstrap.class);
        StoryEngine storyEngine = context.getBean(EnableKstryRegister.DEFAULT_STORY_ENGINE_NAME, StoryEngine.class);

        BuyGoodsRequest buyGoodsRequest = new BuyGoodsRequest();
        buyGoodsRequest.setUserId(1L);
        buyGoodsRequest.setUserType(1);
        TaskResponse<BuyGoodsResponse> login = storyEngine.fire(buyGoodsRequest, "doLogin", BuyGoodsResponse.class);
//        TaskResponse<BuyGoodsResponse> login = storyEngine.fire(buyGoodsRequest, "login", BuyGoodsResponse.class);
        System.out.println(JSON.toJSONString(login));
    }
}
