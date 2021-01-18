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

import cn.kstry.framework.core.enums.CalculateEnum;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.enums.StrategyTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author lykan
 */
public abstract class BaseEventStoryDefConfig implements EventStoryDefConfig {

    /**
     * {
     * 	"story_def": {
     * 		"updateUser": [{
     * 			"event_node": "NODE_1"
     *        }, {
     * 			"event_node": "NODE_2",
     * 			"request_mapping": "MAPPING_1"
     *        }, {
     * 			"strategy": "STRATEGY_1"
     *        }],
     * 		"update01": [{
     * 			"event_node": "NODE_3",
     * 			"request_mapping": "MAPPING_2"
     *        }],
     * 		"update02": [{
     * 			"event_node": "NODE_3"
     *        }]
     *    }
     * }
     */
    @JSONField(name = STORY_DEF)
    private StoryDef<String, StoryDefItem<StoryDefItemConfig>> storyDef;

    /**
     * {
     * 	"strategy_def": {
     * 		"STRATEGY_1": [{
     * 			"next_story": "update01", // 当 strategy_type=match 时，next_story 不允许为空
     * 			"strategy_type": "match", // 不允许为空 @see cn.kstry.framework.core.enums.StrategyTypeEnum
     * 			"strategy": {
     * 				"equals-source": "2f" // 匹配规则（在 CalculateEnum 中定义）-字段名称  “-” 固定写法不可变更 @see cn.kstry.framework.core.enums.CalculateEnum
     *            }
     *        }, {
     * 			"next_story": "update02",
     * 			"strategy_type": "match",
     * 			"strategy": {
     * 				"equals-source": "3f"
     *            }
     *        }, {
     * 			"strategy_type": "filter",
     * 			"strategy": {
     * 				"equals-source": "3f"
     *            }
     *        }]
     *    }
     * }
     */
    @JSONField(name = STRATEGY_DEF)
    private StrategyDef<String, StrategyDefItem<StrategyDefItemConfig>> strategyDef;

    /**
     * {
     * 	"request_mapping_def": {
     * 		"MAPPING_1": {
     * 			"count": "DEFAULT['count']",
     * 			"money": "#data['money']"
     *        },
     * 		"MAPPING_2": {
     * 			"id": "NODE_1['id']"
     *        }
     *    }
     * }
     */
    @JSONField(name = REQUEST_MAPPING_DEF)
    private RequestMappingDef<String, RequestMappingDefItem<String, String>> requestMappingDef;

    /**
     * {
     * 	"event_def": {
     * 		"event_node_group1": {
     * 			"NODE_1": { // 定义不能重复
     * 				"event_type": "TASK", // 不能为空 @see cn.kstry.framework.core.enums.ComponentTypeEnum
     * 				"event_action": "create" // 不能为空
     *            },
     * 			"NODE_2": {
     * 				"event_type": "TASK",
     * 				"event_action": "update"
     *            }
     *        },
     * 		"event_node_group2": {
     * 			"NODE_3": {
     * 				"event_type": "TASK",
     * 				"event_action": "fly"
     *            }
     *        }
     *    }
     * }
     */
    @JSONField(name = EVENT_DEF)
    private EventDef<String, EventDefItem<String, EventDefItemConfig>> eventDef;

    @Override
    public StoryDef<String, StoryDefItem<StoryDefItemConfig>> getStoryDef() {
        return storyDef;
    }

    @Override
    public StrategyDef<String, StrategyDefItem<StrategyDefItemConfig>> getStrategyDef() {
        return strategyDef;
    }

    @Override
    public RequestMappingDef<String, RequestMappingDefItem<String, String>> getRequestMappingDef() {
        return requestMappingDef;
    }

    @Override
    public EventDef<String, EventDefItem<String, EventDefItemConfig>> getEventDef() {
        return eventDef;
    }

    protected void setStoryDef(StoryDef<String, StoryDefItem<StoryDefItemConfig>> storyDef) {
        this.storyDef = storyDef;
    }

    protected void setStrategyDef(StrategyDef<String, StrategyDefItem<StrategyDefItemConfig>> strategyDef) {
        this.strategyDef = strategyDef;
    }

    protected void setRequestMappingDef(RequestMappingDef<String, RequestMappingDefItem<String, String>> requestMappingDef) {
        this.requestMappingDef = requestMappingDef;
    }

    protected void setEventDef(EventDef<String, EventDefItem<String, EventDefItemConfig>> eventDef) {
        this.eventDef = eventDef;
    }

    protected static void checkStrategyDef(EventStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getStrategyDef())) {
            return;
        }

        // EventDef 为空时，StrategyDef 不允许出现
        AssertUtil.isTrue(MapUtils.isNotEmpty(config.getEventDef()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `event_def` is empty but `strategy_def` not empty!");

        // StoryDef 为空时，StrategyDef 不允许出现
        AssertUtil.isTrue(MapUtils.isNotEmpty(config.getStoryDef()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `story_def` is empty but `strategy_def` not empty!");

        List<String> storyNameList = MapUtils.isEmpty(config.getStoryDef()) ? Lists.newArrayList() : Lists.newArrayList(config.getStoryDef().keySet());
        for (String k : config.getStrategyDef().keySet()) {
            AssertUtil.isValidField(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `strategy_def` strategy key is error! ket:%s", k);

            StrategyDefItem<StrategyDefItemConfig> v = config.getStrategyDef().get(k);
            if (CollectionUtils.isEmpty(v)) {
                continue;
            }
            v.forEach(strategy -> {
                AssertUtil.isTrue(StrategyTypeEnum.getStrategyTypeEnum(strategy.getStrategyType()).isPresent(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                        "config `strategy_def` `strategy_type` is error! strategy_type:%s", strategy.getStrategyType());
                if (StringUtils.isNotBlank(strategy.getNextStory())) {
                    AssertUtil.isTrue(storyNameList.contains(strategy.getNextStory()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "config `strategy_def` `next_story` is error! next_story:%s", strategy.getNextStory());
                }
                if (StrategyTypeEnum.isType(strategy.getStrategyType(), StrategyTypeEnum.MATCH)) {
                    AssertUtil.notBlank(strategy.getNextStory(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "config `strategy_def` `next_story` must exit when strategy_type=MATCH");
                }
                if (MapUtils.isNotEmpty(strategy.getStrategy())) {
                    strategy.getStrategy().keySet().forEach(kName -> {
                        AssertUtil.notBlank(kName, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `strategy_def` `strategy` key is blank!");
                        String[] split = kName.split("-");
                        AssertUtil.isTrue(split.length == 2, ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "config `strategy_def` `strategy` key error! strategy:%s", kName);
                        AssertUtil.isTrue(CalculateEnum.getCalculateEnumByName(split[0]).isPresent(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "config `strategy_def` `strategy` key error! strategy:%s", kName);
                    });
                }
            });
        }

        for (StoryDefItem<StoryDefItemConfig> nodeList : config.getStoryDef().values()) {
            for (StoryDefItemConfig node : nodeList) {
                if (StringUtils.isBlank(node.getStrategy())) {
                    continue;
                }

                StrategyDefItem<StrategyDefItemConfig> strategyDefItemConfig = config.getStrategyDef().get(node.getStrategy());
                AssertUtil.notNull(strategyDefItemConfig, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `story_def` 'strategy' error! strategy:%s", node.getStrategy());
                if (StringUtils.isBlank(node.getEventNode())) {
                    AssertUtil.isTrue(strategyDefItemConfig.stream().anyMatch(c -> StrategyTypeEnum.isType(c.getStrategyType(), StrategyTypeEnum.MATCH)),
                            ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "config `story_def` when `event_node` is empty `strategy` must contain `strategy` child of 'strategy_type=match'");
                }
            }
        }
    }

    protected static void checkStoryDef(EventStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getStoryDef())) {
            return;
        }

        // NodeDef 为空时，StoryDef 不允许出现
        AssertUtil.isTrue(MapUtils.isNotEmpty(config.getEventDef()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `event_def` is empty but `story_def` not empty!");

        List<String> nodeNameList = new ArrayList<>();
        for (EventDefItem<String, EventDefItemConfig> stringNodeDefItemConfigEventDefItem : config.getEventDef().values()) {
            nodeNameList.addAll(stringNodeDefItemConfigEventDefItem.keySet());
        }
        List<String> mappingNameList = MapUtils.isEmpty(config.getRequestMappingDef()) ? Lists.newArrayList() : Lists.newArrayList(config.getRequestMappingDef().keySet());
        for (String k : config.getStoryDef().keySet()) {

            AssertUtil.isValidField(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `story_def` story key is error! key:%s", k);

            StoryDefItem<StoryDefItemConfig> v = config.getStoryDef().get(k);
            if (CollectionUtils.isEmpty(v)) {
                continue;
            }
            v.forEach(storyItem -> {
                AssertUtil.isTrue(StringUtils.isNotBlank(storyItem.getEventNode()) || StringUtils.isNotBlank(storyItem.getStrategy()),
                        ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config both `event_node` and `strategy` in `story_def` are blank!");
                if (StringUtils.isNotBlank(storyItem.getEventNode())) {
                    AssertUtil.isTrue(nodeNameList.contains(storyItem.getEventNode()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "config `story_def` `node_name` is error! node_name:%s", storyItem.getEventNode());
                }
                if (StringUtils.isNotBlank(storyItem.getRequestMapping())) {
                    AssertUtil.isTrue(mappingNameList.contains(storyItem.getRequestMapping()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "config `story_def` `request_mapping` is error! request_mapping:%s", storyItem.getRequestMapping());
                }
            });
        }
    }

    protected static void checkRequestMappingDef(EventStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getRequestMappingDef())) {
            return;
        }

        // NodeDef 为空时，RequestMappingDef 不允许出现
        AssertUtil.isTrue(MapUtils.isNotEmpty(config.getEventDef()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `event_def` is empty but `request_mapping_def` not empty!");

        List<String> nodeNameList = new ArrayList<>();
        for (EventDefItem<String, EventDefItemConfig> stringNodeDefItemConfigEventDefItem : config.getEventDef().values()) {
            nodeNameList.addAll(stringNodeDefItemConfigEventDefItem.keySet());
        }
        for (String k : config.getRequestMappingDef().keySet()) {
            AssertUtil.isValidField(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `request_mapping_def` mapping key is error! key:%s", k);

            RequestMappingDefItem<String, String> v = config.getRequestMappingDef().get(k);
            if (MapUtils.isEmpty(v)) {
                continue;
            }
            v.forEach((kIn, vIn) -> {
                AssertUtil.isValidField(kIn, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `request_mapping_def` mapping item key is error! key:%s", kIn);
                AssertUtil.notBlank(vIn, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `request_mapping_def` mapping item value is blank!");

                boolean checkVin = REQUEST_MAPPING_KEYWORD.stream().anyMatch(vIn::startsWith) || nodeNameList.stream().anyMatch(vIn::startsWith);
                AssertUtil.isTrue(checkVin, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `request_mapping_def` mapping item value error! value:%s", vIn);
            });
        }
    }

    protected static void checkEventDef(EventStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getEventDef())) {
            return;
        }

        List<String> nodeNameList = new ArrayList<>();
        for (String k : config.getEventDef().keySet()) {
            AssertUtil.isValidField(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `event_def` event node group key is blank! key:%s", k);

            EventDefItem<String, EventDefItemConfig> v = config.getEventDef().get(k);
            if (MapUtils.isEmpty(v)) {
                continue;
            }

            v.forEach((kIn, vIn) -> {
                AssertUtil.isValidField(kIn, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `event_def` event node key is error! key:%s", kIn);
                AssertUtil.isValidField(vIn.getEventAction(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                        "config `event_def` event node `event_action` error! key:%s", vIn.getEventAction());
                AssertUtil.isValidField(vIn.getEventAction(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                        "config `event_def` event node `event_action` error! event_action:%s", vIn.getEventAction());
                AssertUtil.notBlank(vIn.getEventType(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `event_def` event node `event_type` blank!");
                AssertUtil.isTrue(ComponentTypeEnum.getComponentTypeEnumByName(vIn.getEventType()).isPresent(),
                        ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config `event_def` event node `event_type` error! event_type:%s", vIn.getEventType());
                nodeNameList.add(kIn);
            });
        }
        AssertUtil.isTrue(nodeNameList.size() == new HashSet<>(nodeNameList).size(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                "config `event_def` event node name duplicate definition!");
    }
}
