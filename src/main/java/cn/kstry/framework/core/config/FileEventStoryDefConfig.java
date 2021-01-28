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

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件的形式，解析出配置文件
 *
 * @author lykan
 */
public class FileEventStoryDefConfig extends BaseEventStoryDefConfig implements EventStoryDefConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileEventStoryDefConfig.class);

    @Override
    public EventStoryDefConfig parseEventStoryConfig(String eventStoryConfigName) {
        AssertUtil.notBlank(eventStoryConfigName, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config file name blank!");
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(eventStoryConfigName);
            if (resources == null || resources.length == 0) {
                LOGGER.warn("Kstry configuration file does not exist!");
                return new FileEventStoryDefConfig();
            }

            List<String> configList = new ArrayList<>();
            for (Resource r : resources) {
                BufferedReader br = new BufferedReader(new InputStreamReader(r.getInputStream()));
                StringBuilder sb = new StringBuilder();
                for (String sLine = br.readLine(); sLine != null; sLine = br.readLine()) {
                    sb.append(sLine);
                }
                configList.add(sb.toString());
                LOGGER.info("Read using profile {}", r.getFilename());
            }
            return doParseEventStoryConfig(mergeFiles(configList));
        } catch (Exception e) {
            LOGGER.error("Failed to read or parse a file!", e);
            KstryException.throwException(e);
            return null;
        }
    }

    private Map<String, Map<String, JSON>> mergeFiles(List<String> configList) {

        AssertUtil.notEmpty(configList);
        Map<String, Map<String, JSON>> allConfig = new HashMap<>();
        configList.forEach(item -> {
            Map<String, String> config = JSON.parseObject(item, new TypeReference<HashMap<String, String>>() {
            });
            config.forEach((k, v) -> {
                Map<String, JSON> detailMap = allConfig.get(k);
                Map<String, JSON> detail = JSON.parseObject(v, new TypeReference<HashMap<String, JSON>>() {
                });
                if (MapUtils.isEmpty(detailMap)) {
                    allConfig.put(k, new HashMap<>(detail));
                    return;
                }
                if (MapUtils.isEmpty(detail)) {
                    return;
                }
                detailMap.keySet().forEach(kIn -> AssertUtil.isTrue(!detail.containsKey(kIn),
                        ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "The same resource definition exists in different files! key:%s", kIn));
                detailMap.putAll(detail);
            });
        });
        return allConfig;
    }

    private EventStoryDefConfig doParseEventStoryConfig(Map<String, Map<String, JSON>> configMap) {

        AssertUtil.notNull(configMap);
        if (MapUtils.isNotEmpty(configMap.get(EVENT_DEF))) {
            this.setEventDef(JSON.parseObject(JSON.toJSONString(configMap.get(EVENT_DEF)),
                    new TypeReference<EventDef<String, EventDefItem<String, EventDefItemConfig>>>() {
                    })
            );
        }

        if (MapUtils.isNotEmpty(configMap.get(REQUEST_MAPPING_DEF))) {
            this.setRequestMappingDef(JSON.parseObject(JSON.toJSONString(configMap.get(REQUEST_MAPPING_DEF)),
                    new TypeReference<RequestMappingDef<String, RequestMappingDefItem<String, String>>>() {
                    })
            );
        }

        if (MapUtils.isNotEmpty(configMap.get(STORY_DEF))) {
            this.setStoryDef(JSON.parseObject(JSON.toJSONString(configMap.get(STORY_DEF)),
                    new TypeReference<StoryDef<String, StoryDefItem<StoryDefItemConfig>>>() {
                    })
            );
        }

        if (MapUtils.isNotEmpty(configMap.get(STRATEGY_DEF))) {
            this.setStrategyDef(JSON.parseObject(JSON.toJSONString(configMap.get(STRATEGY_DEF)),
                    new TypeReference<StrategyDef<String, StrategyDefItem<StrategyDefItemConfig>>>() {
                    })
            );
        }
        this.duplicateKeyCheck(this);
        LOGGER.info("Use the following configuration file to generate a global map. config:{}", JSON.toJSONString(this));
        return this;
    }
}
