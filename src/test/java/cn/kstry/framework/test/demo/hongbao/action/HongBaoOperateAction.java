package cn.kstry.framework.test.demo.hongbao.action;


import cn.kstry.framework.core.facade.TaskPipelinePortBox;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.engine.TaskAction;
import cn.kstry.framework.test.demo.hongbao.facade.CreateHongBaoRequest;
import cn.kstry.framework.test.demo.hongbao.facade.CreateHongBaoResponse;
import cn.kstry.framework.test.demo.hongbao.facade.RobHongBaoRequest;
import cn.kstry.framework.test.demo.hongbao.facade.RobHongBaoResponse;
import cn.kstry.framework.test.demo.hongbao.facade.UpdateHongBaoStatusForPaySuccessRequest;
import cn.kstry.framework.test.demo.hongbao.facade.UpdateHongBaoStatusForPaySuccessResponse;

/**
 * 红包可以操作的动作
 */
public interface HongBaoOperateAction extends TaskAction {

    /**
     * 创建红包
     *
     * @param request 非空
     * @return 非空
     */
    TaskResponse<? extends CreateHongBaoResponse> createHongBao(CreateHongBaoRequest request);

    /**
     * 创建红包
     *
     * @param request 非空
     * @return 非空
     */
    TaskResponse<? extends UpdateHongBaoStatusForPaySuccessResponse> updateHongBaoStatusForPaySuccess(UpdateHongBaoStatusForPaySuccessRequest request);

    /**
     * 抢红包
     *
     * @param request 非空
     * @return 非空
     */
    TaskPipelinePortBox<RobHongBaoResponse> robHongBao(RobHongBaoRequest request);
}
