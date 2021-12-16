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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.bus.BasicStoryBus;
import cn.kstry.framework.core.container.MethodWrapper;
import cn.kstry.framework.core.kv.KvThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

import java.util.Optional;


/**
 * @author lykan
 */
public class ProxyUtil {

    public static boolean isProxyObject(Object o) {
        if (o == null) {
            return false;
        }
        return AopUtils.isAopProxy(o) || AopUtils.isCglibProxy(o) || AopUtils.isJdkDynamicProxy(o);
    }

    public static Class<?> noneProxyClass(Object candidate) {
        if (candidate == null) {
            return null;
        }
        if (!ProxyUtil.isProxyObject(candidate)) {
            return candidate.getClass();
        }
        return AopUtils.getTargetClass(candidate);
    }

    public static Object invokeMethod(BasicStoryBus storyBus, MethodWrapper methodWrapper, Object target) {
        return invokeMethod(storyBus, methodWrapper, target, new Object[0]);
    }

    public static Object invokeMethod(BasicStoryBus storyBus, MethodWrapper methodWrapper, Object target, Object... args) {
        try {
            KvThreadLocal.KvScope newKvScope = new KvThreadLocal.KvScope(methodWrapper.getKvScope());
            newKvScope.setBusinessId(storyBus.getBusinessId());
            KvThreadLocal.setKvScope(newKvScope);
            return ReflectionUtils.invokeMethod(methodWrapper.getMethod(), target, args);
        } finally {
            KvThreadLocal.clear();
        }
    }
}
