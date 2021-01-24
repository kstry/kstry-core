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

import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.engine.EventGroup;
import cn.kstry.framework.core.enums.CalculatorEnum;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.enums.StrategyTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.route.*;
import cn.kstry.framework.core.timeslot.TimeSlotOperatorRole;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.TaskActionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lykan
 */
public class ConfigResolver {

    /**
     * 承载 解析后的 配置文件
     */
    private EventStoryDefConfig eventStoryDefConfig;

    /**
     * Task Node Map
     */
    private Map<String, TaskNode> taskNodeMap;

    /**
     * Mapping Group Map
     */
    private Map<String, RequestMappingGroup> mappingGroupMap;

    /**
     * time slot task node
     */
    private TaskNode globalTimeSlotTaskNode;

    public Map<String, List<EventNode>> getStoryEventNode(List<EventGroup> eventGroupList, String eventStoryConfigName) {

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
        TaskNode timeSlotTaskNode = new TaskNode(TimeSlotOperatorRole.TIME_SLOT_TASK_ACTION_NAME, TimeSlotOperatorRole.TIME_SLOT_TASK_NAME, ComponentTypeEnum.TIME_SLOT);
        timeSlotTaskNode.setTaskActionMethod(TaskActionUtil.getTaskActionMethod(eventGroupList, timeSlotTaskNode));
        this.globalTimeSlotTaskNode = timeSlotTaskNode;

        // 根据 request_mapping_def 初始化 参数映射规则表
        this.mappingGroupMap = parseMappingGroupMap();

        // 根据 event_story 初始化 story 的 EventNode（事件故事）
        return parseStoryDef();
    }

    private Map<String, List<EventNode>> parseStoryDef() {
        EventStoryDefConfig.StoryDef<String, EventStoryDefConfig.StoryDefItem<EventStoryDefConfig.StoryDefItemConfig>> storyDef = eventStoryDefConfig.getStoryDef();
        Map<String, List<EventNode>> storyNodeMap = new HashMap<>();
        if (MapUtils.isEmpty(storyDef)) {
            return storyNodeMap;
        }

        Map<String, List<TaskNodeAgentForConfigResolver>> storyNodeAdaptorMap = new HashMap<>();
        storyDef.keySet().forEach(key -> {
            EventStoryDefConfig.StoryDefItem<EventStoryDefConfig.StoryDefItemConfig> value = storyDef.get(key);
            if (CollectionUtils.isEmpty(value)) {
                return;
            }

            List<TaskNodeAgentForConfigResolver> eventNodeAdapterList = new ArrayList<>();
            for (EventStoryDefConfig.StoryDefItemConfig item : value) {
                TaskNodeAgentForConfigResolver eventNodeAgent = new TaskNodeAgentForConfigResolver();
                eventNodeAgent.setGlobalTimeSlotTaskNode(this.globalTimeSlotTaskNode);
                if (StringUtils.isNotBlank(item.getEventNode())) {
                    TaskNode taskNode = this.taskNodeMap.get(item.getEventNode());
                    AssertUtil.notNull(taskNode);
                    eventNodeAgent.setTaskNode(taskNode);
                }

                if (StringUtils.isNotEmpty(item.getRequestMapping())) {
                    RequestMappingGroup mappingGroup = this.mappingGroupMap.get(item.getRequestMapping());
                    AssertUtil.notNull(mappingGroup);
                    eventNodeAgent.setRequestMappingGroup(mappingGroup);
                }

                if (MapUtils.isNotEmpty(eventStoryDefConfig.getStrategyDef()) && StringUtils.isNotBlank(item.getStrategy())) {
                    EventStoryDefConfig.StrategyDefItem<EventStoryDefConfig.StrategyDefItemConfig> routeStrategy =
                            eventStoryDefConfig.getStrategyDef().get(item.getStrategy());
                    AssertUtil.notNull(routeStrategy);
                    if (CollectionUtils.isEmpty(routeStrategy)) {
                        continue;
                    }
                    if (parseStrategyInfoReturnInterruptFlag(eventNodeAgent, routeStrategy, item.getStrategy())) {
                        eventNodeAdapterList.add(eventNodeAgent);
                        break;
                    }
                }
                eventNodeAdapterList.add(eventNodeAgent);
            }
            storyNodeAdaptorMap.put(key, eventNodeAdapterList);
        });

        storyNodeAdaptorMap.forEach((k, v) -> {
            AssertUtil.notEmpty(v);
            storyNodeMap.put(k, initEventNodePipeline(0, storyNodeAdaptorMap, v));
        });
        return storyNodeMap;
    }

    private List<EventNode> initEventNodePipeline(int invokeLevel, Map<String, List<TaskNodeAgentForConfigResolver>> storyNodeAdaptorMap, List<TaskNodeAgentForConfigResolver> v) {

        List<EventNode> firstEventNodeList = new ArrayList<>();

        LinkedList<ParseStoryNodeTaskItem> taskStack = new LinkedList<>();
        taskStack.push(new ParseStoryNodeTaskItem(v, null, null));
        for (ParseStoryNodeTaskItem taskItem = taskStack.poll(); taskItem != null && CollectionUtils.isNotEmpty(taskItem.getNodeDefQueue()); taskItem = taskStack.poll()) {

            EventNode beforeEventNode = taskItem.getBeforeEventNode();
            int maxLevelDepth = (beforeEventNode != null && beforeEventNode.getNodeLevel() > invokeLevel) ? beforeEventNode.getNodeLevel() : invokeLevel;
            AssertUtil.isTrue(maxLevelDepth < GlobalConstant.MAX_NODE_LEVEL_DEPTH, ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                    "Event nodes with circular dependencies! levelDepth:%s", maxLevelDepth);

            List<StrategyRule> matchStrategyRuleList = taskItem.getStrategyRuleList();
            for (TaskNodeAgentForConfigResolver nodeAgent : taskItem.getNodeDefQueue()) {
                if (MapUtils.isEmpty(nodeAgent.getMatchStrategyRuleMap()) || nodeAgent.getTaskNode() != null || StringUtils.isNotBlank(nodeAgent.getFilterStoryName())) {
                    EventNode eventNode = nodeAgent.buildEventNode();

                    List<TaskNodeAgentForConfigResolver> taskNodeAgentList = storyNodeAdaptorMap.get(nodeAgent.getFilterStoryName());
                    if (eventNode instanceof TimeSlotEventNode && CollectionUtils.isNotEmpty(taskNodeAgentList)) {
                        List<EventNode> filterStoryEventNodeList = initEventNodePipeline(++invokeLevel, storyNodeAdaptorMap, taskNodeAgentList);
                        ((TimeSlotEventNode) eventNode).setFirstTimeSlotEventNodeList(filterStoryEventNodeList);
                        AssertUtil.notBlank(nodeAgent.getStrategyName());
                        ((TimeSlotEventNode) eventNode).setStrategyName(nodeAgent.getStrategyName());
                    }
                    if (matchStrategyRuleList != null) {
                        eventNode.setMatchStrategyRuleList(matchStrategyRuleList);
                        matchStrategyRuleList = null;
                    }

                    if (beforeEventNode == null) {
                        eventNode.setNodeLevel(0);
                        firstEventNodeList.add(eventNode);
                    } else {
                        eventNode.setNodeLevel(beforeEventNode.getNodeLevel() + 1);
                        beforeEventNode.addNextEventNode(eventNode);
                    }
                    beforeEventNode = eventNode;
                }
                if (MapUtils.isEmpty(nodeAgent.getMatchStrategyRuleMap())) {
                    continue;
                }

                if (beforeEventNode == null) {
                    nodeAgent.getMatchStrategyRuleMap().forEach((kIn, vIn) -> taskStack.push(new ParseStoryNodeTaskItem(storyNodeAdaptorMap.get(kIn), null, vIn)));
                    break;
                }

                EventNode finalBeforeEventNode = beforeEventNode;
                Map<String, List<StrategyRule>> nextInflectionPointMap = nodeAgent.getMatchStrategyRuleMap();
                nextInflectionPointMap.forEach((kIn, vIn) -> taskStack.push(new ParseStoryNodeTaskItem(storyNodeAdaptorMap.get(kIn), finalBeforeEventNode, vIn)));
                break;
            }
        }
        AssertUtil.notEmpty(firstEventNodeList);
        return firstEventNodeList;
    }

    private boolean parseStrategyInfoReturnInterruptFlag(TaskNodeAgentForConfigResolver eventNodeAgent,
                                                         EventStoryDefConfig.StrategyDefItem<EventStoryDefConfig.StrategyDefItemConfig> routeStrategy, String strategyName) {
        if (CollectionUtils.isEmpty(routeStrategy)) {
            return false;
        }

        if (routeStrategy.stream().anyMatch(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.FILTER))) {
            List<StrategyRule> filterInflectionPointList = routeStrategy.stream()
                    .filter(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.FILTER) && MapUtils.isNotEmpty(s.getRuleSet()))
                    .map(EventStoryDefConfig.StrategyDefItemConfig::getRuleSet)
                    .flatMap(s -> getStrategyRuleList(s, StrategyTypeEnum.FILTER).stream())
                    .collect(Collectors.toList());
            eventNodeAgent.setFilterStrategyRuleList(filterInflectionPointList);
            if (eventNodeAgent.getTaskNode() == null) {
                List<String> filterStoryNameList = routeStrategy.stream()
                        .filter(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.FILTER) && StringUtils.isNotBlank(s.getStory()))
                        .map(EventStoryDefConfig.StrategyDefItemConfig::getStory)
                        .collect(Collectors.toList());
                AssertUtil.oneSize(filterStoryNameList, ExceptionEnum.PARAMS_ERROR);
                eventNodeAgent.setFilterStoryName(filterStoryNameList.get(0));
                eventNodeAgent.setStrategyName(strategyName);
            }
        }

        if (routeStrategy.stream().anyMatch(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.MATCH))) {
            Map<String, List<StrategyRule>> matchStrategyRuleMap = new HashMap<>();
            routeStrategy.stream().filter(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.MATCH) && StringUtils.isNotBlank(s.getStory())).forEach(config -> {
                if (MapUtils.isEmpty(config.getRuleSet())) {
                    matchStrategyRuleMap.put(config.getStory(), Lists.newArrayList());
                    return;
                }

                matchStrategyRuleMap.put(config.getStory(), getStrategyRuleList(config.getRuleSet(), StrategyTypeEnum.MATCH));
            });
            eventNodeAgent.setMatchStrategyRuleMap(matchStrategyRuleMap);
            return true;
        }
        return false;
    }

    private List<StrategyRule> getStrategyRuleList(Map<String, String> strategy, StrategyTypeEnum strategyTypeEnum) {
        AssertUtil.notNull(strategyTypeEnum);
        List<StrategyRule> strategyRuleList = new ArrayList<>();
        if (MapUtils.isEmpty(strategy)) {
            return strategyRuleList;
        }

        strategy.forEach((k, v) -> {
            AssertUtil.notBlank(k);
            String[] splitKeyArray = k.split(GlobalConstant.DISTINCT_SIGN);
            AssertUtil.isTrue(splitKeyArray.length == 2, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);

            StrategyRule strategyRule = new StrategyRule();
            strategyRule.setStrategyTypeEnum(strategyTypeEnum);
            strategyRule.setExpectedValue(v);
            strategyRule.setFieldName(parseStrategyProperty(splitKeyArray[1]));
            strategyRule.setStrategyRuleCalculator(GlobalUtil.notEmpty(CalculatorEnum.getCalculatorEnumByName(splitKeyArray[0])).getExpression());
            strategyRuleList.add(strategyRule);
        });
        return strategyRuleList;
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
                RequestMappingGroup.RequestMappingItem item = new RequestMappingGroup.RequestMappingItem();
                item.setSource(parseStrategyProperty(vIn));
                item.setTarget(kIn);
                mappingItemList.add(item);
            });
            RequestMappingGroup mappingGroup = new RequestMappingGroup();
            mappingGroup.setMappingItemList(mappingItemList);
            mappingGroupMap.put(key, mappingGroup);
        });

        return mappingGroupMap;
    }

    private String parseStrategyProperty(String field) {
        AssertUtil.notBlank(field);

        String[] fieldList = field.split("\\.");
        for (int i = 0; i < fieldList.length; i++) {
            if (i == 0) {
                AssertUtil.isTrue(fieldList[0].startsWith(GlobalConstant.NODE_SIGN) || fieldList[0].startsWith(GlobalConstant.TIME_SLOT_NODE_SIGN));
            }

            if (!fieldList[i].startsWith(GlobalConstant.NODE_SIGN)) {
                continue;
            }
            switch (fieldList[i].toUpperCase()) {
                case GlobalConstant.NODE_SIGN + "REQ":
                    fieldList[i] = StoryBus.DEFAULT_GLOBAL_BUS_REQUEST_KEY.identity();
                    break;
                case GlobalConstant.NODE_SIGN + "STA":
                    fieldList[i] = StoryBus.DEFAULT_GLOBAL_BUS_STABLE_KEY.identity();
                    break;
                case GlobalConstant.NODE_SIGN + "VAR":
                    fieldList[i] = StoryBus.DEFAULT_GLOBAL_BUS_VARIABLE_KEY.identity();
                    break;
                default:
                    TaskNode taskNode = this.taskNodeMap.get(fieldList[i].substring(1));
                    AssertUtil.notNull(taskNode);
                    fieldList[i] = taskNode.identity();
                    break;
            }
        }
        return StringUtils.join(fieldList, ".");
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
    private static class TaskNodeAgentForConfigResolver {

        private TaskNode taskNode;

        private RequestMappingGroup requestMappingGroup;

        private List<StrategyRule> filterStrategyRuleList;

        private Map<String, List<StrategyRule>> matchStrategyRuleMap;

        private String filterStoryName;

        /**
         * time slot task node
         */
        private TaskNode globalTimeSlotTaskNode;

        private String strategyName;

        public TaskNode getTaskNode() {
            return taskNode;
        }

        public void setTaskNode(TaskNode taskNode) {
            this.taskNode = taskNode;
        }

        public EventNode buildEventNode() {
            EventNode eventNode = null;
            if (getTaskNode() != null) {
                eventNode = new EventNode(getTaskNode().cloneTaskNode());
            } else if (StringUtils.isNotBlank(getFilterStoryName())) {
                AssertUtil.notNull(this.globalTimeSlotTaskNode);
                TaskNode timeSlotTaskNode = this.globalTimeSlotTaskNode.cloneTaskNode();
                eventNode = new TimeSlotEventNode(timeSlotTaskNode);
            } else {
                KstryException.throwException(ExceptionEnum.SYSTEM_ERROR);
            }
            eventNode.setFilterStrategyRuleList(getFilterStrategyRuleList());
            eventNode.setRequestMappingGroup(getRequestMappingGroup());
            return eventNode;
        }

        public RequestMappingGroup getRequestMappingGroup() {
            return requestMappingGroup;
        }

        public void setRequestMappingGroup(RequestMappingGroup requestMappingGroup) {
            this.requestMappingGroup = requestMappingGroup;
        }

        public List<StrategyRule> getFilterStrategyRuleList() {
            return filterStrategyRuleList;
        }

        public void setFilterStrategyRuleList(List<StrategyRule> filterStrategyRuleList) {
            this.filterStrategyRuleList = filterStrategyRuleList;
        }

        public Map<String, List<StrategyRule>> getMatchStrategyRuleMap() {
            return matchStrategyRuleMap;
        }

        public void setMatchStrategyRuleMap(Map<String, List<StrategyRule>> matchStrategyRuleMap) {
            this.matchStrategyRuleMap = matchStrategyRuleMap;
        }

        public String getFilterStoryName() {
            return filterStoryName;
        }

        public void setFilterStoryName(String filterStoryName) {
            this.filterStoryName = filterStoryName;
        }

        public void setGlobalTimeSlotTaskNode(TaskNode globalTimeSlotTaskNode) {
            this.globalTimeSlotTaskNode = globalTimeSlotTaskNode;
        }

        public String getStrategyName() {
            return strategyName;
        }

        public void setStrategyName(String strategyName) {
            this.strategyName = strategyName;
        }
    }

    private static class ParseStoryNodeTaskItem {

        private final EventNode beforeEventNode;

        private final List<TaskNodeAgentForConfigResolver> nodeDefQueue;

        private final List<StrategyRule> strategyRuleList;

        public ParseStoryNodeTaskItem(List<TaskNodeAgentForConfigResolver> nodeDefQueue, EventNode beforeEventNode,
                                      List<StrategyRule> strategyRuleList) {
            this.nodeDefQueue = nodeDefQueue;
            this.beforeEventNode = beforeEventNode;
            this.strategyRuleList = strategyRuleList;
        }

        public EventNode getBeforeEventNode() {
            return beforeEventNode;
        }

        public List<TaskNodeAgentForConfigResolver> getNodeDefQueue() {
            return nodeDefQueue;
        }

        public List<StrategyRule> getStrategyRuleList() {
            return strategyRuleList;
        }
    }
}
