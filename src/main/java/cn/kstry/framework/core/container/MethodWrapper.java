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
package cn.kstry.framework.core.container;

import cn.kstry.framework.core.annotation.NoticeAll;
import cn.kstry.framework.core.annotation.NoticeSta;
import cn.kstry.framework.core.annotation.NoticeVar;
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
import org.springframework.core.annotation.AnnotationUtils;
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

    private static final List<ScopeTypeEnum> NEED_PARSER_SCOPE = Lists.newArrayList(ScopeTypeEnum.STABLE, ScopeTypeEnum.VARIABLE, ScopeTypeEnum.RESULT);

    private final Method method;

    private final ReturnTypeNoticeDef returnTypeNoticeDef = new ReturnTypeNoticeDef();

    private List<ParamInjectDef> paramInjectDefs;

    private final String kvScope;

    private final List<ScopeTypeEnum> noticeScopeList;

    private final Class<?> returnClassType;

    private final String ability;

    public MethodWrapper(@Nonnull Method method, @Nonnull TaskService annotation) {

        AssertUtil.notNull(method);
        AssertUtil.notNull(annotation);
        this.method = method;
        this.kvScope = annotation.kvScope();
        this.noticeScopeList = ImmutableList.copyOf(annotation.noticeScope());
        this.returnClassType = annotation.returnClassType();
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
            Optional<TaskFieldProperty> annOptional = ElementParserUtil.getTaskParamAnnotation(p);
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
        this.paramInjectDefs = ImmutableList.copyOf(injectDefs);
    }

    private List<ParamInjectDef> getFieldInjectDefs(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return null;
        }

        return ElementParserUtil.getFieldInjectDefList(clazz);
    }

    private void returnTypeParser(Class<?> returnType) {
        if (Mono.class.isAssignableFrom(returnType)) {
            returnType = returnClassType;
        } else {
            AssertUtil.isTrue(ElementParserUtil.isAssignable(returnClassType, returnType), ExceptionEnum.ANNOTATION_USAGE_ERROR);
            if (returnClassType != Object.class) {
                returnType = returnClassType;
            }
        }
        boolean needParser = !Collections.disjoint(NEED_PARSER_SCOPE, noticeScopeList);

        // NEED_PARSER_SCOPE 与 noticeScopeList 存在交集
        if (needParser) {
            String className = StringUtils.uncapitalize(returnType.getSimpleName());
            boolean returnResult = noticeScopeList.contains(ScopeTypeEnum.RESULT);
            NoticeFieldItem noticeFieldItem = new NoticeFieldItem(className, null, returnType, true, returnResult);
            if (noticeScopeList.contains(ScopeTypeEnum.STABLE)) {
                returnTypeNoticeDef.noticeStaDefSet.add(noticeFieldItem);
            }
            if (noticeScopeList.contains(ScopeTypeEnum.VARIABLE)) {
                returnTypeNoticeDef.noticeVarDefSet.add(noticeFieldItem);
            }
            if (returnResult) {
                returnTypeNoticeDef.storyResultDef = noticeFieldItem;
            }
        } else {
            parseBeanSelf(returnType);
        }
        parseFields(returnType);
    }

    private void parseFields(Class<?> returnType) {

        List<Field> noticeVarList = FieldUtils.getFieldsListWithAnnotation(returnType, NoticeVar.class);
        if (CollectionUtils.isNotEmpty(noticeVarList)) {
            noticeVarList.forEach(field -> {
                NoticeVar annotation = field.getAnnotation(NoticeVar.class);
                AssertUtil.notNull(annotation);
                NoticeFieldItem noticeFieldItem = new NoticeFieldItem(field.getName(), annotation.name(), field.getType(), false, annotation.returnResult());
                returnTypeNoticeDef.noticeVarDefSet.add(noticeFieldItem);
            });
        }

        List<Field> noticeStaList = FieldUtils.getFieldsListWithAnnotation(returnType, NoticeSta.class);
        if (CollectionUtils.isNotEmpty(noticeStaList)) {
            noticeStaList.forEach(field -> {
                NoticeSta annotation = field.getAnnotation(NoticeSta.class);
                AssertUtil.notNull(annotation);
                NoticeFieldItem noticeFieldItem = new NoticeFieldItem(field.getName(), annotation.name(), field.getType(), false, annotation.returnResult());
                returnTypeNoticeDef.noticeStaDefSet.add(noticeFieldItem);
            });
        }

        List<Field> noticeAllList = FieldUtils.getFieldsListWithAnnotation(returnType, NoticeAll.class);
        if (CollectionUtils.isNotEmpty(noticeAllList)) {
            noticeAllList.forEach(field -> {
                NoticeAll annotation = field.getAnnotation(NoticeAll.class);
                AssertUtil.notNull(annotation);
                NoticeFieldItem noticeFieldItem = new NoticeFieldItem(field.getName(), annotation.name(), field.getType(), false, annotation.returnResult());
                returnTypeNoticeDef.noticeStaDefSet.add(noticeFieldItem);
                returnTypeNoticeDef.noticeVarDefSet.add(noticeFieldItem);
            });
        }
    }

    private void parseBeanSelf(Class<?> returnType) {
        String className = StringUtils.uncapitalize(returnType.getSimpleName());
        for (Class<?> c = returnType; c != Object.class; c = c.getSuperclass()) {
            NoticeSta noticeSta = AnnotationUtils.findAnnotation(c, NoticeSta.class);
            if (noticeSta != null) {
                NoticeFieldItem noticeFieldItem = new NoticeFieldItem(className, noticeSta.name(), returnType, true, noticeSta.returnResult());
                returnTypeNoticeDef.noticeStaDefSet.add(noticeFieldItem);
            }
            NoticeVar noticeVar = AnnotationUtils.findAnnotation(c, NoticeVar.class);
            if (noticeVar != null) {
                NoticeFieldItem noticeFieldItem = new NoticeFieldItem(className, noticeVar.name(), returnType, true, noticeVar.returnResult());
                returnTypeNoticeDef.noticeVarDefSet.add(noticeFieldItem);
            }
            NoticeAll noticeAll = AnnotationUtils.findAnnotation(c, NoticeAll.class);
            if (noticeAll != null) {
                NoticeFieldItem noticeFieldItem = new NoticeFieldItem(className, noticeAll.name(), returnType, true, noticeAll.returnResult());
                returnTypeNoticeDef.noticeStaDefSet.add(noticeFieldItem);
                returnTypeNoticeDef.noticeVarDefSet.add(noticeFieldItem);
            }
        }
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

        /**
         * 是否是最终需要返回的结果
         */
        private final boolean returnResult;

        public NoticeFieldItem(String fieldName, String targetName, Class<?> fieldClass, boolean resultSelf, boolean returnResult) {
            super(Optional.ofNullable(targetName).filter(StringUtils::isNotBlank).orElse(fieldName), IdentityTypeEnum.NOTICE_FIELD);
            AssertUtil.notNull(fieldClass);
            this.fieldName = fieldName;
            this.targetName = this.getIdentityId();
            this.fieldClass = fieldClass;
            this.resultSelf = resultSelf;
            this.returnResult = returnResult;
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

        public boolean isReturnResult() {
            return returnResult;
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
