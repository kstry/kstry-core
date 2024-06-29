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
package cn.kstry.framework.core.exception;

import cn.kstry.framework.core.enums.ExceptionTypeEnum;

/**
 * 错误枚举
 *
 * @author lykan
 */
public enum ExceptionEnum {

    /**
     * [K1010001] 未捕获异常
     */
    SYSTEM_ERROR(ExceptionTypeEnum.GLOBAL, "0001", "System error!"),

    /**
     * [K1010002] 字符串或者对象不允许为空
     */
    NOT_ALLOW_EMPTY(ExceptionTypeEnum.GLOBAL, "0002", "The object is not allowed to be empty!"),

    /**
     * [K1010003] 集合不允许为空
     */
    COLLECTION_NOT_ALLOW_EMPTY(ExceptionTypeEnum.GLOBAL, "0003", "The collection is not allowed to be empty!"),

    /**
     * [K1010004] 类型转化异常
     */
    TYPE_TRANSFER_ERROR(ExceptionTypeEnum.GLOBAL, "0004", "Type conversion error!"),

    /**
     * [K1010005] 必须为空
     */
    OBJ_MUST_EMPTY(ExceptionTypeEnum.GLOBAL, "0005", "Objects are not allowed to exist!"),


    ///////////////////////////////////////////////// Global END /////////////////////////////////////////////////

    /**
     * [K1020001] EnableKstry 注解在容器中只允许出现一次
     */
    ENABLE_KSTRY_NUMBER_ERROR(ExceptionTypeEnum.COMPONENT, "0001", "The EnableKstry annotation is allowed to appear only once in the container!"),

    /**
     * [K1020002] 注解使用错误
     */
    ANNOTATION_USAGE_ERROR(ExceptionTypeEnum.COMPONENT, "0002", "Annotation usage exceptions!"),

    /**
     * [K1020003] 必须出现的组件属性未被指定
     */
    COMPONENT_ATTRIBUTES_EMPTY(ExceptionTypeEnum.COMPONENT, "0003", "Must-appear component attributes are not specified!"),

    /**
     * [K1020004] 组件不可被修改
     */
    COMPONENT_IMMUTABLE_ERROR(ExceptionTypeEnum.COMPONENT, "0004", "Component is not modifiable!"),

    /**
     * [K1020005] 组件不允许重复出现
     */
    COMPONENT_DUPLICATION_ERROR(ExceptionTypeEnum.COMPONENT, "0005", "Components are not allowed to be repeated!"),

    /**
     * [K1020006] 服务节点降级策略未生效
     */
    DEMOTION_DEFINITION_ERROR(ExceptionTypeEnum.COMPONENT, "0006", "Service node demotion policy not in effect!"),

    /**
     * [K1020007] 组件参数使用错误
     */
    COMPONENT_PARAMS_ERROR(ExceptionTypeEnum.COMPONENT, "0007", "Component parameters are incorrectly used!"),

    ///////////////////////////////////////////////// COMPONENT END /////////////////////////////////////////////////

    /**
     * [K1030001] 配置文件解析失败
     */
    CONFIGURATION_PARSE_FAILURE(ExceptionTypeEnum.CONFIG, "0001", "Configuration file parsing failure!"),

    /**
     * [K1030002] 元素缺少必须的参数
     */
    CONFIGURATION_ATTRIBUTES_REQUIRED(ExceptionTypeEnum.CONFIG, "0002", "Element is missing a required attributes!"),

    /**
     * [K1030003] 子流程配置存在错误
     */
    CONFIGURATION_SUBPROCESS_ERROR(ExceptionTypeEnum.CONFIG, "0003", "There is an error in the subprocess configuration!"),

    /**
     * [K1030004] 配置文件中存在不支持的 BPMN 元素
     */
    CONFIGURATION_UNSUPPORTED_ELEMENT(ExceptionTypeEnum.CONFIG, "0004", "Unsupported bpmn elements are present in the configuration file!"),

    /**
     * [K1030005] 元素间组成的链路存在错误
     */
    CONFIGURATION_FLOW_ERROR(ExceptionTypeEnum.CONFIG, "0005", "There is an error in the link formed between the elements!"),

    /**
     * [K1030006] 配置资源读取失败
     */
    CONFIGURATION_RESOURCE_ERROR(ExceptionTypeEnum.CONFIG, "0006", "Configuration resource read failure!"),

    /**
     * [K1030007] kv scope 格式解析错误
     */
    KV_SCOPE_PARSING_ERROR(ExceptionTypeEnum.CONFIG, "0007", "kv scope format parsing error!"),

    /**
     * [K1030008] 链路中元素id被重复定义
     */
    ELEMENT_DUPLICATION_ERROR(ExceptionTypeEnum.CONFIG, "0008", "The element id in the link is repeatedly defined!"),

    /**
     * [K1030009] BPMN元素属性无效
     */
    BPMN_ATTRIBUTE_INVALID(ExceptionTypeEnum.CONFIG, "0009", "The bpmn element attribute is invalid!"),

    /**
     * [K1030010] 代码方式配置BPMN执行线路存在异常
     */
    BPMN_DIAGRAM_LINK_ERROR(ExceptionTypeEnum.CONFIG, "0010", "There is an exception in the code way to configure BPMN execution line!"),

    /**
     * [K1030011] Lambda表达式配置流程解析失败
     */
    LAMBDA_PROCESS_PARSE_FAILURE(ExceptionTypeEnum.CONFIG, "0011", "Lambda expression configuration flow parsing failure!"),

    ///////////////////////////////////////////////// CONFIG END /////////////////////////////////////////////////

    /**
     * [K1040001] Story execution failure
     */
    STORY_ERROR(ExceptionTypeEnum.STORY, "0001", "Story execution failure!"),

    /**
     * [K1040003] 参数错误
     */
    PARAMS_ERROR(ExceptionTypeEnum.STORY, "0003", "params error!"),

    /**
     * [K1040004] TaskService匹配失败
     */
    TASK_SERVICE_MATCH_ERROR(ExceptionTypeEnum.STORY, "0004", "No available TaskService matched!"),

    /**
     * [K1040005] 参数校验错误
     */
    PARAM_VERIFICATION_ERROR(ExceptionTypeEnum.STORY, "0005", "Parameter verification error!"),

    /**
     * [K1040006] Story任务被取消
     */
    TASK_CANCELLED(ExceptionTypeEnum.STORY, "0006", "Story task was cancelled!"),

    /**
     * [K1040007] 流程分支上的表达式执行失败
     */
    EXPRESSION_INVOKE_ERROR(ExceptionTypeEnum.STORY, "0007", "Expression execution on process branch failed!"),

    /**
     * [K1040008] Story流程执行中出现错误
     */
    STORY_FLOW_ERROR(ExceptionTypeEnum.STORY, "0008", "Story flow error!"),

    /**
     * [K1040009] story tracking code
     */
    STORY_TRACKING_CODE(ExceptionTypeEnum.STORY, "0009", " story tracking code."),

    /**
     * [K1040010] 线程切换钩子执行异常
     */
    THREAD_SWITCH_HOOK_ERROR(ExceptionTypeEnum.STORY, "0010", "Thread switch hook execution exception!"),

    /**
     * [K1040011] 脚本属性解析异常
     */
    SCRIPT_PROPERTY_PARSER_ERROR(ExceptionTypeEnum.STORY, "0011", "Script property resolution exception!"),

    /**
     * [K1040012] 脚本执行异常
     */
    SCRIPT_EXECUTE_ERROR(ExceptionTypeEnum.STORY, "0012", "Script execution exception!"),

    ///////////////////////////////////////////////// STORY END /////////////////////////////////////////////////

    /**
     * [K1050001] Task 获取 bean 属性信息失败
     */
    FAILED_GET_PROPERTY(ExceptionTypeEnum.NODE_INVOKE, "0001", "BeanUtils failed to get bean property!"),

    /**
     * [K1050002] Task 设置 bean 属性信息失败
     */
    FAILED_SET_PROPERTY(ExceptionTypeEnum.NODE_INVOKE, "0002", "BeanUtils failed to set bean property!"),

    /**
     * [K1050003] 不可变集合中已存在的值不允许被重复设置
     */
    IMMUTABLE_SET_UPDATE(ExceptionTypeEnum.NODE_INVOKE, "0003", "Values that already exist in the immutable set are not allowed to be set repeatedly!"),

    /**
     * [K1050004] TaskService入参实例化失败
     */
    SERVICE_PARAM_ERROR(ExceptionTypeEnum.NODE_INVOKE, "0004", "There is an error in the TaskService parameter!"),

    /**
     * [K1050005] 服务节点执行失败
     */
    SERVICE_INVOKE_ERROR(ExceptionTypeEnum.NODE_INVOKE, "0005", "Service node execution failure!"),

    /**
     * [K1050006] 迭代项执行失败
     */
    ITERATE_ITEM_ERROR(ExceptionTypeEnum.NODE_INVOKE, "0006", "Iteration item execution failure!"),

    /**
     * [K1050007] 业务执行失败
     */
    BUSINESS_INVOKE_ERROR(ExceptionTypeEnum.NODE_INVOKE, "0007", "Business execution failure!"),

    /**
     * [K1050008] 限流异常
     */
    RATE_LIMITER_ERROR(ExceptionTypeEnum.NODE_INVOKE, "0008", "The service node is subject to rate limiting!"),

    ///////////////////////////////////////////////// NODE_INVOKE END /////////////////////////////////////////////////

    /**
     * [K1060001] 异步节点任务被中断
     */
    ASYNC_TASK_INTERRUPTED(ExceptionTypeEnum.ASYNC_TASK, "0001", "Asynchronous node tasks are interrupted!"),

    /**
     * [K1060002] 异步节点任务超时
     */
    ASYNC_TASK_TIMEOUT(ExceptionTypeEnum.ASYNC_TASK, "0002", "Asynchronous node task timeout!"),

    /**
     * [K1060003] 异步节点任务发生异常
     */
    ASYNC_TASK_ERROR(ExceptionTypeEnum.ASYNC_TASK, "0003", "Exception for asynchronous node tasks!"),

    /**
     * [K1060004] 线程池队列溢出
     */
    ASYNC_QUEUE_OVERFLOW(ExceptionTypeEnum.ASYNC_TASK, "0004", "kstry asynchronous task queue overflow!"),

    ///////////////////////////////////////////////// ASYNC_TASK END /////////////////////////////////////////////////
    ;

    ExceptionEnum(ExceptionTypeEnum typeEnum, String code, String desc) {
        this.typeEnum = typeEnum;
        this.code = code;
        this.desc = desc;
    }

    /**
     * typeEnum
     */
    private final ExceptionTypeEnum typeEnum;

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误信息
     */
    private final String desc;

    public ExceptionTypeEnum getTypeEnum() {
        return typeEnum;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getExceptionCode() {
        return "K" + (100 + getTypeEnum().getType()) + getCode();
    }
}
