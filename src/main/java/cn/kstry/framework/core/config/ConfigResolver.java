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
package cn.kstry.framework.core.config;

import cn.kstry.framework.core.bus.GlobalBus;
import cn.kstry.framework.core.enums.CalculateEnum;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.route.GlobalMap;
import cn.kstry.framework.core.route.RouteNode;
import cn.kstry.framework.core.route.TaskRouterInflectionPoint;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author lykan
 */
public class ConfigResolver {

    private String taskRouteConfigName;

    private TaskRouteConfig taskRouteConfig;

    private Map<String, RouteNode> routeNodeMap;

    private Map<String, Map<String, String>> resultMappingConfig;

    private Map<String, GlobalMap.MapNode> mapPlanning;

    public void init() {
        this.taskRouteConfig = parseTaskRouteConfig();
        this.routeNodeMap = parseRouteNodeMap();
        this.resultMappingConfig = parseResultMappingConfig();
    }

    public Map<String, GlobalMap.MapNode> initMapPlanning() {
        this.mapPlanning = parseMapPlanning();
        return this.mapPlanning;
    }

    public Map<String, RouteNode> getRouteNodeMap() {
        return this.routeNodeMap;
    }

    public Map<RouteNode, Map<RouteNode, Map<String, String>>> getRouteNodeMappingMap() {
        return parseRouteNodeMappingMap();
    }

    private Map<RouteNode, Map<RouteNode, Map<String, String>>> parseRouteNodeMappingMap() {
        AssertUtil.notNull(this.mapPlanning);
        if (MapUtils.isEmpty(this.mapPlanning)) {
            return new HashMap<>();
        }
        Map<RouteNode, Map<RouteNode, Map<String, String>>> mappingMap = new HashMap<>();
        this.mapPlanning.values().forEach(mapNode -> getNextMapping(mapNode, mapNode.getNextMapNodeList(), mappingMap, true));
        return mappingMap;
    }

    private void getNextMapping(GlobalMap.MapNode mapNode, List<GlobalMap.MapNode> nextMapNodeList, Map<RouteNode, Map<RouteNode, Map<String, String>>> mappingMap,
                                boolean deepInto) {
        if (CollectionUtils.isEmpty(nextMapNodeList)) {
            return;
        }

        if (deepInto) {
            nextMapNodeList.forEach(n -> getNextMapping(n, n.getNextMapNodeList(), mappingMap, true));
        }

        List<String> mappingNameList = ((MapNodeAdapterForConfigResolver) mapNode).getMappingNameList();
        if (CollectionUtils.isEmpty(mappingNameList)) {
            return;
        }

        for (GlobalMap.MapNode after : nextMapNodeList) {
            if (after.getRouteNode().getComponentTypeEnum() == ComponentTypeEnum.TIME_SLOT) {
                after.getNextMapNodeList().forEach(n -> getNextMapping(mapNode, Lists.newArrayList(n), mappingMap, false));
                continue;
            }

            for (String mappingName : mappingNameList) {
                Map<String, String> mappingMapIn = resultMappingConfig.get(mappingName);
                AssertUtil.notEmpty(mappingMapIn);

                GlobalMap.MapNode beforeNode = mapNode.getPrevMapNode() == null ? new GlobalMap.MapNode(GlobalBus.DEFAULT_GLOBAL_BUS_PARAMS_KEY) : mapNode.getPrevMapNode();
                if (beforeNode.getRouteNode().getComponentTypeEnum() == ComponentTypeEnum.TIME_SLOT) {
                    beforeNode = Optional.ofNullable(beforeNode.getPrevMapNode()).orElse(new GlobalMap.MapNode(GlobalBus.DEFAULT_GLOBAL_BUS_PARAMS_KEY));
                }
                RouteNode before = beforeNode.getRouteNode();
                Map<RouteNode, Map<String, String>> routeNodeMap = mappingMap.get(after.getRouteNode());
                if (MapUtils.isEmpty(routeNodeMap)) {
                    routeNodeMap = new HashMap<>();
                    mappingMap.put(after.getRouteNode(), routeNodeMap);
                }

                Map<String, String> mapping = routeNodeMap.get(before);
                if (MapUtils.isEmpty(mapping)) {
                    mapping = new HashMap<>();
                    routeNodeMap.put(before, mapping);
                }
                mapping.putAll(mappingMapIn);
            }
        }
    }

    private Map<String, GlobalMap.MapNode> parseMapPlanning() {
        Map<String, List<TaskRouteConfig.MapPlanningNodeItem>> mapPlanning = taskRouteConfig.getMapPlanning();
        if (MapUtils.isEmpty(mapPlanning)) {
            return new HashMap<>();
        }

        Map<String, GlobalMap.MapNode> mapPlanningResult = new HashMap<>();
        mapPlanning.forEach((k, v) -> {
            GlobalMap.MapNode next = null;
            for (TaskRouteConfig.MapPlanningNodeItem item : v) {
                GlobalMap.MapNode n = parsePlanningNodeItem(item);
                if (next == null) {
                    next = n;
                    mapPlanningResult.put(k, next);
                    continue;
                }
                next.addNextMapNode(n);
                next = n;
            }
        });
        return mapPlanningResult;
    }

    private GlobalMap.MapNode parsePlanningNodeItem(TaskRouteConfig.MapPlanningNodeItem planningNodeItem) {
        RouteNode routeNode = routeNodeMap.get(planningNodeItem.getNodeName());
        AssertUtil.notNull(routeNode);
        MapNodeAdapterForConfigResolver mapNode = new MapNodeAdapterForConfigResolver(routeNode.cloneRouteNode());
        mapNode.setMappingNameList(planningNodeItem.getMapping());
        mapNode.getRouteNode().setInterruptTimeSlot(BooleanUtils.isTrue(planningNodeItem.getInterruptTimeSlot()));
        if (MapUtils.isEmpty(planningNodeItem.getRouteMap())) {
            return mapNode;
        }

        Map<String, List<TaskRouteConfig.MapPlanningNodeItem>> routeMap = planningNodeItem.getRouteMap();
        routeMap.forEach((kIn, vIn) -> {
            GlobalMap.MapNode next = null;
            List<TaskRouterInflectionPoint> inflectionPointList = getInflectionPoints(planningNodeItem, kIn);
            for (TaskRouteConfig.MapPlanningNodeItem item : vIn) {
                GlobalMap.MapNode mapNodeIn = parsePlanningNodeItem(item);
                if (next == null) {
                    next = mapNodeIn;
                    mapNodeIn.getRouteNode().setInflectionPointList(inflectionPointList);
                    mapNode.addNextMapNode(mapNodeIn);
                    continue;
                }
                next.addNextMapNode(mapNodeIn);
                next = mapNodeIn;
            }
        });
        return mapNode;
    }

    private Map<String, Map<String, String>> parseResultMappingConfig() {
        Map<String, Map<String, String>> resultMapping = taskRouteConfig.getResultMapping();
        Map<String, Map<String, String>> mapping = new HashMap<>();
        if (MapUtils.isEmpty(resultMapping)) {
            return mapping;
        }

        resultMapping.forEach((k, v) -> {
            AssertUtil.notBlank(k);
            if (MapUtils.isEmpty(v)) {
                return;
            }
            Map<String, String> fieldMapping = new HashMap<>();
            v.forEach((kIn, vIn) -> {
                AssertUtil.anyNotBlank(kIn, vIn);
                if (vIn.startsWith("#")) {
                    fieldMapping.put(kIn, vIn);
                    return;
                }
                if (vIn.toUpperCase().startsWith("DEFAULT")) {
                    fieldMapping.put(kIn, "#" + GlobalBus.DEFAULT_GLOBAL_BUS_PARAMS_KEY.identityStr() + vIn.substring(7));
                    return;
                }
                if (vIn.toUpperCase().startsWith("NODE_")) {
                    int index = vIn.toUpperCase().replaceAll("^NODE_\\d+", StringUtils.EMPTY).length();
                    RouteNode routeNode = routeNodeMap.get(vIn.substring(0, vIn.length() - index));
                    AssertUtil.notNull(routeNode, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
                    fieldMapping.put(kIn, "#" + routeNode.identityStr() + vIn.substring(vIn.length() - index));
                    return;
                }
                KstryException.throwException(ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
            });
            mapping.put(k, fieldMapping);
        });
        return mapping;
    }

    private List<TaskRouterInflectionPoint> getInflectionPoints(TaskRouteConfig.MapPlanningNodeItem planningNodeItem, String strategyKey) {
        Map<String, Map<String, String>> routeStrategy = planningNodeItem.getRouteStrategy();
        AssertUtil.notEmpty(routeStrategy);
        Map<String, String> strategyMap = routeStrategy.get(strategyKey);
        AssertUtil.notEmpty(strategyMap);

        List<TaskRouterInflectionPoint> inflectionPointList = new ArrayList<>();
        strategyMap.forEach((k, v) -> {
            AssertUtil.anyNotBlank(k, v);
            String[] splitKeyArray = k.split("-");
            AssertUtil.isTrue(splitKeyArray.length == 2, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);

            TaskRouterInflectionPoint inflectionPoint = new TaskRouterInflectionPoint();
            inflectionPoint.setExpectedValue(v);
            inflectionPoint.setFieldName(splitKeyArray[0]);
            inflectionPoint.setMatchingStrategy(GlobalUtil.notEmpty(CalculateEnum.getCalculateEnumByName(splitKeyArray[1])).getExpression());
            inflectionPointList.add(inflectionPoint);
        });
        return inflectionPointList;
    }

    private Map<String, RouteNode> parseRouteNodeMap() {
        Map<String, RouteNode> routeNodeMap = new HashMap<>();
        Map<String, String> nodeDefinition = taskRouteConfig.getNodeDefinition();
        AssertUtil.notEmpty(nodeDefinition);
        nodeDefinition.forEach((k, v) -> {
            AssertUtil.anyNotBlank(k, v);
            String[] property = v.split("-");
            AssertUtil.isTrue(property.length == 3, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
            ComponentTypeEnum componentTypeEnum = GlobalUtil.notEmpty(ComponentTypeEnum.getComponentTypeEnumByName(property[0]));

            RouteNode routeNode = new RouteNode();
            routeNode.setActionName(property[2]);
            routeNode.setComponentTypeEnum(componentTypeEnum);
            routeNode.setNodeName(property[1]);
            routeNodeMap.put(k, routeNode);
        });
        return routeNodeMap;
    }

    @SuppressWarnings("all")
    private TaskRouteConfig parseTaskRouteConfig() {
        try {
            URL resource = ConfigResolver.class.getClassLoader().getResource(getTaskRouteConfigName());
            AssertUtil.notNull(resource);
            Path path = Paths.get(resource.toURI());
            AssertUtil.notNull(path);

            StringBuilder sb = new StringBuilder();
            Files.lines(path).forEach(sb::append);
            TaskRouteConfig taskRouteConfig = JSON.parseObject(sb.toString(), TaskRouteConfig.class);
            JSONObject jsonObject = JSON.parseObject(sb.toString());
            AssertUtil.notNull(taskRouteConfig);
            return taskRouteConfig;
        } catch (Exception e) {
            KstryException.throwException(e);
            return null;
        }
    }

    private static class MapNodeAdapterForConfigResolver extends GlobalMap.MapNode {
        public MapNodeAdapterForConfigResolver(RouteNode routeNode) {
            super(routeNode);
        }

        /**
         * 保存映射名称列表
         */
        private List<String> mappingNameList;

        public List<String> getMappingNameList() {
            return mappingNameList;
        }

        public void setMappingNameList(List<String> mappingNameList) {
            this.mappingNameList = mappingNameList;
        }
    }

    public String getTaskRouteConfigName() {
        if (StringUtils.isBlank(this.taskRouteConfigName)) {
            this.taskRouteConfigName = "TaskRouteConfig.json";
        }
        return this.taskRouteConfigName;
    }

    public void setTaskRouteConfigName(String taskRouteConfigName) {
        this.taskRouteConfigName = taskRouteConfigName;
    }
}
