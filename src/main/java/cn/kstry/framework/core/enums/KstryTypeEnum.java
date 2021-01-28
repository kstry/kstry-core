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
package cn.kstry.framework.core.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * 组件 类型 枚举
 *
 * @author lykan
 */
public enum KstryTypeEnum {

    /**
     * 全局
     */
    GLOBAL(1),

    /**
     * 1、任务，负责处理真正的业务请求，由 TaskNode 定义 并 执行
     * 2、TaskNode（任务节点）派生自 EventNode（事件节点），
     * 3、Event 是静态的，是对某个事情的抽象与定义，Task 是动态的，是基于运行态存在的
     * 4、Event 静态编排，列举出所有的可能性
     *      ps：A事件被定义了，A事件可以发生的条件是什么，A事件之后后续可能发生的事件又是什么，A事件与B事件必须有先后关系，还是可以同步执行。等等这些在Event定义时要一一列举清楚
     * 5、Task 是动态路由，通过现存已成结果的事实（入参或者之前节点执行后的结果）对当下Task进行路由，判断当下 Task 是否需要执行，执行之后后续的唯一一个 Task 是哪个，是否可以异步化执行
     * 6、容器初始，会 加载成功所有的 Event，并将 Event 串联成图，形成一张全局事件图
     */
    TASK(2),

    /**
     * 时间段标志，Event 虽然是耗时的，但是以全局的视角来看 Event 的耗时并不那么重要了（就好比地铁上贴的全城地铁线路图点与点的距离之于实际地铁的站点之间的距离一样）
     * Event 可以看成一个点，而 TIME_SLOT 就是事件的点连成的线。TIME_SLOT 可以由一个 Event 生成，也可由多个 Event 关联而成
     * TIME_SLOT 好比 子Story，是这个故事发生期间的一个时间段
     * TIME_SLOT 可以同步，也可以异步，同步中可以嵌套异步，异步中也可以进一步嵌套异步
     */
    TIME_SLOT(3),

    /**
     * 一个用户故事，与用户的一次请求相对应。由 EventNode 和 TIME_SLOT 组成
     */
    STORY(4),

    /**
     * STORY_BUS 是基于 STORY 存在的，一个 STORY 有一个或者多个 STORY_BUS。（多个 STORY_BUS 出现在 TIME_SLOT 是异步的情况）
     * STORY_BUS 定义三大数据域，这些数据域可以在 Story 开始前指定初始值，也会因为 Story 中 Event 的发生对其产生变化：
     *      var (Variable): 【 可变 】， 指定后还可变更数据集合，可进行数据覆盖。获取值方式ps：@var.user
     *      sta（Stable）：【 稳定 】，一经指定就不可变数据集合，如果再次指定会打印警告信息，并且当次设置失败，继续保留原值 获取值方式ps：@sta.user
     *      req（Request）：【 不可变 】，请求入参集合 （不可变集合，Story 开始前指定，一经指定再也不会发生变化） 获取值方式ps：@req.user
     *
     * 异步流程中 三大数据域的变化：
     *      var (Variable): 从当前事件流，开启下一个同步时间流时数据共享。开启下一个异步事件流时，将当前事件流当下的值复制给异步事件流（如果索引是对象，还是指向同一个对象）
     *                      异步流程结束后会建立一个基于 TIME_SLOT 的节点保存异步流程发生的数据。与主流程进行数据隔离。 获取值方式ps：$login_event_node.@var.user
     *
     *      sta（Stable）：从当前事件流，开启下一个同步时间流时数据共享。开启下一个异步事件流时，将当前事件流当下的值复制给异步事件流（如果索引是对象，还是指向同一个对象）
     *                      异步流程结束后会建立一个基于 TIME_SLOT 的节点保存异步流程发生的数据。与主流程进行数据隔离。 获取值方式ps：$login_event_node.@sta.user
     *
     *      req（Request）：从当前事件流，无论开启同步还是异步事件流，永远都是一套共享数据
     *
     */
    STORY_BUS(5),

    /**
     * 判断是否需要跳过当下 Task 或者 路由下一个 Task 是哪个的策略。分为框架自定义和用户自定义两部分
     * 框架自定义：cn.kstry.framework.core.enums.CalculatorEnum
     * 用户自定以：定义类，实现 StrategyRuleCalculator 接口，将该类放入 Spring 容器即可生效
     */
    STRATEGY(6),

    /**
     * TIME_SLOT 是 Event 在 Story 中的概念，代表是这些 Event 组成了这个 Story，ROLE 是 Event 另一个维度的分组概念
     *
     * 一个角色拥有自己的 Event 能力。并且这些能力往往不止一个。就比如 厨师可以做很多种菜，做每一种菜都是一个 Event，那么厨师这个 ROLE 下，就有很多的 Event
     */
    ROLE(7),

    /**
     * 事件组，厨师可以做很多菜，但是做菜也有做凉菜、热菜之分。EVENT_GROUP 就是在 ROLE 维度下对 Event 更小力度的分组概念
     *
     * 一个  ROLE 下 可以有不止一个的 Event 也可以有不止一个的 EVENT_GROUP，一个 EVENT_GROUP 可以有不止一个的 Event 。一个 Event 或者 EVENT_GROUP 必须隶属于一个 ROLE
     */
    EVENT_GROUP(8),
    ;

    KstryTypeEnum(int type) {
        this.type = type;
    }

    public static Optional<KstryTypeEnum> getComponentTypeEnumByName(String name) {
        if (StringUtils.isBlank(name)) {
            return Optional.empty();
        }

        for (KstryTypeEnum e : KstryTypeEnum.values()) {

            if (Objects.equals(e.name().toUpperCase(), name.trim().toUpperCase())) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    final int type;

    public int getType() {
        return type;
    }
}
