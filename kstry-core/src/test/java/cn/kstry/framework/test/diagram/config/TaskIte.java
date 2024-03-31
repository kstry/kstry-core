package cn.kstry.framework.test.diagram.config;

import cn.kstry.framework.core.bus.ScopeDataNotice;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.engine.interceptor.Iter;
import cn.kstry.framework.core.engine.interceptor.IterData;
import cn.kstry.framework.core.engine.interceptor.TaskInterceptor;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import org.junit.Assert;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskIte {

    @Bean
    public TaskInterceptor getTaskInterceptor1() {
        return new TaskInterceptor() {
            @Override
            public boolean match(IterData iterData) {
                AssertUtil.notNull(iterData);
                return true;
            }

            @Override
            public int getOrder() {
                return 7;
            }

            @Override
            public Object invoke(Iter iter) {
                ScopeDataOperator scopeDataOperator = iter.getDataOperator();
                scopeDataOperator.setStaData("ci", 7);
                ServiceNodeResource serviceNode = iter.getServiceNode();
                System.out.println(JSON.toJSONString(serviceNode));
                return iter.next();
            }
        };
    }

    @Bean
    public TaskInterceptor getTaskInterceptor2() {
        return new TaskInterceptor() {
            @Override
            public int getOrder() {
                return 3;
            }

            @Override
            public Object invoke(Iter iter) {
                ServiceNodeResource serviceNode = iter.getServiceNode();
                Assert.assertNotNull(serviceNode);
                ScopeDataOperator scopeDataOperator = iter.getDataOperator();
                scopeDataOperator.setStaData("ai", 3);
                Object next = iter.next();
                if ("ite".equals(scopeDataOperator.getBusinessId().orElse(null)) && scopeDataOperator.getStaData("ai").isPresent()) {
                    if (next instanceof ScopeDataNotice) {
                        AssertUtil.equals(((ScopeDataNotice) next).getResult(), 22);
                    } else {
                        AssertUtil.equals(next, 22);
                    }
                }
                return next;
            }
        };
    }

    @Bean
    public TaskInterceptor getTaskInterceptor3() {
        return new TaskInterceptor() {
            @Override
            public int getOrder() {
                return 5;
            }

            @Override
            public Object invoke(Iter iter) {
                ScopeDataOperator scopeDataOperator = iter.getDataOperator();
                scopeDataOperator.setStaData("bi", 5);
                return iter.next();
            }
        };
    }
}
