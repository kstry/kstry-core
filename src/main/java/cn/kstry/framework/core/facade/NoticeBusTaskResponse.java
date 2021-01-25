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

import java.util.Map;

/**
 * Task 执行结果门面，定义结果可以用的规范动作
 *
 * 通知 bus 进行数据变更
 *
 * @param <T>
 */
public interface NoticeBusTaskResponse<T> extends TaskResponse<T> {

    /**
     * 【 新增 】 不可变数据集合，如果已存在会忽略变更
     *
     * @param stableBusDataMap 不可变数据集合
     */
    void addStableDataMap(Map<String, Object> stableBusDataMap);

    /**
     * 【 更新 】 可变数据集合，如果已存在新值会替换旧值
     *
     * @param variableBusDataMap 可变数据集合
     */
    void updateVariableDataMap(Map<String, Object> variableBusDataMap);

    /**
     * 【 获取 】不可变数据集合
     * @return 不可变数据集合
     */
    Map<String, Object> getStableBusDataMap();

    /**
     * 【 获取 】可变数据集合
     * @return 可变数据集合
     */
    Map<String, Object> getVariableBusDataMap();
}
