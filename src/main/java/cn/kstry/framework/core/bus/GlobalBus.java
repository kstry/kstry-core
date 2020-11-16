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
package cn.kstry.framework.core.bus;

import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.facade.TaskRequest;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.facade.DynamicRouteTable;
import cn.kstry.framework.core.route.RouteNode;
import cn.kstry.framework.core.route.TaskRouter;
import cn.kstry.framework.core.util.AssertUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class GlobalBus {

    /**
     * 路由器
     */
    private final TaskRouter router;

    public static final RouteNode DEFAULT_GLOBAL_BUS_PARAMS_KEY = new RouteNode("BASE", "DEFAULT_GLOBAL_BUS_PARAMS_NODE", ComponentTypeEnum.GROUP);

    /**
     * 执行链路中，所有 task 的 response
     */
    private final Map<RouteNode, TaskResponse<?>> globalResponse = new ConcurrentHashMap<>();

    /**
     * 执行链路中，所有 task 的 globalRequest
     */
    private final Map<RouteNode, TaskRequest> globalRequest = new ConcurrentHashMap<>();

    /**
     * 全局流转的参数
     */
    private final Map<RouteNode, Object> globalParams = new ConcurrentHashMap<>();

    /**
     * 动态路由表
     */
    private DynamicRouteTable dynamicRouteTable;

    public GlobalBus(TaskRouter router) {
        AssertUtil.notNull(router);
        this.router = router;
        globalParams.put(DEFAULT_GLOBAL_BUS_PARAMS_KEY, new HashMap<>());

        router.setGlobalBus(this);
    }

    public void addNodeResult(RouteNode node, TaskResponse<?> result) {
        AssertUtil.anyNotNull(node, result);
        try {
            globalResponse.put(node, result);
        } catch (Exception e) {
            KstryException.throwException(e);
        }
    }

    public void addMappingResult(RouteNode node, TaskRequest request) {
        AssertUtil.anyNotNull(node, request);
        try {
            globalRequest.put(node, request);
        } catch (Exception e) {
            KstryException.throwException(e);
        }
    }

    public TaskRequest getRequestByRouteNode(RouteNode node) {
        return globalRequest.get(node);
    }

    public TaskResponse<?> getResultByRouteNode(RouteNode node) {
        return globalResponse.get(node);
    }

    public void setDefaultParams(Object object) {
        if (object == null) {
            return;
        }
        globalParams.put(DEFAULT_GLOBAL_BUS_PARAMS_KEY, object);
    }

    public void doParamsConsumer(BiConsumer<RouteNode, Object> consumer) {
        globalParams.forEach(consumer);
    }

    public void doResponseConsumer(BiConsumer<RouteNode, TaskResponse<?>> consumer) {
        globalResponse.forEach(consumer);
    }

    public TaskRouter getRouter() {
        return router;
    }

    public DynamicRouteTable getDynamicRouteTable() {
        return dynamicRouteTable;
    }

    public void setDynamicRouteTable(DynamicRouteTable dynamicRouteTable) {
        AssertUtil.isNull(this.dynamicRouteTable, ExceptionEnum.DYNAMIC_ROUTING_TABLES_ERROR);
        this.dynamicRouteTable = dynamicRouteTable;
    }
}
