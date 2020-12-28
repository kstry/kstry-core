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
package cn.kstry.framework.core.operator;

import cn.kstry.framework.core.engine.TaskAction;
import cn.kstry.framework.core.util.AssertUtil;

import java.lang.reflect.Proxy;

/**
 * Operator 构造器
 *
 * @author lykan
 */
@SuppressWarnings("unchecked")
public class TaskOperatorCreator {

    /**
     * 根据角色获取实际操作对象
     *
     * @param taskAction task action
     * @return 角色操作对象
     */
    public static <T extends TaskActionOperatorRole> T getTaskOperator(TaskAction taskAction) {
        AssertUtil.notNull(taskAction);
        AssertUtil.notNull(taskAction.getTaskActionOperatorRoleClass());

        TaskOperatorProxy operatorProxy = new TaskOperatorProxy(taskAction);
        Class<? extends TaskActionOperatorRole> operatorRoleClass = taskAction.getTaskActionOperatorRoleClass();
        AssertUtil.notNull(operatorRoleClass);

        T t = (T) Proxy.newProxyInstance(operatorRoleClass.getClassLoader(), new Class[]{operatorRoleClass}, operatorProxy);
        operatorProxy.setTaskOperator(t);
        return t;
    }
}
