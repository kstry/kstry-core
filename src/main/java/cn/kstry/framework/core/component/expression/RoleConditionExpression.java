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
package cn.kstry.framework.core.component.expression;


import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.role.Permission;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.PermissionUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author lykan
 */
public class RoleConditionExpression extends ConditionExpressionImpl implements ConditionExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleConditionExpression.class);

    private static final Cache<String, Optional<Permission>> rolePermissionCache = CacheBuilder.newBuilder()
            .concurrencyLevel(8).initialCapacity(1024).maximumSize(50_000).expireAfterAccess(10, TimeUnit.MINUTES)
            .removalListener(notification -> LOGGER.info("role permission cache lose efficacy. key:{}, value:{}, cause:{}", notification.getKey(), notification.getValue(), notification.getCause()))
            .build();

    @SuppressWarnings("all")
    public RoleConditionExpression() {
        super((scopeData, exp) -> {
            Optional<Permission> permissionOptional = rolePermissionCache.getIfPresent(exp);
            AssertUtil.notNull(permissionOptional);
            Optional<Role> roleOptional = scopeData.getRole();
            if (!roleOptional.isPresent() || !permissionOptional.isPresent()) {
                return false;
            }
            return roleOptional.get().allowPermission(permissionOptional.get());
        });
    }

    @Override
    public boolean match(String expression) {
        Optional<Permission> permissionOptional = PermissionUtil.parsePermission(expression);
        rolePermissionCache.put(expression, permissionOptional);
        try {
            return rolePermissionCache.get(expression, () -> PermissionUtil.parsePermission(expression)).isPresent();
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            throw KstryException.buildException(ExceptionEnum.STORY_ERROR);
        }
    }
}
