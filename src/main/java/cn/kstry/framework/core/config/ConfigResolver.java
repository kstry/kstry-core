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
import cn.kstry.framework.core.engine.EventGroup;
import cn.kstry.framework.core.enums.CalculateEnum;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.enums.InflectionPointTypeEnum;
import cn.kstry.framework.core.enums.StrategyTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.route.EventNode;
import cn.kstry.framework.core.route.TaskNode;
import cn.kstry.framework.core.route.TaskRouterInflectionPoint;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.TaskActionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

    private Map<String, TaskNode> taskNodeMap;

    private Map<String, RequestMappingGroup> mappingGroupMap;

    public Map<String, List<EventNode>> getStoryMapNode(List<EventGroup> eventGroupList, String eventStoryConfigName) {

        if (CollectionUtils.isEmpty(eventGroupList)) {
            return Maps.newHashMap();
        }

        // 解析配置文件
        this.eventStoryDefConfig = FileEventStoryDefConfig.parseEventStoryConfig(getEventStoryConfigName(eventStoryConfigName));
        AssertUtil.notNull(this.eventStoryDefConfig);

        // 根据 event_def 初始化 task_node(task_node 是 event_node 在执行时的叫法，event_node 是静态定义) 定义
        this.taskNodeMap = parseTaskNodeMap();
        if (MapUtils.isEmpty(this.taskNodeMap)) {
            return Maps.newHashMap();
        }

        // 为 event_node 设置 TaskActionMethod
        taskNodeMap.values().forEach(node -> node.setTaskActionMethod(TaskActionUtil.getTaskActionMethod(eventGroupList, node)));

        // 根据 request_mapping_def 初始化 参数映射规则表
        this.mappingGroupMap = parseMappingGroupMap();

        // 根据 event_story 初始化 story 的 MapNode（事件故事）
        return parseStoryDef();
    }

    private Map<String, List<EventNode>> parseStoryDef() {
        EventStoryDefConfig.StoryDef<String, EventStoryDefConfig.StoryDefItem<EventStoryDefConfig.StoryDefItemConfig>> storyDef = eventStoryDefConfig.getStoryDef();
        Map<String, List<EventNode>> storyNodeMap = new HashMap<>();
        if (MapUtils.isEmpty(storyDef)) {
            return storyNodeMap;
        }

        Map<String, List<TaskNodeProxyForConfigResolver>> storyNodeAdaptorMap = new HashMap<>();
        storyDef.keySet().forEach(key -> {
            EventStoryDefConfig.StoryDefItem<EventStoryDefConfig.StoryDefItemConfig> value = storyDef.get(key);
            if (CollectionUtils.isEmpty(value)) {
                return;
            }

            List<TaskNodeProxyForConfigResolver> mapNodeAdapterList = new ArrayList<>();
            for (EventStoryDefConfig.StoryDefItemConfig item : value) {
                TaskNodeProxyForConfigResolver mapNodeAgent = new TaskNodeProxyForConfigResolver();
                if (StringUtils.isNotBlank(item.getEventNode())) {
                    TaskNode taskNode = this.taskNodeMap.get(item.getEventNode());
                    AssertUtil.notNull(taskNode);
                    mapNodeAgent.setTaskNode(taskNode);
                }

                if (StringUtils.isNotEmpty(item.getRequestMapping()) && mapNodeAgent.getTaskNode() != null) {
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

            List<EventNode> firstMapNodeList = new ArrayList<>();

            taskStack.push(new ParseStoryNodeTaskItem(v, null, null));
            for (ParseStoryNodeTaskItem taskItem = taskStack.poll(); taskItem != null && CollectionUtils.isNotEmpty(taskItem.getNodeDefQueue()); taskItem = taskStack.poll()) {

                EventNode beforeMapNode = taskItem.getBeforeMapNode();
                List<TaskRouterInflectionPoint> matchInflectionPointList = taskItem.getMatchInflectionPointList();
                for (TaskNodeProxyForConfigResolver nodeAgent : taskItem.getNodeDefQueue()) {
                    if (MapUtils.isEmpty(nodeAgent.getNextRouteNodeInflectionPointMap()) || nodeAgent.getTaskNode() != null) {
                        EventNode mapNode = nodeAgent.buildMapNode();
                        if (matchInflectionPointList != null) {
                            mapNode.getTaskNode().setInflectionPointList(matchInflectionPointList);
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

                    EventNode finalBeforeMapNode = beforeMapNode;
                    Map<String, List<TaskRouterInflectionPoint>> nextInflectionPointMap = nodeAgent.getNextRouteNodeInflectionPointMap();
                    nextInflectionPointMap.forEach((kIn, vIn) -> taskStack.push(new ParseStoryNodeTaskItem(storyNodeAdaptorMap.get(kIn), finalBeforeMapNode, vIn)));
                }
            }
            storyNodeMap.put(k, firstMapNodeList);
            taskStack.clear();
        });
        return storyNodeMap;
    }

    private void parseRouteStrategyForRouteNodeAgent(TaskNodeProxyForConfigResolver mapNodeAgent,
                                                     EventStoryDefConfig.StrategyDefItem<EventStoryDefConfig.StrategyDefItemConfig> routeStrategy) {
        if (CollectionUtils.isEmpty(routeStrategy)) {
            return;
        }

        if (mapNodeAgent.getTaskNode() != null && routeStrategy.stream().anyMatch(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.FILTER))) {
            List<TaskRouterInflectionPoint> filterInflectionPointList = routeStrategy.stream()
                    .filter(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.FILTER) && MapUtils.isNotEmpty(s.getRuleSet()))
                    .map(EventStoryDefConfig.StrategyDefItemConfig::getRuleSet)
                    .flatMap(s -> getInflectionPointStrategy(s, InflectionPointTypeEnum.CONDITION).stream())
                    .collect(Collectors.toList());
            mapNodeAgent.setFilterInflectionPointList(filterInflectionPointList);
        }

        if (routeStrategy.stream().anyMatch(s -> StringUtils.isNotBlank(s.getNextStory()))) {
            Map<String, List<TaskRouterInflectionPoint>> nextRouteNodeInflectionPointMap = new HashMap<>();
            routeStrategy.stream().filter(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.MATCH) && StringUtils.isNotBlank(s.getNextStory())).forEach(config -> {
                if (MapUtils.isEmpty(config.getRuleSet())) {
                    nextRouteNodeInflectionPointMap.put(config.getNextStory(), Lists.newArrayList());
                    return;
                }

                nextRouteNodeInflectionPointMap.put(config.getNextStory(), getInflectionPointStrategy(config.getRuleSet(), InflectionPointTypeEnum.INVOKE));
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
                    TaskNode taskNode = this.taskNodeMap.get(nodeName);
                    AssertUtil.notNull(taskNode, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
                    vIn = "#" + taskNode.identityStr() + vIn.replaceFirst("^" + nodeName, StringUtils.EMPTY);
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

    private Map<String, TaskNode> parseTaskNodeMap() {
        Map<String, TaskNode> taskNodeMap = new HashMap<>();
        EventStoryDefConfig.EventDef<String, EventStoryDefConfig.EventDefItem<String, EventStoryDefConfig.EventDefItemConfig>> eventDef = eventStoryDefConfig.getEventDef();
        if (MapUtils.isEmpty(eventDef)) {
            return taskNodeMap;
        }

        eventDef.keySet().forEach(key -> {
            EventStoryDefConfig.EventDefItem<String, EventStoryDefConfig.EventDefItemConfig> value = eventDef.get(key);
            if (MapUtils.isEmpty(value)) {
                return;
            }

            value.forEach((kIn, vIn) -> {
                TaskNode taskNode = new TaskNode();
                taskNode.setActionName(vIn.getEventAction());
                taskNode.setEventGroupTypeEnum(GlobalUtil.notEmpty(ComponentTypeEnum.getComponentTypeEnumByName(vIn.getEventType())));
                taskNode.setEventGroupName(key);
                taskNodeMap.put(kIn, taskNode);
            });
        });
        return taskNodeMap;
    }

    private String getEventStoryConfigName(String eventStoryConfigName) {
        if (StringUtils.isBlank(eventStoryConfigName)) {
            return "event-story-config.json";
        }
        return eventStoryConfigName;
    }

    /**
     * 解析 TaskNode 时使用的辅助包装类
     */
    private static class TaskNodeProxyForConfigResolver {

        private TaskNode taskNode;

        private RequestMappingGroup requestMappingGroup;

        private List<TaskRouterInflectionPoint> filterInflectionPointList;

        private Map<String, List<TaskRouterInflectionPoint>> nextRouteNodeInflectionPointMap;

        public TaskNode getTaskNode() {
            return taskNode;
        }

        public void setTaskNode(TaskNode taskNode) {
            this.taskNode = taskNode;
        }

        public EventNode buildMapNode() {
            if (getTaskNode() == null) {
                return null;
            }
            TaskNode taskNode = getTaskNode().cloneRouteNode();
            taskNode.setFilterInflectionPointList(getFilterInflectionPointList());
            taskNode.setRequestMappingGroup(getRequestMappingGroup());
            return new EventNode(taskNode);
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

        private final EventNode beforeMapNode;

        private final List<TaskNodeProxyForConfigResolver> nodeDefQueue;

        private final List<TaskRouterInflectionPoint> matchInflectionPointList;

        public ParseStoryNodeTaskItem(List<TaskNodeProxyForConfigResolver> nodeDefQueue, EventNode beforeMapNode,
                                      List<TaskRouterInflectionPoint> matchInflectionPointList) {
            this.nodeDefQueue = nodeDefQueue;
            this.beforeMapNode = beforeMapNode;
            this.matchInflectionPointList = matchInflectionPointList;
        }

        public EventNode getBeforeMapNode() {
            return beforeMapNode;
        }

        public List<TaskNodeProxyForConfigResolver> getNodeDefQueue() {
            return nodeDefQueue;
        }

        public List<TaskRouterInflectionPoint> getMatchInflectionPointList() {
            return matchInflectionPointList;
        }
    }
}
