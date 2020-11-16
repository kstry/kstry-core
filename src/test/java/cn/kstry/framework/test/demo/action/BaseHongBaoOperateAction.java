package cn.kstry.framework.test.demo.action;

import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.facade.TaskPipelinePortBox;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.operator.TaskActionOperatorRole;
import cn.kstry.framework.core.route.RouteTaskAction;
import cn.kstry.framework.test.demo.HongBaoOperatorRole;
import cn.kstry.framework.test.demo.facade.CreateHongBaoRequest;
import cn.kstry.framework.test.demo.facade.CreateHongBaoResponse;
import cn.kstry.framework.test.demo.facade.RobHongBaoRequest;
import cn.kstry.framework.test.demo.facade.RobHongBaoResponse;
import cn.kstry.framework.test.demo.facade.RobHongBaoTaskPipelinePort;
import cn.kstry.framework.test.demo.facade.UpdateHongBaoStatusForPaySuccessRequest;
import cn.kstry.framework.test.demo.facade.UpdateHongBaoStatusForPaySuccessResponse;


public abstract class BaseHongBaoOperateAction extends RouteTaskAction implements HongBaoOperateAction {

    @Override
    public TaskResponse<? extends CreateHongBaoResponse> createHongBao(CreateHongBaoRequest request) {
        throw new RuntimeException("");
    }

    @Override
    public TaskResponse<? extends UpdateHongBaoStatusForPaySuccessResponse> updateHongBaoStatusForPaySuccess(UpdateHongBaoStatusForPaySuccessRequest request) {
        throw new RuntimeException("");
    }

    @Override
    public TaskPipelinePortBox<RobHongBaoResponse> robHongBao(RobHongBaoRequest request) {
        throw new RuntimeException("");
    }

    @Override
    public ComponentTypeEnum getTaskActionTypeEnum() {
        return ComponentTypeEnum.TASK;
    }

    @Override
    public Class<? extends TaskActionOperatorRole> getTaskActionOperatorRoleClass() {
        return HongBaoOperatorRole.class;
    }
}
