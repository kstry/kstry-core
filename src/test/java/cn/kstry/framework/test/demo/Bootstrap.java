package cn.kstry.framework.test.demo;

import cn.kstry.framework.core.annotation.EnableKstry;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.test.demo.entity.StoryRequest;
import cn.kstry.framework.test.demo.facade.RobHongBaoResponse;
import com.alibaba.fastjson.JSON;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author lykan
 */
@Configuration
@ComponentScan(basePackageClasses = Bootstrap.class)
@EnableKstry(storyEngineName = "hongBaoStoryEngine")
public class Bootstrap {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Bootstrap.class);
        StoryEngine storyEngine = context.getBean("hongBaoStoryEngine", StoryEngine.class);

        StoryRequest storyRequest = new StoryRequest();
        storyRequest.setCount(4);
        storyRequest.setMoney(3401L);

        TaskResponse<RobHongBaoResponse> testF = storyEngine.fire(storyRequest, "testF", RobHongBaoResponse.class);
        System.out.println(JSON.toJSONString(testF));
    }
}
