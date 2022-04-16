## Kstry能做什么？

### 如果您遇到了以下问题：

- 代码复杂、模型文档更新不及时，致使新同学和非技术同学不能短时间内了解业务现状。技术和非技术间对同一业务理解存在分歧而不自知。甚至业务Owner也不能很流畅的描述出自己所负责的业务
- 项目中涉及到许多领域对象，对象间不仅存在复杂的前后依赖关系还相互掺杂没有明显边界，代码多次迭代后更是混乱不堪难以维护
- 某业务链路由一系列子任务组成，其中需要并行处理一些耗时长且数据间没有依赖的子任务，但苦于没有精简且无代码侵入的并发框架
- 维护平台型产品，为众多上游业务线提供着基础服务，但在短时间内应对各个业务方的定制化需求捉襟见肘，更不知如何做好平台与业务、业务与业务之间的隔离
- 业务流转数据状态追踪困难，只存在于线上环境的偶现问题更是难以排查。需要一种可以通过简单操作就能将重要节点数据都保存下来的能力，此能力堪比对链路精细化梳理后的系统性日志打印
- 业务场景多样，不乏一些复杂的链路难以被测试覆盖。或者三方数据Mock困难，测试成本居高不下



### 那么可以尝试一下Kstry框架，因为其具备：

#### 可视化

- 框架引入了业界通用的BPMN流程编排语言
- 使用事件节点、任务节点、网关节点等组件来描述业务动作和执行线路
- 编排好的图示模型即为代码真实的执行链路，通过所见（ *图示模型* ）即所得（ *代码执行* ）的方式在技术和业务之间架起一道通用语言的桥梁，使彼此之间沟通更加顺畅

![image-20211219163429668.png](http://cdn.kstry.cn/doc/img/image-20211219163429668.png)

#### 服务编排

- 框架通过定义任务节点来划分领域边界并实现业务功能，任务节点对应代码中Class的某个方法
- 一个独立的任务节点理论上只承担着一种业务动作或领域能力
- 输入完成任务所需参数的最小集，输出任务完成的结果或处理后的领域对象
- 节点间使用箭头符号这种可视化编排手段来保证彼此间的相互作用有序，通过并行网关、包含网关、排他网关等来丰富节点间的执行依赖关系

![image-20211219163540179](http://cdn.kstry.cn/doc/img/image-20211219163540179.png)

#### 支持并发

- 无需改动代码，仅仅在并行网关或包含网关上配置 *open-async=true*，即可将其后的子链路并行化

![image-20211213145202846](http://cdn.kstry.cn/doc/img/image-20211213145202846.png)

#### RBAC（ *Role-based access control* ）模式

- 针对平台型服务，首先可以定义编排出通用的链路模型
- 模型中的某个任务节点，应对不同业务场景或需求方的诉求时，可以扩展不同的服务能力（ *比如A、B两个业务方都需要抽佣的服务，那么就可以定义一个抽佣的任务节点，然后A业务需要比例抽佣，而B业务需要阶梯式抽佣，这时就可以在抽佣的任务节点上再扩展两个不同的抽佣能力* ）
- 扩展出来的能力可视作资源，所有的资源都有着独一无二的资源名称，携带着包含某个资源名称的权限对象即可访问与之对应的资源（ *资源也可称为：扩展出来的服务能力* ）
- 一批独立的权限对象有着较高的维护成本，所以可依次将某一业务场景所需的全部权限聚合起来组成角色对象
- 提供平台能力时，根据参数标识判断出具体的业务场景或需求方，并找到与之对应的角色，携带该角色执行预设的链路模型，即可完成定制化的业务诉求

![rbac](http://cdn.kstry.cn/doc/img/rbac.svg)

*详见：[RBAC模式](http://kstry.cn/doc/kstry-specification.html#五、rbac模式)*

#### 流程回溯

流程回溯可以在链路执行完之后，拿到结果或者异常之前，打印节点执行日志或执行自定义回调方法，可以应对如下问题：

- 查看运行过节点的信息如：执行顺序、节点耗时、入参、出参、异常信息等重要数据
- 自定义流程回溯日志，或者可以在出现异常时才打印详情日志
- 检查节点执行、参数设置等是否符合预期。因为有时结果确实没有报错，但并不代表过程一定没有问题
- 如果链路中有自定义角色的操作，检查最终角色是否符合预期

#### 简化测试

- 不依赖业务方入参，而是通过Mock业务角色的方式。之后请求携带该角色即可完成对包含有待测试服务节点或者能力扩展点的个性化业务链路的测试



## Kstry如何使用？
> **下面步骤仅为简单介绍，具体细节请参考使用文档**
>
> - [Kstry 使用文档](http://kstry.cn/doc/kstry-specification.html)
> - [Kstry 使用demo](https://gitee.com/kstry/kstry-demo)

### 1、配置引入

``` xml
<dependency>
    <groupId>cn.kstry.framework</groupId>
    <artifactId>kstry-core</artifactId>
    <version>1.0.5</version>
</dependency>
```

### 2、项目引入

``` java
@EnableKstry(bpmnPath = "./bpmn/*.bpmn")
@SpringBootApplication
public class KstryDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KstryDemoApplication.class, args);
    }
}
```

### 3、编写组件代码

``` java
@TaskComponent(name = "goods")
public class GoodsService {
    
    @NoticeResult
    @TaskService(name = "init-base-info")
    public GoodsDetail initBaseInfo(@ReqTaskParam(reqSelf = true) GoodsDetailRequest request) {
        return GoodsDetail.builder().id(request.getId()).name("商品").build();
    }
}
```

### 4、定义bpmn配置文件

<img src="http://cdn.kstry.cn/doc/img/image-20211211151733111.png" alt="image-20211211151733111" style="zoom:70%;" />  

### 5、调用执行

``` java
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private StoryEngine storyEngine;

    @PostMapping("/show")
    public GoodsDetail showGoods(@RequestBody GoodsDetailRequest request) {

        StoryRequest<GoodsDetail> req = ReqBuilder.returnType(GoodsDetail.class)
                                                  .startId("kstry-demo-goods-show").request(request).build();
        TaskResponse<GoodsDetail> fire = storyEngine.fire(req);
        if (fire.isSuccess()) {
            return fire.getResult();
        }
        return null;
    }
}
```

### 6、请求测试

<br />

<img src="http://cdn.kstry.cn/doc/img/image-20211211145528118.png" alt="image-20211211145528118" style="zoom:70%;" />

