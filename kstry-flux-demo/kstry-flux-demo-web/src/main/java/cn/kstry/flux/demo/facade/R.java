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
package cn.kstry.flux.demo.facade;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lykan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {

    private boolean success;

    private Integer code;

    private String msg;

    private T data;

    public static <D> R<D> success(D data) {
        R<D> res = new R<>();
        res.setSuccess(true);
        res.setCode(0);
        res.setMsg("success");
        res.setData(data);
        return res;
    }

    public static <D> R<D> error(int code, String desc) {
        R<D> res = new R<>();
        res.setCode(code);
        res.setMsg(desc);
        res.setSuccess(false);
        return res;
    }
}
