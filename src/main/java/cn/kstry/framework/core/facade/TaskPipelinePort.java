/*
 *
 *  *  Copyright (c) 2020-2020, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.facade;

/**
 * Task 管道端口，有 TaskRequest 的功能，也能当做 TaskResponse 使用
 * <p>
 * 适用于Task的出口是另一个 Task 入口的情况，如果确认 Task 结果已无需再参与任何一个 Task 的计算任务，推荐使用 TaskResponse
 *
 * @author lykan
 */
public interface TaskPipelinePort<T> extends TaskResponse<T>, TaskRequest {

    /**
     * 获取 task request
     */
    TaskRequest getTaskRequest();

    /**
     * set task request
     */
    void setTaskRequest(TaskRequest taskRequest);
}
