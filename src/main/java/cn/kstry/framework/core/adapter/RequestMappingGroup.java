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
package cn.kstry.framework.core.adapter;

import java.util.List;

/**
 * 参数解析项， 映射
 *
 * @author lykan
 */
public class RequestMappingGroup {

    /**
     * 参数解析项， 映射 Item 列表
     */
    private List<RequestMappingItem> mappingItemList;

    public List<RequestMappingItem> getMappingItemList() {
        return mappingItemList;
    }

    public void setMappingItemList(List<RequestMappingItem> mappingItemList) {
        this.mappingItemList = mappingItemList;
    }

    /**
     * 参数解析项， 映射 Item
     */
    public static class RequestMappingItem {

        /**
         * 参数来源
         */
        private String source;

        /**
         * 参数去向
         */
        private String target;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }
}
