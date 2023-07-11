/*
 *
 *  * Copyright (c) 2020-2023, Lykan (jiashuomeng@gmail.com).
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

import java.util.List;
import java.util.Optional;

/**
 * 遍历执行迭代器的每一项数据时，承载当前项数据，作为服务节点方法入参，自动注入赋值
 *
 * @author lykan
 */
public class IterDataItem<T> {

    private final T data;

    private final int index;

    private final boolean batchItem;

    private final List<T> dataList;

    public IterDataItem(boolean batchItem, T data, List<T> dataList, int index) {
        this.batchItem = batchItem;
        this.data = data;
        this.dataList = dataList;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public boolean isBatch() {
        return batchItem;
    }

    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }

    public List<T> getDataList() {
        return dataList;
    }
}
