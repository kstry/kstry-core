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

import cn.kstry.framework.core.route.RequestMappingGroup;
import cn.kstry.framework.core.config.GlobalConstant;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.facade.DynamicRouteTable;
import cn.kstry.framework.core.facade.TaskRequest;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.route.TaskNode;
import cn.kstry.framework.core.route.TaskRouter;
import cn.kstry.framework.core.route.TimeSlotEventNode;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class StoryBus {

    /**
     * 路由器
     */
    private TaskRouter router;

    /**
     * 承载 请求入参 全局流转的参数-Key
     */
    public static final TaskNode DEFAULT_GLOBAL_BUS_REQUEST_KEY = new TaskNode("BASE", "DEFAULT_GLOBAL_BUS_REQUEST_KEY", ComponentTypeEnum.GROUP);

    /**
     * 承载 全局不可变量 全局流转的参数-Key
     */
    public static final TaskNode DEFAULT_GLOBAL_BUS_STABLE_KEY = new TaskNode("BASE", "DEFAULT_GLOBAL_BUS_STABLE_KEY", ComponentTypeEnum.GROUP);

    /**
     * 承载 全局可变量 全局流转的参数-Key
     */
    public static final TaskNode DEFAULT_GLOBAL_BUS_VARIABLE_KEY = new TaskNode("BASE", "DEFAULT_GLOBAL_BUS_VARIABLE_KEY", ComponentTypeEnum.GROUP);

    /**
     * 全局流转的参数
     */
    private final Map<String, Object> globalParamAndResult = new ConcurrentHashMap<>();

    /**
     * 动态路由表
     */
    private DynamicRouteTable dynamicRouteTable;

    public StoryBus(Object request) {
        // 初始化全局参数
        this.globalParamAndResult.put(DEFAULT_GLOBAL_BUS_REQUEST_KEY.identity(), (request == null) ? new HashMap<String, Object>() : request);
        this.globalParamAndResult.put(DEFAULT_GLOBAL_BUS_STABLE_KEY.identity(), new HashMap<String, Object>());
        this.globalParamAndResult.put(DEFAULT_GLOBAL_BUS_VARIABLE_KEY.identity(), new HashMap<String, Object>());
    }

    public void setRouter(TaskRouter router) {
        AssertUtil.notNull(router);
        AssertUtil.isNull(this.router);
        this.router = router;
    }

    @SuppressWarnings("all")
    public void saveTaskResult(TaskNode taskNode, Object result) {
        AssertUtil.notNull(taskNode);
        if (result == null) {
            return;
        }
        AssertUtil.isTrue(result instanceof TaskResponse);
        if (taskNode.getEventNode() instanceof TimeSlotEventNode) {
            AssertUtil.notBlank(((TimeSlotEventNode) taskNode.getEventNode()).getStrategyName());
            globalParamAndResult.put(GlobalConstant.TIME_SLOT_NODE_SIGN + ((TimeSlotEventNode) taskNode.getEventNode()).getStrategyName(), ((TaskResponse) result).getResult());
        } else {
            globalParamAndResult.put(taskNode.identity(), ((TaskResponse) result).getResult());
        }
    }

    public Object getResultByTaskNode(TaskNode taskNode) {
        AssertUtil.notNull(taskNode);
        return globalParamAndResult.get(taskNode.identity());
    }

    public TaskRouter getRouter() {
        return router;
    }

    public DynamicRouteTable getDynamicRouteTable() {
        return dynamicRouteTable;
    }

    public void setDynamicRouteTable(DynamicRouteTable dynamicRouteTable) {
//        AssertUtil.isNull(this.dynamicRouteTable, ExceptionEnum.DYNAMIC_ROUTING_TABLES_ERROR);
        this.dynamicRouteTable = dynamicRouteTable;
    }

    public void loadTaskRequest(TaskRequest innerTaskRequest, RequestMappingGroup requestMappingGroup) {
        if (innerTaskRequest == null || requestMappingGroup == null || CollectionUtils.isEmpty(requestMappingGroup.getMappingItemList())) {
            return;
        }
        requestMappingGroup.getMappingItemList().forEach(mappingItem -> {
            String source = mappingItem.getSource();
            AssertUtil.notBlank(source);
            String[] fieldNameArray = source.split("\\.");
            AssertUtil.isTrue(fieldNameArray.length > 1, ExceptionEnum.PARAMS_ERROR);

            Object value = GlobalUtil.getProperty(this.globalParamAndResult, source.replace(GlobalConstant.NODE_SIGN, StringUtils.EMPTY)).orElse(StringUtils.EMPTY);
            if (value == GlobalUtil.GET_PROPERTY_ERROR_SIGN) {
                return;
            }
            GlobalUtil.setProperty(innerTaskRequest, mappingItem.getTarget(), value);
        });
    }

    public Optional<Object> getGlobalParamValue(String fieldName) {
        if (StringUtils.isBlank(fieldName)) {
            return Optional.empty();
        }

        return GlobalUtil.getProperty(this.globalParamAndResult, fieldName).filter(obj -> obj != GlobalUtil.GET_PROPERTY_ERROR_SIGN);
    }

    @SuppressWarnings("unchecked")
    public StoryBus cloneStoryBus() {
        StoryBus cloneStoryBus = new StoryBus(null);
        // 请求入参 分支流程与主流程公用
        cloneStoryBus.globalParamAndResult.putAll(this.globalParamAndResult);

        // 在分支流程出现时开始，复制当下的 不可变更数据
        Map<String, Object> stableMap = new HashMap<>((Map<String, Object>) this.globalParamAndResult.get(DEFAULT_GLOBAL_BUS_STABLE_KEY.identity()));
        cloneStoryBus.globalParamAndResult.put(DEFAULT_GLOBAL_BUS_STABLE_KEY.identity(), stableMap);

        // 在分支流程出现时开始，复制当下的 可变更数据
        Map<String, Object> variableMap = new HashMap<>((Map<String, Object>) this.globalParamAndResult.get(DEFAULT_GLOBAL_BUS_VARIABLE_KEY.identity()));
        cloneStoryBus.globalParamAndResult.put(DEFAULT_GLOBAL_BUS_VARIABLE_KEY.identity(), variableMap);
        return cloneStoryBus;
    }

    public void loadResultData(Map<String, Object> resultMap) {
        AssertUtil.notNull(resultMap);
        resultMap.putAll(this.globalParamAndResult);
    }
}
