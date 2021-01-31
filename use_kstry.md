## 一、框架引入

### 1.1 pom引入

``` xml
<dependency>
    <groupId>cn.kstry.framework</groupId>
    <artifactId>kstry-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

> 暂未上传maven公有仓库，需要自己打包源码生成jar上传仓库或本地

### 1.2 框架依赖

``` xml
<dependency.spring.version>4.0.9.RELEASE</dependency.spring.version>
<dependency.collections.version>3.2.2</dependency.collections.version>
<dependency.fastjson.version>1.2.75</dependency.fastjson.version>
<dependency.lang3.version>3.11</dependency.lang3.version>
<dependency.guava.version>30.0-jre</dependency.guava.version>
<dependency.beanutils.version>1.9.4</dependency.beanutils.version>
<dependency.slf4j.version>1.7.30</dependency.slf4j.version>
<dependency.jcl-over-slf4j.version>1.7.30</dependency.jcl-over-slf4j.version>

<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>${dependency.spring.version}</version>
    </dependency>
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
        <version>${dependency.fastjson.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>${dependency.lang3.version}</version>
    </dependency>
    <dependency>
        <groupId>commons-collections</groupId>
        <artifactId>commons-collections</artifactId>
        <version>${dependency.collections.version}</version>
    </dependency>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>${dependency.guava.version}</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>${dependency.slf4j.version}</version>
    </dependency>
    <dependency>
        <groupId>commons-beanutils</groupId>
        <artifactId>commons-beanutils</artifactId>
        <version>${dependency.beanutils.version}</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>jcl-over-slf4j</artifactId>
        <version>${dependency.jcl-over-slf4j.version}</version>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.2.3</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 1.3 框架支持

- JDK 1.8+
- Spring 4.0+

## 二、初体验

### 2.1 定义角色接口，并定义事件

``` java
public interface AuthenticationRole extends EventOperatorRole {

    TaskResponse<UserLoginResponse> userLogin(UserLoginRequest userLoginRequest);
}
```

- 接口需继承 `EventOperatorRole` 代表是Kstry 定义的角色
- **事件入参可为空，最多可设置一个参数**
- **事件出参为`TaskResponse`的派生类**

### 2.2 实现具体事件

``` java
@EventGroupComponent(eventGroupName = "USER_AUTHENTICATION_EVENT_GROUP", operatorRoleClass = AuthenticationRole.class)
public class UserAuthenticationEventGroupImpl implements AuthenticationRole {

    @Override
    public TaskResponse<UserLoginResponse> userLogin(UserLoginRequest userLoginRequest) {
      
        // 具体实现内容省略
        return TaskResponseBox.buildSuccess(response);
    }
}
```

- 使用 `EventGroupComponent` 注解。 `EventGroupComponent`集成自Spring的`@Component` 可将该实例增加到Spring容器（Kstry容器）中
  - `eventGroupName`：事件组名称
  - `operatorRoleClass`：事件组所属的角色类

### 2.3 创建配文事件

```javascript
// auth.json
{
  "story_def": { // Story 定义
    "login": [ // Story名称
      {
        "event_node": "user_login_node", // event node名称，与 event_def 中定义的相对应
        "request_mapping": "user_login_mapping" // request mapping 参数填充定义，与 request_mapping_def 中定义相对应
      }
    ]
  },
  "request_mapping_def": { //  request mapping 定义
    "user_login_mapping": {
      "userId": "@req.userId" // 从 request 域中取 `userId` 字段赋值给 request 中的 `userId`
    }
  },
  "event_def": { // 事件定义
    "USER_AUTHENTICATION_EVENT_GROUP": { // 事件组
      "user_login_node": { // 事件名称
        "event_type": "TASK", // 事件类型
        "event_action": "userLogin" // 具体执行时间的方法
      }
    }
  }
}
```

### 2.4 启动 Kstry 引擎，并执行得到结果

``` javascript
@Configuration
@ComponentScan(basePackageClasses = GoodsBootstrap.class)
@EnableKstry(configPath = "classpath:config/*.json") // 启动 Kstry 引擎，并指定配置文件的位置
public class GoodsBootstrap {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(GoodsBootstrap.class);
        StoryEngine storyEngine = context.getBean("storyEngine", StoryEngine.class); // 从 Spring 容器中获取 storyEngine

        BuyGoodsRequest buyGoodsRequest = new BuyGoodsRequest();
        buyGoodsRequest.setUserId(2L);

        TaskResponse<UserLoginResponse> login = storyEngine.fire(buyGoodsRequest, "login", UserLoginResponse.class); // 执行
        System.out.println(JSON.toJSONString(login));
        context.close();
    }
}
```

### 2.5 多个事件组的定义方式

``` java
// 定义事件组
public interface AuthenticationEventGroup {

    TaskResponse<UserLoginResponse> userLogin(UserLoginRequest userLoginRequest);
}

// 定义角色，角色除了继承 EventOperatorRole 代表是Kstry角色外，还需继承 AuthenticationEventGroup 代表 AuthenticationEventGroup 属于该角色
public interface AuthenticationRole extends EventOperatorRole, AuthenticationEventGroup {

}

// 具体实现类，实现事件组接口即可
@EventGroupComponent(eventGroupName = "USER_AUTHENTICATION_EVENT_GROUP", operatorRoleClass = AuthenticationRole.class)
public class UserAuthenticationEventGroupImpl implements AuthenticationEventGroup {

    @Override
    public TaskResponse<UserLoginResponse> userLogin(UserLoginRequest userLoginRequest) {
      
        // 具体实现内容省略
        return TaskResponseBox.buildSuccess(response);
    }
}
```

## 三、功能使用

### 3.1 动态路由（MATCH）

#### 3.1.1 定义 customerLogin

``` java
@EventGroupComponent(eventGroupName = "CUSTOMER_AUTHENTICATION_EVENT_GROUP", operatorRoleClass = AuthenticationRole.class)
public class CustomerAuthenticationEventGroupImpl implements AuthenticationEventGroup {

    @Override
    public TaskResponse<UserLoginResponse> userLogin(UserLoginRequest userLoginRequest) {
        // 具体实现内容省略
        return TaskResponseBox.buildSuccess(response);
    }
}
```

#### 3.3.2 修改配置文件

``` javascript
{
  "story_def": {
    "login": [
      {
        "strategy": "login_strategy" // 登录使用的策略
      }
    ],
    "user_login_story": [
      {
        "event_node": "user_login_node",
        "request_mapping": "user_login_mapping"
      }
    ],
    "customer_login_story": [
      {
        "event_node": "customer_login_node",
        "request_mapping": "user_login_mapping"
      }
    ]
  },
  "strategy_def": { //策略集
    "login_strategy": [ // 登录策略
      {
        "story": "customer_login_story", // 指定匹配成功后之执行的子story
        "strategy_type": "MATCH",
        "rule_set": {
          "equals-@req.userType": "1" // userType==1 时执行
        }
      },
      {
        "story": "user_login_story",
        "strategy_type": "MATCH",
        "rule_set": {
          "compare-@req.userType": ">=2L" // userType>=2 时执行
        }
      }
    ]
  },
  "request_mapping_def": {
    "user_login_mapping": {
      "userId": "@req.userId",
      "userType": "@req.userType"
    }
  },
  "event_def": {
    "USER_AUTHENTICATION_EVENT_GROUP": {
      "user_login_node": {
        "event_type": "TASK",
        "event_action": "userLogin"
      }
    },
    "CUSTOMER_AUTHENTICATION_EVENT_GROUP": {
      "customer_login_node": {
        "event_type": "TASK",
        "event_action": "userLogin"
      }
    }
  }
}
```

### 3.2 是否跳过当前节点(FILTER)

**3.2.1 修改配置文件**

``` javascript
{
  "story_def": {
    "login": [
      {
        "event_node": "user_login_node",
        "request_mapping": "user_login_mapping",
        "strategy": "login_strategy"
      }
    ]
  },
  "strategy_def": {
    "login_strategy": [
      {
        "strategy_type": "FILTER",
        "rule_set": {
          "notNull-@req.userType": "" // request 域中 userType 不为空时执行登录操作
        }
      }
    ]
  },
  "request_mapping_def": {
    "user_login_mapping": {
      "userId": "@req.userId",
      "userType": "@req.userType"
    }
  },
  "event_def": {
    "USER_AUTHENTICATION_EVENT_GROUP": {
      "user_login_node": {
        "event_type": "TASK",
        "event_action": "userLogin"
      }
    }
  }
}
```

### 3.3 异步执行(TIMESLOT)

**3.3.1 修改配置文件**

```javascript
{
  "story_def": {
    "login": [
      {
        "event_node": "user_login_node",
        "request_mapping": "user_login_mapping",
        "strategy": "login_strategy"
      }
    ]
  },
  "strategy_def": {
    "login_strategy": [
      {
        "strategy_type": "TIMESLOT", // 指定脱离当前 Event 主线，开始执行 TIMESLOT（时间片段） 流程
        "async": true, // 异步执行
        "timeout": 2000 // 取值时的超时时间，单位 ms
      }
    ]
  },
  "request_mapping_def": {
    "user_login_mapping": {
      "userId": "@req.userId",
      "userType": "@req.userType"
    }
  },
  "event_def": {
    "USER_AUTHENTICATION_EVENT_GROUP": {
      "user_login_node": {
        "event_type": "TASK",
        "event_action": "userLogin"
      }
    }
  }
}
```

### 3.4 FILTER、MATCH、TIMESLOT结合使用

**3.4.1 修改配置文件**

> 实现功能：userType 不为空时异步登录，登录时需根据 userType 判断是 user 还是 customer 登录

``` javascript
{
  "story_def": {
    "login": [  //（0）入口
      {
        "strategy": "async_login_strategy"  //（1）匹配策略 async_login_strategy
      }
    ],
    "async_login": [  //（3）执行 async_login
      {
        "strategy": "login_strategy"  //（4）再次匹配策略 login_strategy
      }
    ],
    "user_login_story": [  //（6.1）执行具体登录操作
      {
        "event_node": "user_login_node",
        "request_mapping": "user_login_mapping"
      }
    ],
    "customer_login_story": [  //（6.2）执行具体登录操作
      {
        "event_node": "customer_login_node",
        "request_mapping": "user_login_mapping"
      }
    ]
  },
  "strategy_def": {
    "async_login_strategy": [ //（2）策略指定异步调用 async_login，并且要根据 userType 是否存在来判断是否需要执行
      {
        "story": "async_login",
        "strategy_type": "TIMESLOT",
        "async": true,
        "timeout": 2000
      },
      {
        "strategy_type": "FILTER",
        "rule_set": {
          "notNull-@req.userType": ""
        }
      }
    ],
    "login_strategy": [  //（5）策略指出，userType=1 时执行 customer_login_story，userType>=2 时执行 user_login_story
      {
        "story": "customer_login_story",
        "strategy_type": "MATCH",
        "rule_set": {
          "equals-@req.userType": "1"
        }
      },
      {
        "story": "user_login_story",
        "strategy_type": "MATCH",
        "rule_set": {
          "compare-@req.userType": ">=2L"
        }
      }
    ]
  },
  "request_mapping_def": {
    "user_login_mapping": {
      "userId": "@req.userId",
      "userType": "@req.userType"
    }
  },
  "event_def": {
    "USER_AUTHENTICATION_EVENT_GROUP": {
      "user_login_node": {
        "event_type": "TASK",
        "event_action": "userLogin"
      }
    },
    "CUSTOMER_AUTHENTICATION_EVENT_GROUP": {
      "customer_login_node": {
        "event_type": "TASK",
        "event_action": "userLogin"
      }
    }
  }
}
```

### 3.5 自定义策略规则

**3.5.1 创建规则，并放入Spring容器中**

```java
// 添加 @Component 注解，将 策略规则放入Spring容器
// 实现 StrategyRuleCalculator 接口
@Component
public class CustomerStrategy implements StrategyRuleCalculator {

     /**
     * 计算是否匹配成功
     *
     * @param source 从 StoryBus 中指定获取的值
     * @param expected 配置文件中定义的值
     * @return 是否匹配成功
     */
    @Override
    public boolean calculate(Object source, Object expected) {
        return source != null && NumberUtils.toInt(source.toString(), 0) > 0;
    }

     /**
     * 容器启动时调用，判断配置文件指定的值是否符合预期，做格式校验
     *
     * @return 是否符合预期格式
     */
    @Override
    public boolean checkExpected(String expected) {
        return true;
    }

     /**
     * 在配置文件中使用时的规则名称
     *
     * @return 规则名称
     */
    @Override
    public String getCalculatorName() {
        return "typeCheck";
    }
}
```

**3.5.2 使用规则**

``` javascript
{
  "strategy_def": {
    "login_strategy": [
      {
        "strategy_type": "FILTER",
        "rule_set": {
          "typeCheck-@req.userType": "" // typeCheck 为代码中指定的规则名称
        }
      }
    ]
  }
}
```

### 3.6 StoryBus 数据获取

- 同步获取使用@，比如：`@req.user`
- 异步获取TIMESLOT执行结果使用$，比如：`$login_strategy.@sta.user`
- StoryBus对应的三大数据域获取方式
  - 稳定数据集：@sta
  - 可变数据集：@var
  - request数据集：@req

### 3.7 数据放入StoryBus方式

#### 3.7.1 默认放入

- Event节点执行完成，如果有返回值，默认会放入StoryBus
- 获取方式：`@user_login_node.user`其中 `user_login_node`是EventNode名称

#### 3.7.2 返回值使用 `NoticeBusTaskResponse`

- `NoticeBusTaskResponse` 是 `TaskResponse` 的子类，定义有`addStableDataMap`和`updateVariableDataMap`两个方法
- 两个方法可以分别更新var数据域，添加sta数据域（如果新增时已存在，打印警告日志，且新增失败保留原值）

#### 3.7.3 response实体类中字段使用注解

- @NoticeSta：字段结果被通知到 bus 中的 stable 变量集中
- @NoticeVar：字段结果被通知到 bus 中的 variable 变量集中
- @NoticeStaAndVar：字段结果被通知到 bus 中的 stable 和 variable 两个变量集中



