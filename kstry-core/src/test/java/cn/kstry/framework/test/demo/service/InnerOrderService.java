package cn.kstry.framework.test.demo.service;

import cn.kstry.framework.core.annotation.NoticeResult;
import cn.kstry.framework.core.annotation.NoticeVar;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.test.demo.facade.CommonFields;

@TaskComponent(name = "orderService")
public class InnerOrderService implements OrderService {

    @Override
    @TaskService
    @NoticeVar(target = CommonFields.F.price)
    public long calculatePrice(long goodsId) {
        System.out.println("InnerOrderService calculatePrice...");
        return 100L;
    }

    @Override
    @TaskService
    @NoticeVar(target = CommonFields.F.lockStockResult)
    public boolean lockStock(long goodsId) {
        System.out.println("InnerOrderService lockStock...");
        return true;
    }

    @Override
    @NoticeResult
    @TaskService
    public long geneOrderId(long price, long goodsId) {
        System.out.println("InnerOrderService geneOrderId...");
        return 2987;
    }
}
