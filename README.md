
```
public enum ComponentTypeEnum {

    /**
     * 全局
     */
    GLOBAL(1),

    /**
     * 任务，负责处理真正的业务请求
     * <p>
     * 一个角色可以有多个任务，但是一个任务只能出现在一个唯一角色中，一个角色至少有一个任务
     */
    TASK(2),

    /**
     * 任务之间结果转换，基于 TASK 存在
     * <p>
     * 参数：ResultAdapterRequest 或者其子类
     * 返回值：必须实现 TaskPipelinePort 接口，且 result 不允许填充，taskRequest 不能为空
     */
    MAPPING(3),

    /**
     * 操作者，从自己角色派生而来，负责执行 Task
     */
    OPERATOR(4),

    /**
     * 时间段，负责执行一个时间片段内的任务，时间段不能嵌套，只能一个执行完之后再执行另一个 TASK 或者另一个时间段
     * <p>
     * 至少包含一个 TASK，可以包含且执行多个。一个 TIME_SLOT 内的 TASK 独立于非该时间段内 TASK 队列之外，比如可以单独开启本时间段内的事务
     */
    TIME_SLOT(5),

    /**
     * 一个用户故事，与用户的一次请求相对应
     */
    STORY(6),

    /**
     * 组
     */
    GROUP(7);
}
```
