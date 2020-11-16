/*
 *
 *  *  Copyright (c) 2020-2020, Lykan (jiashuomeng@gmail.com).
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
import cn.kstry.framework.core.bus.GlobalBus;
import cn.kstry.framework.core.engine.TaskAction;
import cn.kstry.framework.core.engine.TaskActionMethod;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.facade.RouteMapResponse;
import cn.kstry.framework.core.facade.TaskPipelinePort;
import cn.kstry.framework.core.facade.TaskRequest;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.operator.TaskActionOperatorRole;
import cn.kstry.framework.core.operator.TaskOperatorCreator;
import cn.kstry.framework.core.facade.DynamicRouteTable;
import cn.kstry.framework.core.route.GlobalMap;
import cn.kstry.framework.core.route.RouteNode;
import cn.kstry.framework.core.route.TaskRouter;
import cn.kstry.framework.core.route.TaskRouterInflectionPoint;
import cn.kstry.framework.core.timeslot.TimeSlotInvokeRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    public static void saveTaskResultToGlobalBus(GlobalBus globalBus, RouteNode routeNode, TaskResponse<Object> o) {
        ComponentTypeEnum componentEnum = routeNode.getComponentTypeEnum();
        if (componentEnum == ComponentTypeEnum.TIME_SLOT) {
            return;
        }

        if (o instanceof RouteMapResponse) {
            RouteMapResponse<?> routeMapResponse = GlobalUtil.transferNotEmpty(o, RouteMapResponse.class);
            DynamicRouteTable taskRouterTable = routeMapResponse.getTaskRouterTable();
            updateDynamicRouteTable(globalBus, taskRouterTable);
        }

        if (componentEnum == ComponentTypeEnum.MAPPING) {
            AssertUtil.isTrue(o instanceof TaskPipelinePort, ExceptionEnum.MAPPING_RESULT_ERROR);
            TaskRequest taskRequest = GlobalUtil.transferNotEmpty(o, TaskPipelinePort.class).getTaskRequest();
            AssertUtil.notNull(taskRequest, ExceptionEnum.MAPPING_RESULT_ERROR);
            AssertUtil.isNull(GlobalUtil.transferNotEmpty(o, TaskPipelinePort.class).getResult(), ExceptionEnum.MAPPING_RESULT_ERROR);
            globalBus.addMappingResult(routeNode, taskRequest);
            return;
        }
        if (o == null) {
            return;
        }
        globalBus.addNodeResult(routeNode, o);
    }

    /**
     * 获取下一个 task 请求的 request
     */
    public static Object getNextRequest(Object taskRequest, TaskRouter router, ResultMappingRepository resultMappingRepository, GlobalBus globalBus, List<TaskAction> taskGroup) throws InstantiationException,
            IllegalAccessException {
        RouteNode routeNode = GlobalUtil.notEmpty(router.currentRouteNode());
        Class<? extends TaskRequest> currentRequestClass = routeNode.getRequestClass();
        if (currentRequestClass == null) {
            return null;
        }

        if (routeNode.getComponentTypeEnum() == ComponentTypeEnum.TIME_SLOT) {
            TimeSlotInvokeRequest timeSlotInvokeRequest = new TimeSlotInvokeRequest();
            timeSlotInvokeRequest.setGlobalBus(globalBus);
            timeSlotInvokeRequest.setTaskGroup(taskGroup);
            timeSlotInvokeRequest.setResultMappingRepository(resultMappingRepository);
            return timeSlotInvokeRequest;
        }

        TaskRequest nextRequestInstance = currentRequestClass.newInstance();
        if (nextRequestInstance instanceof ResultAdapterRequest) {
            AssertUtil.isTrue(routeNode.getComponentTypeEnum() == ComponentTypeEnum.MAPPING, ExceptionEnum.MAPPING_REQUEST_ERROR);
            ResultAdapterRequest adapterRequest = (ResultAdapterRequest) nextRequestInstance;
            adapterRequest.setGlobalBus(globalBus);
            adapterRequest.setTaskGroup(taskGroup);
            adapterRequest.setRouter(router);
            adapterRequest.setResultMappingRepository(resultMappingRepository);
            fillResultInfo(taskRequest, adapterRequest);
            return adapterRequest;
        } else {
            AssertUtil.isTrue(routeNode.getComponentTypeEnum() != ComponentTypeEnum.MAPPING, ExceptionEnum.MAPPING_REQUEST_ERROR);
        }

        // MAPPING 返回的结果直接被赋值给下一个任务
        Optional<RouteNode> beforeInvokeRouteNodeOptional = router.beforeInvokeRouteNodeIgnoreTimeSlot();
        if (beforeInvokeRouteNodeOptional.isPresent() && beforeInvokeRouteNodeOptional.get().getComponentTypeEnum() == ComponentTypeEnum.MAPPING) {
            return globalBus.getRequestByRouteNode(beforeInvokeRouteNodeOptional.get());
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
    public static TaskActionOperatorRole getTaskActionOperator(TaskRouter router, List<TaskAction> taskGroup) {
        TaskAction taskAction = TaskActionUtil.getNextTaskAction(taskGroup, router);
        TaskActionOperatorRole actionOperator = taskAction.getTaskActionOperator();
        if (actionOperator == null) {
            actionOperator = TaskOperatorCreator.getTaskOperator(taskAction);
        }
        return actionOperator;
    }

    /**
     * 执行目标方法
     *
     * @param taskRequest    task request
     * @param routeNode      route node
     * @param actionOperator action operator
     * @return target result
     */
    public static Object invokeTarget(Object taskRequest, RouteNode routeNode, TaskActionOperatorRole actionOperator) throws NoSuchMethodException {
        Object o;
        if (routeNode.getRequestClass() == null) {
            Method method = actionOperator.getClass().getMethod(routeNode.getActionName());
            o = ReflectionUtils.invokeMethod(method, actionOperator);
        } else {
            Method method = actionOperator.getClass().getMethod(routeNode.getActionName(), routeNode.getRequestClass());
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
        RouteNode routeNode = GlobalUtil.notEmpty(router.currentRouteNode());
        GlobalMap.MapNode mapNode = GlobalUtil.notNull(routeNode.getMapNode());

        if (mapNode.nextMapNodeSize() <= 1 && CollectionUtils.isEmpty(router.nextRouteNodeIgnoreTimeSlot().get().getInflectionPointList())) {
            return;
        }

        GlobalBus globalBus = GlobalUtil.notNull(router.getGlobalBus());
        DynamicRouteTable dynamicRouteTable = globalBus.getDynamicRouteTable();
        AssertUtil.notNull(dynamicRouteTable);
        router.reRouteNodeMap(node -> matchRouteNode(node, dynamicRouteTable));
    }

    public static Class<? extends TaskRequest> getRouteNodeRequestClass(List<TaskAction> taskActionList, RouteNode routeNode) {
        AssertUtil.notNull(routeNode);
        AssertUtil.notEmpty(taskActionList);

        List<TaskAction> collect = taskActionList.stream().filter(taskAction -> {
            if (StringUtils.isBlank(routeNode.getNodeName())) {
                return false;
            }
            if (!Objects.equals(taskAction.getTaskActionName(), routeNode.getNodeName())) {
                return false;
            }
            return taskAction.getTaskActionTypeEnum() == routeNode.getComponentTypeEnum();
        }).collect(Collectors.toList());
        AssertUtil.oneSize(collect, ExceptionEnum.MUST_ONE_TASK_ACTION);

        TaskAction taskAction = collect.get(0);
        TaskActionMethod taskActionMethod = taskAction.getActionMethodMap().get(routeNode.getActionName());
        AssertUtil.notNull(taskActionMethod);
        return taskActionMethod.getRequestClass();
    }

    private static boolean matchRouteNode(RouteNode node, DynamicRouteTable dynamicRouteTable) {
        List<TaskRouterInflectionPoint> inflectionPointList = node.getInflectionPointList();
        if (CollectionUtils.isEmpty(inflectionPointList)) {
            return false;
        }

        boolean flag = true;
        try {
            for (TaskRouterInflectionPoint inflectionPoint : inflectionPointList) {
                AssertUtil.notBlank(inflectionPoint.getFieldName());
                AssertUtil.anyNotNull(inflectionPoint.getExpectedValue(), inflectionPoint.getMatchingStrategy());

                Field field = FieldUtils.getField(dynamicRouteTable.getClass(), inflectionPoint.getFieldName(), true);
                AssertUtil.notNull(field);

                flag = flag && inflectionPoint.getMatchingStrategy().calculate(field.get(dynamicRouteTable), inflectionPoint.getExpectedValue());
            }
        } catch (Exception e) {
            KstryException.throwException(e);
        }
        return flag;
    }

    private static void updateDynamicRouteTable(GlobalBus globalBus, DynamicRouteTable taskRouterTable) {
        if (taskRouterTable == null) {
            return;
        }

        DynamicRouteTable dynamicRouteTable = globalBus.getDynamicRouteTable();
        if (dynamicRouteTable == null) {
            globalBus.setDynamicRouteTable(taskRouterTable);
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
    private static TaskAction getNextTaskAction(List<TaskAction> taskGroup, TaskRouter router) {
        AssertUtil.notEmpty(taskGroup);
        List<TaskAction> taskActionList = taskGroup.stream().filter(s -> s.route(router)).collect(Collectors.toList());
        AssertUtil.oneSize(taskActionList, ExceptionEnum.MUST_ONE_TASK_ACTION);
        return taskActionList.get(0);
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
