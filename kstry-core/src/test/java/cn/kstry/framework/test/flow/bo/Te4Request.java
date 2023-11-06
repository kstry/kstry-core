package cn.kstry.framework.test.flow.bo;

import com.google.common.collect.Lists;

import java.util.List;

public class Te4Request {

    private Long goodsId;

    private Long activityId;

    private long hospitalId;

    private int count;

    private List<String> nodeList = Lists.newCopyOnWriteArrayList();

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(long hospitalId) {
        this.hospitalId = hospitalId;
    }

    public List<String> getNodeList() {
        return nodeList;
    }

    public synchronized int getCount() {
        return count;
    }

    public synchronized void increase() {
        this.count++;
    }
}
