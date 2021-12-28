/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.component.utils;

import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author lykan
 */
public class BasicInStack<T> implements InStack<T> {

    private final LinkedList<T> stack = Lists.newLinkedList();

    @Override
    public void push(T t) {
        AssertUtil.notNull(t);
        stack.push(t);
    }

    /**
     * 1, 2, 3  push ->[ 4, 5 ]  =>  [ 1, 2, 3, 4, 5 ]
     *
     * @param list list
     */
    @Override
    public void pushList(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Lists.reverse(list).forEach(this::push);
    }

    @Override
    public void pushCollection(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return;
        }
        collection.forEach(this::push);
    }

    @Override
    public Optional<T> pop() {
        if (stack.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(stack.pop());
    }

    @Override
    public Optional<T> peek() {
        if (stack.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(stack.peekFirst());
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
