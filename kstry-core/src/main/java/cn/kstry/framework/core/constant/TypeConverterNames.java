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
package cn.kstry.framework.core.constant;

/**
 * 类型转换器名字列表
 *
 * @author lykan
 */
public interface TypeConverterNames {

    /**
     * Object -> Object
     */
    String BASIC_CONVERTER = "BASIC_CONVERTER";

    /**
     * Date -> String
     */
    String DATE_TO_STRING = "DATE_TO_STRING";

    /**
     * String -> Date
     */
    String STRING_TO_DATE = "STRING_TO_DATE";

    /**
     * LocalDateTime -> String
     */
    String LOCAL_DATETIME_TO_STRING = "LOCAL_DATETIME_TO_STRING";

    /**
     * String -> LocalDateTime
     */
    String STRING_TO_LOCAL_DATETIME = "STRING_TO_LOCAL_DATETIME";

    /**
     * Object -> List
     */
    String ONE_ITEM_TO_LIST = "ONE_ITEM_TO_LIST";

    /**
     * Object -> Set
     */
    String ONE_ITEM_TO_SET = "ONE_ITEM_TO_SET";

    /**
     * Object -> Boolean
     */
    String OBJECT_TO_BOOLEAN = "OBJECT_TO_BOOLEAN";

    /**
     * List -> Object
     */
    String FIRST_ITEM_FROM_LIST = "FIRST_ITEM_FROM_LIST";

    /**
     * Collection<A> -> Collection<B>
     */
    String COLLECTION_GENERIC_TYPE_CONVERTER = "COLLECTION_GENERIC_TYPE_CONVERTER";
}
