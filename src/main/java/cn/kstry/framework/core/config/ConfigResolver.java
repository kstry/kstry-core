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
package cn.kstry.framework.core.config;

import cn.kstry.framework.core.adapter.RequestMappingGroup;
import cn.kstry.framework.core.bus.GlobalBus;
import cn.kstry.framework.core.enums.CalculateEnum;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.enums.InflectionPointTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.route.GlobalMap;
import cn.kstry.framework.core.route.RouteNode;
import cn.kstry.framework.core.route.TaskRouterInflectionPoint;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lykan
 */
public class ConfigResolver {

    private TaskStoryDefConfig taskStoryDefConfig;

    private Map<String, RouteNode> routeNodeMap;

    private Map<String, RequestMappingGroup> mappingGroupMap;

    private Map<String, List<GlobalMap.MapNode>> storyDefinition;

    public void initNodeAndMapping(String taskStoryConfigName) {
        this.taskStoryDefConfig = FileTaskStoryDefConfig.parseTaskStoryConfig(getTaskStoryConfigName(taskStoryConfigName));
        this.routeNodeMap = parseRouteNodeMap();
        this.mappingGroupMap = parseMappingGroupMap();
    }

    public Map<String, List<GlobalMap.MapNode>> initStoryDefinition() {
        this.storyDefinition = parseStoryDef();
        return this.storyDefinition;
    }

    public Map<String, RouteNode> getRouteNodeMap() {
        return this.routeNodeMap;
    }

    public Map<RouteNode, Map<RouteNode, Map<String, String>>> getRouteNodeMappingMap() {
        return parseRouteNodeMappingMap();
    }

    private Map<RouteNode, Map<RouteNode, Map<String, String>>> parseRouteNodeMappingMap() {
        AssertUtil.notNull(this.storyDefinition);
        if (MapUtils.isEmpty(this.storyDefinition)) {
            return new HashMap<>();
        }
        Map<RouteNode, Map<RouteNode, Map<String, String>>> mappingMap = new HashMap<>();
//        this.storyDefinition.values().forEach(mapNode -> getNextMapping(mapNode, mapNode.getNextMapNodeList(), mappingMap, true));
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

        List<String> mappingNameList = null;
        if (CollectionUtils.isEmpty(mappingNameList)) {
            return;
        }

        for (GlobalMap.MapNode after : nextMapNodeList) {
            if (after.getRouteNode().getComponentTypeEnum() == ComponentTypeEnum.TIME_SLOT) {
                after.getNextMapNodeList().forEach(n -> getNextMapping(mapNode, Lists.newArrayList(n), mappingMap, false));
                continue;
            }

            for (String mappingName : mappingNameList) {
                Map<String, String> mappingMapIn = null;
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

    private Map<String, List<GlobalMap.MapNode>> parseStoryDef() {
        TaskStoryDefConfig.StoryDef<String, TaskStoryDefConfig.StoryDefItem<TaskStoryDefConfig.StoryDefItemConfig>> storyDef = taskStoryDefConfig.getStoryDef();
        Map<String, List<GlobalMap.MapNode>> storyNodeMap = new HashMap<>();
        if (MapUtils.isEmpty(storyDef)) {
            return storyNodeMap;
        }

        Map<String, List<MapNodeAdapterForConfigResolver>> storyNodeAdaptorMap = new HashMap<>();
        storyDef.keySet().forEach(key -> {
            TaskStoryDefConfig.StoryDefItem<TaskStoryDefConfig.StoryDefItemConfig> value = storyDef.get(key);
            if (CollectionUtils.isEmpty(value)) {
                return;
            }

            List<MapNodeAdapterForConfigResolver> mapNodeAdapterList = new ArrayList<>();
            for (TaskStoryDefConfig.StoryDefItemConfig item : value) {
                MapNodeAdapterForConfigResolver mapNodeAdapter = new MapNodeAdapterForConfigResolver();
                if (StringUtils.isNotBlank(item.getNodeName())) {
                    RouteNode routeNode = this.routeNodeMap.get(item.getNodeName());
                    AssertUtil.notNull(routeNode);
                    GlobalMap.MapNode mapNode = new GlobalMap.MapNode(routeNode.cloneRouteNode());
                    mapNodeAdapter.setMapNode(mapNode);
                }

                if (StringUtils.isNotEmpty(item.getRequestMapping()) && mapNodeAdapter.getMapNode() != null) {
                    RequestMappingGroup mappingGroup = this.mappingGroupMap.get(item.getRequestMapping());
                    AssertUtil.notNull(mappingGroup);
                    mapNodeAdapter.getMapNode().getRouteNode().setRequestMappingGroup(mappingGroup);
                }

                if (MapUtils.isNotEmpty(taskStoryDefConfig.getRouteStrategyDef()) && StringUtils.isNotBlank(item.getRouteStrategy())) {
                    TaskStoryDefConfig.RouteStrategyDefItem<TaskStoryDefConfig.RouteStrategyDefItemConfig> routeStrategy =
                            taskStoryDefConfig.getRouteStrategyDef().get(item.getRouteStrategy());
                    AssertUtil.notNull(routeStrategy);
                    mapNodeAdapter.setRouteStrategy(routeStrategy);
                }
                mapNodeAdapterList.add(mapNodeAdapter);
            }
            storyNodeAdaptorMap.put(key, mapNodeAdapterList);
        });

        storyNodeAdaptorMap.forEach((k, v) -> {
            if (CollectionUtils.isEmpty(v)) {
                return;
            }

            GlobalMap.MapNode beforeMapNode = null;
            for (MapNodeAdapterForConfigResolver mapNodeAdapter : v) {
                if (CollectionUtils.isNotEmpty(mapNodeAdapter.getRouteStrategy()) && mapNodeAdapter.getRouteStrategy().stream().anyMatch(s -> MapUtils.isNotEmpty(s.getFilterStrategy()))) {
                    GlobalMap.MapNode mapNode = mapNodeAdapter.getMapNode();
                    AssertUtil.notNull(mapNode);

                    List<TaskRouterInflectionPoint> filterInflectionPointList = mapNodeAdapter.getRouteStrategy().stream()
                            .filter(s -> MapUtils.isNotEmpty(s.getFilterStrategy()))
                            .map(TaskStoryDefConfig.RouteStrategyDefItemConfig::getFilterStrategy)
                            .flatMap(s -> getInflectionPointStrategy(s, InflectionPointTypeEnum.CONDITION).stream())
                            .collect(Collectors.toList());
                    mapNode.getRouteNode().setFilterInflectionPointList(filterInflectionPointList);
                }
                if (CollectionUtils.isEmpty(mapNodeAdapter.getRouteStrategy()) || mapNodeAdapter.getRouteStrategy().stream().noneMatch(s -> MapUtils.isNotEmpty(s.getMatchStrategy()))) {
                    AssertUtil.notNull(mapNodeAdapter.getMapNode());
                    if (beforeMapNode == null) {
                        beforeMapNode = mapNodeAdapter.getMapNode();
                        storyNodeMap.put(k, Lists.newArrayList(mapNodeAdapter.getMapNode()));
                        continue;
                    }
                    beforeMapNode.addNextMapNode(mapNodeAdapter.getMapNode());
                    beforeMapNode = mapNodeAdapter.getMapNode();
                    continue;
                }

                mapNodeAdapter.getRouteStrategy().stream().filter(s -> StringUtils.isNotBlank(s.getNextStory())).forEach(config ->{
                    List<MapNodeAdapterForConfigResolver> mapNodeAdapterForConfigResolvers = storyNodeAdaptorMap.get(config.getNextStory());

                });
            }
        });
        return storyNodeMap;
    }

    private List<TaskRouterInflectionPoint> getInflectionPointStrategy(Map<String, String> strategy, InflectionPointTypeEnum inflectionPointTypeEnum) {
        AssertUtil.notNull(inflectionPointTypeEnum);
        List<TaskRouterInflectionPoint> inflectionPointList = new ArrayList<>();
        if (MapUtils.isEmpty(strategy)) {
            return inflectionPointList;
        }

        strategy.forEach((k, v) -> {
            AssertUtil.anyNotBlank(k, v);
            String[] splitKeyArray = k.split("-");
            AssertUtil.isTrue(splitKeyArray.length == 2, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);

            TaskRouterInflectionPoint inflectionPoint = new TaskRouterInflectionPoint();
            inflectionPoint.setInflectionPointTypeEnum(inflectionPointTypeEnum);
            inflectionPoint.setExpectedValue(v);
            inflectionPoint.setFieldName(splitKeyArray[1]);
            inflectionPoint.setMatchingStrategy(GlobalUtil.notEmpty(CalculateEnum.getCalculateEnumByName(splitKeyArray[0])).getExpression());
            inflectionPointList.add(inflectionPoint);
        });
        return inflectionPointList;
    }

//    private GlobalMap.MapNode parsePlanningNodeItem(TaskRouteConfig.StoryDefinitionNodeItem planningNodeItem) {
//        RouteNode routeNode = routeNodeMap.get(planningNodeItem.getNodeName());
//        AssertUtil.notNull(routeNode);
//        MapNodeAdapterForConfigResolver mapNode = new MapNodeAdapterForConfigResolver(routeNode.cloneRouteNode());
//        mapNode.setMappingNameList(planningNodeItem.getMapping());
//        mapNode.getRouteNode().setInterruptTimeSlot(BooleanUtils.isTrue(planningNodeItem.getInterruptTimeSlot()));
//        if (MapUtils.isEmpty(planningNodeItem.getRouteMap())) {
//            return mapNode;
//        }
//
//        Map<String, List<TaskRouteConfig.StoryDefinitionNodeItem>> routeMap = planningNodeItem.getRouteMap();
//        routeMap.forEach((kIn, vIn) -> {
//            GlobalMap.MapNode next = null;
//            List<TaskRouterInflectionPoint> inflectionPointList = getInflectionPoints(planningNodeItem, kIn);
//            for (TaskRouteConfig.StoryDefinitionNodeItem item : vIn) {
//                GlobalMap.MapNode mapNodeIn = parsePlanningNodeItem(item);
//                if (next == null) {
//                    next = mapNodeIn;
//                    mapNodeIn.getRouteNode().setInflectionPointList(inflectionPointList);
//                    mapNode.addNextMapNode(mapNodeIn);
//                    continue;
//                }
//                next.addNextMapNode(mapNodeIn);
//                next = mapNodeIn;
//            }
//        });
//        return mapNode;
//    }

    private Map<String, RequestMappingGroup> parseMappingGroupMap() {

        Map<String, RequestMappingGroup> mappingGroupMap = new HashMap<>();
        TaskStoryDefConfig.RequestMappingDef<String, TaskStoryDefConfig.RequestMappingDefItem<String, String>> requestMappingDef = taskStoryDefConfig.getRequestMappingDef();
        if (MapUtils.isEmpty(requestMappingDef)) {
            return mappingGroupMap;
        }

        requestMappingDef.keySet().forEach(key -> {
            TaskStoryDefConfig.RequestMappingDefItem<String, String> value = requestMappingDef.get(key);
            if (MapUtils.isEmpty(value)) {
                return;
            }

            List<RequestMappingGroup.RequestMappingItem> mappingItemList = new ArrayList<>();
            value.forEach((kIn, vIn) -> {
                if (vIn.toUpperCase().startsWith("DEFAULT['")) {
                    vIn = "#" + GlobalBus.DEFAULT_GLOBAL_BUS_PARAMS_KEY.identityStr() + vIn.substring(7);
                }
                if (!vIn.startsWith("#")) {
                    String nodeName = vIn.substring(0, vIn.indexOf("["));
                    RouteNode routeNode = this.routeNodeMap.get(nodeName);
                    AssertUtil.notNull(routeNode, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
                    vIn = "#" + routeNode.identityStr() + vIn.replaceFirst("^" + nodeName, StringUtils.EMPTY);
                }

                RequestMappingGroup.RequestMappingItem item = new RequestMappingGroup.RequestMappingItem();
                item.setSource(kIn);
                item.setTarget(vIn);
                mappingItemList.add(item);
            });
            RequestMappingGroup mappingGroup = new RequestMappingGroup();
            mappingGroup.setMappingItemList(mappingItemList);
            mappingGroupMap.put(key, mappingGroup);
        });

        return mappingGroupMap;
    }

    private List<TaskRouterInflectionPoint> getInflectionPoints(TaskRouteConfig.StoryDefinitionNodeItem planningNodeItem, String strategyKey) {
        Map<String, Map<String, String>> routeStrategy = planningNodeItem.getRouteStrategy();
        AssertUtil.notEmpty(routeStrategy);
        Map<String, String> strategyMap = routeStrategy.get(strategyKey);
        AssertUtil.notEmpty(strategyMap);

        List<TaskRouterInflectionPoint> inflectionPointList = new ArrayList<>();
        strategyMap.forEach((k, v) -> {
            AssertUtil.anyNotBlank(k, v);
            String[] splitKeyArray = k.split("-");
            AssertUtil.isTrue(splitKeyArray.length == 3, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);

            TaskRouterInflectionPoint inflectionPoint = new TaskRouterInflectionPoint();
            inflectionPoint.setInflectionPointTypeEnum(GlobalUtil.notEmpty(InflectionPointTypeEnum.getInflectionPointTypeEnum(splitKeyArray[0])));
            inflectionPoint.setExpectedValue(v);
            inflectionPoint.setFieldName(splitKeyArray[1]);
            inflectionPoint.setMatchingStrategy(GlobalUtil.notEmpty(CalculateEnum.getCalculateEnumByName(splitKeyArray[2])).getExpression());
            inflectionPointList.add(inflectionPoint);
        });
        return inflectionPointList;
    }

    private Map<String, RouteNode> parseRouteNodeMap() {
        Map<String, RouteNode> routeNodeMap = new HashMap<>();
        TaskStoryDefConfig.NodeDef<String, TaskStoryDefConfig.NodeDefItem<String, TaskStoryDefConfig.NodeDefItemConfig>> nodeDef = taskStoryDefConfig.getNodeDef();
        if (MapUtils.isEmpty(nodeDef)) {
            return routeNodeMap;
        }

        nodeDef.keySet().forEach(key -> {
            TaskStoryDefConfig.NodeDefItem<String, TaskStoryDefConfig.NodeDefItemConfig> value = nodeDef.get(key);
            if (MapUtils.isEmpty(value)) {
                return;
            }
            value.forEach((kIn, vIn) -> {
                RouteNode routeNode = new RouteNode();
                routeNode.setActionName(vIn.getMethod());
                routeNode.setComponentTypeEnum(GlobalUtil.notEmpty(ComponentTypeEnum.getComponentTypeEnumByName(vIn.getType())));
                routeNode.setNodeName(key);
                routeNodeMap.put(kIn, routeNode);
            });
        });
        return routeNodeMap;
    }

    /**
     * 解析 MapNode 时使用的辅助包装类
     */
    private static class MapNodeAdapterForConfigResolver {

        private GlobalMap.MapNode mapNode;

        private TaskStoryDefConfig.RouteStrategyDefItem<TaskStoryDefConfig.RouteStrategyDefItemConfig> routeStrategy;

        public GlobalMap.MapNode getMapNode() {
            return mapNode;
        }

        public void setMapNode(GlobalMap.MapNode mapNode) {
            this.mapNode = mapNode;
        }

        public TaskStoryDefConfig.RouteStrategyDefItem<TaskStoryDefConfig.RouteStrategyDefItemConfig> getRouteStrategy() {
            return routeStrategy;
        }

        public void setRouteStrategy(TaskStoryDefConfig.RouteStrategyDefItem<TaskStoryDefConfig.RouteStrategyDefItemConfig> routeStrategy) {
            this.routeStrategy = routeStrategy;
        }
    }

    public String getTaskStoryConfigName(String taskStoryConfigName) {
        if (StringUtils.isBlank(taskStoryConfigName)) {
            return "task-story-config.json";
        }
        return taskStoryConfigName;
    }
}
