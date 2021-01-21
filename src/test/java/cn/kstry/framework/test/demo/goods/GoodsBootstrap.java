package cn.kstry.framework.test.demo.goods;

import cn.kstry.framework.core.annotation.EnableKstry;
import cn.kstry.framework.core.annotation.EnableKstryRegister;
import cn.kstry.framework.core.engine.StoryEngine;
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
        System.out.println(storyEngine);
    }
}
