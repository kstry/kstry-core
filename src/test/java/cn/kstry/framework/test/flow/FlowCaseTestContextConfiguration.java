package cn.kstry.framework.test.flow;

import cn.kstry.framework.core.annotation.EnableKstry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableKstry(bpmnPath = "./bpmn/flow/*.bpmn")
@ComponentScan(basePackageClasses = FlowCaseTestContextConfiguration.class)
@PropertySource("classpath:application.properties")
public class FlowCaseTestContextConfiguration {

    @Bean(name = "custom-fly-t")
    public ThreadPoolExecutor executor() {
        return new ThreadPoolExecutor(
                5,
                10,
                10,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(3),
                new ThreadFactoryBuilder().setNameFormat("custom-fly-t-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
