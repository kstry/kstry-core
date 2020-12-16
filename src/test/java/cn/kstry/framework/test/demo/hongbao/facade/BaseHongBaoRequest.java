package cn.kstry.framework.test.demo.hongbao.facade;

import cn.kstry.framework.core.facade.TaskRequest;


public class BaseHongBaoRequest implements TaskRequest {

    private static final long serialVersionUID = 2425979162303471330L;

    /**
     * 红包 ID
     */
    private Long hongBaoId;

    public Long getHongBaoId() {
        return hongBaoId;
    }

    public void setHongBaoId(Long hongBaoId) {
        this.hongBaoId = hongBaoId;
    }
}
