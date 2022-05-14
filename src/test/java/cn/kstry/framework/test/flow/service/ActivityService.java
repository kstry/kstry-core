package cn.kstry.framework.test.flow.service;

import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.test.flow.bo.Activity;
import cn.kstry.framework.test.flow.bo.Goods;
import com.alibaba.fastjson.JSON;

@SuppressWarnings("unused")
@TaskComponent(name = "activity_service")
public class ActivityService {

    @TaskService(name = "get_activity", invoke = @Invoke(demotion = "pr:activity_service@get_activity_demotion"))
    public Activity getActivity(@ReqTaskParam("activityId") Long id) {

        Activity activity = new Activity();
        activity.setId(id);
        activity.setDiscountPrice(5L);
        System.out.println("thread ->" + Thread.currentThread().getName() + ", get activity ->" + JSON.toJSONString(activity));
        return activity;
    }

    @TaskService(name = "get_activity_demotion")
    public Activity getActivityDemotion() {
        Activity activity = new Activity();
        return null;
    }

    @TaskService(name = "calculate_price", invoke = @Invoke(demotion = "pr:activity_service@calculate_price_demotion"))
    public void fillGoods(@StaTaskParam("goods") Goods goods, @StaTaskParam("activity") Activity activity) {
        if (activity != null) {
            goods.setPrice(goods.getPrice() - activity.getDiscountPrice());
        }
        System.out.println("thread ->" + Thread.currentThread().getName() + ", calculate price ->" + JSON.toJSONString(goods));
    }

    @TaskService(name = "calculate_price_demotion")
    public void fillGoods(Goods goods, Goods goods2) {

    }
}
