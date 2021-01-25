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

import cn.kstry.framework.core.enums.CalculatorEnum;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.enums.StrategyTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lykan
 */
public abstract class BaseEventStoryDefConfig implements EventStoryDefConfig {

    /**
     * 限制条件：
     *  1. event_node、strategy 不允许同时为空
     *  2. story_def 非空时，event_def 必须非空
     *  3. event_node 为空时，strategy 必须存在，且 strategy 的规则项列表不能为空
     *
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
     * 限制条件：
     *  1. strategy_def 非空时，event_def 必须非空
     *  2. strategy_def 非空时，story_def 必须非空
     *  3. strategy_type 必须存在，并且是 cn.kstry.framework.core.enums.StrategyTypeEnum 枚举中的值
     *  4. 当 strategy_type=MATCH 时
     *      - story 必须存在，且不能被重复定义
     *  5. 当 strategy_type=FILTER 时
     *      - event_node 和 strategy_def 中的 story 只能且必须存在一个
     *      - 当 strategy_def.story 出现时，允许出现多个相同的 story 值，不允许出现不同的 strategy_def.story，因为可以 match 多个分支，只能 filter 一个 story
     *
     *  6. strategy.key：[ 匹配规则（在 CalculatorEnum 中定义）- 字段名称 ]  “-” 固定写法不可变更 @see cn.kstry.framework.core.enums.CalculatorEnum
     *     strategy.value  期望值
     *  7. rule_set 取值规则写法校验同 request_mapping value 校验
     *
     * {
     *   "strategy_def": {
     *     "USER_LOGIN": [{
     *       "story": "doLogin",
     *       "strategy_type": "FILTER",
     *       "rule_set": {
     *         "notNull-@req.userType": "" // 匹配规则（在 CalculateEnum 中定义）-字段取值  “-” 固定写法不可变更 @see cn.kstry.framework.core.enums.CalculateEnum
     *       }
     *     }],
     *     "LOGIN_RULE_SET": [{
     *       "story": "customerLogin",
     *       "strategy_type": "MATCH",
     *       "rule_set": {
     *         "equals-@req.userType": "1"
     *       }
     *     }, {
     *       "story": "userLogin",
     *       "strategy_type": "MATCH",
     *       "rule_set": {
     *         "compare-@req.userType": ">=2L"
     *       }
     *     }],
     *     "IMMEDIATELY_DISCOUNT_S": [{
     *       "strategy_type": "FILTER",
     *       "rule_set": {
     *         "compare-@get_goods.goodsList[0].money": ">500"
     *       }
     *     }]
     *   }
     * }
     */
    @JSONField(name = STRATEGY_DEF)
    private StrategyDef<String, StrategyDefItem<StrategyDefItemConfig>> strategyDef;

    /**
     * 限制条件：
     *  1. request_mapping_def 非空时，event_def 必须非空
     *  2. mapping value 规则
     *      - 必须是  @ 或 $ 开头， @代表从 node 或者 全局变量(req、sta、var) 中取值，$代表从 time_slot 的计算结果中取值
     *      - 中间以 '.' 字符分隔
     *
     * {
     *   "request_mapping_def": {
     *     "userMapping": {
     *       "user": "$USER_LOGIN.@user_login_node.user"
     *     },
     *     "userTologinMapping": {
     *       "userType": "@req.userType",
     *       "userId": "@req.userId"
     *     }
     * }
     */
    @JSONField(name = REQUEST_MAPPING_DEF)
    private RequestMappingDef<String, RequestMappingDefItem<String, String>> requestMappingDef;

    /**
     * 限制条件：
     * 1. event node key 不允许重复定义，在不同 event node group 中也不行
     *
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

    /**
     * 字段校验过程中出现的可疑待校验的字段
     */
    private final Set<String> questionFieldNameSet = new HashSet<>();

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
        checkStoryDef(this);
    }

    protected void setStrategyDef(StrategyDef<String, StrategyDefItem<StrategyDefItemConfig>> strategyDef) {
        this.strategyDef = strategyDef;
        checkStrategyDef(this);
    }

    protected void setRequestMappingDef(RequestMappingDef<String, RequestMappingDefItem<String, String>> requestMappingDef) {
        this.requestMappingDef = requestMappingDef;
        checkRequestMappingDef(this);
    }

    protected void setEventDef(EventDef<String, EventDefItem<String, EventDefItemConfig>> eventDef) {
        this.eventDef = eventDef;
        checkEventDef(this);
    }

    protected void checkStrategyDef(EventStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getStrategyDef())) {
            return;
        }

        // EventDef 为空时，StrategyDef 不允许出现
        AssertUtil.notEmpty(config.getEventDef(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "`event_def` is empty but `strategy_def` is not!");

        // StoryDef 为空时，StrategyDef 不允许出现
        AssertUtil.notEmpty(config.getStoryDef(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "`story_def` is empty but `strategy_def` is not!!");

        List<String> nodeNameList = new ArrayList<>();
        for (EventDefItem<String, EventDefItemConfig> stringNodeDefItemConfigEventDefItem : config.getEventDef().values()) {
            nodeNameList.addAll(stringNodeDefItemConfigEventDefItem.keySet());
        }

        List<String> storyNameList = Lists.newArrayList(config.getStoryDef().keySet());
        for (String k : config.getStrategyDef().keySet()) {
            AssertUtil.isValidField(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "The assignment of key in `strategy_def` is error! key:%s", k);

            StrategyDefItem<StrategyDefItemConfig> v = config.getStrategyDef().get(k);
            if (CollectionUtils.isEmpty(v)) {
                continue;
            }
            v.forEach(strategy -> {
                AssertUtil.present(StrategyTypeEnum.getStrategyTypeEnum(strategy.getStrategyType()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                        "The assignment of `strategy_type` in `strategy_def` is error! strategy_type:%s", strategy.getStrategyType());
                if (StringUtils.isNotBlank(strategy.getStory())) {
                    AssertUtil.isTrue(storyNameList.contains(strategy.getStory()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "The assignment of `story` in `strategy_def` is error! story:%s", strategy.getStory());
                }
                if (StrategyTypeEnum.isType(strategy.getStrategyType(), StrategyTypeEnum.MATCH)) {
                    AssertUtil.notBlank(strategy.getStory(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "`story` must exist when 'strategy_type=MATCH'");
                }

                if (MapUtils.isNotEmpty(strategy.getRuleSet())) {
                    strategy.getRuleSet().keySet().forEach(kName -> {
                        AssertUtil.notBlank(kName, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "The assignment of `rule_set` key in `strategy_def` is blank!");
                        String[] split = kName.split(GlobalConstant.DISTINCT_SIGN);
                        AssertUtil.isTrue(split.length == 2, ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "The assignment of `rule_set` key in `strategy_def` is error! rule_set:%s", kName);
                        Optional<CalculatorEnum> calculatorEnumOptional = CalculatorEnum.getCalculatorEnumByName(split[0]);
                        AssertUtil.present(calculatorEnumOptional, ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "The assignment of `rule_set` key in `strategy_def` is error! rule_set:%s", kName);
                        AssertUtil.notBlank(split[1], ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "The assignment of `rule_set` key in `strategy_def` is error! rule_set:%s", kName);
                        AssertUtil.isTrue(checkStrategyField(nodeNameList, split[1]), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "The assignment of `rule_set` key in `strategy_def` is error! rule_set:%s", kName);
                        String expected = strategy.getRuleSet().get(kName);
                        AssertUtil.isTrue(calculatorEnumOptional.isPresent() && calculatorEnumOptional.get().getExpression().checkExpected(expected),
                                ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "The assignment of `rule_set` expected value in `strategy_def` is error! expected:%s", expected);
                    });
                }
            });
            List<StrategyDefItemConfig> matchStrategy = v.stream().filter(s -> StrategyTypeEnum.isType(s.getStrategyType(), StrategyTypeEnum.MATCH)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(matchStrategy)) {
                AssertUtil.isTrue(matchStrategy.size() == matchStrategy.stream().map(StrategyDefItemConfig::getStory).distinct().count(),
                        ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "When `strategy_type=match`, `story` cannot be repeatedly defined!");
            }
        }

        // 校验策略名称是否合法
        questionFieldNameSet.forEach(fieldName -> AssertUtil.isTrue(config.getStrategyDef().containsKey(fieldName), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                "Undefined `strategy_def` or `event_def` node! node:%s", fieldName));

        for (String key : config.getStoryDef().keySet()) {
            StoryDefItem<StoryDefItemConfig> nodeList = config.getStoryDef().get(key);
            if (CollectionUtils.isEmpty(nodeList)) {
                continue;
            }
            for (StoryDefItemConfig node : nodeList) {
                if (StringUtils.isBlank(node.getStrategy())) {
                    continue;
                }

                StrategyDefItem<StrategyDefItemConfig> strategyDefItemConfig = config.getStrategyDef().get(node.getStrategy());
                AssertUtil.notNull(strategyDefItemConfig, ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                        "The assignment of `strategy` in `story_def` is error! strategy:%s", node.getStrategy());

                if (CollectionUtils.isEmpty(strategyDefItemConfig)) {
                    AssertUtil.notBlank(node.getEventNode(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "In `story_def` `event_node` and `strategy` are not allowed to be empty at the same time!");
                }
                if (CollectionUtils.isNotEmpty(strategyDefItemConfig)) {
                    AssertUtil.notTrue(strategyDefItemConfig.stream().filter(c -> StrategyTypeEnum.isType(c.getStrategyType(), StrategyTypeEnum.MATCH))
                            .anyMatch(s -> key.equals(s.getStory())), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "Event nodes with circular dependencies! story:%s", key);
                }

                List<StrategyDefItemConfig> filterStrategyList = strategyDefItemConfig.stream()
                        .filter(c -> StrategyTypeEnum.isType(c.getStrategyType(), StrategyTypeEnum.FILTER)).collect(Collectors.toList());

                boolean existFilterStory = CollectionUtils.isNotEmpty(filterStrategyList) && filterStrategyList.stream().anyMatch(s -> StringUtils.isNotBlank(s.getStory()));
                boolean notExistFilterStory = CollectionUtils.isEmpty(filterStrategyList) || filterStrategyList.stream().noneMatch(s -> StringUtils.isNotBlank(s.getStory()));
                if (CollectionUtils.isNotEmpty(filterStrategyList)) {
                    AssertUtil.isTrue((StringUtils.isBlank(node.getEventNode()) && existFilterStory) || (StringUtils.isNotBlank(node.getEventNode()) && notExistFilterStory),
                            ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "When `strategy_type=filter`, `story` in strategy_def and `event_node` can exist only one! event_node:%s", node.getEventNode());

                }
                if (existFilterStory) {
                    AssertUtil.oneSize(filterStrategyList.stream().map(StrategyDefItemConfig::getStory).collect(Collectors.toSet()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "When `strategy_type=filter`, `story` in strategy_def and `event_node` can exist only one! event_node:%s", node.getEventNode());
                }
            }
        }
    }

    protected void checkStoryDef(EventStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getStoryDef())) {
            return;
        }

        // NodeDef 为空时，StoryDef 不允许出现
        AssertUtil.notEmpty(config.getEventDef(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "`event_def` is empty but `story_def` is not!");

        List<String> nodeNameList = new ArrayList<>();
        for (EventDefItem<String, EventDefItemConfig> stringNodeDefItemConfigEventDefItem : config.getEventDef().values()) {
            nodeNameList.addAll(stringNodeDefItemConfigEventDefItem.keySet());
        }
        List<String> mappingNameList = MapUtils.isEmpty(config.getRequestMappingDef()) ? Lists.newArrayList() : Lists.newArrayList(config.getRequestMappingDef().keySet());
        for (String k : config.getStoryDef().keySet()) {

            AssertUtil.isValidField(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "The assignment of key in `story_def` is error! key:%s", k);

            StoryDefItem<StoryDefItemConfig> v = config.getStoryDef().get(k);
            if (CollectionUtils.isEmpty(v)) {
                continue;
            }
            v.forEach(storyItem -> {
                AssertUtil.isTrue(StringUtils.isNotBlank(storyItem.getEventNode()) || StringUtils.isNotBlank(storyItem.getStrategy()),
                        ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "In `story_def` `event_node` and `strategy` are not allowed to be empty at the same time!");
                if (StringUtils.isNotBlank(storyItem.getEventNode())) {
                    AssertUtil.isTrue(nodeNameList.contains(storyItem.getEventNode()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "The assignment of `node_name` in `story_def` is error! node_name:%s", storyItem.getEventNode());
                }
                if (StringUtils.isNotBlank(storyItem.getRequestMapping())) {
                    AssertUtil.isTrue(mappingNameList.contains(storyItem.getRequestMapping()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "The assignment of `request_mapping` in `story_def` is error! request_mapping:%s", storyItem.getRequestMapping());
                }
            });
        }
    }

    protected void checkRequestMappingDef(EventStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getRequestMappingDef())) {
            return;
        }

        // NodeDef 为空时，RequestMappingDef 不允许出现
        AssertUtil.notEmpty(config.getEventDef(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "`event_def` is empty but `request_mapping_def` is not!");

        List<String> nodeNameList = new ArrayList<>();
        for (EventDefItem<String, EventDefItemConfig> stringNodeDefItemConfigEventDefItem : config.getEventDef().values()) {
            nodeNameList.addAll(stringNodeDefItemConfigEventDefItem.keySet());
        }
        for (String k : config.getRequestMappingDef().keySet()) {
            AssertUtil.isValidField(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "The assignment of key in `request_mapping_def` is error! key:%s", k);

            RequestMappingDefItem<String, String> v = config.getRequestMappingDef().get(k);
            if (MapUtils.isEmpty(v)) {
                continue;
            }
            v.forEach((kIn, vIn) -> {
                AssertUtil.isValidField(kIn, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "The assignment of mapping item key in `request_mapping_def` is error! key:%s", kIn);
                AssertUtil.notBlank(vIn, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "The assignment of mapping item value in `request_mapping_def` is blank!");
                AssertUtil.isTrue(checkStrategyField(nodeNameList, vIn), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                        "The assignment of mapping item value in `request_mapping_def` is error! value:%s", vIn);
            });
        }
    }

    protected void checkEventDef(EventStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getEventDef())) {
            return;
        }

        List<String> nodeNameList = new ArrayList<>();
        for (String k : config.getEventDef().keySet()) {
            AssertUtil.isValidField(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "The assignment of event group key in `event_def` is error! key:%s", k);

            EventDefItem<String, EventDefItemConfig> v = config.getEventDef().get(k);
            if (MapUtils.isEmpty(v)) {
                continue;
            }

            v.forEach((kIn, vIn) -> {
                AssertUtil.isValidField(kIn, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "The assignment of event node key in `event_def` is error! key:%s", kIn);
                AssertUtil.isValidField(vIn.getEventAction(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                        "The assignment of `event_action` in `event_def` is error! key:%s", vIn.getEventAction());
                AssertUtil.notBlank(vIn.getEventType(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "The assignment of `event_action` in `event_type` is blank!");
                AssertUtil.present(ComponentTypeEnum.getComponentTypeEnumByName(vIn.getEventType()),
                        ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "The assignment of `event_action` in `event_type` is error! event_type:%s", vIn.getEventType());
                nodeNameList.add(kIn);
            });
        }
        AssertUtil.isTrue(nodeNameList.size() == new HashSet<>(nodeNameList).size(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                "event node key in `event_def` is repeatedly defined!");
    }

    private boolean checkStrategyField(List<String> nodeNameList, String value) {
        AssertUtil.notNull(nodeNameList);
        if (StringUtils.isBlank(value)) {
            return false;
        }
        String[] fieldNameArray = value.split("\\.");
        if (fieldNameArray.length <= 1) {
            return false;
        }
        if (!fieldNameArray[0].startsWith(GlobalConstant.NODE_SIGN) && !fieldNameArray[0].startsWith(GlobalConstant.TIME_SLOT_NODE_SIGN)) {
            return false;
        }

        boolean checkVin = true;
        for (String fieldName : fieldNameArray) {
            if (!fieldName.startsWith(GlobalConstant.NODE_SIGN) && !fieldName.startsWith(GlobalConstant.TIME_SLOT_NODE_SIGN)) {
                checkVin = checkVin && GlobalUtil.isValidField(fieldName.replaceAll("\\[\\d+]", StringUtils.EMPTY));
                continue;
            }

            if (fieldName.startsWith(GlobalConstant.TIME_SLOT_NODE_SIGN)) {
                this.questionFieldNameSet.add(fieldName.replace(GlobalConstant.TIME_SLOT_NODE_SIGN, StringUtils.EMPTY));
                continue;
            }
            fieldName = fieldName.replace(GlobalConstant.NODE_SIGN, StringUtils.EMPTY);
            checkVin = checkVin && (GlobalConstant.RESERVED_WORDS_LIST.stream().anyMatch(fieldName::equals) || nodeNameList.stream().anyMatch(fieldName::equals));
        }
        return checkVin;
    }
}
