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

import cn.kstry.framework.core.annotation.StoryEnginePropertyRegister;
import cn.kstry.framework.core.engine.EventGroup;
import cn.kstry.framework.core.route.RouteEventGroup;
import cn.kstry.framework.core.util.AssertUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Operator 代理
 *
 * @author lykan
 */
public class TaskOperatorProxy implements InvocationHandler {

    /**
     * 实际参加工作的 action
     */
    private final EventGroup workRouteAction;

    public TaskOperatorProxy(EventGroup eventActionGroup) {
        this.workRouteAction = eventActionGroup;
    }

    public void setTaskOperator(EventOperatorRole taskOperator) {
        AssertUtil.notNull(taskOperator);
        AssertUtil.notNull(this.workRouteAction);
        ((RouteEventGroup) this.workRouteAction).setTaskActionOperator(taskOperator);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object action = this.workRouteAction;
        if (action instanceof StoryEnginePropertyRegister.AnnotationEventGroupProxy) {
            action = ((StoryEnginePropertyRegister.AnnotationEventGroupProxy) action).getEventGroup();
        }
        AssertUtil.notNull(action);
        return method.invoke(action, args);
    }
}
