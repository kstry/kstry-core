package cn.kstry.flux.demo.process;

import cn.kstry.flux.demo.service.goods.EvaluationService;
import cn.kstry.flux.demo.service.goods.GoodsService;
import cn.kstry.flux.demo.service.goods.OrderService;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.joinpoint.InclusiveJoinPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefSubProcess {

    /**
     * 构建统计子流程
     *
     * @return 统计子流程
     */
    @Bean
    public SubProcessLink buildStatisticsSubProcess() {
        return SubProcessLink.build(DefSubProcess::buildStatisticsSubProcess, link -> {

            // 构建一个游离的包含网关，网关标识开启异步
            InclusiveJoinPoint inclusive1 = link.inclusive().openAsync().build();

            // 开始节点指向包含网关
            link.nextInclusive(inclusive1);

            // 再次构建一个游离的包含网关
            InclusiveJoinPoint inclusive2 = link.inclusive().build();

            // 包含网关1分别指向三个 TaskService， 三个 TaskService 再分别指向包含网关2，包含网关2指向结束节点
            inclusive2.joinLinks(
                    inclusive1.nextService("res.needEvaluate == true", EvaluationService::getEvaluationInfo).build(),
                    inclusive1.nextService(GoodsService::getGoodsExtInfo).build(),
                    inclusive1.nextService(OrderService::getOrderInfo).build()
            ).end();
        });
    }
}
