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
package cn.kstry.framework.core.annotation;

import cn.kstry.framework.core.timeslot.TimeSlotEngine;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启 Kstry 框架
 *
 * @author lykan
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({TimeSlotEngine.class, EnableKstryRegister.class, StoryEnginePropertyRegister.class})
public @interface EnableKstry {

    /**
     * story engine 名字
     */
    String storyEngineName() default "";

    /**
     * @return 配置文件路径
     */
    String configPath() default "event-story-config.json";
}

