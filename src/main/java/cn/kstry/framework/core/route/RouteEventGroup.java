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
package cn.kstry.framework.core.route;

import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.core.annotation.EventGroupParent;
import cn.kstry.framework.core.annotation.IgnoreEventNode;
import cn.kstry.framework.core.bus.TaskNode;
import cn.kstry.framework.core.config.TaskActionMethod;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.operator.EventOperatorRole;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ProxyUtil;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author lykan
 */
public abstract class RouteEventGroup implements EventGroup {

    /**
     * Task Operator
     */
    private EventOperatorRole taskActionOperator;

    /**
     * Task Action 被代理方法集合
     */
    private Map<String, TaskActionMethod> taskActionMethodMap;

    @Override
    public boolean route(TaskRouter router) {
        AssertUtil.notNull(router);

        Optional<TaskNode> routeNodeOptional = router.currentTaskNode();
        if (!routeNodeOptional.isPresent()) {
            return false;
        }

        TaskNode taskNode = routeNodeOptional.get();
        boolean nameEquals = taskNode.getEventGroupName().equals(getEventGroupName());
        boolean enumEquals = getEventGroupTypeEnum() == taskNode.getEventGroupTypeEnum();

        return nameEquals && enumEquals;
    }

    @Override
    public Class<? extends EventOperatorRole> getOperatorRoleClass() {
        KstryException.throwException(ExceptionEnum.NEED_CONFIRM_NECESSARY_METHODS);
        return null;
    }

    public Map<String, TaskActionMethod> getActionMethodMap() {
        Class<?> targetClass = ProxyUtil.noneProxyClass(this);
        return getTaskActionMethodMap(targetClass);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, TaskActionMethod> getTaskActionMethodMap(Class<?> targetClass) {
        AssertUtil.notNull(targetClass);
        if (MapUtils.isNotEmpty(this.taskActionMethodMap)) {
            return this.taskActionMethodMap;
        }

        Method[] methods = targetClass.getMethods();
        HashMap<String, TaskActionMethod> taskActionMethodHashMap = Maps.newHashMap();
        if (ArrayUtils.isEmpty(methods)) {
            return taskActionMethodHashMap;
        }

        for (Method method : methods) {
            Class<?> declaringClass = method.getDeclaringClass();
            if (!EventGroup.class.isAssignableFrom(declaringClass)
                    && declaringClass.getAnnotation(EventGroupComponent.class) == null
                    && declaringClass.getAnnotation(EventGroupParent.class) == null) {
                continue;
            }

            if (method.isAnnotationPresent(IgnoreEventNode.class)) {
                continue;
            }
            Class<?> requestClass = null;
            Class<?>[] parameterTypes = method.getParameterTypes();

            // 入参要求：如果有参数，要求只有一个
            if (ArrayUtils.isNotEmpty(parameterTypes)) {
                if (parameterTypes.length != 1) {
                    continue;
                }
                requestClass = parameterTypes[0];
            }

            // 出参要求：无参 或 实现 TaskResponse
            Class<?> returnClass = method.getReturnType();
            if (!"void".equals(returnClass.getName()) && !TaskResponse.class.isAssignableFrom(returnClass)) {
                continue;
            }

            TaskActionMethod taskActionMethod = new TaskActionMethod();
            taskActionMethod.setMethodName(method.getName());
            taskActionMethod.setRequestClass(requestClass);
            taskActionMethod.setReturnClass((Class<? extends TaskResponse<?>>) returnClass);
            taskActionMethod.setClassName(method.getDeclaringClass().getName());

            // 不允许方法重载
            AssertUtil.isNull(taskActionMethodHashMap.get(method.getName()), ExceptionEnum.NOT_ALLOWED_OVERLOADS,
                    "Do not allow method(eventAction) overloading in EventGroup! methodName:%s", method.getName());
            taskActionMethodHashMap.put(method.getName(), taskActionMethod);
            this.taskActionMethodMap = taskActionMethodHashMap;
        }
        return taskActionMethodHashMap;
    }

    public EventOperatorRole getTaskActionOperator() {
        return this.taskActionOperator;
    }

    public void setTaskActionOperator(EventOperatorRole taskActionOperator) {
        this.taskActionOperator = taskActionOperator;
    }
}
