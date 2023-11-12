package cn.kstry.framework.core.component.conversion.converter;

import cn.kstry.framework.core.component.conversion.TypeConverter;
import cn.kstry.framework.core.constant.TypeConverterNames;
import cn.kstry.framework.core.util.ElementParserUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class CollectionGenericTypeConverter implements TypeConverter<Collection, Collection> {

    @Override
    public boolean match(Object source, Class<?> needType, @Nullable Class<?> collGeneric) {
        if (!(source instanceof Set || source instanceof List)) {
            return false;
        }
        if (!(Set.class.isAssignableFrom(needType) || List.class.isAssignableFrom(needType))) {
            return false;
        }
        if (CollectionUtils.isEmpty((Collection) source) || collGeneric == null) {
            return false;
        }
        return !ElementParserUtil.isAssignable(collGeneric, ((Collection) source).iterator().next().getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection convert(Collection source, Class<?> needType, Class<?> collGeneric) {
        if (source == null || needType == null || collGeneric == null) {
            return source;
        }
        Collection collection = (List.class.isAssignableFrom(needType) ? Lists.newArrayList() : Sets.newHashSet());
        for (Object next : source) {
            String s = (next instanceof String) ? (String) next : JSON.toJSONString(next, SerializerFeature.DisableCircularReferenceDetect);
            collection.add(JSON.parseObject(s, collGeneric));
        }
        return collection;
    }

    @Override
    public Collection doConvert(Collection source, @Nullable Class<?> needType) {
        return null;
    }

    @Override
    public Class<Collection> getSourceType() {
        return Collection.class;
    }

    @Override
    public Class<Collection> getTargetType() {
        return Collection.class;
    }

    @Override
    public String getConvertName() {
        return TypeConverterNames.COLLECTION_GENERIC_TYPE_CONVERTER;
    }

    @Override
    public int getOrder() {
        return 1000;
    }
}
