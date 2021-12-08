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

import cn.kstry.framework.core.util.KValueUtil;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;

/**
 *
 * @author lykan
 */
public class BasicKvSelector implements KvSelector {

    private final Map<String, KValue> kValueMap = Maps.newConcurrentMap();

    @Override
    public Optional<KValue> getKValue(String scope) {
        return Optional.ofNullable(kValueMap.get(KValueUtil.getKValueIdentityId(scope)));
    }

    public void addKValue(BasicKValue kValue) {
        if (kValue == null) {
            return;
        }
        kValueMap.put(kValue.getScope(), kValue);
    }
}
