/*
 *
 *  * Copyright (c) 2020-2022, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.enums.PermissionType;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author lykan
 */
public class KeyUtil {

    private final static Pattern scopeItemPattern = Pattern.compile("[a-zA-Z_][\\w-()]*");

    private final static List<String> valueKeyword = Lists.newArrayList("true", "false", "null");

    public static String r(String service) {
        AssertUtil.anyNotBlank(service);
        return GlobalUtil.format("{}:{}", PermissionType.SERVICE.getPrefix(), service);
    }

    public static String r(String service, String ability) {
        if (StringUtils.isBlank(ability)) {
            return r(service);
        }
        AssertUtil.anyNotBlank(service, ability);
        return GlobalUtil.format("{}:{}@{}", PermissionType.SERVICE_ABILITY.getPrefix(), service, ability);
    }

    public static String pr(String component, String service) {
        AssertUtil.anyNotBlank(component, service);
        return GlobalUtil.format("{}:{}@{}", PermissionType.COMPONENT_SERVICE.getPrefix(), component, service);
    }

    public static String pr(String component, String service, String ability) {
        if (StringUtils.isBlank(ability)) {
            return pr(component, service);
        }
        AssertUtil.anyNotBlank(component, service);
        return GlobalUtil.format("{}:{}@{}@{}", PermissionType.COMPONENT_SERVICE_ABILITY.getPrefix(), component, service, ability);
    }

    public static String nr(String service) {
        return "!" + r(service);
    }

    public static String nr(String service, String ability) {
        return "!" + r(service, ability);
    }

    public static String npr(String component, String service) {
        return "!" + pr(component, service);
    }

    public static String npr(String component, String service, String ability) {
        return "!" + pr(component, service, ability);
    }

    public static String req(Object... items) {
        return scopeKeyAppend(ScopeTypeEnum.REQUEST, items);
    }

    public static String sta(Object... items) {
        return scopeKeyAppend(ScopeTypeEnum.STABLE, items);
    }

    public static String var(Object... items) {
        return scopeKeyAppend(ScopeTypeEnum.VARIABLE, items);
    }

    public static String res(Object... items) {
        return scopeKeyAppend(ScopeTypeEnum.RESULT, items);
    }

    private static String scopeKeyAppend(ScopeTypeEnum typeEnum, Object... items) {
        if (items == null || items.length == 0) {
            return typeEnum.getKey();
        }
        StringBuilder sb = new StringBuilder(typeEnum.getKey());
        for (Object itemObj : items) {
            AssertUtil.notNull(itemObj);
            String item = itemObj.toString();
            if (!valueKeyword.contains(item.toLowerCase()) && scopeItemPattern.matcher(item).matches()) {
                sb.append(".");
            }
            sb.append(item);
        }
        return sb.toString();
    }
}
