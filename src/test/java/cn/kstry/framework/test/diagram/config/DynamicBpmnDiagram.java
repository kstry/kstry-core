package cn.kstry.framework.test.diagram.config;

import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.enums.IterateStrategyEnum;
import cn.kstry.framework.core.bpmn.extend.ElementIterable;
import cn.kstry.framework.core.component.bpmn.joinpoint.InclusiveJoinPoint;
import cn.kstry.framework.core.component.bpmn.joinpoint.ParallelJoinPoint;
import cn.kstry.framework.core.component.bpmn.link.BpmnLink;
import cn.kstry.framework.core.component.bpmn.link.StartBpmnLink;
import cn.kstry.framework.core.component.dynamic.creator.DynamicProcess;
import cn.kstry.framework.test.diagram.constants.SCS;
import cn.kstry.framework.test.diagram.constants.StoryNameConstants;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class DynamicBpmnDiagram implements DynamicProcess {

    @Override
    public Optional<BpmnLink> getBpmnLink(String startId) {
        if (Objects.equals(StoryNameConstants.D001, startId)) {
            StartBpmnLink bpmnLink = StartBpmnLink.build(StoryNameConstants.D001);
            BpmnLink buildOneChain = bpmnLink.nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build();

            bpmnLink.parallel().build().joinLinks().joinLinks(
                            bpmnLink.inclusive().build().joinLinks(
                                    buildOneChain
                                            .nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                                            .nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build(),
                                    buildOneChain
                                            .nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                                            .nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                                            .nextSubProcess("a-call-link-diagram-sub-process2").build()
                            ),
                            bpmnLink
                                    .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                                    .nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    )
                    .nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .nextSubProcess("diagram-sub-process2").build()
                    .nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .end();
            return Optional.of(bpmnLink);
        } else if (Objects.equals(StoryNameConstants.D002, startId)) {
            StartBpmnLink bpmnLink = StartBpmnLink.build(StoryNameConstants.D002);

            BpmnLink exclusiveGateway = bpmnLink.nextExclusive().build();
            exclusiveGateway
                    .nextService("req.a < 10", SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .end();

            ParallelJoinPoint par01 = exclusiveGateway.nextParallel("req.a >= 10", bpmnLink.parallel().notStrictMode().openAsync().build());
            par01.nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
            par01.nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
            par01.nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
            par01.nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
            par01.nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
            return Optional.of(bpmnLink);
        } else if (Objects.equals(StoryNameConstants.D003, startId)) {
            StartBpmnLink bpmnLink = StartBpmnLink.build(StoryNameConstants.D003);

            ServiceTask st1 = ServiceTask.builder("s-02345-id1")
                    .component(SCS.F.CALCULATE_SERVICE)
                    .service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE)
                    .ins();

            BpmnLink exclusiveGateway = bpmnLink.nextExclusive().serviceTask(st1).build();
            exclusiveGateway
                    .nextTask("req.a < 10",
                            ServiceTask.builder(null).component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).ins()
                    )
                    .nextTask(ServiceTask.builder(null).component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).ins())
                    .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_TIMEOUT).timeout(1000).notStrictMode().build()
                    .nextTask(SCS.F.CALCULATE_SERVICE, "aaa").allowAbsent().build()
                    .end();

            ServiceTask st2 = ServiceTask.builder("s-02345-id2")
                    .component(SCS.F.CALCULATE_SERVICE)
                    .service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE)
                    .ins();
            InclusiveJoinPoint inc01 = exclusiveGateway.nextInclusive("req.a >= 10", bpmnLink.inclusive().serviceTask(st2).openAsync().build());
            inc01.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).name("XXX").build().end();
            inc01.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
            inc01.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
            inc01.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
            inc01.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
            return Optional.of(bpmnLink);
        } else if (Objects.equals(StoryNameConstants.D004, startId)) {
            StartBpmnLink bpmnLink = StartBpmnLink.build(StoryNameConstants.D004);
            bpmnLink.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ARRAY_ONE).property("test-prop")
                    .iterable(ElementIterable.builder("sta.arr").openAsync().iteStrategy(IterateStrategyEnum.ALL_SUCCESS).build()).build()
                    .end();
            return Optional.of(bpmnLink);
        } else if (Objects.equals(StoryNameConstants.D005, startId)) {
            StartBpmnLink bpmnLink = StartBpmnLink.build(StoryNameConstants.D005);
            bpmnLink.nextSubProcess("ite-test-sub-process").timeout(0).notStrictMode()
                    .iterable(ElementIterable.builder("sta.arr").openAsync().iteStrategy(IterateStrategyEnum.ALL_SUCCESS).build()).build()
                    .end();
            return Optional.of(bpmnLink);
        } else if (Objects.equals(StoryNameConstants.D006, startId)) {
            StartBpmnLink bpmnLink = StartBpmnLink.build(StoryNameConstants.D006);
            bpmnLink.nextSubProcess("ite-test-sub-process2").timeout(1000)
                    .iterable(ElementIterable.builder("sta.arr").openAsync().iteStrategy(IterateStrategyEnum.ALL_SUCCESS).build()).build()
                    .end();
            return Optional.of(bpmnLink);
        } else if (Objects.equals(StoryNameConstants.D007, startId)) {
            StartBpmnLink bpmnLink = StartBpmnLink.build(StoryNameConstants.D007);
            bpmnLink.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.CALCULATE_ERROR).build().end();
            return Optional.of(bpmnLink);
        } else if (Objects.equals(StoryNameConstants.D008, startId)) {
            StartBpmnLink bpmnLink = StartBpmnLink.build(StoryNameConstants.D008);
            bpmnLink.nextService(SCS.CALCULATE_SERVICE.F.MULTIPLY_PLUS).build().end();
            return Optional.of(bpmnLink);
        }
        return Optional.empty();
    }
}
