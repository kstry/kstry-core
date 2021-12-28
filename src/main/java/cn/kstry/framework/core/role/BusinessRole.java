/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.role;

import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author lykan
 */
public class BusinessRole {

    private final List<String> businessIdList;

    private final List<String> startIdList;

    private final Role role;

    public BusinessRole(List<String> businessIdList, List<String> startIdList, Role role) {
        AssertUtil.notNull(role);
        AssertUtil.notNull(businessIdList);
        AssertUtil.notEmpty(startIdList);
        AssertUtil.notEmpty(startIdList.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        this.businessIdList = businessIdList.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        this.startIdList = startIdList.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        this.role = role;
    }

    public BusinessRole(String startId, Role role) {
        this(Lists.newArrayList(), Lists.newArrayList(startId), role);
    }

    public BusinessRole(String businessId, String startId, Role role) {
        this(Lists.newArrayList(businessId), Lists.newArrayList(startId), role);
    }

    public BusinessRole(List<String> startIdList, Role role) {
        this(Lists.newArrayList(), startIdList, role);
    }

    public BusinessRole(String businessId, List<String> startIdList, Role role) {
        this(Lists.newArrayList(businessId).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()), startIdList, role);
    }

    public BusinessRole(List<String> businessIdList, String startId, Role role) {
        this(businessIdList, Lists.newArrayList(startId).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()), role);
    }

    public boolean priorityMatch(String businessId, String startId) {
        if (StringUtils.isBlank(businessId) && CollectionUtils.isEmpty(businessIdList)) {
            return startIdList.contains(startId);
        }
        return businessIdList.contains(businessId) && startIdList.contains(startId);
    }

    public boolean secondMatch(String startId) {
        if (CollectionUtils.isNotEmpty(businessIdList)) {
            return false;
        }
        return startIdList.contains(startId);
    }

    public List<String> getBusinessIdList() {
        return ImmutableList.copyOf(businessIdList);
    }

    public List<String> getStartIdList() {
        return ImmutableList.copyOf(startIdList);
    }

    public Role getRole() {
        return role;
    }
}
