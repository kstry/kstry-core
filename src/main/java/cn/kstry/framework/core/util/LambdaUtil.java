/*
 *
 *  * Copyright (c) 2020-2023, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.component.bpmn.lambda.LambdaParam;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author lykan
 */
public class LambdaUtil {

    public static <Component> String getServiceName(LambdaParam.LambdaParam0<Component> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A> String getServiceName(LambdaParam.LambdaParam1<Component, A> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B> String getServiceName(LambdaParam.LambdaParam2<Component, A, B> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C> String getServiceName(LambdaParam.LambdaParam3<Component, A, B, C> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D> String getServiceName(LambdaParam.LambdaParam4<Component, A, B, C, D> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D, E> String getServiceName(LambdaParam.LambdaParam5<Component, A, B, C, D, E> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D, E, F> String getServiceName(LambdaParam.LambdaParam6<Component, A, B, C, D, E, F> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D, E, F, G> String getServiceName(LambdaParam.LambdaParam7<Component, A, B, C, D, E, F, G> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D, E, F, G, H> String getServiceName(LambdaParam.LambdaParam8<Component, A, B, C, D, E, F, G, H> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D, E, F, G, H, I> String getServiceName(LambdaParam.LambdaParam9<Component, A, B, C, D, E, F, G, H, I> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D, E, F, G, H, I, J> String getServiceName(LambdaParam.LambdaParam10<Component, A, B, C, D, E, F, G, H, I, J> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component> String getServiceName(LambdaParam.LambdaParamR0<Component> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A> String getServiceName(LambdaParam.LambdaParamR1<Component, A> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B> String getServiceName(LambdaParam.LambdaParamR2<Component, A, B> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C> String getServiceName(LambdaParam.LambdaParamR3<Component, A, B, C> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D> String getServiceName(LambdaParam.LambdaParamR4<Component, A, B, C, D> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D, E> String getServiceName(LambdaParam.LambdaParamR5<Component, A, B, C, D, E> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D, E, F> String getServiceName(LambdaParam.LambdaParamR6<Component, A, B, C, D, E, F> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D, E, F, G> String getServiceName(LambdaParam.LambdaParamR7<Component, A, B, C, D, E, F, G> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D, E, F, G, H> String getServiceName(LambdaParam.LambdaParamR8<Component, A, B, C, D, E, F, G, H> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D, E, F, G, H, I> String getServiceName(LambdaParam.LambdaParamR9<Component, A, B, C, D, E, F, G, H, I> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Component, A, B, C, D, E, F, G, H, I, J> String getServiceName(LambdaParam.LambdaParamR10<Component, A, B, C, D, E, F, G, H, I, J> service) {
        return ProxyUtil.getSerializedLambda(service).getImplMethodName();
    }

    public static <Link> String getSubprocessName(LambdaParam.LambdaSubProcess<Link> subProcess) {
        Pair<String, String> lambdaPair = ProxyUtil.getComponentServiceFromLambda(subProcess);
        return TaskServiceUtil.joinName(lambdaPair.getRight(), lambdaPair.getLeft());
    }

    public static <Link> String getProcessName(LambdaParam.LambdaProcess<Link> process) {
        Pair<String, String> lambdaPair = ProxyUtil.getComponentServiceFromLambda(process);
        return TaskServiceUtil.joinName(lambdaPair.getRight(), lambdaPair.getLeft());
    }

    public static <Component> Pair<String, String> getComponentService(LambdaParam.LambdaParam0<Component> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A> Pair<String, String> getComponentService(LambdaParam.LambdaParam1<Component, A> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B> Pair<String, String> getComponentService(LambdaParam.LambdaParam2<Component, A, B> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C> Pair<String, String> getComponentService(LambdaParam.LambdaParam3<Component, A, B, C> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D> Pair<String, String> getComponentService(LambdaParam.LambdaParam4<Component, A, B, C, D> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D, E> Pair<String, String> getComponentService(LambdaParam.LambdaParam5<Component, A, B, C, D, E> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D, E, F> Pair<String, String> getComponentService(LambdaParam.LambdaParam6<Component, A, B, C, D, E, F> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D, E, F, G> Pair<String, String> getComponentService(LambdaParam.LambdaParam7<Component, A, B, C, D, E, F, G> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D, E, F, G, H> Pair<String, String> getComponentService(LambdaParam.LambdaParam8<Component, A, B, C, D, E, F, G, H> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D, E, F, G, H, I> Pair<String, String> getComponentService(LambdaParam.LambdaParam9<Component, A, B, C, D, E, F, G, H, I> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D, E, F, G, H, I, J> Pair<String, String> getComponentService(LambdaParam.LambdaParam10<Component, A, B, C, D, E, F, G, H, I, J> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component> Pair<String, String> getComponentService(LambdaParam.LambdaParamR0<Component> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A> Pair<String, String> getComponentService(LambdaParam.LambdaParamR1<Component, A> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B> Pair<String, String> getComponentService(LambdaParam.LambdaParamR2<Component, A, B> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C> Pair<String, String> getComponentService(LambdaParam.LambdaParamR3<Component, A, B, C> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D> Pair<String, String> getComponentService(LambdaParam.LambdaParamR4<Component, A, B, C, D> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D, E> Pair<String, String> getComponentService(LambdaParam.LambdaParamR5<Component, A, B, C, D, E> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D, E, F> Pair<String, String> getComponentService(LambdaParam.LambdaParamR6<Component, A, B, C, D, E, F> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D, E, F, G> Pair<String, String> getComponentService(LambdaParam.LambdaParamR7<Component, A, B, C, D, E, F, G> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D, E, F, G, H> Pair<String, String> getComponentService(LambdaParam.LambdaParamR8<Component, A, B, C, D, E, F, G, H> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D, E, F, G, H, I> Pair<String, String> getComponentService(LambdaParam.LambdaParamR9<Component, A, B, C, D, E, F, G, H, I> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }

    public static <Component, A, B, C, D, E, F, G, H, I, J> Pair<String, String> getComponentService(LambdaParam.LambdaParamR10<Component, A, B, C, D, E, F, G, H, I, J> service) {
        return ProxyUtil.getComponentServiceFromLambda(service);
    }
}
