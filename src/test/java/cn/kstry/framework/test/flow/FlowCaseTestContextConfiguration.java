package cn.kstry.framework.test.flow;

import cn.kstry.framework.core.annotation.EnableKstry;
import cn.kstry.framework.test.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableKstry(bpmnPath = "./bpmn/*.bpmn", propertiesPath = "./kstry-*.yml")
@ComponentScan(basePackageClasses = Test.class)
@PropertySource("classpath:application.properties")
public class FlowCaseTestContextConfiguration {
}
