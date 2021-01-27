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
import cn.kstry.framework.core.bus.TaskNode;
import cn.kstry.framework.core.engine.timeslot.TimeSlotEventNode;
import cn.kstry.framework.core.engine.timeslot.TimeSlotOperatorRole;
import cn.kstry.framework.core.enums.CalculatorEnum;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.enums.StrategyTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.route.EventGroup;
import cn.kstry.framework.core.route.EventNode;
import cn.kstry.framework.core.route.StrategyRule;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.TaskActionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                } else {
                    AssertUtil.notNull(eventNodeAgent.getTaskNode());
                }
                eventNodeAdapterList.add(eventNodeAgent);
            }
            storyNodeAdaptorMap.put(key, eventNodeAdapterList);
        });
        if (MapUtils.isEmpty(storyNodeAdaptorMap)) {
            return storyNodeMap;
        }
        storyNodeAdaptorMap.forEach((k, v) -> {
            AssertUtil.notEmpty(v);
            storyNodeMap.put(k, initEventNodePipeline(0, storyNodeAdaptorMap, v));
        });
        return storyNodeMap;
    }

    private List<EventNode> initEventNodePipeline(int invokeLevel, Map<String, List<TaskNodeAgentForConfigResolver>> storyNodeAdaptorMap,
                                                  List<TaskNodeAgentForConfigResolver> eventNodeList) {

        List<EventNode> firstEventNodeList = new ArrayList<>();

        LinkedList<ParseStoryNodeTaskItem> taskStack = new LinkedList<>();
        taskStack.push(new ParseStoryNodeTaskItem(eventNodeList, null, null));
        for (ParseStoryNodeTaskItem taskItem = taskStack.poll(); taskItem != null && CollectionUtils.isNotEmpty(taskItem.getNodeDefQueue()); taskItem = taskStack.poll()) {

            EventNode beforeEventNode = taskItem.getBeforeEventNode();
            int maxLevelDepth = (beforeEventNode != null && beforeEventNode.getNodeLevel() > invokeLevel) ? beforeEventNode.getNodeLevel() : invokeLevel;
            AssertUtil.isTrue(maxLevelDepth < GlobalConstant.MAX_NODE_LEVEL_DEPTH, ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                    "Event nodes with circular dependencies! levelDepth:%s", maxLevelDepth);

            List<StrategyRule> matchStrategyRuleList = taskItem.getStrategyRuleList();
            for (TaskNodeAgentForConfigResolver nodeAgent : taskItem.getNodeDefQueue()) {
                if (MapUtils.isEmpty(nodeAgent.getMatchStrategyRuleMap()) || nodeAgent.getTaskNode() != null || StringUtils.isNotBlank(nodeAgent.getTimeSlotStoryName())) {
                    EventNode eventNode = nodeAgent.buildEventNode();
                    List<TaskNodeAgentForConfigResolver> taskNodeAgentList =
                            StringUtils.isBlank(nodeAgent.getTimeSlotStoryName()) ? null : storyNodeAdaptorMap.get(nodeAgent.getTimeSlotStoryName());
                    if (eventNode instanceof TimeSlotEventNode && CollectionUtils.isNotEmpty(taskNodeAgentList)
                            && ((TimeSlotEventNode) eventNode).getOriginalTaskNode() == null) {
                        List<EventNode> timeSlotStoryEventNodeList = initEventNodePipeline(++invokeLevel, storyNodeAdaptorMap, taskNodeAgentList);
                        TimeSlotEventNode timeSlotEventNode = (TimeSlotEventNode) eventNode;
                        timeSlotEventNode.setFirstTimeSlotEventNodeList(timeSlotStoryEventNodeList);
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
                Map<String, List<StrategyRule>> nextMatchStrategyRuleMap = nodeAgent.getMatchStrategyRuleMap();
                nextMatchStrategyRuleMap.forEach((kIn, vIn) -> taskStack.push(new ParseStoryNodeTaskItem(storyNodeAdaptorMap.get(kIn), finalBeforeEventNode, vIn)));
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

        List<StrategyRule> filterStrategyRuleList = routeStrategy.stream()
                .filter(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.FILTER) && MapUtils.isNotEmpty(s.getRuleSet()))
                .map(EventStoryDefConfig.StrategyDefItemConfig::getRuleSet)
                .flatMap(s -> getStrategyRuleList(s, StrategyTypeEnum.FILTER).stream())
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(filterStrategyRuleList)) {
            eventNodeAgent.setFilterStrategyRuleList(filterStrategyRuleList);
        }

        List<EventStoryDefConfig.StrategyDefItemConfig> timeSlotStrategyList = routeStrategy.stream()
                .filter(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.TIMESLOT))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(timeSlotStrategyList)) {
            AssertUtil.oneSize(timeSlotStrategyList, ExceptionEnum.PARAMS_ERROR);
            EventStoryDefConfig.StrategyDefItemConfig strategyConfig = timeSlotStrategyList.get(0);
            boolean async = Optional.ofNullable(strategyConfig.getAsync()).orElse(false);
            int timeout = Optional.ofNullable(strategyConfig.getTimeout()).orElse(GlobalConstant.DEFAULT_ASYNC_TIMEOUT);
            eventNodeAgent.setTimeSlotStoryName(strategyConfig.getStory());
            eventNodeAgent.setStrategyName(strategyName);
            eventNodeAgent.setAsync(async);
            eventNodeAgent.setTimeout(timeout);
            eventNodeAgent.setTimeSlotTask(true);
            AssertUtil.isTrue(eventNodeAgent.getTaskNode() == null || StringUtils.isBlank(eventNodeAgent.getTimeSlotStoryName()));
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

        StringBuilder realFieldName = new StringBuilder();
        String[] fieldList = field.split("\\.");
        for (int i = 0; i < fieldList.length; i++) {
            if (i == 0) {
                AssertUtil.isTrue(fieldList[0].startsWith(GlobalConstant.NODE_SIGN) || fieldList[0].startsWith(GlobalConstant.TIME_SLOT_NODE_SIGN));
            }

            if (realFieldName.length() != 0) {
                realFieldName.append(".");
            }

            if (!fieldList[i].startsWith(GlobalConstant.NODE_SIGN) && !fieldList[i].startsWith(GlobalConstant.TIME_SLOT_NODE_SIGN)) {
                realFieldName.append(fieldList[i]);
                continue;
            }
            if (!fieldList[i].startsWith(GlobalConstant.NODE_SIGN)) {
                TaskNode taskNode = this.taskNodeMap.get(fieldList[i].substring(1));
                if (taskNode == null) {
                    realFieldName.append(fieldList[i]);
                    continue;
                }

                realFieldName.append(GlobalConstant.TIME_SLOT_NODE_SIGN).append(taskNode.identity());
                if ((i + 1 < fieldList.length)
                        && fieldList[i + 1].startsWith(GlobalConstant.NODE_SIGN)
                        && GlobalConstant.RESERVED_WORDS_LIST.contains(fieldList[i + 1].substring(1).toUpperCase())) {
                    continue;
                } else {
                    realFieldName.append(".");
                }
            }

            switch (fieldList[i].toUpperCase()) {
                case GlobalConstant.NODE_SIGN + "REQ":
                    realFieldName.append(StoryBus.DEFAULT_GLOBAL_BUS_REQUEST_KEY.identity());
                    break;
                case GlobalConstant.NODE_SIGN + "STA":
                    realFieldName.append(StoryBus.DEFAULT_GLOBAL_BUS_STABLE_KEY.identity());
                    break;
                case GlobalConstant.NODE_SIGN + "VAR":
                    realFieldName.append(StoryBus.DEFAULT_GLOBAL_BUS_VARIABLE_KEY.identity());
                    break;
                default:
                    TaskNode taskNode = this.taskNodeMap.get(fieldList[i].substring(1));
                    AssertUtil.notNull(taskNode);
                    realFieldName.append(taskNode.identity());
                    break;
            }
        }
        return GlobalUtil.notBlank(realFieldName.toString());
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

        private String timeSlotStoryName;

        /**
         * time slot task node
         */
        private TaskNode globalTimeSlotTaskNode;

        private String strategyName;

        /**
         * 异步任务的超时时间，单位 ms
         * 默认值：cn.kstry.framework.core.config.GlobalConstant#DEFAULT_ASYNC_TIMEOUT
         */
        private Integer timeout;

        /**
         * true: 代表使用异步任务
         */
        private Boolean async;

        private Boolean timeSlotTask;

        public TaskNode getTaskNode() {
            return taskNode;
        }

        public void setTaskNode(TaskNode taskNode) {
            this.taskNode = taskNode;
        }

        public EventNode buildEventNode() {
            EventNode eventNode = null;
            if (BooleanUtils.isTrue(getTimeSlotTask())) {
                AssertUtil.notNull(this.globalTimeSlotTaskNode);
                TaskNode timeSlotTaskNode = this.globalTimeSlotTaskNode.cloneTaskNode();
                TimeSlotEventNode timeSlotEventNode = new TimeSlotEventNode(timeSlotTaskNode);
                timeSlotEventNode.setStrategyName(GlobalUtil.notBlank(getStrategyName()));
                timeSlotEventNode.setAsync(GlobalUtil.notNull(getAsync()));
                timeSlotEventNode.setTimeout(GlobalUtil.notNull(getTimeout()));
                if (getTaskNode() != null) {
                    EventNode firstEventNode = new EventNode(getTaskNode().cloneTaskNode());
                    firstEventNode.setFilterStrategyRuleList(getFilterStrategyRuleList());
                    firstEventNode.setRequestMappingGroup(getRequestMappingGroup());
                    timeSlotEventNode.setOriginalTaskNode(firstEventNode.getTaskNode());
                    timeSlotEventNode.setStrategyName(firstEventNode.getTaskNode().identity());
                    timeSlotEventNode.setFirstTimeSlotEventNodeList(Lists.newArrayList(firstEventNode));
                }
                eventNode = timeSlotEventNode;
            } else if (getTaskNode() != null) {
                eventNode = new EventNode(getTaskNode().cloneTaskNode());
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

        public String getTimeSlotStoryName() {
            return timeSlotStoryName;
        }

        public void setTimeSlotStoryName(String timeSlotStoryName) {
            this.timeSlotStoryName = timeSlotStoryName;
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

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public Boolean getAsync() {
            return async;
        }

        public void setAsync(Boolean async) {
            this.async = async;
        }

        public Boolean getTimeSlotTask() {
            return timeSlotTask;
        }

        public void setTimeSlotTask(Boolean timeSlotTask) {
            this.timeSlotTask = timeSlotTask;
        }
    }

    private static class ParseStoryNodeTaskItem {

        private final EventNode beforeEventNode;

        private final List<TaskNodeAgentForConfigResolver> nodeDefQueue;

        private final List<StrategyRule> strategyRuleList;

        public ParseStoryNodeTaskItem(List<TaskNodeAgentForConfigResolver> nodeDefQueue, EventNode beforeEventNode,
                                      List<StrategyRule> strategyRuleList) {
            AssertUtil.notEmpty(nodeDefQueue);
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
