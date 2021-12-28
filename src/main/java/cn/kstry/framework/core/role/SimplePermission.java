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

import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.resource.identity.BasicIdentity;

import javax.annotation.Nonnull;

/**
 *
 * @author lykan
 */
public class SimplePermission extends BasicIdentity implements Permission {

    public SimplePermission(@Nonnull String identityId, @Nonnull IdentityTypeEnum identityType) {
        super(identityId, identityType);
    }
}
