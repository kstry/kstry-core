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
package cn.kstry.framework.core.exception;

/**
 * 异常
 *
 * @author lykan
 */
public class KstryException extends RuntimeException {

    private static final long serialVersionUID = 331496378583084864L;

    /**
     * 异常信息枚举
     */
    private final ExceptionEnum errorCode;

    public KstryException(ExceptionEnum errorCode) {
        super(errorCode.getExceptionInfo());
        this.errorCode = errorCode;
    }

    public KstryException(Throwable cause) {
        super(cause);
        this.errorCode = ExceptionEnum.SYSTEM_ERROR;
    }

    public ExceptionEnum getErrorCode() {
        return errorCode;
    }

    public static void throwException(ExceptionEnum exceptionEnum) {
        throw new KstryException(exceptionEnum);
    }

    public static void throwException(Exception e) {
        throw new KstryException(e);
    }
}
