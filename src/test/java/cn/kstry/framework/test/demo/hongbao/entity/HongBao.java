package cn.kstry.framework.test.demo.hongbao.entity;

import cn.kstry.framework.test.demo.hongbao.enums.HongBaoStatusEnum;

import java.util.Date;

/**
 * @author lykan
 */
public class HongBao {

    /**
     * 红包ID
     */
    private Long id;

    /**
     * 红包库存
     */
    private Integer fullCount;

    /**
     * 红包剩余库存
     */
    private Integer remainCount;

    /**
     * 红包状态
     */
    private HongBaoStatusEnum status;

    /**
     * 红包总金额 单位：分
     */
    private Long fullMoney;

    /**
     * 红包剩余金额 单位：分
     */
    private Long remainMoney;

    /**
     * 已花费金额
     * <p>
     * spendMoney = fullMoney - remainMoney
     */
    private Long spendMoney;

    /**
     * 本次抢红包金额，单位分
     */
    private Long robMoney;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFullCount() {
        return fullCount;
    }

    public void setFullCount(Integer fullCount) {
        this.fullCount = fullCount;
    }

    public Integer getRemainCount() {
        return remainCount;
    }

    public void setRemainCount(Integer remainCount) {
        this.remainCount = remainCount;
    }

    public HongBaoStatusEnum getStatus() {
        return status;
    }

    public void setStatus(HongBaoStatusEnum status) {
        this.status = status;
    }

    public Long getFullMoney() {
        return fullMoney;
    }

    public void setFullMoney(Long fullMoney) {
        this.fullMoney = fullMoney;
    }

    public Long getRemainMoney() {
        return remainMoney;
    }

    public void setRemainMoney(Long remainMoney) {
        this.remainMoney = remainMoney;
    }

    public Long getSpendMoney() {
        return spendMoney;
    }

    public void setSpendMoney(Long spendMoney) {
        this.spendMoney = spendMoney;
    }

    public Long getRobMoney() {
        return robMoney;
    }

    public void setRobMoney(Long robMoney) {
        this.robMoney = robMoney;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
