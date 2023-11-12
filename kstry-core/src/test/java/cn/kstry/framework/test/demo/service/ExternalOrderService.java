package cn.kstry.framework.test.demo.service;

import cn.kstry.framework.core.annotation.NoticeVar;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.test.demo.facade.CommonFields;

@TaskComponent(name = "orderService", scanSuper = false)
public class ExternalOrderService extends InnerOrderService {

    @Override
    @NoticeVar(target = CommonFields.F.price)
    @TaskService(ability = "external")
    public long calculatePrice(long goodsId) {
        System.out.println("ExternalOrderService calculatePrice...");
        return 200L;
    }

    @Override
    @NoticeVar(target = CommonFields.F.lockStockResult)
    @TaskService(ability = "external")
    public boolean lockStock(long goodsId) {
        System.out.println("ExternalOrderService lockStock...");
        return false;
    }
}
