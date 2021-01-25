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

import java.util.Map;

/**
 * Task 执行结果门面，定义结果可以用的规范动作
 *
 * 通知 bus 进行数据变更
 *
 * @author lykan
 */
public class NoticeBusTaskResponseBox<T> extends TaskResponseBox<T> implements NoticeBusTaskResponse<T> {

    /**
     * 承载不可变更数据
     */
    private Map<String, Object> stableBusDataMap;

    /**
     * 承载可变更数据
     */
    private Map<String, Object> variableBusDataMap;

    @Override
    public void addStableDataMap(Map<String, Object> stableBusDataMap) {
        this.stableBusDataMap = stableBusDataMap;
    }

    @Override
    public void updateVariableDataMap(Map<String, Object> variableBusDataMap) {
        this.variableBusDataMap = variableBusDataMap;
    }

    @Override
    public Map<String, Object> getStableBusDataMap() {
        return stableBusDataMap;
    }

    @Override
    public Map<String, Object> getVariableBusDataMap() {
        return variableBusDataMap;
    }

    public static <E> NoticeBusTaskResponse<E> buildSuccess(E buildTarget) {
        NoticeBusTaskResponseBox<E> result = new NoticeBusTaskResponseBox<>();
        result.resultSuccess();
        result.setResult(buildTarget);
        return result;
    }

    public static <E> NoticeBusTaskResponse<E> buildError(String code, String desc) {
        AssertUtil.notBlank(code);
        NoticeBusTaskResponseBox<E> result = new NoticeBusTaskResponseBox<>();
        result.setResultCode(code);
        result.setResultDesc(desc);
        result.setSuccess(false);
        result.setResult(null);
        return result;
    }
}
