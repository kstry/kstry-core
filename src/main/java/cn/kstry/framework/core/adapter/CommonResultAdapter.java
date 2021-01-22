/*
 *
 *  *  Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
 *  *  <p>
 *  *  Licensed under the GNU Lesser General Public License 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *  <p>
 *  * https://www.gnu.org/licenses/lgpl.html
 *  *  <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.core.adapter;

import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.facade.TaskPipelinePort;
import cn.kstry.framework.core.facade.TaskPipelinePortBox;
import cn.kstry.framework.core.facade.TaskRequest;
import cn.kstry.framework.core.operator.EventOperatorRole;
import cn.kstry.framework.core.route.TaskNode;
import cn.kstry.framework.core.route.RouteEventGroup;
import cn.kstry.framework.core.route.TaskRouter;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

import static cn.kstry.framework.core.bus.StoryBus.DEFAULT_GLOBAL_BUS_PARAMS_KEY;

/**
 * @author lykan
 */
public class CommonResultAdapter extends RouteEventGroup implements ResultAdapterRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonResultAdapter.class);

    /**
     * SpEL 表达式解析器
     */
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    @Override
    public TaskPipelinePort<Object> mapping(ResultAdapterRequest request) {
        try {

            TaskRouter router = request.getRouter();
            TaskNode nextInvokeTaskNode = GlobalUtil.notEmpty(router.nextRouteNodeIgnoreTimeSlot());

            TaskRequest taskRequest = GlobalUtil.notNull(nextInvokeTaskNode.getRequestClass()).newInstance();
            EvaluationContext standardContext = getEvaluationContext(request, taskRequest);

            TaskNode fromTaskNode = router.beforeInvokeRouteNodeIgnoreTimeSlot().orElse(DEFAULT_GLOBAL_BUS_PARAMS_KEY);
            Map<String, String> taskResultMapping = request.getResultMappingRepository().getTaskResultMapping(fromTaskNode, nextInvokeTaskNode);
            taskResultMapping.forEach((k, v) -> {
                try {
                    PARSER.parseExpression(k).setValue(standardContext, PARSER.parseExpression(v).getValue(standardContext));
                } catch (Exception e) {
                    LOGGER.warn("result adapter parse expression error! k:{}, v:{}", k, v, e);
                }
            });
            TaskPipelinePort<Object> result = new TaskPipelinePortBox<>();
            result.setSuccess(true);
            result.setTaskRequest(taskRequest);
            return result;
        } catch (Exception e) {
            KstryException.throwException(e);
            return null;
        }
    }

    private EvaluationContext getEvaluationContext(ResultAdapterRequest request, TaskRequest taskRequest) {
        EvaluationContext standardContext = new StandardEvaluationContext(taskRequest);
        request.getStoryBus().doParamsConsumer((k, v) -> {
            AssertUtil.anyNotNull(k, v);
            standardContext.setVariable(k.identityStr(), v);
        });
        request.getStoryBus().doResponseConsumer((k, v) -> {
            AssertUtil.anyNotNull(k, v);
            standardContext.setVariable(k.identityStr(), v.getResult());
        });
        if (request.isSuccess() && request.getResult() != null) {
            standardContext.setVariable(TASK_RESULT_DATA_KEY, request.getResult());
        }
        return standardContext;
    }

    @Override
    public String getEventGroupName() {
        return COMMON_TASK_RESULT_MAPPING_TASK_KEY;
    }

    @Override
    public ComponentTypeEnum getEventGroupTypeEnum() {
        return ComponentTypeEnum.MAPPING;
    }

    @Override
    public Class<? extends EventOperatorRole> getTaskActionOperatorRoleClass() {
        return ResultAdapterRole.class;
    }
}
