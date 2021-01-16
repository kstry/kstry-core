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
package cn.kstry.framework.core.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * 异常
 *
 * @author lykan
 */
public class KstryException extends RuntimeException {

    private static final long serialVersionUID = 331496378583084864L;

    private final String errorCode;

    public KstryException(String code, String desc) {
        super(StringUtils.isBlank(desc) ? "System Error!" : desc);
        this.errorCode = code;
    }

    public KstryException(ExceptionEnum errorCodeEnum) {
        super(errorCodeEnum.getDesc());
        this.errorCode = errorCodeEnum.getExceptionCode();
    }

    public KstryException(Throwable cause) {
        super(cause.getMessage(), cause);
        this.errorCode = ExceptionEnum.SYSTEM_ERROR.getExceptionCode();
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public static void throwException(ExceptionEnum exceptionEnum) {
        throw new KstryException(exceptionEnum);
    }

    public static void throwException(String code, String desc) {
        throw new KstryException(code, desc);
    }

    public static void throwException(Throwable e) {
        if (e instanceof KstryException) {
            throw (KstryException) e;
        }
        throw new KstryException(e);
    }
}
