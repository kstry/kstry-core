package cn.kstry.flux.demo.config;

import cn.kstry.flux.demo.process.DefProcess;
import cn.kstry.framework.core.role.BusinessRole;
import cn.kstry.framework.core.role.BusinessRoleRegister;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.role.ServiceTaskRole;
import cn.kstry.framework.core.role.permission.Permission;
import cn.kstry.framework.core.util.KeyUtil;
import cn.kstry.framework.core.util.LambdaUtil;
import cn.kstry.framework.core.util.PermissionUtil;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lykan
 */
@Component
public class RoleRegister implements BusinessRoleRegister {

    @Override
    public List<BusinessRole> register() {

        List<String> list = Lists.newArrayList();

        list.add(KeyUtil.r("checkImg", "triple"));
//        list.add("r:init-sku");
        List<Permission> permissions = PermissionUtil.permissionList(String.join(",", list));

        Role role = new ServiceTaskRole();
        role.addPermission(permissions);

        BusinessRole businessRole = new BusinessRole("special-channel", LambdaUtil.getProcessName(DefProcess::buildShowGoodsLink), role);
        return Lists.newArrayList(businessRole);
    }
}
