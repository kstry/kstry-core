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
package cn.kstry.framework.core.bus;

import cn.kstry.framework.core.config.GlobalConstant;
import cn.kstry.framework.core.config.RequestMappingGroup;
import cn.kstry.framework.core.engine.timeslot.TimeSlotEventNode;
import cn.kstry.framework.core.engine.timeslot.TimeSlotTaskResponse;
import cn.kstry.framework.core.engine.timeslot.TimeSlotTaskResultWrapper;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.enums.TimeSlotTaskStatusEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.facade.NoticeBusTaskResponse;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.route.TaskRouter;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.NoticeBusDataUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class StoryBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoryBus.class);

    /**
     * 路由器
     */
    private TaskRouter router;

    /**
     * 承载 请求入参 全局流转的参数-Key ( 整个Story共享 )
     */
    public static final TaskNode DEFAULT_GLOBAL_BUS_REQUEST_KEY = new TaskNode("BASE", "DEFAULT_GLOBAL_BUS_REQUEST_KEY", ComponentTypeEnum.GROUP);

    /**
     * 承载 全局不可变量 全局流转的参数-Key （ 分支进行复制 ）
     */
    public static final TaskNode DEFAULT_GLOBAL_BUS_STABLE_KEY = new TaskNode("BASE", "DEFAULT_GLOBAL_BUS_STABLE_KEY", ComponentTypeEnum.GROUP);

    /**
     * 承载 全局可变量 全局流转的参数-Key （ 分支进行复制 ）
     */
    public static final TaskNode DEFAULT_GLOBAL_BUS_VARIABLE_KEY = new TaskNode("BASE", "DEFAULT_GLOBAL_BUS_VARIABLE_KEY", ComponentTypeEnum.GROUP);

    /**
     * 承载 timeSlot 任务触发的结果集 ( 整个Story共享 )
     */
    public static final TaskNode TIMESLOT_TASK_RESULT_WRAPPER_KEY = new TaskNode("BASE", "TIMESLOT_TASK_RESULT_WRAPPER_KEY", ComponentTypeEnum.GROUP);

    /**
     * 全局流转的参数
     */
    private final Map<String, Object> globalParamAndResult;

    public StoryBus(Map<String, Object> globalParamAndResult) {
        if (globalParamAndResult == null) {
            globalParamAndResult = new ConcurrentHashMap<>();
        }
        this.globalParamAndResult = globalParamAndResult;
    }

    public StoryBus(Object request, BusDataBox stableDataBox, BusDataBox variableDataBox) {
        this(null);

        // 初始化全局参数
        this.globalParamAndResult.put(DEFAULT_GLOBAL_BUS_REQUEST_KEY.identity(), (request == null) ? new DefaultDataBox() : request);
        this.globalParamAndResult.put(DEFAULT_GLOBAL_BUS_STABLE_KEY.identity(), (stableDataBox == null) ? new DefaultDataBox() : stableDataBox);
        this.globalParamAndResult.put(DEFAULT_GLOBAL_BUS_VARIABLE_KEY.identity(), (variableDataBox == null) ? new DefaultDataBox() : variableDataBox);
        this.globalParamAndResult.put(TIMESLOT_TASK_RESULT_WRAPPER_KEY.identity(), new ConcurrentHashMap<>());
    }

    public void setRouter(TaskRouter router) {
        AssertUtil.notNull(router);
        AssertUtil.isNull(this.router);
        this.router = router;
    }

    @SuppressWarnings("ConstantConditions")
    public void saveTaskResult(TaskNode taskNode, Object taskResponse) {
        AssertUtil.notNull(taskNode);
        if (taskResponse == null) {
            return;
        }

        AssertUtil.isTrue(taskResponse instanceof TaskResponse);
        AssertUtil.isTrue(((TaskResponse<?>) taskResponse).isSuccess());
        if (taskResponse instanceof TimeSlotTaskResponse) {
            AssertUtil.isTrue(taskNode.getEventNode() instanceof TimeSlotEventNode);
            saveTimeSlotTaskResult(taskResponse);
            return;
        }

        Object result = ((TaskResponse<?>) taskResponse).getResult();
        if (result == null) {
            return;
        }

        globalParamAndResult.put(taskNode.identity(), result);
        Map<Class<?>, List<NoticeBusDataUtil.NoticeFieldItem>> noticeFieldMap = NoticeBusDataUtil.getNoticeFieldMap(result);
        if (MapUtils.isNotEmpty(noticeFieldMap)) {
            taskResponse = NoticeBusDataUtil.transferNoticeBusTaskResponse(taskResponse, noticeFieldMap);
        }

        if (taskResponse instanceof NoticeBusTaskResponse) {
            NoticeBusTaskResponse<?> noticeBusTaskResponse = (NoticeBusTaskResponse<?>) taskResponse;
            noticeBusDataChange(noticeBusTaskResponse);
        }
    }

    @SuppressWarnings("unchecked")
    public void saveTimeSlotTaskResult(Object response) {
        if (!(response instanceof TimeSlotTaskResponse)) {
            return;
        }

        Map<String, TimeSlotTaskResultWrapper> timeSlotTaskResultWrapperMap =
                GlobalUtil.transferNotEmpty(globalParamAndResult.get(TIMESLOT_TASK_RESULT_WRAPPER_KEY.identity()), Map.class);
        AssertUtil.notNull(timeSlotTaskResultWrapperMap);
        TimeSlotTaskResponse taskResponse = (TimeSlotTaskResponse) response;
        AssertUtil.notBlank(taskResponse.getStrategyName());
        TimeSlotTaskResultWrapper resultWrapper = new TimeSlotTaskResultWrapper();
        if (!taskResponse.isSuccess()) {
            resultWrapper.setTaskStatusEnum(TimeSlotTaskStatusEnum.ERROR);
            timeSlotTaskResultWrapperMap.put(taskResponse.getStrategyName(), resultWrapper);
            return;
        }

        if (taskResponse.getFutureTask() == null) {
            resultWrapper.setTaskStatusEnum(TimeSlotTaskStatusEnum.SUCCESS);
            globalParamAndResult.put(GlobalConstant.TIME_SLOT_NODE_SIGN + taskResponse.getStrategyName(), taskResponse.getResult());
            timeSlotTaskResultWrapperMap.put(taskResponse.getStrategyName(), resultWrapper);
            return;
        }

        resultWrapper.setTimeout(taskResponse.getTimeout());
        resultWrapper.setStrategyName(taskResponse.getStrategyName());
        resultWrapper.setFutureTask(taskResponse.getFutureTask());
        resultWrapper.setTaskStatusEnum(TimeSlotTaskStatusEnum.DOING);
        timeSlotTaskResultWrapperMap.put(taskResponse.getStrategyName(), resultWrapper);
    }

    private void noticeBusDataChange(NoticeBusTaskResponse<?> noticeBusTaskResponse) {
        if (noticeBusTaskResponse == null) {
            return;
        }
        if (MapUtils.isNotEmpty(noticeBusTaskResponse.getVariableBusDataMap())) {
            Object variableData = GlobalUtil.notNull(this.globalParamAndResult.get(DEFAULT_GLOBAL_BUS_VARIABLE_KEY.identity()));
            noticeBusTaskResponse.getVariableBusDataMap().forEach((k, v) -> GlobalUtil.setProperty(variableData, k, v));
        }
        if (MapUtils.isNotEmpty(noticeBusTaskResponse.getStableBusDataMap())) {
            Object stableData = GlobalUtil.notNull(this.globalParamAndResult.get(DEFAULT_GLOBAL_BUS_STABLE_KEY.identity()));
            noticeBusTaskResponse.getStableBusDataMap().forEach((k, v) -> {
                Optional<Object> property = GlobalUtil.getProperty(stableData, k);
                if (property.isPresent() && property.get() != GlobalUtil.GET_PROPERTY_ERROR_SIGN) {
                    LOGGER.warn("[{}] Existing values in the immutable union are not allowed to be set repeatedly! k:{}, oldV:{}, newV:{}",
                            ExceptionEnum.IMMUTABLE_SET_UPDATE.getExceptionCode(), k, JSON.toJSONString(property.get()), JSON.toJSONString(v));
                    return;
                }
                GlobalUtil.setProperty(stableData, k, v);
            });
        }
    }

    public Object getResultByTaskNode(TaskNode taskNode) {
        AssertUtil.notNull(taskNode);
        return globalParamAndResult.get(taskNode.identity());
    }

    public TaskRouter getRouter() {
        return router;
    }

    public void loadTaskRequest(Object innerTaskRequest, RequestMappingGroup requestMappingGroup) {
        if (innerTaskRequest == null || requestMappingGroup == null || CollectionUtils.isEmpty(requestMappingGroup.getMappingItemList())) {
            return;
        }
        requestMappingGroup.getMappingItemList().forEach(mappingItem -> {
            String source = mappingItem.getSource();
            AssertUtil.notBlank(source);
            String[] fieldNameArray = source.split("\\.");
            AssertUtil.isTrue(fieldNameArray.length > 1, ExceptionEnum.PARAMS_ERROR);

            if (!tryGetTimeSlotResult(source)) {
                return;
            }
            Object value = GlobalUtil.getProperty(this.globalParamAndResult, source).orElse(null);
            if (value == GlobalUtil.GET_PROPERTY_ERROR_SIGN) {
                return;
            }
            GlobalUtil.setProperty(innerTaskRequest, mappingItem.getTarget(), value);
        });
    }

    @SuppressWarnings("unchecked")
    private boolean tryGetTimeSlotResult(String source) {
        if (StringUtils.isBlank(source) || !source.startsWith(GlobalConstant.TIME_SLOT_NODE_SIGN)) {
            return true;
        }

        Map<String, Object> resultMap = this.globalParamAndResult;
        for (String timeSlotTaskKey : source.split("\\.")) {
            if (!timeSlotTaskKey.startsWith(GlobalConstant.TIME_SLOT_NODE_SIGN)) {
                break;
            }
            if (!doGetTimeSlotResult(timeSlotTaskKey.replace(GlobalConstant.TIME_SLOT_NODE_SIGN, StringUtils.EMPTY), resultMap)) {
                return false;
            }
            resultMap = GlobalUtil.transferNotEmpty(resultMap.get(timeSlotTaskKey), Map.class);
            AssertUtil.notEmpty(resultMap);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean doGetTimeSlotResult(String timeSlotTaskKey, Map<String, Object> resultMap) {

        AssertUtil.notBlank(timeSlotTaskKey);
        AssertUtil.notNull(resultMap);
        Map<String, TimeSlotTaskResultWrapper> timeSlotTaskResultWrapperMap =
                GlobalUtil.transferNotEmpty(resultMap.get(TIMESLOT_TASK_RESULT_WRAPPER_KEY.identity()), Map.class);
        AssertUtil.notEmpty(timeSlotTaskResultWrapperMap);
        TimeSlotTaskResultWrapper resultWrapper = timeSlotTaskResultWrapperMap.get(timeSlotTaskKey);
        if (resultWrapper == null || resultWrapper.getTaskStatusEnum() == TimeSlotTaskStatusEnum.ERROR) {
            return false;
        }

        if (resultWrapper.getTaskStatusEnum() == TimeSlotTaskStatusEnum.SUCCESS) {
            return true;
        }

        AssertUtil.isTrue(resultWrapper.getTaskStatusEnum() == TimeSlotTaskStatusEnum.DOING);
        AssertUtil.notNull(resultWrapper.getFutureTask());
        synchronized (TIMESLOT_TASK_RESULT_WRAPPER_KEY) {
            if (resultWrapper.getTaskStatusEnum() == TimeSlotTaskStatusEnum.ERROR) {
                return false;
            }

            if (resultWrapper.getTaskStatusEnum() == TimeSlotTaskStatusEnum.SUCCESS) {
                return true;
            }
            TaskResponse<Map<String, Object>> mapTaskResponse;
            try {
                mapTaskResponse = resultWrapper.getFutureTask().get(resultWrapper.getTimeout(), TimeUnit.MILLISECONDS);
                LOGGER.debug("Get asynchronous task results normally! strategyName:{} result success:{}", resultWrapper.getStrategyName(), mapTaskResponse.isSuccess());
            } catch (Exception e) {
                resultWrapper.getFutureTask().cancel(true);
                LOGGER.warn("Cancel timeSlot's asynchronous task because of timeout! strategyName:{}, config timeout:{}", resultWrapper.getStrategyName(),
                        resultWrapper.getTimeout());
                resultWrapper.setFutureTask(null);
                resultWrapper.setTaskStatusEnum(TimeSlotTaskStatusEnum.ERROR);
                return false;
            }

            if (!mapTaskResponse.isSuccess() || mapTaskResponse.getResult() == null) {
                resultWrapper.setFutureTask(null);
                resultWrapper.setTaskStatusEnum(TimeSlotTaskStatusEnum.ERROR);
                return false;
            }

            resultMap.put(GlobalConstant.TIME_SLOT_NODE_SIGN + resultWrapper.getStrategyName(), mapTaskResponse.getResult());
            resultWrapper.setFutureTask(null);
            resultWrapper.setTaskStatusEnum(TimeSlotTaskStatusEnum.SUCCESS);
        }
        return true;
    }

    public Optional<Object> getGlobalParamValue(String fieldName) {
        if (StringUtils.isBlank(fieldName)) {
            return Optional.empty();
        }
        if (!tryGetTimeSlotResult(fieldName)) {
            return Optional.empty();
        }
        return GlobalUtil.getProperty(this.globalParamAndResult, fieldName).filter(obj -> obj != GlobalUtil.GET_PROPERTY_ERROR_SIGN);
    }

    public StoryBus cloneStoryBus(boolean isAsync) {
        // 同步执行时，数据与主流程共享
        if (!isAsync) {
            return new StoryBus(this.globalParamAndResult);
        }

        // 请求入参 分支流程与主流程共享
        // timeSlot 异步任务结果集共享
        StoryBus cloneStoryBus = new StoryBus(null);
        cloneStoryBus.globalParamAndResult.putAll(this.globalParamAndResult);
        try {
            // 在分支流程出现时开始，复制当下的 不可变更数据
            Object stableObj = GlobalUtil.notNull(this.globalParamAndResult.get(DEFAULT_GLOBAL_BUS_STABLE_KEY.identity()));
            AssertUtil.isTrue(stableObj instanceof BusDataBox);
            BusDataBox stableData = (BusDataBox) BeanUtils.cloneBean(stableObj);
            cloneStoryBus.globalParamAndResult.put(DEFAULT_GLOBAL_BUS_STABLE_KEY.identity(), stableData);

            // 在分支流程出现时开始，复制当下的 可变更数据
            Object variableObj = GlobalUtil.notNull(this.globalParamAndResult.get(DEFAULT_GLOBAL_BUS_VARIABLE_KEY.identity()));
            AssertUtil.isTrue(variableObj instanceof BusDataBox);
            BusDataBox variableData = (BusDataBox) BeanUtils.cloneBean(variableObj);
            cloneStoryBus.globalParamAndResult.put(DEFAULT_GLOBAL_BUS_VARIABLE_KEY.identity(), variableData);
        } catch (Exception e) {
            KstryException.throwException(e);
        }
        return cloneStoryBus;
    }

    public void loadResultData(Map<String, Object> resultMap) {
        AssertUtil.notNull(resultMap);
        resultMap.putAll(this.globalParamAndResult);
    }
}
