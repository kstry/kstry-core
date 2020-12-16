package cn.kstry.framework.test.demo.hongbao.facade;


import cn.kstry.framework.test.demo.hongbao.entity.HongBao;

public class RobHongBaoResponse {

    /**
     * 红包 success == true时，hongBao 一定存在
     */
    private HongBao hongBao;

    public HongBao getHongBao() {
        return hongBao;
    }

    public void setHongBao(HongBao hongBao) {
        this.hongBao = hongBao;
    }
}
