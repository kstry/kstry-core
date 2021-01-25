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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.annotation.NoticeSta;
import cn.kstry.framework.core.annotation.NoticeStaAndVar;
import cn.kstry.framework.core.annotation.NoticeVar;
import cn.kstry.framework.core.facade.NoticeBusTaskResponse;
import cn.kstry.framework.core.facade.NoticeBusTaskResponseBox;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author lykan
 */
public class NoticeBusDataUtil {

    /**
     * 保存 request 中需要回填数据字段的 class 字段映射信息
     */
    private static final Map<Class<?>, Map<Class<?>, List<NoticeBusDataUtil.NoticeFieldItem>>> NOTICE_BUS_FIELD_MAP_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取目标类中需要通知给 bus 变量集的 字段
     *
     * @param result 目标对象
     * @return 需要通知给 bus 数据集的字段
     */
    public static Map<Class<?>, List<NoticeFieldItem>> getNoticeFieldMap(Object result) {
        if (result == null || result instanceof Map || result instanceof Collection) {
            return Maps.newHashMap();
        }

        Map<Class<?>, List<NoticeFieldItem>> noticeFieldMap = NOTICE_BUS_FIELD_MAP_CACHE.get(result.getClass());
        if (noticeFieldMap != null) {
            return noticeFieldMap;
        }

        noticeFieldMap = new HashMap<>();
        Field[] declaredFields = result.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(NoticeSta.class)) {
                List<NoticeFieldItem> noticeFieldItems = noticeFieldMap.computeIfAbsent(NoticeSta.class, k -> new ArrayList<>());
                NoticeSta annotation = declaredField.getAnnotation(NoticeSta.class);
                noticeFieldItems.add(new NoticeFieldItem(declaredField.getName(), Optional.of(annotation.name()).filter(StringUtils::isNotBlank).orElse(declaredField.getName())));
            }
            if (declaredField.isAnnotationPresent(NoticeVar.class)) {
                List<NoticeFieldItem> noticeFieldItems = noticeFieldMap.computeIfAbsent(NoticeVar.class, k -> new ArrayList<>());
                NoticeVar annotation = declaredField.getAnnotation(NoticeVar.class);
                noticeFieldItems.add(new NoticeFieldItem(declaredField.getName(), Optional.of(annotation.name()).filter(StringUtils::isNotBlank).orElse(declaredField.getName())));
            }
            if (declaredField.isAnnotationPresent(NoticeStaAndVar.class)) {
                List<NoticeFieldItem> noticeFieldItems = noticeFieldMap.computeIfAbsent(NoticeStaAndVar.class, k -> new ArrayList<>());
                NoticeStaAndVar annotation = declaredField.getAnnotation(NoticeStaAndVar.class);
                noticeFieldItems.add(new NoticeFieldItem(declaredField.getName(), Optional.of(annotation.name()).filter(StringUtils::isNotBlank).orElse(declaredField.getName())));
            }
        }
        NOTICE_BUS_FIELD_MAP_CACHE.put(result.getClass(), noticeFieldMap);
        return noticeFieldMap;
    }

    /**
     * taskResponse 转换成 noticeBusTaskResponse
     *
     * @param taskResponse 非空
     * @param noticeFieldMap 需要通知到 bus 的字段
     * @return NoticeBusTaskResponse
     */
    public static NoticeBusTaskResponse<?> transferNoticeBusTaskResponse(Object taskResponse, Map<Class<?>, List<NoticeFieldItem>> noticeFieldMap) {

        AssertUtil.notNull(taskResponse);
        AssertUtil.notEmpty(noticeFieldMap);
        NoticeBusTaskResponseBox<?> noticeBusTaskResponse;
        if (taskResponse instanceof NoticeBusTaskResponse) {
            noticeBusTaskResponse = (NoticeBusTaskResponseBox<?>) taskResponse;
        } else {
            noticeBusTaskResponse = new NoticeBusTaskResponseBox<>();
            org.springframework.beans.BeanUtils.copyProperties(taskResponse, noticeBusTaskResponse);
            taskResponse = noticeBusTaskResponse;
        }

        Object result = GlobalUtil.notNull(noticeBusTaskResponse.getResult());
        if (MapUtils.isEmpty(noticeBusTaskResponse.getStableBusDataMap())) {
            noticeBusTaskResponse.addStableDataMap(new HashMap<>());
        }
        if (MapUtils.isEmpty(noticeBusTaskResponse.getVariableBusDataMap())) {
            noticeBusTaskResponse.updateVariableDataMap(new HashMap<>());
        }
        List<NoticeFieldItem> stableNoticeFieldItems = noticeFieldMap.get(NoticeSta.class);
        if (CollectionUtils.isNotEmpty(stableNoticeFieldItems)) {
            stableNoticeFieldItems.forEach(item -> {
                if (noticeBusTaskResponse.getStableBusDataMap().containsKey(item.getTargetName())) {
                    return;
                }
                noticeBusTaskResponse.getStableBusDataMap().put(item.getTargetName(),
                        GlobalUtil.getProperty(result, item.getFieldName()).filter(r -> r != GlobalUtil.GET_PROPERTY_ERROR_SIGN).orElse(null));
            });
        }
        List<NoticeFieldItem> variableFieldItems = noticeFieldMap.get(NoticeVar.class);
        if (CollectionUtils.isNotEmpty(variableFieldItems)) {
            variableFieldItems.forEach(item -> {
                if (noticeBusTaskResponse.getVariableBusDataMap().containsKey(item.getTargetName())) {
                    return;
                }
                noticeBusTaskResponse.getVariableBusDataMap().put(item.getTargetName(),
                        GlobalUtil.getProperty(result, item.getFieldName()).filter(r -> r != GlobalUtil.GET_PROPERTY_ERROR_SIGN).orElse(null));
            });
        }
        List<NoticeFieldItem> allNoticeFieldItems = noticeFieldMap.get(NoticeStaAndVar.class);
        if (CollectionUtils.isNotEmpty(allNoticeFieldItems)) {
            allNoticeFieldItems.forEach(item -> {
                if (noticeBusTaskResponse.getStableBusDataMap().containsKey(item.getTargetName())) {
                    return;
                }
                noticeBusTaskResponse.getStableBusDataMap().put(item.getTargetName(),
                        GlobalUtil.getProperty(result, item.getFieldName()).filter(r -> r != GlobalUtil.GET_PROPERTY_ERROR_SIGN).orElse(null));
            });
            allNoticeFieldItems.forEach(item -> {
                if (noticeBusTaskResponse.getVariableBusDataMap().containsKey(item.getTargetName())) {
                    return;
                }
                noticeBusTaskResponse.getVariableBusDataMap().put(item.getTargetName(),
                        GlobalUtil.getProperty(result, item.getFieldName()).filter(r -> r != GlobalUtil.GET_PROPERTY_ERROR_SIGN).orElse(null));
            });
        }
        return (NoticeBusTaskResponse<?>) taskResponse;
    }

    public static class NoticeFieldItem {

        /**
         * 字段名称
         */
        private final String fieldName;

        /**
         * 实际字段保存的名称
         */
        private final String targetName;

        public NoticeFieldItem(String fieldName, String targetName) {
            AssertUtil.notBlank(fieldName);
            AssertUtil.notBlank(targetName);
            this.fieldName = fieldName;
            this.targetName = targetName;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getTargetName() {
            return targetName;
        }
    }
}
