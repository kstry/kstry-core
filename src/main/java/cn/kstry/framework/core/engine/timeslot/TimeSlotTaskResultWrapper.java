package cn.kstry.framework.core.engine.timeslot;

import cn.kstry.framework.core.enums.TimeSlotTaskStatusEnum;
import cn.kstry.framework.core.facade.TaskResponse;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * TimeSlotTask 结果的封装类
 *
 * @author lykan
 */
public class TimeSlotTaskResultWrapper {

    /**
     * 超时时间
     */
    private long timeout;

    /**
     * time slot 执行 story 的 策略名称
     */
    private String strategyName;

    /**
     * 异步任务执行结果
     */
    private Future<TaskResponse<Map<String, Object>>> futureTask;

    /**
     * 任务状态
     */
    private TimeSlotTaskStatusEnum taskStatusEnum;

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public Future<TaskResponse<Map<String, Object>>> getFutureTask() {
        return futureTask;
    }

    public void setFutureTask(Future<TaskResponse<Map<String, Object>>> futureTask) {
        this.futureTask = futureTask;
    }

    public TimeSlotTaskStatusEnum getTaskStatusEnum() {
        return taskStatusEnum;
    }

    public void setTaskStatusEnum(TimeSlotTaskStatusEnum taskStatusEnum) {
        this.taskStatusEnum = taskStatusEnum;
    }
}
