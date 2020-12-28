package cn.kstry.framework.core.engine;
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
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.operator.TaskActionOperatorRole;
import cn.kstry.framework.core.route.TaskRouter;

import java.util.Map;

/**
 * Task 最上层功能定义，具备以下功能：
 * - 路由
 * - 获取 Task 执行的 Operator
 * - Task 身份识别
 *
 * @author lykan
 */
public interface TaskAction {

    /**
     * Task 路由
     *
     * @param router 路由器
     * @return Task 是否允许被执行
     */
    boolean route(TaskRouter router);

    /**
     * 获取 Task 名称
     *
     * @return Task 名称
     */
    String getTaskActionName();

    /**
     * 获取 Task 类型
     *
     * @return 类型
     */
    ComponentTypeEnum getTaskActionTypeEnum();

    /**
     * 获取 Task Operator
     *
     * @return Task Operator
     */
    TaskActionOperatorRole getTaskActionOperator();

    /**
     * 设置  Task Operator
     *
     * @param taskOperator Task Operator
     */
    void setTaskActionOperator(TaskActionOperatorRole taskOperator);

    /**
     * 获取 Task Action 操作者的 角色类
     */
    Class<? extends TaskActionOperatorRole> getTaskActionOperatorRoleClass();

    /**
     * 获取 TaskAction 中被代理 Method 的集合
     */
    Map<String, TaskActionMethod> getActionMethodMap();
}
