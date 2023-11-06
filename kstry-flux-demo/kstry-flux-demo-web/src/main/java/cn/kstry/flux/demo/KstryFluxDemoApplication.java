package cn.kstry.flux.demo;

import cn.kstry.framework.core.annotation.EnableKstry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * bpmnPath： 指定bpmn文件位置
 */
@EnableScheduling
@EnableKstry(bpmnPath = "./bpmn/*.bpmn,./bpmn/*.json", propertiesPath = "./config/*.yml")
@SpringBootApplication
public class KstryFluxDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KstryFluxDemoApplication.class, args);
    }
}
