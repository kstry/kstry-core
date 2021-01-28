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
package cn.kstry.framework.core.config;

import cn.kstry.framework.core.facade.TaskResponse;

/**
 * Task Action 方法详情
 *
 * @author lykan
 */
public class TaskActionMethod {

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 请求参数
     */
    private Class<?> requestClass;

    /**
     * 返回结果
     */
    private Class<? extends TaskResponse<?>> returnClass;

    /**
     * 类名
     */
    private String className;

    /**
     * 是否为 Story 的最终节点
     */
    private boolean lastEventNode;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?> getRequestClass() {
        return requestClass;
    }

    public void setRequestClass(Class<?> requestClass) {
        this.requestClass = requestClass;
    }

    public Class<? extends TaskResponse<?>> getReturnClass() {
        return returnClass;
    }

    public void setReturnClass(Class<? extends TaskResponse<?>> returnClass) {
        this.returnClass = returnClass;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isLastEventNode() {
        return lastEventNode;
    }

    public void setLastEventNode(boolean lastEventNode) {
        this.lastEventNode = lastEventNode;
    }
}
