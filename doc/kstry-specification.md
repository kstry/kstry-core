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

1. `@EnableKstry` 代表启动 Kstry 容器
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

1. 起到 Spring 容器 `@Component` 注解作用，将组件托管至 Spring 容器中
2. 指定该类是 Kstry 定义 Task 的组件
   - name 指定组件名称，与 bpmn 流程配置文件中 `task-component` 属性进行匹配对应

 `@TaskService` 作用：

1. 指定该方法是 Kstry 容器中的 TaskService 节点，也是最小的可编排的执行单元，对应于 bpmn 配置文件中的 `bpmn:serviceTask` 节点
2. 该注解只有标注在 Kstry 组件的类方法上，否则将不被解析
   - name 指定该 service node 的名称，与 bpmn 配置文件中的 `task-service` 属性进行匹配对应
   - noticeScope 指定执行结果将被通知到 StoryBus 中的哪些作用域中，`ScopeTypeEnum.RESULT` 说明，该方法执行结果将作为 Story 执行的最终返回结果
3. `@ReqTaskParam` 标注在 TaskService 的 params 某个参数上，用来从 StoryBus 的 req 域获取变量值，直接赋值给该参数
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

1. `bpmn:startEvent` 中的 id属性，指定 Story的执行ID，**全局唯一**
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

1. 从 Spring 容器中注入 StoryEngine 执行器
2. ReqBuilder 构建执行入参，传入 startId，request。调用 fire，获取最终结果

## 1.3 测试

![image-20211211145528118](.\img\image-20211211145528118.png) 

# 二、流程编排

## 2.1 并行网关

> 加载商品基础信息之后，假设需要再加载SKU信息、店铺信息，两个加载过程可以并行进行。加载完所有信息之后再对商详信息进行后置处理，流程如图：

![image-20211211184127107](.\img\image-20211211184127107.png) 

### 2.1.1 编写组件代码

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

1. `getShopInfoByGoodsId` 中线程 sleep 了 200ms 模拟耗时较长的任务，**并行网关中，只有全部任务都执行完成之后才会继续向下执行**



