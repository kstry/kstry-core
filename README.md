

&nbsp;&nbsp;&nbsp;&nbsp;🟢 [项目主页](http://kstry.cn)

&nbsp;&nbsp;&nbsp;&nbsp;🟢 [Kstry概念介绍](http://kstry.cn/doc/understandkstry/understand-kstry.html)

&nbsp;&nbsp;&nbsp;&nbsp;🟢 [Kstry 使用文档](http://kstry.cn/doc/specification/quick_start.html)

&nbsp;&nbsp;&nbsp;&nbsp;🟢 [Kstry 使用demo](https://gitee.com/kstry/kstry-demo)

&nbsp;&nbsp;&nbsp;&nbsp;🟢 [Kstry 流程注解+flux 使用demo](https://gitee.com/kstry/kstry-flux-demo)

&nbsp;&nbsp;&nbsp;&nbsp;🟢 [功能测试](https://gitee.com/kstry/kstry-core/tree/master/src/test/java/cn/kstry/framework/test)

## Kstry是什么？

&nbsp;&nbsp;&nbsp;&nbsp;Kstry可以将原本存在于代码中错综复杂的方法调用关系以可视化流程图的形式更直观的展示出来，并提供了将所见的方法节点加以控制的配置手段。框架不能脱离程序执行之外存在，只能在方法与方法的调用中生效和使用，比如某个接口的一次调用。不会像Activiti、Camunda等任务流框架一样，脱离程序执行之外将任务实例存储和管理。

&nbsp;&nbsp;&nbsp;&nbsp;不同使用场景中，因其发挥作用的不同，可以理解成不同的框架，Kstry是：

&nbsp;&nbsp;&nbsp;&nbsp;🟢 【**流程编排框架**】提供所见（ 流程图示 ）即所得（ 代码执行 ）的可视化能力，可自定义流程协议，支持流程配置的热部署

&nbsp;&nbsp;&nbsp;&nbsp;🟢 【**并发框架**】可通过极简操作将流程从串行升级到并行，支持任务拆分、任务重试、任务降级、子任务遍历、指定流程或任务的超时时间

&nbsp;&nbsp;&nbsp;&nbsp;🟢 【**微服务业务整合框架**】支持自定义指令和任务脚本，可负责各种基础能力组件的拼装

&nbsp;&nbsp;&nbsp;&nbsp;🟢 【**轻量的[TMF2.0](https://developer.aliyun.com/ebook/read/796?spm=a2c6h.26392459.ebook-detail.4.65e0407dHAnPgD)框架**】可以通过以下三个步骤来满足同一接口下各类业务对实现功能的不同诉求：抽象能力资源、定义并将抽象出的能力资源授权给业务角色、同一流程的不同场景可分别匹配不同角色再将其下的能力资源任务加以执行

## Kstry有哪些特点？

&nbsp;&nbsp;&nbsp;&nbsp;🟢【**业务可视**】编排好的图示模型即为代码真实的执行链路，通过所见（ *图示模型* ）即所得（ *代码执行* ）的方式在技术和业务之间架起一道通用语言的桥梁，使彼此之间沟通更加顺畅

&nbsp;&nbsp;&nbsp;&nbsp;🟢【**配置灵活**】提供开始事件、结束事件、服务节点、脚本节点、排他网关、包含网关、并行网关、条件表达式、自定义指令、子流程、拦截器等配置组件，可以支持变态复杂的业务流程

&nbsp;&nbsp;&nbsp;&nbsp;🟢【**动态配置**】主流程、子流程、角色、变量等组件支持动态化配置，不启动应用的前提下可以动态变更，动态化配置支持包括开源和公司自研在内的全部存储介质

&nbsp;&nbsp;&nbsp;&nbsp;🟢【**适配度高**】包含BPMN可视化配置文件和代码两套流程定义API，在保证可视化配置的前提下，又支持通过代码方式解析任意格式的流程配置文件，从而结合合适的前端产品搭建个性化的流程配置平台

&nbsp;&nbsp;&nbsp;&nbsp;🟢【**轻松运维**】服务节点支持定义超时时间、重试次数、失败降级、严格模式、资源迭代等，可满足生产环境下对应用稳定性的严苛要求

&nbsp;&nbsp;&nbsp;&nbsp;🟢【**性能优异**】最底层采用Spring工具集进行服务节点调用，任务执行消耗与Spring切面相当

&nbsp;&nbsp;&nbsp;&nbsp;🟢【**秒变异步**】无缝衔接SpringFlux。无需改动代码，仅仅在并行网关或包含网关上配置 `open-async=true`，即可在保证线程安全的前提下将其后的子链路全部并行化

&nbsp;&nbsp;&nbsp;&nbsp;🟢【**交互顺畅**】引入StoryBus和其中四个数据域的概念。节点之间数据存取交互可以做到安全、灵活、方便

&nbsp;&nbsp;&nbsp;&nbsp;🟢【**业务抽象**】引入资源、权限、角色等概念，构建定制化业务身份，为抽象化业务能力提供技术支持和解决方案

&nbsp;&nbsp;&nbsp;&nbsp;🟢【**流程回溯**】可以零成本记录节点执行顺序、节点耗时、入参、出参、异常信息等重要数据，并支持自定义执行监控日志

## Kstry可以解决哪些问题？

&nbsp;&nbsp;&nbsp;&nbsp;🟢 【**业务模糊**】代码复杂、模型文档更新不及时，致使新同学和非技术同学不能短时间内了解业务现状。技术和非技术间对同一业务理解存在分歧而不自知。甚至业务Owner也不能很流畅的描述出自己所负责的业务

&nbsp;&nbsp;&nbsp;&nbsp;🟢 【**代码杂乱**】项目中涉及到许多领域对象，对象间不仅存在复杂的前后依赖关系还相互掺杂没有明显边界，代码多次迭代后更是混乱不堪难以维护

&nbsp;&nbsp;&nbsp;&nbsp;🟢 【**性能低下**】某业务链路由一系列子任务组成，其中需要并行处理一些耗时长且数据间没有依赖的子任务，但苦于没有精简且无代码侵入的并发框架

&nbsp;&nbsp;&nbsp;&nbsp;🟢 【**平台之殇**】维护平台型产品，为众多上游业务线提供着基础服务，但在短时间内应对各个业务方的定制化需求捉襟见肘，更不知如何做好平台与业务、业务与业务之间的隔离

&nbsp;&nbsp;&nbsp;&nbsp;🟢 【**回溯困难**】业务流转数据状态追踪困难，只存在于线上环境的偶现问题更是难以排查。需要一种可以通过简单操作就能将重要节点数据都保存下来的能力，此能力堪比对链路精细化梳理后的系统性日志打印

&nbsp;&nbsp;&nbsp;&nbsp;🟢 【**测试复杂**】业务场景多样，不乏一些复杂的链路难以被测试覆盖。或者三方数据Mock困难，测试成本居高不下


## 我想为Kstry提交代码
[代码提交步骤](https://gitee.com/kstry/kstry-core/blob/master/doc/join-and-coding.md)

