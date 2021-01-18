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
import cn.kstry.framework.core.enums.StrategyTypeEnum;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lykan
 */
public class ConfigResolver {

    private EventStoryDefConfig eventStoryDefConfig;

    private Map<String, RouteNode> routeNodeMap;

    private Map<String, RequestMappingGroup> mappingGroupMap;

    public void initNodeAndMapping(String eventStoryConfigName) {
        this.eventStoryDefConfig = FileEventStoryDefConfig.parseEventStoryConfig(getEventStoryConfigName(eventStoryConfigName));
        this.routeNodeMap = parseRouteNodeMap();
        this.mappingGroupMap = parseMappingGroupMap();
    }

    public Map<String, RouteNode> getRouteNodeMap() {
        return this.routeNodeMap;
    }

    public Map<String, List<GlobalMap.MapNode>> parseStoryDef() {
        EventStoryDefConfig.StoryDef<String, EventStoryDefConfig.StoryDefItem<EventStoryDefConfig.StoryDefItemConfig>> storyDef = eventStoryDefConfig.getStoryDef();
        Map<String, List<GlobalMap.MapNode>> storyNodeMap = new HashMap<>();
        if (MapUtils.isEmpty(storyDef)) {
            return storyNodeMap;
        }

        Map<String, List<RouteNodeAgentForConfigResolver>> storyNodeAdaptorMap = new HashMap<>();
        storyDef.keySet().forEach(key -> {
            EventStoryDefConfig.StoryDefItem<EventStoryDefConfig.StoryDefItemConfig> value = storyDef.get(key);
            if (CollectionUtils.isEmpty(value)) {
                return;
            }

            List<RouteNodeAgentForConfigResolver> mapNodeAdapterList = new ArrayList<>();
            for (EventStoryDefConfig.StoryDefItemConfig item : value) {
                RouteNodeAgentForConfigResolver mapNodeAgent = new RouteNodeAgentForConfigResolver();
                if (StringUtils.isNotBlank(item.getEventNode())) {
                    RouteNode routeNode = this.routeNodeMap.get(item.getEventNode());
                    AssertUtil.notNull(routeNode);
                    mapNodeAgent.setRouteNode(routeNode);
                }

                if (StringUtils.isNotEmpty(item.getRequestMapping()) && mapNodeAgent.getRouteNode() != null) {
                    RequestMappingGroup mappingGroup = this.mappingGroupMap.get(item.getRequestMapping());
                    AssertUtil.notNull(mappingGroup);
                    mapNodeAgent.setRequestMappingGroup(mappingGroup);
                }

                if (MapUtils.isNotEmpty(eventStoryDefConfig.getStrategyDef()) && StringUtils.isNotBlank(item.getStrategy())) {
                    EventStoryDefConfig.StrategyDefItem<EventStoryDefConfig.StrategyDefItemConfig> routeStrategy =
                            eventStoryDefConfig.getStrategyDef().get(item.getStrategy());
                    AssertUtil.notNull(routeStrategy);
                    parseRouteStrategyForRouteNodeAgent(mapNodeAgent, routeStrategy);
                }
                mapNodeAdapterList.add(mapNodeAgent);
            }
            storyNodeAdaptorMap.put(key, mapNodeAdapterList);
        });

        LinkedList<ParseStoryNodeTaskItem> taskStack = new LinkedList<>();
        storyNodeAdaptorMap.forEach((k, v) -> {

            List<GlobalMap.MapNode> firstMapNodeList = new ArrayList<>();

            taskStack.push(new ParseStoryNodeTaskItem(v, null, null));
            for (ParseStoryNodeTaskItem taskItem = taskStack.poll(); taskItem != null && CollectionUtils.isNotEmpty(taskItem.getNodeDefQueue()); taskItem = taskStack.poll()) {

                GlobalMap.MapNode beforeMapNode = taskItem.getBeforeMapNode();
                List<TaskRouterInflectionPoint> matchInflectionPointList = taskItem.getMatchInflectionPointList();
                for (RouteNodeAgentForConfigResolver nodeAgent : taskItem.getNodeDefQueue()) {
                    if (MapUtils.isEmpty(nodeAgent.getNextRouteNodeInflectionPointMap()) || nodeAgent.getRouteNode() != null) {
                        GlobalMap.MapNode mapNode = nodeAgent.buildMapNode();
                        if (matchInflectionPointList != null) {
                            mapNode.getRouteNode().setInflectionPointList(matchInflectionPointList);
                            matchInflectionPointList = null;
                        }

                        if (beforeMapNode == null) {
                            firstMapNodeList.add(mapNode);
                        } else {
                            beforeMapNode.addNextMapNode(mapNode);
                        }
                        beforeMapNode = mapNode;
                        if (MapUtils.isEmpty(nodeAgent.getNextRouteNodeInflectionPointMap())) {
                            continue;
                        }
                    }

                    if (beforeMapNode == null) {
                        nodeAgent.getNextRouteNodeInflectionPointMap().forEach((kIn, vIn) -> taskStack.push(new ParseStoryNodeTaskItem(storyNodeAdaptorMap.get(kIn), null, vIn)));
                        continue;
                    }

                    GlobalMap.MapNode finalBeforeMapNode = beforeMapNode;
                    Map<String, List<TaskRouterInflectionPoint>> nextInflectionPointMap = nodeAgent.getNextRouteNodeInflectionPointMap();
                    nextInflectionPointMap.forEach((kIn, vIn) -> taskStack.push(new ParseStoryNodeTaskItem(storyNodeAdaptorMap.get(kIn), finalBeforeMapNode, vIn)));
                }
            }
            storyNodeMap.put(k, firstMapNodeList);
            taskStack.clear();
        });
        return storyNodeMap;
    }

    private void parseRouteStrategyForRouteNodeAgent(RouteNodeAgentForConfigResolver mapNodeAgent,
                                                     EventStoryDefConfig.StrategyDefItem<EventStoryDefConfig.StrategyDefItemConfig> routeStrategy) {
        if (CollectionUtils.isEmpty(routeStrategy)) {
            return;
        }

        if (mapNodeAgent.getRouteNode() != null && routeStrategy.stream().anyMatch(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.FILTER))) {
            List<TaskRouterInflectionPoint> filterInflectionPointList = routeStrategy.stream()
                    .filter(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.FILTER) && MapUtils.isNotEmpty(s.getStrategy()))
                    .map(EventStoryDefConfig.StrategyDefItemConfig::getStrategy)
                    .flatMap(s -> getInflectionPointStrategy(s, InflectionPointTypeEnum.CONDITION).stream())
                    .collect(Collectors.toList());
            mapNodeAgent.setFilterInflectionPointList(filterInflectionPointList);
        }

        if (routeStrategy.stream().anyMatch(s -> StringUtils.isNotBlank(s.getNextStory()))) {
            Map<String, List<TaskRouterInflectionPoint>> nextRouteNodeInflectionPointMap = new HashMap<>();
            routeStrategy.stream().filter(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.MATCH) && StringUtils.isNotBlank(s.getNextStory())).forEach(config -> {
                if (MapUtils.isEmpty(config.getStrategy())) {
                    nextRouteNodeInflectionPointMap.put(config.getNextStory(), Lists.newArrayList());
                    return;
                }

                nextRouteNodeInflectionPointMap.put(config.getNextStory(), getInflectionPointStrategy(config.getStrategy(), InflectionPointTypeEnum.INVOKE));
            });
            mapNodeAgent.setNextRouteNodeInflectionPointMap(nextRouteNodeInflectionPointMap);
        }
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

    private Map<String, RequestMappingGroup> parseMappingGroupMap() {

        Map<String, RequestMappingGroup> mappingGroupMap = new HashMap<>();
        EventStoryDefConfig.RequestMappingDef<String, EventStoryDefConfig.RequestMappingDefItem<String, String>> requestMappingDef = eventStoryDefConfig.getRequestMappingDef();
        if (MapUtils.isEmpty(requestMappingDef)) {
            return mappingGroupMap;
        }

        requestMappingDef.keySet().forEach(key -> {
            EventStoryDefConfig.RequestMappingDefItem<String, String> value = requestMappingDef.get(key);
            if (MapUtils.isEmpty(value)) {
                RequestMappingGroup mappingGroup = new RequestMappingGroup();
                mappingGroup.setMappingItemList(Lists.newArrayList());
                mappingGroupMap.put(key, mappingGroup);
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

    public String getEventStoryConfigName(String eventStoryConfigName) {
        if (StringUtils.isBlank(eventStoryConfigName)) {
            return "event-story-config.json";
        }
        return eventStoryConfigName;
    }

    private Map<String, RouteNode> parseRouteNodeMap() {
        Map<String, RouteNode> routeNodeMap = new HashMap<>();
        EventStoryDefConfig.EventDef<String, EventStoryDefConfig.EventDefItem<String, EventStoryDefConfig.EventDefItemConfig>> eventDef = eventStoryDefConfig.getEventDef();
        if (MapUtils.isEmpty(eventDef)) {
            return routeNodeMap;
        }

        eventDef.keySet().forEach(key -> {
            EventStoryDefConfig.EventDefItem<String, EventStoryDefConfig.EventDefItemConfig> value = eventDef.get(key);
            if (MapUtils.isEmpty(value)) {
                return;
            }
            value.forEach((kIn, vIn) -> {
                RouteNode routeNode = new RouteNode();
                routeNode.setActionName(vIn.getEventAction());
                routeNode.setComponentTypeEnum(GlobalUtil.notEmpty(ComponentTypeEnum.getComponentTypeEnumByName(vIn.getEventType())));
                routeNode.setNodeName(key);
                routeNodeMap.put(kIn, routeNode);
            });
        });
        return routeNodeMap;
    }

    /**
     * 解析 MapNode 时使用的辅助包装类
     */
    private static class RouteNodeAgentForConfigResolver {

        private RouteNode routeNode;

        private RequestMappingGroup requestMappingGroup;

        private List<TaskRouterInflectionPoint> filterInflectionPointList;

        private Map<String, List<TaskRouterInflectionPoint>> nextRouteNodeInflectionPointMap;

        public RouteNode getRouteNode() {
            return routeNode;
        }

        public void setRouteNode(RouteNode routeNode) {
            this.routeNode = routeNode;
        }

        public GlobalMap.MapNode buildMapNode() {
            if (getRouteNode() == null) {
                return null;
            }
            RouteNode routeNode = getRouteNode().cloneRouteNode();
            routeNode.setFilterInflectionPointList(getFilterInflectionPointList());
            routeNode.setRequestMappingGroup(getRequestMappingGroup());
            return new GlobalMap.MapNode(routeNode);
        }

        public RequestMappingGroup getRequestMappingGroup() {
            return requestMappingGroup;
        }

        public void setRequestMappingGroup(RequestMappingGroup requestMappingGroup) {
            this.requestMappingGroup = requestMappingGroup;
        }

        public List<TaskRouterInflectionPoint> getFilterInflectionPointList() {
            return filterInflectionPointList;
        }

        public void setFilterInflectionPointList(List<TaskRouterInflectionPoint> filterInflectionPointList) {
            this.filterInflectionPointList = filterInflectionPointList;
        }

        public Map<String, List<TaskRouterInflectionPoint>> getNextRouteNodeInflectionPointMap() {
            return nextRouteNodeInflectionPointMap;
        }

        public void setNextRouteNodeInflectionPointMap(Map<String, List<TaskRouterInflectionPoint>> nextRouteNodeInflectionPointMap) {
            this.nextRouteNodeInflectionPointMap = nextRouteNodeInflectionPointMap;
        }
    }

    private static class ParseStoryNodeTaskItem {

        private final GlobalMap.MapNode beforeMapNode;

        private final List<RouteNodeAgentForConfigResolver> nodeDefQueue;

        private final List<TaskRouterInflectionPoint> matchInflectionPointList;

        public ParseStoryNodeTaskItem(List<RouteNodeAgentForConfigResolver> nodeDefQueue, GlobalMap.MapNode beforeMapNode,
                                      List<TaskRouterInflectionPoint> matchInflectionPointList) {
            this.nodeDefQueue = nodeDefQueue;
            this.beforeMapNode = beforeMapNode;
            this.matchInflectionPointList = matchInflectionPointList;
        }

        public GlobalMap.MapNode getBeforeMapNode() {
            return beforeMapNode;
        }

        public List<RouteNodeAgentForConfigResolver> getNodeDefQueue() {
            return nodeDefQueue;
        }

        public List<TaskRouterInflectionPoint> getMatchInflectionPointList() {
            return matchInflectionPointList;
        }
    }
}
