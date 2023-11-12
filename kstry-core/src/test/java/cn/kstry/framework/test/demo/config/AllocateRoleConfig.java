package cn.kstry.framework.test.demo.config;

import cn.kstry.framework.core.component.dynamic.creator.DynamicRole;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.role.ServiceTaskRole;
import cn.kstry.framework.core.util.PermissionUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class AllocateRoleConfig implements DynamicRole {

    @Override
    public Optional<Role> getRole(String key) {
        if (Objects.equals("test-rbac-flow-demo@external-business-id", key)) {
            ServiceTaskRole serviceTaskRole = new ServiceTaskRole();
            serviceTaskRole.addPermission(PermissionUtil.permissionList("r:calculatePrice@external, r:lockStock@external"));
            return Optional.of(serviceTaskRole);
        }
        return Optional.empty();
    }
}
