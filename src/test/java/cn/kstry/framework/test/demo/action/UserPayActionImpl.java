package cn.kstry.framework.test.demo.action;


import cn.kstry.framework.core.annotation.TaskActionComponent;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.facade.RouteMapResponse;
import cn.kstry.framework.core.facade.RouteMapResponseBox;
import cn.kstry.framework.test.demo.UserPayRole;
import cn.kstry.framework.test.demo.entity.HongBaoDynamicRouteTable;
import cn.kstry.framework.test.demo.facade.UserPayRequest;
import cn.kstry.framework.test.demo.facade.UserPayResponse;
import com.alibaba.fastjson.JSON;
import org.springframework.util.Assert;

@TaskActionComponent(taskActionName = "USER_PAY_ACTION", taskActionTypeEnum = ComponentTypeEnum.TASK, operatorRoleClass = UserPayRole.class)
public class UserPayActionImpl implements UserPayAction {

    @Override
    public RouteMapResponse<UserPayResponse> pay(UserPayRequest request) {
        Assert.notNull(request, "");
        Assert.notNull(request.getPayMoney(), "");

        UserPayResponse userPayResponse = new UserPayResponse();

        UserPayResponse.Money m = new UserPayResponse.Money();
        m.setPayMoney(request.getPayMoney());
        userPayResponse.setMoney(m);
        System.out.println("pay money ->" + JSON.toJSONString(userPayResponse));

        RouteMapResponse<UserPayResponse> result = RouteMapResponseBox.buildSuccess(userPayResponse);
        HongBaoDynamicRouteTable hongBaoDynamicRouteTable = new HongBaoDynamicRouteTable();
        hongBaoDynamicRouteTable.setSource(2f);
        result.updateTaskRouterTable(hongBaoDynamicRouteTable);
        return result;
    }

    @Override
    public void first() {
        System.out.println("pay first invoke~");
    }
}
