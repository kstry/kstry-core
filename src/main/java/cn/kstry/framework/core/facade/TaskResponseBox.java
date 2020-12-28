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
 * Task 执行后返回结果
 *
 * @author lykan
 */
public class TaskResponseBox<T> implements TaskResponse<T> {

    private static final long serialVersionUID = -1806941962936550135L;

    /**
     * Task 执行结果
     */
    private boolean success = false;

    /**
     * code
     */
    private String resultCode;

    /**
     * code desc
     */
    private String resultDesc;

    /**
     * exception
     */
    private Exception resultException;

    /**
     * result
     */
    private T result;

    @Override
    public boolean isSuccess() {
        return this.success;
    }

    @Override
    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public void resultSuccess(String code) {
        AssertUtil.notBlank(code);
        this.success = true;
        this.resultCode = code;
    }

    @Override
    public void resultSuccess() {
        resultSuccess("200");
    }

    @Override
    public String getResultCode() {
        return this.resultCode;
    }

    @Override
    public void setResultCode(String code) {
        this.resultCode = code;
    }

    @Override
    public String getResultDesc() {
        return this.resultDesc;
    }

    @Override
    public void setResultDesc(String resultDesc) {
        this.resultDesc = resultDesc;
    }

    @Override
    public Exception getResultException() {
        return this.resultException;
    }

    @Override
    public void setResultException(Exception exception) {
        this.resultException = exception;
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public void setResult(T result) {
        this.result = result;
    }

    public static <E> TaskResponse<E> buildSuccess(E buildTarget) {
        TaskResponseBox<E> result = new TaskResponseBox<>();
        result.resultSuccess();
        result.setResult(buildTarget);
        return result;
    }
}
