# 一、快速开始

## 1.1 配置引入

``` xml
<dependency>
    <groupId>cn.kstry.framework</groupId>
    <artifactId>kstry-core</artifactId>
    <version>1.0.2-SNAPSHOT</version>
</dependency>
```

## 1.2 项目引入

### 1.2.1 开启Kstry容器

> kstry框架与spring容器有较为密切的关联和依赖，当前版本暂时只能在spring环境中运行

``` java
@EnableKstry(bpmnPath = "./bpmn/*.bpmn")
@SpringBootApplication
public class KstryDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KstryDemoApplication.class, args);
    }
}
```

### 1.2.2 编写组件代码

- `@EnableKstry` 代表启动 Kstry 容器
  - bpmnPath： 指定bpmn文件位置

``` java
@TaskComponent(name = GoodsCompKey.goods)
public class GoodsService {

    @TaskService(name = GoodsCompKey.GOODS.initBaseInfo, noticeScope = {ScopeTypeEnum.RESULT})
    public GoodsDetail initBaseInfo(@ReqTaskParam(reqSelf = true) GoodsDetailRequest request) {
        return GoodsDetail.builder().id(request.getId()).name("商品").build();
    }
}
```

> ``` java
> public interface GoodsCompKey {
> 
>     String goods = "goods";
> 
>     interface GOODS {
>         String initBaseInfo = "init-base-info";
>     }
> }
> ```

`@TaskComponent` 作用：

- 起到 Spring 容器 `@Component` 注解作用，将组件托管至 Spring 容器中

- 指定该类是 Kstry 定义 Task 的组件
  - name 指定组件名称，与 bpmn 流程配置文件中 `task-component` 属性进行匹配对应

 `@TaskService` 作用：

- 指定该方法是 Kstry 容器中的 TaskService 节点，也是最小的可编排的执行单元，对应于 bpmn 配置文件中的 `bpmn:serviceTask` 节点

- 该注解只有标注在 Kstry 组件的类方法上，否则将不被解析

  - name 指定该 service node 的名称，与 bpmn 配置文件中的 `task-service` 属性进行匹配对应

  - noticeScope 指定执行结果将被通知到 StoryBus 中的哪些作用域中，`ScopeTypeEnum.RESULT` 说明，该方法执行结果将作为 Story 执行的最终返回结果

- `@ReqTaskParam` 标注在 TaskService 的 params 某个参数上，用来从 StoryBus 的 req 域获取变量值，直接赋值给该参数
  - reqSelf 只有从 req 域获取参数时才有这个属性，代表将客户端传入的request对象直接赋值给被标注的参数

### 1.2.3 定义bpmn配置文件

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL">
  <bpmn:process id="Process_0zcsieh" isExecutable="true">
    <bpmn:startEvent id="kstry-demo-goods-show" name="kstry-demo-goods-show">
      <bpmn:outgoing>Flow_1w64322</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:serviceTask id="Activity_04a99ll" name="show goods">
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="task-component" value="goods" />
          <camunda:property name="task-service" value="init-base-info" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1w64322</bpmn:incoming>
      <bpmn:outgoing>Flow_0f9ephk</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:sequenceFlow id="Flow_1w64322" sourceRef="kstry-demo-goods-show" targetRef="Activity_04a99ll" />
    <bpmn:endEvent id="Event_1jdtnd8">
      <bpmn:incoming>Flow_0f9ephk</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_0f9ephk" sourceRef="Activity_04a99ll" targetRef="Event_1jdtnd8" />
  </bpmn:process>
</bpmn:definitions>
```

![image-20211211151733111](.\img\image-20211211151733111.png)   

- `bpmn:startEvent` 中的 id属性，指定 Story的执行ID，**全局唯一**
  - id 需要符合一定格式的前缀, 默认是：`story-def-`，可通过配置文件进行修改，如下

``` yaml
# application.yml
spring:
  application:
    name: kstry-demo

kstry:
  story:
    prefix: kstry-demo- # 指定 Story 的 StartId 前缀
```

### 1.2.4 执行Story

``` java
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private StoryEngine storyEngine;

    @PostMapping("/show")
    public GoodsDetail showGoods(@RequestBody GoodsDetailRequest request) {

        StoryRequest<GoodsDetail> req = ReqBuilder.returnType(GoodsDetail.class).startId(StartIdEnum.GOODS_SHOW.getId()).request(request).build();
        TaskResponse<GoodsDetail> fire = storyEngine.fire(req);
        if (fire.isSuccess()) {
            return fire.getResult();
        }
        return null;
    }
}
```

> ``` java
> public enum StartIdEnum {
> 
>     GOODS_SHOW("goods-show");
> 
>     StartIdEnum(String id) {
>         this.id = "kstry-demo-" + id;
>     }
> 
>     @Getter
>     private final String id;
> }
> ```

- 从 Spring 容器中注入 StoryEngine 执行器

- ReqBuilder 构建执行入参，传入 startId，request。调用 fire，获取最终结果

## 1.3 测试

![image-20211211145528118](.\img\image-20211211145528118.png) 

# 二、流程编排

## 2.1 节点多支路

> 在上传商品图片时，一般会经过风控系统，对所传图片进行筛查，防止有违规图片暴露给用户。暂且忽略性能问题，可以将风控做在商品获取链路中

<img src=".\img\image-20211211230004495.png" alt="image-20211211230004495" style="zoom:80%;" />   

新增图片筛查节点：

``` java
@Slf4j
@TaskComponent(name = RiskControlCompKey.riskControl)
public class RiskControlService {

    @TaskService(name = RiskControlCompKey.checkImg)
    public void checkImg(CheckInfo checkInfo) {

        AssertUtil.notNull(checkInfo);
        AssertUtil.notBlank(checkInfo.getImg());
        log.info("check img: " + checkInfo.getImg());
    }
}
```

- 流程表示：商品信息初始化完成之后，如果商品有图片信息，则进行风控筛查，如果没有直接返回结果

- 节点间的箭头线可以定义执行条件，格式如上图，`result.img != null` 代表 StoryBus 中的 result 不为空，且result的img属性不为null

- Kstry引擎中条件表达式解析器有三个，boolean解析器、角色解析器、Spel解析器
  - 如果是直接输入boolean值，比如 true、y、no等会被认定为boolean值，使用 boolean 解析器解析判断
  - 如果符合权限定义的格式，使用角色解析器解析判断，后面讲到角色权限时会再详细介绍
  - 前两者都不符合时则使用Spel解析器，解析引擎是Spring的Spel解析器，执行格式不对时会报错。返回结果一定是Boolean值。比如上面 `result.img != null`如果result为null时，会抛异常结束

- 事件、网关、任务节点都可以从当前节点引出多个支路，但是**只有并行网关、包含网关、结束事件可以接收并归并多个支路**，其他节点有多个入度时会出现配置文件解析失败的情况

- **一个链路图中有且仅有一个开始事件和结束事件**（子事件中同样有这个限制，外围事件和子链路中的事件是可以共同存在的）。

## 2.2 并行网关

> 加载商品基础信息之后，假设需要再加载SKU信息、店铺信息，两个加载过程可以并行进行。加载完所有信息之后再对商详信息进行后置处理，流程如图：

![image-20211211184127107](.\img\image-20211211184127107.png) 

新增SKU初始化任务、商详后置处理任务、店铺加载任务：

```java
// 初始化 sku信息，GoodsService.java
@TaskService(name = GoodsCompKey.initSku)
public InitSkuResponse initSku(@ReqTaskParam("id") Long goodsId) {
    SkuInfo sku1 = new SkuInfo();
    sku1.set...

    SkuInfo sku2 = new SkuInfo();
    sku2.set...
    return InitSkuResponse.builder().skuInfos(Lists.newArrayList(sku1, sku2)).build();
}

// 商详信息后置处理，GoodsService.java
@TaskService(name = GoodsCompKey.detailPostProcess)
public void detailPostProcess(DetailPostProcessRequest request) {

    GoodsDetail goodsDetail = request.getGoodsDetail();
    ShopInfo shopInfo = request.getShopInfo();
    if (shopInfo != null) {
        goodsDetail.setShopInfo(shopInfo);
    }
}

// 加载店铺信息，ShopService.java
@TaskService(name = ShopCompKey.getShopInfoByGoodsId)
public ShopInfo getShopInfoByGoodsId(@ReqTaskParam("id") Long goodsId) throws InterruptedException {
    TimeUnit.MILLISECONDS.sleep(200L);
    return goodsIdShopInfoMapping.get(goodsId);
}
```

- 并行网关一般分为两部分，前面将一个分支拆解成多个，后面将多个分支进行聚合。并行网关要求，所有入度全部执行完才能向下执行

- 未开启多线程模式时，并行网关拆分出的多个分支还是一个线程逐一执行，开启多线程模式后，几个分支将逐一创建异步任务提交到线程池中执行

- 并行网关后面的支路判断条件会被忽略，无论设置与否都不会解析，都会默认为true

- `getShopInfoByGoodsId` 中线程 sleep 了 200ms 模拟耗时较长的任务，**并行网关中，只有全部任务都执行完成之后才会继续向下执行**

将风控组件加到流程之后，得到流程图如下：

![image-20211212002539988](.\img\image-20211212002539988.png) 

- 这时再次执行这个Story会报错，报错信息： `[K1040008] A process branch that cannot reach the ParallelGateway appears! sequenceFlowId: Flow_0attv25`

- 报错信息提示存在不能到达并行网关的分支。原因是商品图片只有出现和不出现两种情况，所以两条链路只能执行一条分支，而并行网关要求的是所有入度分支都进入时才能继续执行。解决这个问题有两种方式：

  - 将前一个并行网关改为包含网关，包含网关不要求所有入度分支必须被执行

  - 如图关闭并行网关的严格模式：`strict-mode=false`，关闭严格模式的并行网关，不在限制入度必须被执行。关闭严格模式的并行网关与包含网关并非完全等价的。因为并行网关后面支路的判断条件是被忽略的，但是包含网关后面支路的判断条件是会被解析执行起到决策作用的

![image-20211212004020932](.\img\image-20211212004020932.png)  

## 2.3 排他网关



