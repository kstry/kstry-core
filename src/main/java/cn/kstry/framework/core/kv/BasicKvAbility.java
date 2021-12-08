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
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author lykan
 */
public class BasicKvAbility implements KvAbility {

    private final KvSelector kvSelector;

    public BasicKvAbility(KvSelector kvSelector) {
        AssertUtil.notNull(kvSelector);
        this.kvSelector = kvSelector;
    }

    @Override
    public Optional<Object> getValue(String key) {
        KvThreadLocal.KvScope kvScope = KvThreadLocal.getKvScope().orElseThrow(() -> KstryException.buildException(ExceptionEnum.SYSTEM_ERROR));
        return doGetValue(key, kvScope.getScope());
    }

    @Override
    public Optional<Object> getValueByScope(String scope, String key) {
        return doGetValue(key, scope);
    }

    @Override
    public <T> Optional<T> getObject(String key, Class<T> clazz) {
        Optional<Object> valueOptional = getValue(key);
        SerializerFeature[] sf = {SerializerFeature.WriteMapNullValue, SerializerFeature.DisableCircularReferenceDetect};
        return valueOptional.map(o -> JSON.parseObject(JSON.toJSONString(o, sf), clazz));
    }

    @Override
    public Optional<String> getString(String key) {
        Optional<Object> valueOptional = getValue(key);
        return valueOptional.map(v -> {
            if (v instanceof String) {
                return (String) v;
            }
            return JSON.toJSONString(v);
        });
    }

    private Optional<Object> doGetValue(String key, String scope) {
        Optional<KValue> kValueOptional = kvSelector.getKValue(scope);
        if (kValueOptional.isPresent()) {
            Optional<Object> valueOptional = kValueOptional.get().getValue(key);
            if (valueOptional.isPresent()) {
                return valueOptional.filter(v -> v != KValue.KV_NULL);
            }
        }
        if (!Objects.equals(scope, GlobalConstant.VARIABLE_SCOPE_DEFAULT)) {
            kValueOptional = kvSelector.getKValue(GlobalConstant.VARIABLE_SCOPE_DEFAULT);
            if (kValueOptional.isPresent()) {
                return kValueOptional.get().getValue(key).filter(v -> v != KValue.KV_NULL);
            }
        }
        return Optional.empty();
    }
}
