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
package cn.kstry.framework.core.container;

import cn.kstry.framework.core.annotation.NoticeResult;
import cn.kstry.framework.core.annotation.NoticeScope;
import cn.kstry.framework.core.annotation.NoticeSta;
import cn.kstry.framework.core.annotation.NoticeVar;
import cn.kstry.framework.core.util.AssertUtil;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 *
 * @author lykan
 */
public class NoticeAnnotationWrapper {

    private final NoticeSta noticeSta;

    private final NoticeVar noticeVar;

    private final NoticeResult noticeResult;

    private final NoticeScope noticeScope;

    private final boolean isField;

    public NoticeAnnotationWrapper(AnnotatedElement element) {
        AssertUtil.notNull(element);
        this.noticeSta = element.getAnnotation(NoticeSta.class);
        this.noticeVar = element.getAnnotation(NoticeVar.class);
        this.noticeResult = element.getAnnotation(NoticeResult.class);
        this.noticeScope = element.getAnnotation(NoticeScope.class);
        this.isField = element instanceof Field;
    }

    public NoticeAnnotationWrapper(Class<?> clazz) {
        AssertUtil.notNull(clazz);
        this.noticeSta = AnnotationUtils.findAnnotation(clazz, NoticeSta.class);
        this.noticeVar = AnnotationUtils.findAnnotation(clazz, NoticeVar.class);
        this.noticeResult = AnnotationUtils.findAnnotation(clazz, NoticeResult.class);
        this.noticeScope = AnnotationUtils.findAnnotation(clazz, NoticeScope.class);
        this.isField = false;
    }

    public Optional<NoticeSta> getNoticeSta() {
        return Optional.ofNullable(noticeSta);
    }

    public Optional<NoticeVar> getNoticeVar() {
        return Optional.ofNullable(noticeVar);
    }

    public Optional<NoticeResult> getNoticeResult() {
        return Optional.ofNullable(noticeResult);
    }

    public Optional<NoticeScope> getNoticeScope() {
        return Optional.ofNullable(noticeScope);
    }

    public boolean isNotField() {
        return !isField;
    }
}
