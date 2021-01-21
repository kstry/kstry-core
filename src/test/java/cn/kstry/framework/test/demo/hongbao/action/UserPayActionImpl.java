package cn.kstry.framework.test.demo.hongbao.action;


import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.facade.RouteMapResponse;
import cn.kstry.framework.core.facade.RouteMapResponseBox;
import cn.kstry.framework.test.demo.hongbao.UserPayRole;
import cn.kstry.framework.test.demo.hongbao.entity.HongBaoDynamicRouteTable;
import cn.kstry.framework.test.demo.hongbao.facade.UserPayRequest;
import cn.kstry.framework.test.demo.hongbao.facade.UserPayResponse;
import com.alibaba.fastjson.JSON;
import org.springframework.util.Assert;

@EventGroupComponent(eventGroupName = "USER_PAY_ACTION", operatorRoleClass = UserPayRole.class)
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
