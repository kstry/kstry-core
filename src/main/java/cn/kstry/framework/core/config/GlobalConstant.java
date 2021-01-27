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
package cn.kstry.framework.core.config;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 全局常量
 *
 * @author lykan
 */
public interface GlobalConstant {

    /**
     *
     * 以下字段不能作为 配置 key 定义出现
     * var (Variable): 可变， 指定后还可变更数据集合
     * sta（Stable）：稳定，一经指定就不可变数据集合
     * req（Request）：请求入参集合 （不可变集合）
     */
    List<String> RESERVED_WORDS_LIST = Lists.newArrayList("VAR", "STA", "REQ");

    /**
     * 允许的 eventNode 的最大深度
     */
    int MAX_NODE_LEVEL_DEPTH = 1000;

    /**
     * 配置文件中 node 引导开始 标志
     */
    String NODE_SIGN = "@";

    /**
     * 配置文件中 timeSlot node 引导开始 标志
     */
    String TIME_SLOT_NODE_SIGN = "$";

    /**
     * 配置文件中 同一字符串中 类型分隔符 标志
     */
    String DISTINCT_SIGN = "-";

    /**
     * Story 执行成功后的 code
     */
    String STORY_SUCCESS_CODE = "200";

    /**
     * 默认的，异步任务的超时时间，单位：ms
     */
    int DEFAULT_ASYNC_TIMEOUT = 2000;

    /**
     * Engine shutdown 后，校验任务是否全部关闭的等待时间
     * Engine 关闭策略:
     *  - 监听 Spring 容器关闭事件，事件发生后调用 shutdown()
     *  - 等待 ENGINE_SHUTDOWN_SLEEP_SECONDS s 后，check 线程池中是否有活动中的线程，如果没有关闭成功，如果存在进行后续操作
     *  - 调用 shutdownNow()
     *  - 等待 ENGINE_SHUTDOWN_NOW_SLEEP_SECONDS s 后，check 线程池中是否有活动中的线程，并将结果通过日志打印
     *
     * @see GlobalConstant#ENGINE_SHUTDOWN_NOW_SLEEP_SECONDS
     */
    int ENGINE_SHUTDOWN_SLEEP_SECONDS = 1;

    /**
     * Engine shutdownNow 后，校验任务是否全部关闭的等待时间
     * @see GlobalConstant#ENGINE_SHUTDOWN_SLEEP_SECONDS
     */
    int ENGINE_SHUTDOWN_NOW_SLEEP_SECONDS = 1;
}
