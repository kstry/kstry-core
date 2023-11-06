package cn.kstry.framework.test.diagram;

import cn.kstry.framework.core.annotation.EnableKstry;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableKstry(bpmnPath = "./bpmn/diagram/*.bpmn")
@PropertySource("classpath:application.properties")
@ComponentScan(basePackageClasses = DiagramCaseTestContextConfiguration.class)
public class DiagramCaseTestContextConfiguration {


}
