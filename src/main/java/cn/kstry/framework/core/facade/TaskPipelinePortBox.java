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
package cn.kstry.framework.core.facade;

import cn.kstry.framework.core.util.AssertUtil;

/**
 * @author lykan
 */
public class TaskPipelinePortBox<T> extends TaskResponseBox<T> implements TaskPipelinePort<T> {

    private static final long serialVersionUID = -5309058166680530916L;

    /**
     * task request
     */
    private TaskRequest taskRequest;

    @Override
    public void setTaskRequest(TaskRequest taskRequest) {
        AssertUtil.notNull(taskRequest);
        this.taskRequest = taskRequest;
    }

    @Override
    public TaskRequest getTaskRequest() {
        return taskRequest;
    }

    public static <E> TaskPipelinePort<E> buildSuccess(E buildTarget) {
        TaskPipelinePortBox<E> result = new TaskPipelinePortBox<>();
        result.resultSuccess();
        result.setResult(buildTarget);
        return result;
    }
}
