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

import cn.kstry.framework.core.annotation.NoticeScope;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.constant.GlobalConstant;
import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.identity.BasicIdentity;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 *
 * @author lykan
 */
public class MethodWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodWrapper.class);

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final Method method;

    private final ReturnTypeNoticeDef returnTypeNoticeDef = new ReturnTypeNoticeDef();

    private List<ParamInjectDef> paramInjectDefs;

    private final String kvScope;

    private final NoticeAnnotationWrapper noticeMethodSpecify;

    private final Class<?> targetType;

    private final String ability;

    public MethodWrapper(@Nonnull Method method, @Nonnull TaskService annotation, @Nonnull NoticeAnnotationWrapper noticeMethodSpecify) {
        AssertUtil.notNull(method);
        AssertUtil.notNull(annotation);
        this.method = method;
        this.kvScope = annotation.kvScope();
        this.noticeMethodSpecify = noticeMethodSpecify;
        this.targetType = annotation.targetType();
        this.ability = annotation.ability();

        methodParser(method);
    }

    public Method getMethod() {
        return method;
    }

    public ReturnTypeNoticeDef getReturnTypeNoticeDef() {
        return returnTypeNoticeDef;
    }

    public List<ParamInjectDef> getParamInjectDefs() {
        return paramInjectDefs;
    }

    public String getKvScope() {
        return kvScope;
    }

    public String getAbility() {
        return ability;
    }

    private void methodParser(Method method) {
        Class<?> returnType = method.getReturnType();
        if (!Objects.equals(GlobalConstant.VOID, returnType.getName())) {
            returnTypeParser(returnType);
        }

        Parameter[] parameters = method.getParameters();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        if (ArrayUtils.isNotEmpty(parameters)) {
            AssertUtil.equals(parameters.length, parameterNames.length);
            parametersParser(parameters, parameterNames);
        }
    }

    private void parametersParser(Parameter[] parameters, String[] parameterNames) {
        ParamInjectDef[] injectDefs = new ParamInjectDef[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            injectDefs[i] = null;
            Optional<TaskFieldProperty> annOptional = ElementParserUtil.getTaskParamAnnotation(p, parameterNames[i]);
            if (GlobalUtil.isCollection(p.getType())) {
                if (!annOptional.isPresent()) {
                    continue;
                }
                injectDefs[i] = new ParamInjectDef(p.getType(), parameterNames[i], annOptional.get());
                continue;
            }

            if (Role.class.isAssignableFrom(p.getType())) {
                injectDefs[i] = new ParamInjectDef(p.getType(), parameterNames[i], null);
                continue;
            }

            List<ParamInjectDef> injectDefList = getFieldInjectDefs(p.getType());
            boolean isSpringInitialization = ElementParserUtil.isSpringInitialization(p.getType());
            if (annOptional.isPresent() || CollectionUtils.isNotEmpty(injectDefList) || p.getType().isPrimitive() || isSpringInitialization) {
                ParamInjectDef injectDef = new ParamInjectDef(p.getType(), parameterNames[i], annOptional.orElse(null));
                injectDef.setFieldInjectDefList(injectDefList);
                injectDef.setSpringInitialization(isSpringInitialization);
                injectDefs[i] = injectDef;
            }
        }
        this.paramInjectDefs = Collections.unmodifiableList(Arrays.asList(injectDefs));
    }

    private List<ParamInjectDef> getFieldInjectDefs(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return null;
        }

        return ElementParserUtil.getFieldInjectDefList(clazz);
    }

    private void returnTypeParser(Class<?> returnType) {
        if (Mono.class.isAssignableFrom(returnType)) {
            returnType = targetType;
        } else {
            AssertUtil.isTrue(ElementParserUtil.isAssignable(targetType, returnType), ExceptionEnum.ANNOTATION_USAGE_ERROR);
            if (targetType != Object.class) {
                returnType = targetType;
            }
        }

        String className = StringUtils.uncapitalize(returnType.getSimpleName());
        parseReturnType(noticeMethodSpecify, returnType, className);

        parseReturnType(new NoticeAnnotationWrapper(returnType), returnType, className);

        List<Field> fieldsList = FieldUtils.getAllFieldsList(returnType);
        if (CollectionUtils.isNotEmpty(fieldsList)) {
            fieldsList.forEach(field -> {
                NoticeAnnotationWrapper fieldNoticeAnn = new NoticeAnnotationWrapper(field);
                parseReturnType(fieldNoticeAnn, field.getType(), field.getName());
            });
        }
    }

    private void noticeScopeDef(NoticeScope annotation, NoticeFieldItem noticeFieldItem) {
        AssertUtil.notNull(annotation);
        if (ArrayUtils.isEmpty(annotation.scope())) {
            returnTypeNoticeDef.noticeStaDefSet.add(noticeFieldItem);
            returnTypeNoticeDef.noticeVarDefSet.add(noticeFieldItem);
            return;
        }

        List<ScopeTypeEnum> scopeTypeList = Lists.newArrayList(annotation.scope());
        if (scopeTypeList.contains(ScopeTypeEnum.STABLE)) {
            returnTypeNoticeDef.noticeStaDefSet.add(noticeFieldItem);
        }
        if (scopeTypeList.contains(ScopeTypeEnum.VARIABLE)) {
            returnTypeNoticeDef.noticeVarDefSet.add(noticeFieldItem);
        }
        if (scopeTypeList.contains(ScopeTypeEnum.RESULT) && returnTypeNoticeDef.storyResultDef == null) {
            returnTypeNoticeDef.storyResultDef = noticeFieldItem;
        }
    }

    private void parseReturnType(NoticeAnnotationWrapper noticeAnn, Class<?> returnType, String fieldName) {

        AssertUtil.notBlank(fieldName);
        AssertUtil.anyNotNull(noticeAnn, returnType);

        noticeAnn.getNoticeSta().ifPresent(noticeSta -> {
            NoticeFieldItem noticeFieldItem = new NoticeFieldItem(fieldName, noticeSta.target(), returnType, noticeAnn.isNotField());
            returnTypeNoticeDef.noticeStaDefSet.add(noticeFieldItem);
        });
        noticeAnn.getNoticeVar().ifPresent(noticeVar -> {
            NoticeFieldItem noticeFieldItem = new NoticeFieldItem(fieldName, noticeVar.target(), returnType, noticeAnn.isNotField());
            returnTypeNoticeDef.noticeVarDefSet.add(noticeFieldItem);
        });
        noticeAnn.getNoticeResult().ifPresent(noticeResult -> {
            if (returnTypeNoticeDef.storyResultDef != null) {
                return;
            }
            returnTypeNoticeDef.storyResultDef = new NoticeFieldItem(fieldName, null, returnType, noticeAnn.isNotField());
        });
        noticeAnn.getNoticeScope().ifPresent(noticeScope -> {
            NoticeFieldItem noticeFieldItem = new NoticeFieldItem(fieldName, noticeScope.target(), returnType, noticeAnn.isNotField());
            noticeScopeDef(noticeScope, noticeFieldItem);
        });
    }

    public static class ParamInjectDef {

        private final Class<?> paramType;

        private final String fieldName;

        private String targetName;

        private List<ParamInjectDef> fieldInjectDefList;

        private ScopeTypeEnum scopeTypeEnum;

        private boolean springInitialization;

        private boolean injectSelf;

        public ParamInjectDef(Class<?> paramType, String fieldName, TaskFieldProperty taskFieldProperty) {
            AssertUtil.notNull(paramType);
            this.paramType = paramType;
            this.fieldName = fieldName;
            if (taskFieldProperty != null) {
                this.targetName = taskFieldProperty.getName();
                this.scopeTypeEnum = taskFieldProperty.getScopeDataEnum();
                this.injectSelf = taskFieldProperty.isInjectSelf();
            }
        }

        public Class<?> getParamType() {
            return paramType;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getTargetName() {
            return targetName;
        }

        public ScopeTypeEnum getScopeDataEnum() {
            return scopeTypeEnum;
        }

        public void setFieldInjectDefList(List<ParamInjectDef> fieldInjectDefList) {
            AssertUtil.isNull(this.fieldInjectDefList);
            if (fieldInjectDefList != null) {
                this.fieldInjectDefList = ImmutableList.copyOf(fieldInjectDefList);
            }
        }

        public List<ParamInjectDef> getFieldInjectDefList() {
            if (fieldInjectDefList == null) {
                return null;
            }
            return fieldInjectDefList;
        }

        public boolean isSpringInitialization() {
            return springInitialization;
        }

        public void setSpringInitialization(boolean springInitialization) {
            this.springInitialization = springInitialization;
        }

        public boolean isInjectSelf() {
            return injectSelf;
        }
    }

    public static class ReturnTypeNoticeDef {

        private final Set<NoticeFieldItem> noticeVarDefSet = new InSet<>();

        private final Set<NoticeFieldItem> noticeStaDefSet = new InSet<>();

        private NoticeFieldItem storyResultDef;

        public Set<NoticeFieldItem> getNoticeVarDefSet() {
            return noticeVarDefSet;
        }

        public Set<NoticeFieldItem> getNoticeStaDefSet() {
            return noticeStaDefSet;
        }

        public NoticeFieldItem getStoryResultDef() {
            return storyResultDef;
        }
    }

    public static class InSet<T> extends HashSet<T> {

        @Override
        public boolean add(T t) {
            boolean addResult = super.add(t);
            if (!addResult && (t instanceof NoticeFieldItem)) {
                LOGGER.warn("Fields in TaskService results are repeatedly defined! name: {}", ((NoticeFieldItem) t).getTargetName());
            }
            return addResult;
        }
    }

    public static class NoticeFieldItem extends BasicIdentity {

        /**
         * 字段名称
         */
        private final String fieldName;

        /**
         * 实际字段保存的名称
         */
        private final String targetName;

        /**
         * 字段的类型
         */
        private final Class<?> fieldClass;

        /**
         * 是否为结果本身
         */
        private final boolean resultSelf;

        public NoticeFieldItem(String fieldName, String targetName, Class<?> fieldClass, boolean resultSelf) {
            super(Optional.ofNullable(targetName).filter(StringUtils::isNotBlank).orElse(fieldName), IdentityTypeEnum.NOTICE_FIELD);
            AssertUtil.notNull(fieldClass);
            this.fieldName = fieldName;
            this.targetName = this.getIdentityId();
            this.fieldClass = fieldClass;
            this.resultSelf = resultSelf;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getTargetName() {
            return targetName;
        }

        public Class<?> getFieldClass() {
            return fieldClass;
        }

        public boolean isResultSelf() {
            return resultSelf;
        }
    }

    /**
     *
     * @author lykan
     */
    public static class TaskFieldProperty {

        private final String name;

        private final ScopeTypeEnum scopeTypeEnum;

        private boolean injectSelf;

        public TaskFieldProperty(String name, ScopeTypeEnum scopeTypeEnum) {
            AssertUtil.isTrue(GlobalConstant.STORY_DATA_SCOPE.contains(scopeTypeEnum),
                    ExceptionEnum.ANNOTATION_USAGE_ERROR, "Existence of an impermissible scopeType! scopeType: {}", scopeTypeEnum);
            this.name = name;
            this.scopeTypeEnum = scopeTypeEnum;
        }

        public String getName() {
            return name;
        }

        public ScopeTypeEnum getScopeDataEnum() {
            return scopeTypeEnum;
        }

        public boolean isInjectSelf() {
            return injectSelf;
        }

        public void setInjectSelf(boolean injectSelf) {
            this.injectSelf = injectSelf;
        }
    }
}
