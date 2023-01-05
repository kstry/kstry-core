package cn.kstry.framework.test.kv.service;

import cn.kstry.framework.core.component.dynamic.creator.DynamicKValue;
import cn.kstry.framework.core.kv.KvScope;
import org.junit.Assert;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DynamicValue implements DynamicKValue {

    @Override
    public long version(String key) {
        return DynamicKValue.super.version(key);
    }

    @Override
    public Optional<Object> getValue(String key, KvScope kvScope) {
        Assert.assertEquals("new-per-scope", kvScope.getScope());
        Assert.assertEquals("business-channel", kvScope.getBusinessId().orElse(null));
        if (key.equals("dynamic")) {
            return Optional.of("dynamic");
        }
        return Optional.empty();
    }
}
