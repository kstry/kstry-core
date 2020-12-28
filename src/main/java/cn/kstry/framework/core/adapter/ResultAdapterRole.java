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

import cn.kstry.framework.core.facade.TaskPipelinePort;
import cn.kstry.framework.core.operator.TaskActionOperatorRole;

/**
 * @author lykan
 */
public interface ResultAdapterRole extends TaskActionOperatorRole {

    String COMMON_TASK_RESULT_MAPPING_TASK_KEY = "COMMON_TASK_RESULT_MAPPING";

    String TASK_RESULT_DATA_KEY = "data";

    /**
     * 参数映射
     *
     * @param request 非空
     * @return 映射结果
     */
    TaskPipelinePort<Object> mapping(ResultAdapterRequest request);
}
