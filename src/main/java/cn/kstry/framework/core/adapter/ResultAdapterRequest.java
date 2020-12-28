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

import cn.kstry.framework.core.bus.GlobalBus;
import cn.kstry.framework.core.facade.TaskPipelinePort;
import cn.kstry.framework.core.facade.TaskPipelinePortBox;
import cn.kstry.framework.core.engine.TaskAction;
import cn.kstry.framework.core.route.TaskRouter;
import cn.kstry.framework.core.util.AssertUtil;

import java.util.List;

/**
 * @author lykan
 */
public class ResultAdapterRequest extends TaskPipelinePortBox<Object> implements TaskPipelinePort<Object> {

    private static final long serialVersionUID = 3146375674065919627L;

    /**
     * 全局 Bus
     */
    private GlobalBus globalBus;

    /**
     * task group
     */
    private List<TaskAction> taskGroup;

    /**
     * mapping repository
     */
    private ResultMappingRepository resultMappingRepository;

    /**
     * 路由器
     */
    private TaskRouter router;

    public TaskRouter getRouter() {
        return router;
    }

    public void setRouter(TaskRouter router) {
        AssertUtil.notNull(router);
        this.router = router;
    }

    public GlobalBus getGlobalBus() {
        return globalBus;
    }

    public void setGlobalBus(GlobalBus globalBus) {
        AssertUtil.notNull(globalBus);
        this.globalBus = globalBus;
    }

    public ResultMappingRepository getResultMappingRepository() {
        return resultMappingRepository;
    }

    public void setResultMappingRepository(ResultMappingRepository resultMappingRepository) {
        AssertUtil.notNull(resultMappingRepository);
        this.resultMappingRepository = resultMappingRepository;
    }

    public List<TaskAction> getTaskGroup() {
        return taskGroup;
    }

    public void setTaskGroup(List<TaskAction> taskGroup) {
        AssertUtil.notEmpty(taskGroup);
        this.taskGroup = taskGroup;
    }
}
