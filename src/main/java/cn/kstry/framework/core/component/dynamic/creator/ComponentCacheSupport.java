/*
 *
 *  * Copyright (c) 2020-2022, Lykan (jiashuomeng@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.core.component.dynamic.creator;

/**
 * 组件缓存支持
 */
public interface ComponentCacheSupport {

    /**
     * 获取动态组件版本
     *      当 version <  0 时：获取组件不使用缓存，每次都会调用 getComponent(key) 获取最新组件
     *      当 version >= 0 时：获取组件使用缓存，缓存key为：$key-$version，缓存 value 为校验且被装饰过可直接使用的组件对象。
     *                         在 key 一定的情况下，如果当次 version 与上次 version 相同时，不会调用 getComponent(key) 直接返回组件对象。反之，会重新获取并校验装饰
     *                         缓存时间为24h，首次调用或者缓存失效时会重新构建组件对象
     *
     * @param key key
     * @return version
     */
    long version(String key);
}
