package cn.kstry.framework.test.demo.xiaoming;

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
@ComponentScan(basePackageClasses = XiaoMingOneDayBootstrap.class)
@EnableKstry(configPath = "xiaoming-oneday-config.json")
public class XiaoMingOneDayBootstrap {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(XiaoMingOneDayBootstrap.class);
        StoryEngine storyEngine = context.getBean(EnableKstryRegister.DEFAULT_STORY_ENGINE_NAME, StoryEngine.class);
        System.out.println(storyEngine);
    }
}
