package cn.kstry.framework.test.demo.hongbao.action;

import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.facade.TaskResponseBox;
import cn.kstry.framework.test.demo.hongbao.facade.CreateHongBao2Response;
import cn.kstry.framework.test.demo.hongbao.facade.CreateHongBaoRequest;
import org.springframework.stereotype.Component;

@Component
public class HongBaoOperateAction2Impl extends BaseHongBaoOperateAction implements HongBaoOperateAction {

    @Override
    public TaskResponse<CreateHongBao2Response> createHongBao(CreateHongBaoRequest request) {

        CreateHongBao2Response createHongBao2Response = new CreateHongBao2Response();
        return TaskResponseBox.buildSuccess(createHongBao2Response);
    }

    @Override
    public String getTaskActionName() {
        return "HONGBAO_OPERATE_ACTION2";
    }
}
