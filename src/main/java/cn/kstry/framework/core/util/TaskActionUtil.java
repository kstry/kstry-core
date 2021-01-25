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

import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.bus.TaskNode;
import cn.kstry.framework.core.config.TaskActionMethod;
import cn.kstry.framework.core.engine.timeslot.TimeSlotEventNode;
import cn.kstry.framework.core.engine.timeslot.TimeSlotInvokeRequest;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.operator.EventOperatorRole;
import cn.kstry.framework.core.operator.TaskOperatorCreator;
import cn.kstry.framework.core.route.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskActionUtil.class);

    /**
     * 获取下一个 task 请求的 request
     */
    public static Object getNextRequest(TaskRouter router, StoryBus storyBus, List<EventGroup> taskGroup) throws IllegalAccessException, InstantiationException {

        TaskNode taskNode = GlobalUtil.notEmpty(router.currentTaskNode());
        if (taskNode.getEventNode() instanceof TimeSlotEventNode) {
            StoryBus timeSlotStoryBus = storyBus.cloneStoryBus();
            TimeSlotEventNode currentEventNode = (TimeSlotEventNode) taskNode.getEventNode();
            List<EventNode> timeSlotFirstEventNodeList = currentEventNode.getFirstTimeSlotEventNodeList();
            AssertUtil.notEmpty(timeSlotFirstEventNodeList);
            EventNode timeSlotEventNode = TaskActionUtil.locateInvokeEventNode(timeSlotStoryBus, timeSlotFirstEventNodeList);
            TaskRouter timeSlotTaskRouter = new TaskRouter(timeSlotEventNode, timeSlotStoryBus);
            TimeSlotInvokeRequest timeSlotInvokeRequest = new TimeSlotInvokeRequest();
            timeSlotInvokeRequest.setTaskGroup(taskGroup);
            timeSlotInvokeRequest.setStoryBus(timeSlotStoryBus);
            timeSlotInvokeRequest.setTaskRouter(timeSlotTaskRouter);
            timeSlotInvokeRequest.setStrategyName(currentEventNode.getStrategyName());
            timeSlotInvokeRequest.setAsync(currentEventNode.isAsync());
            timeSlotInvokeRequest.setTimeout(currentEventNode.getTimeout());
            return timeSlotInvokeRequest;
        }

        Class<?> currentRequestClass = taskNode.getRequestClass();
        if (currentRequestClass == null) {
            return null;
        }

        EventNode eventNode = GlobalUtil.notNull(taskNode.getEventNode());
        Object innerTaskRequest = currentRequestClass.newInstance();
        storyBus.loadTaskRequest(innerTaskRequest, eventNode.getRequestMappingGroup());
        return innerTaskRequest;
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

    public static boolean matchStrategyRule(List<StrategyRule> strategyRuleList, StoryBus storyBus) {
        AssertUtil.notNull(storyBus);
        if (CollectionUtils.isEmpty(strategyRuleList)) {
            return true;
        }

        return strategyRuleList.stream().allMatch(rule -> {
            AssertUtil.notBlank(rule.getFieldName());

            StrategyRuleCalculator ruleCalculator = rule.getStrategyRuleCalculator();
            AssertUtil.notNull(ruleCalculator);
            AssertUtil.isTrue(ruleCalculator.checkExpected(rule.getExpectedValue()), ExceptionEnum.PARAMS_ERROR);
            Optional<Object> valueOptional = storyBus.getGlobalParamValue(rule.getFieldName());
            return ruleCalculator.calculate(valueOptional.orElse(null), rule.getExpectedValue());
        });
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

    public static EventNode locateInvokeEventNode(StoryBus storyBus, List<EventNode> eventNodeList) {
        AssertUtil.notNull(storyBus);
        AssertUtil.notEmpty(eventNodeList);

        if (eventNodeList.size() == 1) {
            return eventNodeList.get(0);
        }
        List<EventNode> taskNodeCollect = eventNodeList.stream()
                .filter(eventNode -> TaskActionUtil.matchStrategyRule(eventNode.getMatchStrategyRuleList(), storyBus))
                .collect(Collectors.toList());
        AssertUtil.oneSize(taskNodeCollect, ExceptionEnum.MUST_ONE_TASK_ACTION);
        return taskNodeCollect.get(0);
    }
}
