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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.adapter.ResultAdapterRequest;
import cn.kstry.framework.core.adapter.ResultMappingRepository;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.engine.EventGroup;
import cn.kstry.framework.core.engine.TaskActionMethod;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.enums.StrategyTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.facade.DynamicRouteTable;
import cn.kstry.framework.core.facade.RouteMapResponse;
import cn.kstry.framework.core.facade.TaskPipelinePort;
import cn.kstry.framework.core.facade.TaskRequest;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.operator.EventOperatorRole;
import cn.kstry.framework.core.operator.TaskOperatorCreator;
import cn.kstry.framework.core.route.EventNode;
import cn.kstry.framework.core.route.RouteEventGroup;
import cn.kstry.framework.core.route.StrategyRule;
import cn.kstry.framework.core.route.TaskNode;
import cn.kstry.framework.core.route.TaskRouter;
import cn.kstry.framework.core.timeslot.TimeSlotInvokeRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * task action util
 *
 * @author lykan
 */
public class TaskActionUtil {

    /**
     * 将 Task 执行的结果保存在 globalBus 中
     */
    public static void saveTaskResultToGlobalBus(StoryBus storyBus, TaskNode taskNode, TaskResponse<Object> o) {
        ComponentTypeEnum componentEnum = taskNode.getEventGroupTypeEnum();
        if (componentEnum == ComponentTypeEnum.TIME_SLOT) {
            return;
        }

        if (o instanceof RouteMapResponse) {
            RouteMapResponse<?> routeMapResponse = GlobalUtil.transferNotEmpty(o, RouteMapResponse.class);
            DynamicRouteTable taskRouterTable = routeMapResponse.getTaskRouterTable();
            updateDynamicRouteTable(storyBus, taskRouterTable);
        }

        if (componentEnum == ComponentTypeEnum.MAPPING) {
            AssertUtil.isTrue(o instanceof TaskPipelinePort, ExceptionEnum.MAPPING_RESULT_ERROR);
            TaskRequest taskRequest = GlobalUtil.transferNotEmpty(o, TaskPipelinePort.class).getTaskRequest();
            AssertUtil.notNull(taskRequest, ExceptionEnum.MAPPING_RESULT_ERROR);
            AssertUtil.isNull(GlobalUtil.transferNotEmpty(o, TaskPipelinePort.class).getResult(), ExceptionEnum.MAPPING_RESULT_ERROR);
            storyBus.addMappingResult(taskNode, taskRequest);
            return;
        }
        if (o == null) {
            return;
        }
        storyBus.addNodeResult(taskNode, o);
    }

    /**
     * 获取下一个 task 请求的 request
     */
    public static Object getNextRequest(Object taskRequest, TaskRouter router, ResultMappingRepository resultMappingRepository, StoryBus storyBus, List<EventGroup> taskGroup) throws InstantiationException,
            IllegalAccessException {
        TaskNode taskNode = GlobalUtil.notEmpty(router.currentRouteNode());
        Class<? extends TaskRequest> currentRequestClass = taskNode.getRequestClass();
        if (currentRequestClass == null) {
            return null;
        }

        if (taskNode.getEventGroupTypeEnum() == ComponentTypeEnum.TIME_SLOT) {
            TimeSlotInvokeRequest timeSlotInvokeRequest = new TimeSlotInvokeRequest();
            timeSlotInvokeRequest.setStoryBus(storyBus);
            timeSlotInvokeRequest.setTaskGroup(taskGroup);
            timeSlotInvokeRequest.setResultMappingRepository(resultMappingRepository);
            return timeSlotInvokeRequest;
        }

        TaskRequest nextRequestInstance = currentRequestClass.newInstance();
        if (nextRequestInstance instanceof ResultAdapterRequest) {
            AssertUtil.isTrue(taskNode.getEventGroupTypeEnum() == ComponentTypeEnum.MAPPING, ExceptionEnum.MAPPING_REQUEST_ERROR);
            ResultAdapterRequest adapterRequest = (ResultAdapterRequest) nextRequestInstance;
            adapterRequest.setStoryBus(storyBus);
            adapterRequest.setTaskGroup(taskGroup);
            adapterRequest.setRouter(router);
            adapterRequest.setResultMappingRepository(resultMappingRepository);
            fillResultInfo(taskRequest, adapterRequest);
            return adapterRequest;
        } else {
            AssertUtil.isTrue(taskNode.getEventGroupTypeEnum() != ComponentTypeEnum.MAPPING, ExceptionEnum.MAPPING_REQUEST_ERROR);
        }

        // MAPPING 返回的结果直接被赋值给下一个任务
        Optional<TaskNode> beforeInvokeRouteNodeOptional = router.beforeInvokeRouteNodeIgnoreTimeSlot();
        if (beforeInvokeRouteNodeOptional.isPresent() && beforeInvokeRouteNodeOptional.get().getEventGroupTypeEnum() == ComponentTypeEnum.MAPPING) {
            return storyBus.getRequestByRouteNode(beforeInvokeRouteNodeOptional.get());
        }

        // 非 MAPPING 返回的结果进行参数拷贝后赋值，并且只能拿到 request 中的内容，无法获取 TaskResponse 的返回结果
        AssertUtil.isTrue(taskRequest instanceof TaskPipelinePort, ExceptionEnum.REQUEST_NOT_MATCHED);
        TaskRequest pipelineTaskRequest = GlobalUtil.transferNotEmpty(taskRequest, TaskPipelinePort.class).getTaskRequest();
        if (pipelineTaskRequest == null) {
            return null;
        }

        BeanUtils.copyProperties(pipelineTaskRequest, nextRequestInstance);
        return nextRequestInstance;
    }

    /**
     * 获取执行 task 的 operator，该 operator 一定是从 role 中派生而来
     */
    public static EventOperatorRole getTaskActionOperator(TaskRouter router, List<EventGroup> taskGroup) {
        EventGroup eventActionGroup = TaskActionUtil.getNextTaskAction(taskGroup, router);
        EventOperatorRole actionOperator = ((RouteEventGroup) eventActionGroup).getTaskActionOperator();
        if (actionOperator == null) {
            actionOperator = TaskOperatorCreator.getTaskOperator(eventActionGroup);
        }
        return actionOperator;
    }

    /**
     * 执行目标方法
     *
     * @param taskRequest    task request
     * @param taskNode       route node
     * @param actionOperator action operator
     * @return target result
     */
    public static Object invokeTarget(Object taskRequest, TaskNode taskNode, EventOperatorRole actionOperator) throws NoSuchMethodException {
        Object o;
        if (taskNode.getRequestClass() == null) {
            Method method = actionOperator.getClass().getMethod(taskNode.getActionName());
            o = ReflectionUtils.invokeMethod(method, actionOperator);
        } else {
            Method method = actionOperator.getClass().getMethod(taskNode.getActionName(), taskNode.getRequestClass());
            o = ReflectionUtils.invokeMethod(method, actionOperator, taskRequest);
        }
        AssertUtil.isTrue(o == null || o instanceof TaskResponse, ExceptionEnum.TASK_RESULT_TYPE_ERROR);
        return o;
    }

    /**
     * 给予重新规划接未执行 Task 线路的机会
     *
     * @param router router
     */
    public static void reRouteNodeMap(TaskRouter router) {
        if (!router.nextRouteNodeIgnoreTimeSlot().isPresent()) {
            return;
        }
        TaskNode taskNode = GlobalUtil.notEmpty(router.currentRouteNode());
        EventNode eventNode = GlobalUtil.notNull(taskNode.getEventNode());
        List<StrategyRule> inflectionPointList = router.nextRouteNodeIgnoreTimeSlot().get().getMatchStrategyRuleList();
        if (CollectionUtils.isEmpty(inflectionPointList) || eventNode.nextEventNodeSize() == 0) {
            return;
        }

        StoryBus storyBus = GlobalUtil.notNull(router.getStoryBus());
        DynamicRouteTable dynamicRouteTable = storyBus.getDynamicRouteTable();
        AssertUtil.notNull(dynamicRouteTable);
        router.reRouteNodeMap(node -> matchStrategyRule(node.getMatchStrategyRuleList(), dynamicRouteTable));

        Optional<TaskNode> nextRouteNode = router.nextRouteNodeIgnoreTimeSlot();
        if (!nextRouteNode.isPresent() || CollectionUtils.isEmpty(nextRouteNode.get().getMatchStrategyRuleList())) {
            return;
        }

        if (nextRouteNode.get().getMatchStrategyRuleList().stream().noneMatch(point -> point.getStrategyTypeEnum() == StrategyTypeEnum.FILTER)) {
            return;
        }

        if (TaskActionUtil.matchStrategyRule(nextRouteNode.get().getMatchStrategyRuleList(), dynamicRouteTable)) {
            return;
        }

        router.skipRouteNode();
        reRouteNodeMap(router);
    }

    public static TaskActionMethod getTaskActionMethod(List<EventGroup> eventGroupList, TaskNode taskNode) {
        AssertUtil.notNull(taskNode);
        AssertUtil.notEmpty(eventGroupList);

        List<EventGroup> collect = eventGroupList.stream().filter(taskAction -> {
            if (StringUtils.isBlank(taskNode.getEventGroupName())) {
                return false;
            }
            if (!Objects.equals(taskAction.getEventGroupName(), taskNode.getEventGroupName())) {
                return false;
            }
            return taskAction.getEventGroupTypeEnum() == taskNode.getEventGroupTypeEnum();
        }).collect(Collectors.toList());
        AssertUtil.oneSize(collect, ExceptionEnum.MUST_ONE_TASK_ACTION, "There is one and only one event_group allowed to be matched! taskNode:%s", taskNode);

        RouteEventGroup routeEventGroup = (RouteEventGroup) collect.get(0);
        TaskActionMethod taskActionMethod = routeEventGroup.getActionMethodMap().get(taskNode.getActionName());
        AssertUtil.notNull(taskActionMethod, ExceptionEnum.MUST_ONE_TASK_ACTION,
                "There is one and only one event_action allowed to be matched! taskNode:%s", taskNode);
        return taskActionMethod;
    }

    public static void throwException(Exception e) {
        Throwable exception = e;
        if (exception instanceof UndeclaredThrowableException) {
            exception = ((UndeclaredThrowableException) exception).getUndeclaredThrowable();
        }
        if (exception instanceof InvocationTargetException) {
            exception = ((InvocationTargetException) exception).getTargetException();
        }
        if (exception instanceof KstryException) {
            throw (KstryException) exception;
        }
        KstryException.throwException(exception);
    }

    public static boolean matchStrategyRule(List<StrategyRule> strategyRuleList, DynamicRouteTable dynamicRouteTable) {

        if (CollectionUtils.isEmpty(strategyRuleList)) {
            return false;
        }

        boolean flag = true;
        try {
            for (StrategyRule strategyRule : strategyRuleList) {
                AssertUtil.notBlank(strategyRule.getFieldName());
                AssertUtil.anyNotNull(strategyRule.getExpectedValue(), strategyRule.getStrategyRuleCalculator());

                Field field = FieldUtils.getField(dynamicRouteTable.getClass(), strategyRule.getFieldName(), true);
                AssertUtil.notNull(field);

                flag = flag && strategyRule.getStrategyRuleCalculator().calculate(field.get(dynamicRouteTable), strategyRule.getExpectedValue());
            }
        } catch (Exception e) {
            KstryException.throwException(e);
        }
        return flag;
    }

    private static void updateDynamicRouteTable(StoryBus storyBus, DynamicRouteTable taskRouterTable) {
        if (taskRouterTable == null) {
            return;
        }

        DynamicRouteTable dynamicRouteTable = storyBus.getDynamicRouteTable();
        if (dynamicRouteTable == null) {
            storyBus.setDynamicRouteTable(taskRouterTable);
            return;
        }

        AssertUtil.equals(dynamicRouteTable.getClass(), taskRouterTable.getClass(), ExceptionEnum.DYNAMIC_ROUTING_TABLES_ERROR);
        try {
            List<Field> allFieldsList = FieldUtils.getAllFieldsList(taskRouterTable.getClass());
            for (Field field : allFieldsList) {
                Object o = FieldUtils.readField(field, taskRouterTable, true);
                if (o == null) {
                    continue;
                }
                FieldUtils.writeField(field, dynamicRouteTable, o, true);
            }
        } catch (Exception e) {
            KstryException.throwException(e);
        }
    }

    /**
     * 从 taskGroup 中根据 Router 路由出需要被执行的 TaskAction
     *
     * @param taskGroup 非空
     * @param router    非空
     * @return 下一个将要被执行的 TaskAction 有且仅有一个
     */
    private static EventGroup getNextTaskAction(List<EventGroup> taskGroup, TaskRouter router) {
        AssertUtil.notEmpty(taskGroup);
        List<EventGroup> eventActionGroupList = taskGroup.stream().filter(s -> s.route(router)).collect(Collectors.toList());
        AssertUtil.oneSize(eventActionGroupList, ExceptionEnum.MUST_ONE_TASK_ACTION);
        return eventActionGroupList.get(0);
    }

    @SuppressWarnings("all")
    private static void fillResultInfo(Object taskRequest, ResultAdapterRequest adapterRequest) {
        if (!(taskRequest instanceof TaskResponse)) {
            return;
        }
        Optional<TaskResponse> taskResponseOptional = GlobalUtil.transfer(taskRequest, TaskResponse.class);
        if (!taskResponseOptional.isPresent() || !taskResponseOptional.get().isSuccess()) {
            return;
        }
        adapterRequest.setResult(taskResponseOptional.get().getResult());
        adapterRequest.resultSuccess();
    }
}
