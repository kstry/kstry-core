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
package cn.kstry.framework.core.bpmn;

import cn.kstry.framework.core.resource.config.Config;

import java.util.Optional;

/**
 * StartEvent
 */
public interface StartEvent extends Event {

    /**
     * 获取 Config
     *
     * @return Config
     */
    Optional<Config> getConfig();

    /**
     * 获取 EndEvent
     *
     * @return EndEvent
     */
    EndEvent getEndEvent();

    /**
     * 设置 Config
     *
     * @param config Config
     */
    void setConfig(Config config);

    /**
     * 设置 EndEvent
     *
     * @param endEvent 非空
     */
    void setEndEvent(EndEvent endEvent);
}
