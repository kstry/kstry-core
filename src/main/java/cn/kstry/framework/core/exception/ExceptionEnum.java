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

import cn.kstry.framework.core.enums.KstryTypeEnum;

/**
 * 错误枚举
 *
 * @author lykan
 */
public enum ExceptionEnum {

    /**
     * [K1010001] 未捕获异常
     */
    SYSTEM_ERROR(KstryTypeEnum.GLOBAL, "0001", "System error!"),

    /**
     * [K1010002] 不允许为空
     */
    NOT_ALLOW_EMPTY(KstryTypeEnum.GLOBAL, "0002", "The object is not allowed to be empty!"),

    /**
     * [K1010003] 集合不允许为空
     */
    COLLECTION_NOT_ALLOW_EMPTY(KstryTypeEnum.GLOBAL, "0003", "The collection is not allowed to be empty!"),

    /**
     * [K1010004] 框架执行中，不被允许的参数出现
     */
    INVALID_SYSTEM_PARAMS(KstryTypeEnum.GLOBAL, "0004", "Invalid system parameters are present!"),

    /**
     * [K1010005] 必要的方法未实现
     */
    NEED_CONFIRM_NECESSARY_METHODS(KstryTypeEnum.GLOBAL, "0005", "Necessary methods not achieved!"),

    /**
     * [K1010006] 类型转化异常
     */
    TYPE_TRANSFER_ERROR(KstryTypeEnum.GLOBAL, "0006", "Type conversion anomaly!"),

    /**
     * [K1010007] 同时只允许一个 Task 被执行，Router 定位出多个时非法
     */
    MUST_ONE_TASK_ACTION(KstryTypeEnum.GLOBAL, "0007", "Only one task is allowed to be executed!"),

    /**
     * [K1010008] 配置文件解析失败
     */
    CONFIGURATION_PARSE_FAILURE(KstryTypeEnum.GLOBAL, "0008", "Configuration file parsing failure!"),

    /**
     * [K1010009] 参数错误
     */
    PARAMS_ERROR(KstryTypeEnum.GLOBAL, "0009", "params error!"),

    /**
     * [K1010010] 必须为空
     */
    OBJ_MUST_EMPTY(KstryTypeEnum.GLOBAL, "0010", "Objects are not allowed to exist!"),

    /**
     * [K1010011] 返回结果错误
     */
    RESPONSE_ERROR(KstryTypeEnum.GLOBAL, "0011", "response error!"),

    ///////////////////////////////////////////////// Global END /////////////////////////////////////////////////

    /**
     *  [K1020001] Task 被重复定义
     */
    TASK_IDENTIFY_DUPLICATE_DEFINITION(KstryTypeEnum.TASK, "0001", "event group duplication of definitions is not allowed!"),

    /**
     *  [K1020002] Task 中不允许出现方法重载
     */
    NOT_ALLOWED_OVERLOADS(KstryTypeEnum.TASK, "0002", "Method overloads are not allowed in Task!"),

    /**
     * [K1020003] Task 获取 bean 属性信息失败
     */
    FAILED_GET_PROPERTY(KstryTypeEnum.TASK, "0003", "BeanUtils failed to get bean property!"),

    /**
     * [K1020004] Task 设置 bean 属性信息失败
     */
    FAILED_SET_PROPERTY(KstryTypeEnum.TASK, "0004", "BeanUtils failed to set bean property!"),

    /**
     * [K1020005] Task 执行结果，必须是 TaskResponse 或者其子类，或者 null
     */
    TASK_RESULT_TYPE_ERROR(KstryTypeEnum.TASK, "0005", "The result of Task execution must be TaskResponse or its subclass, or null!"),

    /**
     * [K1020006] 不可变集合中已存在的值不允许被重复设置
     */
    IMMUTABLE_SET_UPDATE(KstryTypeEnum.TASK, "0006", "Values that already exist in the immutable set are not allowed to be set repeatedly!"),

    /**
     * [K1020007] 当前线程被中断
     */
    TASK_INTERRUPTED_ERROR(KstryTypeEnum.TASK, "0007", "The current thread is interrupted!"),

    /**
     * [K1020008] 策略规则名称错误
     */
    STRATEGY_RULE_NAME_ERROR(KstryTypeEnum.TASK, "0008", "Strategy policy rule name!"),

    ///////////////////////////////////////////////// Task END /////////////////////////////////////////////////

    /**
     * [K1030001] time slot 执行期间发生错误
     */
    TIME_SLOT_EXECUTION_ERROR(KstryTypeEnum.TIME_SLOT, "0001", "Time slot task An error occurred during execution!"),

    /**
     * [K1030002] TimeSlotThreadPoolExecutor 只能在容器中出现一次
     */
    THREAD_POOL_COUNT_ERROR(KstryTypeEnum.TIME_SLOT, "0002", "Only one TimeSlotThreadPoolExecutor can exist in the container!"),

    /**
     * [K1030003] time slot 线程池已满，任务被丢弃
     */
    CONTAINER_QUEUE_FULL_ERROR(KstryTypeEnum.TIME_SLOT, "0003", "Thread pool container queue is full, discard task!"),

    /**
     * [K1030004] time slot 中出现未定义的异常
     */
    TIME_SLOT_SYSTEM_ERROR(KstryTypeEnum.TIME_SLOT, "0004", "Undefined exception in time slot!"),


    ///////////////////////////////////////////////// TimeSlot END /////////////////////////////////////////////////

    /**
     * [K1040001] story name 非法
     */
    STORY_NAME_NOT_VALID(KstryTypeEnum.STORY, "0001", "Story name not valid!"),

    ///////////////////////////////////////////////// Story END /////////////////////////////////////////////////
    ;

    ExceptionEnum(KstryTypeEnum typeEnum, String code, String desc) {
        this.typeEnum = typeEnum;
        this.code = code;
        this.desc = desc;
    }

    /**
     * 发生错误的组件类型
     */
    private final KstryTypeEnum typeEnum;

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误信息
     */
    private final String desc;

    public KstryTypeEnum getTypeEnum() {
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
