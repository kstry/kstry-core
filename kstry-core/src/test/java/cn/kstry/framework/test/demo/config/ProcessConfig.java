package cn.kstry.framework.test.demo.config;

import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.joinpoint.InclusiveJoinPoint;
import cn.kstry.framework.core.component.bpmn.joinpoint.ParallelJoinPoint;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.bpmn.link.StartProcessLink;
import cn.kstry.framework.core.component.expression.Exp;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.util.KeyUtil;
import cn.kstry.framework.test.demo.facade.CommonFields;
import cn.kstry.framework.test.demo.facade.QueryScoreRequest;
import cn.kstry.framework.test.demo.service.CalculateService;
import cn.kstry.framework.test.demo.service.FlowDemoService;
import cn.kstry.framework.test.demo.service.StudentScoreService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessConfig {

    /**
     * 定义子流程
     */
    @Bean
    public SubProcessLink calculateSubProcessDemo() {
        return SubProcessLink.build(ProcessConfig::calculateSubProcessDemo, link -> {
            InclusiveJoinPoint inclusiveJoinPoint = link.nextInclusive(link.inclusive().openAsync().build());
            link.inclusive().build().joinLinks(
                    inclusiveJoinPoint.nextService(Ex.b(e -> e.equals("var.factor", "'+'")), CalculateService::plusCalculate).build(),
                    inclusiveJoinPoint.nextService(Ex.b(e -> e.equals("var.factor", "'*'")), CalculateService::multiplyCalculate).build(),
                    inclusiveJoinPoint.nextService(Ex.b(e -> e.equals("var.factor", "'-'")), CalculateService::minusCalculate).build()
            ).end();
        });
    }

    @Bean
    public ProcessLink testCalculateErrorDemotionProcess() {
        StartProcessLink processLink = StartProcessLink.build(ProcessConfig::testCalculateErrorDemotionProcess);
        processLink.nextService(CalculateService::calculateError).build().end();
        return processLink;
    }

    @Bean
    public ProcessLink testCalculateSubProcessDemo() {
        StartProcessLink processLink = StartProcessLink.build(ProcessConfig::testCalculateSubProcessDemo);
        processLink
                .nextService(CalculateService::setCalculateNumber).params("{'a': 11, 'b': 6}").build()
                .nextSubProcess(ProcessConfig::calculateSubProcessDemo).build() // 引用子流程
                .end();
        return processLink;
    }

    @Bean
    public ProcessLink testInclusiveGatewayDemoProcess() {
        StartProcessLink processLink = StartProcessLink.build(ProcessConfig::testInclusiveGatewayDemoProcess);

        InclusiveJoinPoint inclusiveJoinPoint = processLink.nextInclusive(processLink.inclusive().openAsync().build());
        processLink.inclusive().build().joinLinks(
                inclusiveJoinPoint.nextService(Ex.b(e -> e.equals("var.factor", "'+'")), CalculateService::plusCalculate).build(),
                inclusiveJoinPoint.nextService(Ex.b(e -> e.equals("var.factor", "'*'")), CalculateService::multiplyCalculate).build(),
                inclusiveJoinPoint.nextService(Ex.b(e -> e.equals("var.factor", "'-'")), CalculateService::minusCalculate).build()
        ).end();
        return processLink;
    }

    @Bean
    public ProcessLink testExclusiveGatewayDemoProcess() {
        StartProcessLink processLink = StartProcessLink.build(ProcessConfig::testExclusiveGatewayDemoProcess);
        ProcessLink exclusive = processLink.nextExclusive().build();
        exclusive.nextService(Ex.b(e -> e.order(1).equals("var.factor", "'+'")), CalculateService::plusCalculate).build().end();
        exclusive.nextService(Ex.b(e -> e.order(2).equals("var.factor", "'*'")), CalculateService::multiplyCalculate).build().end();
        exclusive.nextService(CalculateService::minusCalculate).build().end();
        return processLink;
    }

    @Bean
    public ProcessLink testParallelGatewayDemoProcess() {
        StartProcessLink processLink = StartProcessLink.build(ProcessConfig::testParallelGatewayDemoProcess);
        ParallelJoinPoint parallelJoinPoint = processLink.nextParallel(processLink.parallel().openAsync().build());
        processLink.parallel().build().joinLinks(
                parallelJoinPoint.nextService(FlowDemoService::getStudentInfo).build(),
                parallelJoinPoint.nextService(FlowDemoService::getClassInfo).build()
        ).end();
        return processLink;
    }

    @Bean
    public ProcessLink startEventSequenceFlowProcess() {
        StartProcessLink processLink = StartProcessLink.build(ProcessConfig::startEventSequenceFlowProcess);
        processLink.nextService("@eq(@toInt(@toString(var.type)), @toInt('1')) && @noneNull(var.type)", CalculateService::plusCalculate).build().end();
        processLink.nextService(Ex.bu(e -> e.customEquals(KeyUtil.var(CommonFields.F.type), "2")), CalculateService::multiplyCalculate).build().end();
        return processLink;
    }

    @Bean
    public ProcessLink startEventSequenceFlowProcess2() {
        StartProcessLink processLink = StartProcessLink.build(ProcessConfig::startEventSequenceFlowProcess2);
        processLink.nextService(Exp.b(e -> e.equals(KeyUtil.var(CommonFields.F.type), "1")), CalculateService::plusCalculate).build().end();
        processLink.nextService(Exp.b(e -> e.equals(KeyUtil.var(CommonFields.F.type), "2")), CalculateService::multiplyCalculate).build().end();
        return processLink;
    }

    @Bean
    public ProcessLink studentScoreQueryProcess() {
        StartProcessLink processLink = StartProcessLink.build(ProcessConfig::studentScoreQueryProcess);
        InclusiveJoinPoint asyncInclusive = processLink.nextInclusive(processLink.inclusive().openAsync().build());
        InclusiveJoinPoint asyncInclusive2 = asyncInclusive
                .nextService(Exp.b(e -> e.isTrue(ScopeTypeEnum.REQUEST, QueryScoreRequest.F.needScore)), StudentScoreService::getStudyExperienceList).build()
                .nextInclusive(processLink.inclusive().openAsync().build());

        processLink.inclusive().build().joinLinks(
                        processLink.parallel().notStrictMode().build().joinLinks(
                                asyncInclusive2.nextService(StudentScoreService::getClasInfoById).build(),
                                asyncInclusive2.nextService(StudentScoreService::getStudentScoreList).build()
                        ).nextService(StudentScoreService::assembleScoreClassInfo).build(),
                        processLink.inclusive().build().joinLinks(
                                asyncInclusive.nextService(StudentScoreService::getStudentBasic).build(),
                                asyncInclusive.nextService(StudentScoreService::getStudentPrivacy).build()
                        ).nextService(StudentScoreService::assembleStudentInfo).build()
                )
                .nextService("cycleTimes > 0", StudentScoreService::getQueryScoreResponse).build()
                .end();
        return processLink;
    }

    @Bean
    public ProcessLink testAsyncFlowProcess() {
        StartProcessLink processLink = StartProcessLink.build(ProcessConfig::testAsyncFlowProcess);
        InclusiveJoinPoint inclusive01 = processLink
                .nextService(CalculateService::atomicInc).name("Task01").build()
                .nextInclusive(processLink.inclusive().openAsync().build());
        InclusiveJoinPoint inclusive04 = processLink
                .nextService(CalculateService::atomicInc).name("Task04").build()
                .nextInclusive(processLink.inclusive().openAsync().build());

        processLink.inclusive().build().joinLinks(
                        inclusive01.nextService(CalculateService::atomicInc).name("Task02").build(),
                        processLink.inclusive().build().joinLinks(
                                inclusive01.nextService(CalculateService::atomicInc).name("Task03").build(),
                                inclusive04.nextService(CalculateService::atomicInc).name("Task05").build()
                        ).nextService(CalculateService::atomicInc).name("Task07").build(),
                        inclusive04.nextService(CalculateService::atomicInc).name("Task06").build()
                ).nextService(CalculateService::atomicInc).name("Task08").build()
                .end();
        return processLink;
    }

    @Bean
    public ProcessLink testSimpleFlowDemoProcess() {
        StartProcessLink processLink = StartProcessLink.build(ProcessConfig::testSimpleFlowDemoProcess);
        processLink
                .nextService(CalculateService::setNumber).build()
                .nextService(CalculateService::plusCalculate).build()
                .end();
        return processLink;
    }
}
