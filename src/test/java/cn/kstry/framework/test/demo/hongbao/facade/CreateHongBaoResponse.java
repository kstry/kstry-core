package cn.kstry.framework.test.demo.hongbao.facade;

import cn.kstry.framework.core.facade.TaskResponseBox;
import cn.kstry.framework.test.demo.hongbao.entity.HongBao;

public class CreateHongBaoResponse extends TaskResponseBox {

    private static final long serialVersionUID = -8512853565263321141L;

    private HongBao hongBao;

    public HongBao getHongBao() {
        return hongBao;
    }

    public void setHongBao(HongBao hongBao) {
        this.hongBao = hongBao;
    }
}
