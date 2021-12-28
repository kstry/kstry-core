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
package cn.kstry.framework.core.engine.facade;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 *
 * @author lykan
 */
public class CustomRoleInfo {

    private String taskComponentName;

    private String taskServiceName;

    public static Optional<CustomRoleInfo> buildCustomRole(String customRole) {
        if (StringUtils.isBlank(customRole)) {
            return Optional.empty();
        }

        String[] split = customRole.split("@");
        if (split.length != 2) {
            return Optional.empty();
        }
        CustomRoleInfo customRoleInfo = new CustomRoleInfo();
        customRoleInfo.taskComponentName = split[0];
        customRoleInfo.taskServiceName = split[1];
        return Optional.of(customRoleInfo);
    }

    public String getTaskComponentName() {
        return taskComponentName;
    }

    public String getTaskServiceName() {
        return taskServiceName;
    }
}
