package cn.kstry.framework.test.diagram.config;

import cn.kstry.framework.core.component.bpmn.BpmnProcessParser;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.dynamic.creator.DynamicSubProcess;
import cn.kstry.framework.core.component.expression.Exp;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.test.diagram.constants.SCS;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DynamicBpmnSubDiagram implements DynamicSubProcess {

    @Override
    public List<SubProcessLink> getSubProcessLinks() {
        String subXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" id=\"Definitions_0aqif10\" targetNamespace=\"http://bpmn.io/schema/bpmn\">"
                + "  <bpmn:process id=\"Process_0ibnexd\" isExecutable=\"true\">"
                + "    <bpmn:subProcess id=\"Activity_1t2opos\" name=\"DDDD\">"
                + "      <bpmn:startEvent id=\"Event_0tnfpo6\">"
                + "        <bpmn:outgoing>Flow_12l39eo</bpmn:outgoing>"
                + "      </bpmn:startEvent>"
                + "      <bpmn:inclusiveGateway id=\"Gateway_0b7iikh\">"
                + "        <bpmn:extensionElements>"
                + "          <camunda:properties>"
                + "            <camunda:property name=\"open-async\" value=\"true\" />"
                + "          </camunda:properties>"
                + "        </bpmn:extensionElements>"
                + "        <bpmn:incoming>Flow_12l39eo</bpmn:incoming>"
                + "        <bpmn:outgoing>Flow_1w3i31z</bpmn:outgoing>"
                + "      </bpmn:inclusiveGateway>"
                + "      <bpmn:serviceTask id=\"Activity_04s0a6o\" name=\"increase-one\">"
                + "        <bpmn:extensionElements>"
                + "          <camunda:properties>"
                + "            <camunda:property name=\"task-component\" value=\"CALCULATE_SERVICE\" />"
                + "            <camunda:property name=\"task-service\" value=\"INCREASE_ONE\" />"
                + "          </camunda:properties>"
                + "        </bpmn:extensionElements>"
                + "        <bpmn:incoming>Flow_1w3i31z</bpmn:incoming>"
                + "        <bpmn:outgoing>Flow_09576ug</bpmn:outgoing>"
                + "      </bpmn:serviceTask>"
                + "      <bpmn:callActivity id=\"Activity_00ykmvu\" name=\"Call Diagram\" calledElement=\"a-call-link-diagram-sub-process2\">"
                + "        <bpmn:incoming>Flow_09576ug</bpmn:incoming>"
                + "        <bpmn:outgoing>Flow_0a27455</bpmn:outgoing>"
                + "      </bpmn:callActivity>"
                + "      <bpmn:endEvent id=\"Event_1rhdy0k\">"
                + "        <bpmn:incoming>Flow_0a27455</bpmn:incoming>"
                + "      </bpmn:endEvent>"
                + "      <bpmn:sequenceFlow id=\"Flow_0a27455\" sourceRef=\"Activity_00ykmvu\" targetRef=\"Event_1rhdy0k\" />"
                + "      <bpmn:sequenceFlow id=\"Flow_09576ug\" sourceRef=\"Activity_04s0a6o\" targetRef=\"Activity_00ykmvu\" />"
                + "      <bpmn:sequenceFlow id=\"Flow_1w3i31z\" sourceRef=\"Gateway_0b7iikh\" targetRef=\"Activity_04s0a6o\" />"
                + "      <bpmn:sequenceFlow id=\"Flow_12l39eo\" sourceRef=\"Event_0tnfpo6\" targetRef=\"Gateway_0b7iikh\" />"
                + "    </bpmn:subProcess>"
                + "  </bpmn:process>"
                + "</bpmn:definitions>";
        BpmnProcessParser parser = new BpmnProcessParser("自定义XML子流程", subXml);
        return Lists.newArrayList(parser.getSubProcessLink("Activity_1t2opos").orElse(null),
                SubProcessLink.build("a-call-link-diagram-sub-process2",
                        link -> link
                                .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                                .nextSubProcess(Exp.b(e -> e.notNull(ScopeTypeEnum.REQUEST, "d")), "gateway-sub-process2").build()
                                .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                                .end()
                ), SubProcessLink.build(DynamicBpmnSubDiagram::getSubProcessLinks,
                        link -> link
                                .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                                .nextSubProcess("Activity_1t2opos").build()
                                .nextSubProcess("a-call-link-diagram-sub-process2").build()
                                .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                                .end()
                ), SubProcessLink.build("gateway-sub-process2", link -> {
                    ProcessLink eg1 = link
                            .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                            .nextExclusive().build();

                    eg1
                            .nextTask(Exp.b(e -> e.order(1).req("d").ge().value(0))).component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                            .nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                            .end();
                    eg1
                            .nextTask(Exp.b(e -> e.req("d").lt().value(0)), SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                            .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                            .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                            .end();
                }), SubProcessLink.build("ite-test-sub-process2",
                        link -> link.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ARRAY_ONE).property("test-prop").build().end())
        );
    }
}
