/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.core.bpmn.impl;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.TaskServiceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class TaskParamWrapper {

    /**
     * 指定参数
     */
    private final String params;

    /**
     * 需要类型转换的参数key映射
     */
    private Map<String, String> converterMapping = Maps.newHashMap();

    /**
     * 需要获取值参数key映射
     */
    private Map<String, String> valueMapping = Maps.newHashMap();

    public TaskParamWrapper(String taskParams) {
        AssertUtil.isTrue(isTaskParamsJson(taskParams), ExceptionEnum.COMPONENT_PARAMS_ERROR, "taskParams is not a valid json object. taskParams: {}", taskParams);
        Map<String, Object> taskParamsObj = JSON.parseObject(taskParams, new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> recurveParams = Maps.newHashMap();
        recurveMap(StringUtils.EMPTY, taskParamsObj, recurveParams);
        this.params = MapUtils.isEmpty(recurveParams) ? null : JSON.toJSONString(recurveParams, SerializerFeature.DisableCircularReferenceDetect);
        this.converterMapping = ImmutableMap.copyOf(this.converterMapping);
        this.valueMapping = ImmutableMap.copyOf(this.valueMapping);
    }

    private boolean isTaskParamsJson(String taskParams) {
        AssertUtil.notBlank(taskParams);
        try {
            JSON.parseObject(taskParams);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void recurveMap(String key, Map<String, Object> taskParamsObj, Map<String, Object> recurveParams) {
        taskParamsObj.forEach((k, v) -> {
            AssertUtil.notBlank(k, ExceptionEnum.SERVICE_PARAM_ERROR, "taskParams specifies that the input key cannot be empty. key: {}, taskParams: {}", k, taskParamsObj);
            String varKey = key;
            if (StringUtils.isNotBlank(varKey)) {
                varKey += ".";
            }
            Pair<String, String> ktPair = TaskServiceUtil.getConverterMapping(k);
            if (v instanceof Map) {
                Map<String, Object> iMap = Maps.newHashMap();
                if (StringUtils.isAnyBlank(ktPair.getKey(), ktPair.getValue())) {
                    recurveParams.put(k, iMap);
                } else {
                    recurveParams.put(ktPair.getKey(), iMap);
                    converterMapping.put(varKey + ktPair.getKey(), ktPair.getValue());
                }
                recurveMap(varKey + (StringUtils.isAnyBlank(ktPair.getKey(), ktPair.getValue()) ? k : ktPair.getKey()), (Map<String, Object>) v, iMap);
                return;
            }
            if (v instanceof List) {
                List<Object> copeList = Lists.newArrayList();
                if (StringUtils.isAnyBlank(ktPair.getKey(), ktPair.getValue())) {
                    recurveParams.put(k, copeList);
                } else {
                    recurveParams.put(ktPair.getKey(), copeList);
                    converterMapping.put(varKey + ktPair.getKey(), ktPair.getValue());
                }
                if (CollectionUtils.isEmpty((List<?>) v)) {
                    return;
                }
                recurveList((List<?>) v, varKey + (StringUtils.isAnyBlank(ktPair.getKey(), ktPair.getValue()) ? k : ktPair.getKey()), copeList);
                return;
            }
            if (StringUtils.isAnyBlank(ktPair.getKey(), ktPair.getValue())) {
                recurveParams.put(k, v);
            } else {
                recurveParams.put(ktPair.getKey(), v);
                converterMapping.put(varKey + ktPair.getKey(), ktPair.getValue());
            }
            if (v instanceof String) {
                String valStr = (String) v;
                if (valStr.startsWith("@") && ElementParserUtil.isValidDataExpression(valStr.substring(1))) {
                    valueMapping.put(varKey + (StringUtils.isAnyBlank(ktPair.getKey(), ktPair.getValue()) ? k : ktPair.getKey()), valStr.substring(1));
                }
            }
        });
    }

    private void recurveList(List<?> vList, String varKey, List<Object> copeList) {
        if (CollectionUtils.isEmpty(vList)) {
            return;
        }
        for (int i = 0; i < vList.size(); i++) {
            Object o = vList.get(i);
            if (o instanceof Map) {
                Map<String, Object> iMap = Maps.newHashMap();
                copeList.add(iMap);
                recurveMap(GlobalUtil.format("{}.[{}]", varKey, i), (Map<String, Object>) vList.get(i), iMap);
                continue;
            }
            if (o instanceof List) {
                List<Object> cList = Lists.newArrayList();
                copeList.add(cList);
                recurveList((List<?>) o, GlobalUtil.format("{}.[{}]", varKey, i), cList);
                continue;
            }
            copeList.add(o);
            if (o instanceof String) {
                String valStr = (String) o;
                if (valStr.startsWith("@") && ElementParserUtil.isValidDataExpression(valStr.substring(1))) {
                    valueMapping.put(GlobalUtil.format("{}.[{}]", varKey, i), valStr.substring(1));
                }
            }
        }
    }

    public Map<String, Object> getParams() {
        return StringUtils.isEmpty(params) ? Maps.newHashMap() : JSON.parseObject(params);
    }

    public Map<String, String> getConverterMapping() {
        return converterMapping;
    }

    public Map<String, String> getValueMapping() {
        return valueMapping;
    }
}
