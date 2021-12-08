package cn.kstry.framework.test.flow.bo;

public class Te4Request {

    private Long goodsId;

    private Long activityId;

    private long hospitalId;

    private int count;

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

    public int getCount() {
        return count;
    }

    public synchronized void increase() {
        this.count++;
    }
}
