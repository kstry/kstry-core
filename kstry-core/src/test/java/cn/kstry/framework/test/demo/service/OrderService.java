package cn.kstry.framework.test.demo.service;

public interface OrderService {

    long calculatePrice(long goodsId);

    boolean lockStock(long goodsId);

    long geneOrderId(long price, long goodsId);
}
