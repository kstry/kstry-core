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
package cn.kstry.framework.core.kv;

import cn.kstry.framework.core.constant.GlobalConstant;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 *
 * @author lykan
 */
public class KvThreadLocal {

    private static final ThreadLocal<KvScope> KV_THREAD_LOCAL = new ThreadLocal<>();

    public static void setKvScope(KvScope kvScope) {
        KV_THREAD_LOCAL.set(kvScope);
    }

    public static Optional<KvScope> getKvScope() {
        return Optional.ofNullable(KV_THREAD_LOCAL.get());
    }

    public static void clear() {
        KV_THREAD_LOCAL.remove();
    }

    public static class KvScope {

        private final String scope;

        public KvScope(String scope) {
            this.scope = Optional.ofNullable(scope).filter(StringUtils::isNotBlank).orElse(GlobalConstant.VARIABLE_SCOPE_DEFAULT);
        }

        public String getScope() {
            return scope;
        }
    }
}
