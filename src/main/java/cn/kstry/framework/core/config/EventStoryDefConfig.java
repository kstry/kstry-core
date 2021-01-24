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

import cn.kstry.framework.core.enums.ComponentTypeEnum;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定义 & 获取配置信息
 *
 * @author lykan
 */
public interface EventStoryDefConfig {

    /**
     * 事件故事定义 Key
     */
    String STORY_DEF = "story_def";

    /**
     * 事件定义 Key
     */
    String EVENT_DEF = "event_def";

    /**
     * 策略定义 Key
     */
    String STRATEGY_DEF = "strategy_def";

    /**
     * 请求参数映射定义 Key
     */
    String REQUEST_MAPPING_DEF = "request_mapping_def";

    /**
     * 获取 story 定义
     */
    StoryDef<String, StoryDefItem<StoryDefItemConfig>> getStoryDef();

    /**
     * 获取策略
     */
    StrategyDef<String, StrategyDefItem<StrategyDefItemConfig>> getStrategyDef();

    /**
     * 获取参数映射规则
     */
    RequestMappingDef<String, RequestMappingDefItem<String, String>> getRequestMappingDef();

    /**
     * 获取 Event 定义
     */
    EventDef<String, EventDefItem<String, EventDefItemConfig>> getEventDef();

    class StoryDef<String, StoryDefItem> extends HashMap<String, StoryDefItem> {

    }

    class StoryDefItem<StoryDefItemConfig> extends ArrayList<StoryDefItemConfig> {

    }

    class StoryDefItemConfig {

        /**
         * 执行 事件 NODE 名称
         */
        @JSONField(name = "event_node")
        private String eventNode;

        /**
         * 请求映射规则
         */
        @JSONField(name = "request_mapping")
        private String requestMapping;

        /**
         * 规则
         */
        @JSONField(name = "strategy")
        private String strategy;

        public String getEventNode() {
            return eventNode;
        }

        public void setEventNode(String eventNode) {
            this.eventNode = eventNode;
        }

        public String getRequestMapping() {
            return requestMapping;
        }

        public void setRequestMapping(String requestMapping) {
            this.requestMapping = requestMapping;
        }

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }
    }

    class StrategyDef<String, StrategyDefItem> extends HashMap<String, StrategyDefItem> {

    }

    class StrategyDefItem<StrategyDefItemConfig> extends ArrayList<StrategyDefItemConfig> {

    }

    class StrategyDefItemConfig {

        /**
         * 待执行的 Story
         */
        @JSONField(name = "story")
        private String story;

        /**
         * 策略类型
         *
         * @see cn.kstry.framework.core.enums.StrategyTypeEnum
         */
        @JSONField(name = "strategy_type")
        private String strategyType;

        /**
         * 策略规则集
         */
        @JSONField(name = "rule_set")
        private Map<String, String> ruleSet;

        public String getStory() {
            return story;
        }

        public void setStory(String story) {
            this.story = story;
        }

        public String getStrategyType() {
            return strategyType;
        }

        public void setStrategyType(String strategyType) {
            this.strategyType = strategyType;
        }

        public Map<String, String> getRuleSet() {
            return ruleSet;
        }

        public void setRuleSet(Map<String, String> ruleSet) {
            this.ruleSet = ruleSet;
        }
    }

    class EventDef<String, EventDefItem> extends HashMap<String, EventDefItem> {

    }

    class EventDefItem<String, EventDefItemConfig> extends HashMap<String, EventDefItemConfig> {

    }

    class EventDefItemConfig {

        /**
         * Event Type
         *
         * @see ComponentTypeEnum
         */
        @JSONField(name = "event_type")
        private String eventType;

        /**
         * Event Action
         */
        @JSONField(name = "event_action")
        private String eventAction;

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getEventAction() {
            return eventAction;
        }

        public void setEventAction(String eventAction) {
            this.eventAction = eventAction;
        }
    }

    class RequestMappingDef<String, RequestMappingDefItem> extends HashMap<String, RequestMappingDefItem> {

    }

    class RequestMappingDefItem<String, String0> extends HashMap<String, String0> {

    }
}
