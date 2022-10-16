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
package cn.kstry.framework.core.bus;

/**
 *
 * @author lykan
 */
public class ExpressionBus {

    private final StoryBus storyBus;

    public ExpressionBus(StoryBus storyBus) {
        this.storyBus = storyBus;
    }

    /**
     * 获取 Request 域 数据
     * @return data
     */
    public Object getReq() {
        return storyBus.getReq();
    }

    /**
     * 获取 执行结果
     *
     * @return ReturnResult
     */
    public Object getRes() {
        return storyBus.getResult().orElse(null);
    }

    /**
     * 获取 Var 域变量
     *
     * @return var scope data
     */
    public Object getVar() {
        return storyBus.getVar();
    }

    /**
     * 获取 Sta 域变量
     *
     * @return sta scope data
     */
    public Object getSta() {
        return storyBus.getSta();
    }
}
