/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.core.component.bpmn.lambda;

import cn.kstry.framework.core.component.bpmn.builder.ServiceTaskBuilder;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessBuilder;
import cn.kstry.framework.core.util.LambdaUtil;
import cn.kstry.framework.core.util.ProxyUtil;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 支持Lambda表达式定义服务节点
 *
 * @author lykan
 */
public interface LambdaServiceSupport {

    /**
     * 定义下一个  ServiceTask
     *
     * @param flowExpression 指向 ServiceTask 箭头的条件表达式
     * @param component      对应 @TaskComponent
     * @param service        对应 @TaskService
     * @return ServiceTask Builder
     */
    ServiceTaskBuilder nextTask(String flowExpression, String component, String service);

    /**
     * 定义下一个子流程
     *
     * @param flowExpression 指向 SubProcess 箭头的条件表达式
     * @param processId      子流程Id
     * @return SubProcess Builder
     */
    SubProcessBuilder nextSubProcess(String flowExpression, String processId);

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component> ServiceTaskBuilder nextService(LambdaParam.LambdaParam0<Component> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A> ServiceTaskBuilder nextService(LambdaParam.LambdaParam1<Component, A> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B> ServiceTaskBuilder nextService(LambdaParam.LambdaParam2<Component, A, B> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C> ServiceTaskBuilder nextService(LambdaParam.LambdaParam3<Component, A, B, C> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D> ServiceTaskBuilder nextService(LambdaParam.LambdaParam4<Component, A, B, C, D> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E> ServiceTaskBuilder nextService(LambdaParam.LambdaParam5<Component, A, B, C, D, E> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F> ServiceTaskBuilder nextService(LambdaParam.LambdaParam6<Component, A, B, C, D, E, F> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G> ServiceTaskBuilder nextService(LambdaParam.LambdaParam7<Component, A, B, C, D, E, F, G> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G, H> ServiceTaskBuilder nextService(LambdaParam.LambdaParam8<Component, A, B, C, D, E, F, G, H> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G, H, I> ServiceTaskBuilder nextService(LambdaParam.LambdaParam9<Component, A, B, C, D, E, F, G, H, I> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G, H, I, J> ServiceTaskBuilder nextService(LambdaParam.LambdaParam10<Component, A, B, C, D, E, F, G, H, I, J> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }


    /**
     * 定义下一个 ServiceTask
     *
     * @param flowExpression 指向 ServiceTask 箭头的条件表达式
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParam0<Component> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param flowExpression 指向 ServiceTask 箭头的条件表达式
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParam1<Component, A> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParam2<Component, A, B> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParam3<Component, A, B, C> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParam4<Component, A, B, C, D> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParam5<Component, A, B, C, D, E> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParam6<Component, A, B, C, D, E, F> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParam7<Component, A, B, C, D, E, F, G> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G, H> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParam8<Component, A, B, C, D, E, F, G, H> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G, H, I> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParam9<Component, A, B, C, D, E, F, G, H, I> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G, H, I, J> ServiceTaskBuilder nextService(String flowExpression,
                                                                                     LambdaParam.LambdaParam10<Component, A, B, C, D, E, F, G, H, I, J> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }


    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component> ServiceTaskBuilder nextService(LambdaParam.LambdaParamR0<Component> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A> ServiceTaskBuilder nextService(LambdaParam.LambdaParamR1<Component, A> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B> ServiceTaskBuilder nextService(LambdaParam.LambdaParamR2<Component, A, B> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C> ServiceTaskBuilder nextService(LambdaParam.LambdaParamR3<Component, A, B, C> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D> ServiceTaskBuilder nextService(LambdaParam.LambdaParamR4<Component, A, B, C, D> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E> ServiceTaskBuilder nextService(LambdaParam.LambdaParamR5<Component, A, B, C, D, E> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F> ServiceTaskBuilder nextService(LambdaParam.LambdaParamR6<Component, A, B, C, D, E, F> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G> ServiceTaskBuilder nextService(LambdaParam.LambdaParamR7<Component, A, B, C, D, E, F, G> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G, H> ServiceTaskBuilder nextService(LambdaParam.LambdaParamR8<Component, A, B, C, D, E, F, G, H> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G, H, I> ServiceTaskBuilder nextService(LambdaParam.LambdaParamR9<Component, A, B, C, D, E, F, G, H, I> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G, H, I, J> ServiceTaskBuilder nextService(LambdaParam.LambdaParamR10<Component, A, B, C, D, E, F, G, H, I, J> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(null, componentService.getLeft(), componentService.getRight());
    }


    /**
     * 定义下一个 ServiceTask
     *
     * @param flowExpression 指向 ServiceTask 箭头的条件表达式
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParamR0<Component> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param flowExpression 指向 ServiceTask 箭头的条件表达式
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParamR1<Component, A> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParamR2<Component, A, B> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParamR3<Component, A, B, C> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParamR4<Component, A, B, C, D> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParamR5<Component, A, B, C, D, E> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParamR6<Component, A, B, C, D, E, F> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParamR7<Component, A, B, C, D, E, F, G> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G, H> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParamR8<Component, A, B, C, D, E, F, G, H> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G, H, I> ServiceTaskBuilder nextService(String flowExpression, LambdaParam.LambdaParamR9<Component, A, B, C, D, E, F, G, H, I> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个 ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    default <Component, A, B, C, D, E, F, G, H, I, J> ServiceTaskBuilder nextService(String flowExpression,
                                                                                     LambdaParam.LambdaParamR10<Component, A, B, C, D, E, F, G, H, I, J> service) {
        Pair<String, String> componentService = ProxyUtil.getComponentServiceFromLambda(service);
        return nextTask(flowExpression, componentService.getLeft(), componentService.getRight());
    }

    /**
     * 定义下一个子流程
     *
     * @param flowExpression 指向 SubProcess 箭头的条件表达式
     * @param subProcess      子流程Lambda表达式
     * @return SubProcess Builder
     */
    default <Link> SubProcessBuilder nextSubProcess(String flowExpression, LambdaParam.LambdaSubProcess<Link> subProcess) {
        return nextSubProcess(flowExpression, LambdaUtil.getSubprocessName(subProcess));
    }

    /**
     * 定义下一个子流程
     *
     * @param subProcess      子流程Lambda表达式
     * @return SubProcess Builder
     */
    default <Link> SubProcessBuilder nextSubProcess(LambdaParam.LambdaSubProcess<Link> subProcess) {
        return nextSubProcess(null, LambdaUtil.getSubprocessName(subProcess));
    }
}
