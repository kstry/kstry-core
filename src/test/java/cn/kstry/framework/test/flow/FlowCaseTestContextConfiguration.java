package cn.kstry.framework.test.flow;

import cn.kstry.framework.core.annotation.EnableKstry;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableKstry(bpmnPath = "./bpmn/flow/*.bpmn")
@ComponentScan(basePackageClasses = FlowCaseTestContextConfiguration.class)
@PropertySource("classpath:application.properties")
public class FlowCaseTestContextConfiguration {
}
