package cn.kstry.framework.test.demo.hongbao.enums;

/**
 * @author lykan
 */
public enum HongBaoStatusEnum {

    /**
     * 创建
     */
    CREATE(1),

    /**
     * 生效
     */
    EFFECT(2),

    /**
     * 过期
     */
    OVERDUE(3),

    /**
     * 已抢光
     */
    FINISH(4),
    ;

    private final int status;


    HongBaoStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
