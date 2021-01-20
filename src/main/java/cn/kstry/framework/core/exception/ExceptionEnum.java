/*
 *
 *  *  Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
 *  *  <p>
 *  *  Licensed under the GNU Lesser General Public License 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *  <p>
 *  * https://www.gnu.org/licenses/lgpl.html
 *  *  <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.core.exception;

import cn.kstry.framework.core.enums.ComponentTypeEnum;

/**
 * 错误枚举
 *
 * @author lykan
 */
public enum ExceptionEnum {

    /**
     * 同时只允许一个 Task 被执行，Router 定位出多个时非法
     */
    MUST_ONE_TASK_ACTION(ComponentTypeEnum.GLOBAL, "0001", "Only one task is allowed to be executed!"),

    /**
     * 不允许为空
     */
    NOT_ALLOW_EMPTY(ComponentTypeEnum.GLOBAL, "0002", "The object is not allowed to be empty!"),

    /**
     * 集合不允许为空
     */
    COLLECTION_NOT_ALLOW_EMPTY(ComponentTypeEnum.GLOBAL, "0003", "The collection is not allowed to be empty!"),

    /**
     * 框架执行中，不被允许的参数出现
     */
    INVALID_SYSTEM_PARAMS(ComponentTypeEnum.GLOBAL, "0004", "Invalid system parameters are present!"),

    /**
     * 必要的方法未实现
     */
    NEED_CONFIRM_NECESSARY_METHODS(ComponentTypeEnum.GLOBAL, "0005", "Necessary methods not achieved!"),

    /**
     * 类型转化异常
     */
    TYPE_TRANSFER_ERROR(ComponentTypeEnum.GLOBAL, "0006", "Type conversion anomaly!"),

    /**
     * 未捕获异常
     */
    SYSTEM_ERROR(ComponentTypeEnum.GLOBAL, "0007", "System error!"),

    /**
     * 配置文件解析失败
     */
    CONFIGURATION_PARSE_FAILURE(ComponentTypeEnum.GLOBAL, "0008", "Configuration file parsing failure!"),

    /**
     * 参数错误
     */
    PARAMS_ERROR(ComponentTypeEnum.GLOBAL, "0009", "params error!"),

    /**
     * 必须为空
     */
    OBJ_MUST_EMPTY(ComponentTypeEnum.GLOBAL, "0010", "Objects are not allowed to exist!"),

    /**
     * 上一个任务的返回值需匹配下一个任务的入参
     */
    REQUEST_NOT_MATCHED(ComponentTypeEnum.TASK, "0001", "The return value type cannot be matched with the next task's request!"),

    /**
     * 动态路由表只能指定一个类
     */
    DYNAMIC_ROUTING_TABLES_ERROR(ComponentTypeEnum.TASK, "0002", "Dynamic routing tables can only specify one class!"),

    /**
     * Task 被重复定义
     */
    TASK_IDENTIFY_DUPLICATE_DEFINITION(ComponentTypeEnum.TASK, "0003", "event group duplication of definitions is not allowed!"),

    /**
     * Task 中不允许出现方法重载
     */
    NOT_ALLOWED_OVERLOADS(ComponentTypeEnum.TASK, "0004", "Method overloads are not allowed in Task!"),

    /**
     * TIME_SLOT 只能执行完一个之后再执行另一个，不能叠加出现
     */
    TIME_SLOT_SUPERIMPOSED_EXECUTED(ComponentTypeEnum.TIME_SLOT, "0001", "TIME_SLOT cannot be superimposed to be executed!"),

    /**
     * 无效的定位行为
     */
    INVALID_POSITIONING_BEHAVIOR(ComponentTypeEnum.STORY, "0001", "Invalid positioning behavior!"),

    /**
     * MAPPING Task 的 request 必须是 ResultAdapterRequest 或者是其子类，反之亦然，
     * ResultAdapterRequest 其本身或者子类只能出现在 MAPPING Task 中
     */
    MAPPING_REQUEST_ERROR(ComponentTypeEnum.MAPPING, "0001", "The request parameter of MAPPING Task must be a subclass of ResultAdapterRequest. and vice versa!"),

    /**
     * MAPPING Task 结果为空或者类型错误
     */
    MAPPING_RESULT_ERROR(ComponentTypeEnum.MAPPING, "0002", "MAPPING Task null result or wrong type!"),

    /**
     * Task 执行结果，必须是 TaskResponse 或者其子类，或者 null
     */
    TASK_RESULT_TYPE_ERROR(ComponentTypeEnum.MAPPING, "0003", "The result of Task execution must be TaskResponse or its subclass, or null!"),
    ;

    ExceptionEnum(ComponentTypeEnum typeEnum, String code, String desc) {
        this.typeEnum = typeEnum;
        this.code = code;
        this.desc = desc;
    }

    /**
     * 发生错误的组件类型
     */
    private final ComponentTypeEnum typeEnum;

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误信息
     */
    private final String desc;

    public ComponentTypeEnum getTypeEnum() {
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
