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
package cn.kstry.framework.core.resource.identity;

import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.util.AssertUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;

/**
 * BasicIdentity
 *
 * @author lykan
 */
public abstract class BasicIdentity implements Identity {

    /**
     * 资源ID
     */
    private final String identityId;

    /**
     * 资源类型
     */
    private final IdentityTypeEnum identityType;

    public BasicIdentity(String identityId, IdentityTypeEnum identityType) {
        AssertUtil.notBlank(identityId);
        AssertUtil.notNull(identityType);

        this.identityId = identityId;
        this.identityType = identityType;
    }

    @Nonnull
    @Override
    public String getIdentityId() {
        return this.identityId;
    }

    @Nonnull
    @Override
    public IdentityTypeEnum getIdentityType() {
        return this.identityType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BasicIdentity that = (BasicIdentity) o;

        return new EqualsBuilder().append(identityId, that.identityId).append(identityType, that.identityType).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(identityId).append(identityType).toHashCode();
    }
}
