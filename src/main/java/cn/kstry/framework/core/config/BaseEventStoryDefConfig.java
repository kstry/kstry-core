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

    @JSONField(name = STORY_DEF)
    private StoryDef<String, StoryDefItem<StoryDefItemConfig>> storyDef;

    @JSONField(name = STRATEGY_DEF)
    private RouteStrategyDef<String, StrategyDefItem<StrategyDefItemConfig>> routeStrategyDef;

    @JSONField(name = REQUEST_MAPPING_DEF)
    private RequestMappingDef<String, RequestMappingDefItem<String, String>> requestMappingDef;

    @JSONField(name = EVENT_DEF)
    private EventDef<String, EventDefItem<String, EventDefItemConfig>> eventDef;

    @Override
    public StoryDef<String, StoryDefItem<StoryDefItemConfig>> getStoryDef() {
        return storyDef;
    }

    @Override
    public RouteStrategyDef<String, StrategyDefItem<StrategyDefItemConfig>> getStrategyDef() {
        return routeStrategyDef;
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

    protected void setRouteStrategyDef(RouteStrategyDef<String, StrategyDefItem<StrategyDefItemConfig>> routeStrategyDef) {
        this.routeStrategyDef = routeStrategyDef;
    }

    protected void setRequestMappingDef(RequestMappingDef<String, RequestMappingDefItem<String, String>> requestMappingDef) {
        this.requestMappingDef = requestMappingDef;
    }

    protected void setEventDef(EventDef<String, EventDefItem<String, EventDefItemConfig>> eventDef) {
        this.eventDef = eventDef;
    }

    protected static void checkRouteStrategyDef(EventStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getStrategyDef())) {
            return;
        }

        // NodeDef 为空时，RouteStrategyDef 不允许出现
        AssertUtil.isTrue(MapUtils.isNotEmpty(config.getEventDef()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def is empty but route strategy not empty!");

        // StoryDef 为空时，RouteStrategyDef 不允许出现
        AssertUtil.isTrue(MapUtils.isNotEmpty(config.getStoryDef()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config story_def is empty but route strategy not empty!");

        List<String> storyNameList = MapUtils.isEmpty(config.getStoryDef()) ? Lists.newArrayList() : Lists.newArrayList(config.getStoryDef().keySet());
        for (String k : config.getStrategyDef().keySet()) {
            AssertUtil.notBlank(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config route_strategy_def strategy key is blank!");

            StrategyDefItem<StrategyDefItemConfig> v = config.getStrategyDef().get(k);
            if (CollectionUtils.isEmpty(v)) {
                continue;
            }
            v.forEach(strategy -> {
                if (StringUtils.isNotBlank(strategy.getNextStory())) {
                    AssertUtil.isTrue(storyNameList.contains(strategy.getNextStory()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "config route_strategy_def 'next_story' is error! next_story:%s", strategy.getNextStory());
                }
                if (StrategyTypeEnum.isType(strategy.getStrategyType(), StrategyTypeEnum.FILTER) && MapUtils.isNotEmpty(strategy.getStrategy())) {
                    strategy.getStrategy().keySet().forEach(kName -> {
                        AssertUtil.notBlank(kName, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config route_strategy_def filter_strategy key is blank!");
                        String[] split = kName.split("-");
                        AssertUtil.isTrue(split.length == 2, ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "config route_strategy_def filter_strategy key error! filter_strategy:%s", kName);
                        AssertUtil.isTrue(CalculateEnum.getCalculateEnumByName(split[0]).isPresent(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "config route_strategy_def filter_strategy key error! filter_strategy:%s", kName);
                    });
                }

                if (StrategyTypeEnum.isType(strategy.getStrategyType(), StrategyTypeEnum.MATCH) && MapUtils.isNotEmpty(strategy.getStrategy())) {
                    strategy.getStrategy().keySet().forEach(kName -> {
                        AssertUtil.notBlank(kName, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config route_strategy_def match_strategy key is blank!");
                        String[] split = kName.split("-");
                        AssertUtil.isTrue(split.length == 2, ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "config route_strategy_def match_strategy key error! match_strategy:%s", kName);
                        AssertUtil.isTrue(CalculateEnum.getCalculateEnumByName(split[0]).isPresent(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "config route_strategy_def match_strategy key error! match_strategy:%s", kName);
                    });
                }
            });
        }

        for (StoryDefItem<StoryDefItemConfig> nodeList : config.getStoryDef().values()) {
            for (StoryDefItemConfig node : nodeList) {
                if (StringUtils.isNotBlank(node.getStrategy())) {
                    String strategy = node.getStrategy();
                    AssertUtil.notNull(config.getStrategyDef().get(strategy),
                            ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config story_def 'route_strategy' error! route_strategy:%s", strategy);
                }
            }
        }
    }

    protected static void checkStoryDef(EventStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getStoryDef())) {
            return;
        }

        // NodeDef 为空时，StoryDef 不允许出现
        AssertUtil.isTrue(MapUtils.isNotEmpty(config.getEventDef()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def is empty but story def not empty!");

        List<String> nodeNameList = new ArrayList<>();
        for (EventDefItem<String, EventDefItemConfig> stringNodeDefItemConfigNodeDefItem : config.getEventDef().values()) {
            nodeNameList.addAll(stringNodeDefItemConfigNodeDefItem.keySet());
        }
        List<String> mappingNameList = MapUtils.isEmpty(config.getRequestMappingDef()) ? Lists.newArrayList() : Lists.newArrayList(config.getRequestMappingDef().keySet());
        for (String k : config.getStoryDef().keySet()) {
            AssertUtil.notBlank(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config story_def story key is blank!");

            StoryDefItem<StoryDefItemConfig> v = config.getStoryDef().get(k);
            if (CollectionUtils.isEmpty(v)) {
                continue;
            }
            v.forEach(storyItem -> {
                AssertUtil.isTrue(StringUtils.isNotBlank(storyItem.getEventNode()) || StringUtils.isNotBlank(storyItem.getStrategy()),
                        ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config story_def node_name is blank!");
                if (StringUtils.isNotBlank(storyItem.getEventNode())) {
                    AssertUtil.isTrue(nodeNameList.contains(storyItem.getEventNode()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "config story_def node_name is error! node_name:%s", storyItem.getEventNode());
                }
                if (StringUtils.isNotBlank(storyItem.getRequestMapping())) {
                    AssertUtil.isTrue(mappingNameList.contains(storyItem.getRequestMapping()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "config story_def request_mapping is error! request_mapping:%s", storyItem.getRequestMapping());
                }
            });
        }
    }

    protected static void checkRequestMappingDef(EventStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getRequestMappingDef())) {
            return;
        }

        // NodeDef 为空时，RequestMappingDef 不允许出现
        AssertUtil.isTrue(MapUtils.isNotEmpty(config.getEventDef()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def is empty but request mapping not empty!");

        List<String> nodeNameList = new ArrayList<>();
        for (EventDefItem<String, EventDefItemConfig> stringNodeDefItemConfigNodeDefItem : config.getEventDef().values()) {
            nodeNameList.addAll(stringNodeDefItemConfigNodeDefItem.keySet());
        }
        for (String k : config.getRequestMappingDef().keySet()) {
            AssertUtil.notBlank(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config request_mapping_def mapping key is blank!");

            RequestMappingDefItem<String, String> v = config.getRequestMappingDef().get(k);
            if (MapUtils.isEmpty(v)) {
                continue;
            }
            v.forEach((kIn, vIn) -> {
                AssertUtil.notBlank(kIn, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config request_mapping_def mapping item key is blank!");
                AssertUtil.notBlank(vIn, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config request_mapping_def mapping item value is blank!");

                boolean checkVin = REQUEST_MAPPING_KEYWORD.stream().anyMatch(vIn::startsWith) || nodeNameList.stream().anyMatch(vIn::startsWith);
                AssertUtil.isTrue(checkVin, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config request_mapping_def mapping item value error! value:%s", vIn);
            });
        }
    }

    protected static void checkNodeDef(EventStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getEventDef())) {
            return;
        }

        List<String> nodeNameList = new ArrayList<>();
        for (String k : config.getEventDef().keySet()) {
            AssertUtil.notBlank(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def group key is blank!");

            EventDefItem<String, EventDefItemConfig> v = config.getEventDef().get(k);
            if (MapUtils.isEmpty(v)) {
                continue;
            }

            v.forEach((kIn, vIn) -> {
                AssertUtil.notBlank(kIn, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def node key is blank!");
                AssertUtil.notBlank(vIn.getEventAction(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def 'method' is blank!");
                AssertUtil.notBlank(vIn.getEventType(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def 'type' is blank!");
                AssertUtil.isTrue(ComponentTypeEnum.getComponentTypeEnumByName(vIn.getEventType()).isPresent(),
                        ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def 'type' error! type:%s", vIn.getEventType());
                nodeNameList.add(kIn);
            });
        }
        AssertUtil.isTrue(nodeNameList.size() == new HashSet<>(nodeNameList).size(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                "config node_def node name duplicate definition!");
    }
}
