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

import cn.kstry.framework.core.annotation.TaskActionComponent;
import cn.kstry.framework.core.engine.TaskAction;
import cn.kstry.framework.core.engine.TaskActionMethod;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.facade.TaskRequest;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.operator.TaskActionOperatorRole;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ProxyUtil;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author lykan
 */
public abstract class RouteTaskAction implements TaskAction {

    /**
     * Task Operator
     */
    private TaskActionOperatorRole taskActionOperator;

    /**
     * Task Action 被代理方法集合
     */
    private Map<String, TaskActionMethod> taskActionMethodMap;

    @Override
    public boolean route(TaskRouter router) {
        AssertUtil.notNull(router);

        Optional<RouteNode> routeNodeOptional = router.currentRouteNode();
        if (!routeNodeOptional.isPresent()) {
            return false;
        }

        RouteNode routeNode = routeNodeOptional.get();
        boolean nameEquals = routeNode.getNodeName().equals(getTaskActionName());
        boolean enumEquals = getTaskActionTypeEnum() == routeNode.getComponentTypeEnum();

        return nameEquals && enumEquals;
    }

    @Override
    public TaskActionOperatorRole getTaskActionOperator() {
        return this.taskActionOperator;
    }

    @Override
    public void setTaskActionOperator(TaskActionOperatorRole taskActionOperator) {
        this.taskActionOperator = taskActionOperator;
    }

    @Override
    public Class<? extends TaskActionOperatorRole> getTaskActionOperatorRoleClass() {
        KstryException.throwException(ExceptionEnum.NEED_CONFIRM_NECESSARY_METHODS);
        return null;
    }

    @Override
    public Map<String, TaskActionMethod> getActionMethodMap() {
        Class<?> targetClass = this.getClass();
        if (ProxyUtil.isProxyObject(this)) {
            targetClass = ProxyUtil.getTargetClass(this);
        }
        return getTaskActionMethodMap(targetClass);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, TaskActionMethod> getTaskActionMethodMap(Class<?> targetClass) {
        AssertUtil.notNull(targetClass);

        if (this.taskActionMethodMap != null) {
            return this.taskActionMethodMap;
        }

        synchronized (this) {
            if (this.taskActionMethodMap != null) {
                return this.taskActionMethodMap;
            }
            Method[] methods = targetClass.getMethods();
            HashMap<String, TaskActionMethod> taskActionMethodHashMap = Maps.newHashMap();
            if (ArrayUtils.isEmpty(methods)) {
                return taskActionMethodHashMap;
            }

            for (Method method : methods) {
                Class<?> declaringClass = method.getDeclaringClass();
                if (!TaskAction.class.isAssignableFrom(declaringClass) && declaringClass.getAnnotation(TaskActionComponent.class) == null) {
                    continue;
                }

                Class<? extends TaskRequest> requestClass = null;
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (ArrayUtils.isNotEmpty(parameterTypes)) {
                    if (parameterTypes.length != 1) {
                        continue;
                    }
                    if (!TaskRequest.class.isAssignableFrom(parameterTypes[0])) {
                        continue;
                    }
                    requestClass = (Class<? extends TaskRequest>) parameterTypes[0];
                }
                Class<?> returnClass = method.getReturnType();
                if (!"void".equals(returnClass.getName()) && !TaskResponse.class.isAssignableFrom(returnClass)) {
                    continue;
                }

                TaskActionMethod taskActionMethod = new TaskActionMethod();
                taskActionMethod.setMethodName(method.getName());
                taskActionMethod.setRequestClass(requestClass);
                taskActionMethod.setReturnClass((Class<? extends TaskResponse<?>>) method.getReturnType());
                taskActionMethod.setClassName(method.getDeclaringClass().getName());
                AssertUtil.isNull(taskActionMethodHashMap.get(method.getName()), ExceptionEnum.NOT_ALLOWED_OVERLOADS);
                taskActionMethodHashMap.put(method.getName(), taskActionMethod);
            }
            this.taskActionMethodMap = taskActionMethodHashMap;
            return taskActionMethodHashMap;
        }
    }
}
