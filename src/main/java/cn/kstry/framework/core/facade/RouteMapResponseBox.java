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
package cn.kstry.framework.core.facade;

/**
 * 具备重新规划接下来待执行 Task 链路的能力
 *
 * @author lykan
 */
public class RouteMapResponseBox<T> extends TaskPipelinePortBox<T> implements RouteMapResponse<T> {

    /**
     * 动态路由表
     */
    private DynamicRouteTable dynamicRouteTable;

    @Override
    public void updateTaskRouterTable(DynamicRouteTable dynamicRouteTable) {
        this.dynamicRouteTable = dynamicRouteTable;
    }

    @Override
    public DynamicRouteTable getTaskRouterTable() {
        return this.dynamicRouteTable;
    }

    public static <E> RouteMapResponse<E> buildSuccess(E buildTarget) {
        RouteMapResponseBox<E> result = new RouteMapResponseBox<>();
        result.resultSuccess();
        result.setResult(buildTarget);
        return result;
    }
}
