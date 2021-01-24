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
import cn.kstry.framework.core.operator.EventOperatorRole;
import cn.kstry.framework.core.route.TaskRouter;

/**
 * 定义一组相关的 Event
 *
 * @author lykan
 */
public interface EventGroup {

    /**
     * 路由，选择最终承接任务执行的事件组
     *
     * @param router 路由器
     * @return 是否承接成功
     */
    boolean route(TaskRouter router);

    /**
     * 获取 EventGroup 名称
     *
     * @return EventGroup 名称
     */
    String getEventGroupName();

    /**
     * 获取 EventGroup 类型
     *
     * @return 类型
     */
    ComponentTypeEnum getEventGroupTypeEnum();

    /**
     * 获取 Event Operator 的 角色类
     */
    Class<? extends EventOperatorRole> getOperatorRoleClass();
}
