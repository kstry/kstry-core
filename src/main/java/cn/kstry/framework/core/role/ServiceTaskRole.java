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

/**
 * 使用该角色，无需授权就可以执行全部的服务节点，但是服务能力节点不会被执行。
 * 如果分配了服务能力节点，与之对应的服务节点权限将不允许被分配，否则会出现匹配到多个能力的异常
 *
 * @author lykan
 */
public class ServiceTaskRole extends BasicRole {

    public ServiceTaskRole() {
        super("default-service-task-role");
    }
}
