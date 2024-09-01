/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.monitor;

/**
 * 循环信息
 *
 * @author lykan
 */
public class CycleTracking {

    /**
     * 子流程循环次数，主流程循环3次，子流程循环2次，子循环次数就是2次
     */
    private Long times;

    /**
     * 总循环次数，主流程循环3次，子流程循环2次，总循环次数就是3*2=6次
     */
    private Long allTimes;

    /**
     * 总花费时长
     */
    private Long totalTime;

    public CycleTracking(Long allTimes, Long totalTime) {
        this.allTimes = allTimes;
        this.totalTime = totalTime == null ? 0 : totalTime;
    }

    public Long getTimes() {
        return times;
    }

    public void setTimes(Long times) {
        this.times = times;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    public Long getAllTimes() {
        return allTimes;
    }

    public void setAllTimes(Long allTimes) {
        this.allTimes = allTimes;
    }
}
