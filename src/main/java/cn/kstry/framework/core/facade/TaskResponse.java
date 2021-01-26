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

import java.io.Serializable;

/**
 * Task 执行结果门面，定义结果可以用的规范动作
 *
 * @param <T>
 */
public interface TaskResponse<T> extends Serializable {

    /**
     * Task 调用是否成功
     *
     * @return Task 执行是否获取到预期结果 true：是
     */
    boolean isSuccess();

    /**
     * 设置成功状态
     */
    void setSuccess(boolean success);

    /**
     * 结果执行成功
     */
    void resultSuccess();

    /**
     * 结果执行成功
     */
    void resultSuccess(String code);

    /**
     * 获取执行结果状态码
     *
     * @return 执行结果状态码
     */
    String getResultCode();

    /**
     * 设置执行结果状态码
     */
    void setResultCode(String resultCode);

    /**
     * 获取执行结果的描述信息
     *
     * @return 执行结果的描述信息
     */
    String getResultDesc();

    /**
     * 设置执行结果的描述信息
     */
    void setResultDesc(String resultDesc);

    /**
     * Task 执行出现异常时，保存的异常信息
     *
     * @return Task 执行的异常信息
     */
    Throwable getResultException();

    /**
     * Task 执行出现异常时，保存异常信息
     */
    void setResultException(Throwable resultException);

    /**
     * 获取 Task 执行任务结果
     *
     * @return Task 执行任务结果
     */
    T getResult();

    /**
     * 设置 Task 执行任务结果
     *
     */
    void setResult(T result);
}
