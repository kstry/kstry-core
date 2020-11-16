package cn.kstry.framework.test.demo.action;


import cn.kstry.framework.core.facade.TaskPipelinePortBox;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.facade.TaskResponseBox;
import cn.kstry.framework.test.demo.entity.HongBao;
import cn.kstry.framework.test.demo.enums.HongBaoStatusEnum;
import cn.kstry.framework.test.demo.facade.CreateHongBaoRequest;
import cn.kstry.framework.test.demo.facade.CreateHongBaoResponse;
import cn.kstry.framework.test.demo.facade.RobHongBaoRequest;
import cn.kstry.framework.test.demo.facade.RobHongBaoResponse;
import cn.kstry.framework.test.demo.facade.UpdateHongBaoStatusForPaySuccessRequest;
import cn.kstry.framework.test.demo.facade.UpdateHongBaoStatusForPaySuccessResponse;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class HongBaoOperateActionImpl extends BaseHongBaoOperateAction implements HongBaoOperateAction {

    /**
     * mock hongBao DB
     */
    private static final List<HongBao> HONG_BAO_LIST = new ArrayList<>();

    @Override
    public TaskResponse<CreateHongBaoResponse> createHongBao(CreateHongBaoRequest request) {

        Date now = new Date();

        HongBao hongBao = new HongBao();

        hongBao.setFullCount(request.getCount());
        hongBao.setRemainCount(request.getCount());
        hongBao.setCreateTime(now);
        hongBao.setId((long) HONG_BAO_LIST.size() + 1);
        hongBao.setFullMoney(request.getFullMoney());
        hongBao.setRemainMoney(request.getFullMoney());
        hongBao.setSpendMoney(0L);
        hongBao.setUpdateTime(now);

        hongBao.setStatus(HongBaoStatusEnum.CREATE);

        HongBao hongBaoCache = new HongBao();
        BeanUtils.copyProperties(hongBao, hongBaoCache);
        HONG_BAO_LIST.add(hongBaoCache);

        CreateHongBaoResponse createHongBaoResponse = new CreateHongBaoResponse();
        createHongBaoResponse.setHongBao(hongBao);

        System.out.println("create hongbao ->" + JSON.toJSONString(createHongBaoResponse));
        return TaskResponseBox.buildSuccess(createHongBaoResponse);
    }

    @Override
    public TaskResponse<UpdateHongBaoStatusForPaySuccessResponse> updateHongBaoStatusForPaySuccess(UpdateHongBaoStatusForPaySuccessRequest request) {

        Optional<HongBao> first =
                HONG_BAO_LIST.stream().filter(h -> h.getId().equals(request.getHongBaoId())).findFirst();

        if (!first.isPresent()) {
            throw new RuntimeException("");
        }

        first.get().setStatus(HongBaoStatusEnum.EFFECT);
        System.out.println("update status ->" + JSON.toJSONString(first));
        return TaskResponseBox.buildSuccess(new UpdateHongBaoStatusForPaySuccessResponse());
    }

    @Override
    public TaskPipelinePortBox<RobHongBaoResponse> robHongBao(RobHongBaoRequest request) {

        Optional<HongBao> first =
                HONG_BAO_LIST.stream().filter(h -> h.getId().equals(request.getHongBaoId())).findFirst();

        if (!first.isPresent()) {
            throw new RuntimeException("");
        }

        HongBao hongBao = first.get();

        if (!HongBaoStatusEnum.EFFECT.equals(hongBao.getStatus()) || hongBao.getRemainMoney() <= (long) hongBao.getRemainCount()) {
            throw new RuntimeException("");
        }

        long robMoney;
        if (hongBao.getRemainCount() == 1) {
            robMoney = hongBao.getRemainMoney();
        } else {
            robMoney = (long) (Math.random() * (hongBao.getRemainMoney() - hongBao.getRemainCount()));
            robMoney = (robMoney == 0) ? 1 : robMoney;
        }

        int remainCount = hongBao.getRemainCount() - 1;

        hongBao.setRemainCount(remainCount);
        hongBao.setRemainMoney(hongBao.getRemainMoney() - robMoney);
        hongBao.setUpdateTime(new Date());
        hongBao.setSpendMoney(hongBao.getSpendMoney() + robMoney);
        hongBao.setRobMoney(robMoney);
        hongBao.setRobMoney(robMoney);
        if (remainCount <= 0) {
            hongBao.setStatus(HongBaoStatusEnum.FINISH);
        }

        HongBao hongBaoCache = new HongBao();
        BeanUtils.copyProperties(hongBao, hongBaoCache);
        System.out.println("rob status ->" + JSON.toJSONString(hongBaoCache));

        RobHongBaoResponse response = new RobHongBaoResponse();
        response.setHongBao(hongBaoCache);

        TaskPipelinePortBox<RobHongBaoResponse> responseBox = new TaskPipelinePortBox<>();

        responseBox.resultSuccess();
        responseBox.setResult(response);

        RobHongBaoRequest r = new RobHongBaoRequest();
        r.setHongBaoId(request.getHongBaoId());
        responseBox.setTaskRequest(r);
        return responseBox;
    }

    @Override
    public String getTaskActionName() {
        return "HONGBAO_OPERATE_ACTION";
    }
}
