package cn.kstry.framework.test.flow.service;

import cn.kstry.framework.core.annotation.ReqTaskParam;
import cn.kstry.framework.core.annotation.StaTaskParam;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.test.flow.bo.Activity;
import cn.kstry.framework.test.flow.bo.Goods;
import com.alibaba.fastjson.JSON;

@SuppressWarnings("unused")
@TaskComponent(name = "activity_service")
public class ActivityService {

    @TaskService(name = "get_activity")
    public Activity getActivity(@ReqTaskParam("activityId") Long id) {

        Activity activity = new Activity();
        activity.setId(id);
        activity.setDiscountPrice(5L);
        System.out.println("thread ->" + Thread.currentThread().getName() + ", get activity ->" + JSON.toJSONString(activity));
        return activity;
    }

    @TaskService(name = "calculate_price")
    public void fillGoods(@StaTaskParam("goods") Goods goods, @StaTaskParam("activity") Activity activity) {
        if (activity != null) {
            goods.setPrice(goods.getPrice() - activity.getDiscountPrice());
        }
        System.out.println("thread ->" + Thread.currentThread().getName() + ", calculate price ->" + JSON.toJSONString(goods));
    }
}
