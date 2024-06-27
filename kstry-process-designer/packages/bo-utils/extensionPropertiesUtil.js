import { getBusinessObject, is } from "bpmn-js/lib/util/ModelUtil";
import { without } from "min-dash";
import { createModdleElement, getExtensionElementsList } from "@packages/bpmn-utils/BpmnExtensionElements";
import { getModeler, getProcessEngine } from "@packages/bpmn-utils/BpmnDesignerUtils";

/////// 功能函数
export function getExtensionProperties(element) {
  const businessObject = getRelevantBusinessObject(element);

  if (!businessObject) return [];
  return getPropertiesList(businessObject) || [];
}

export function addExtensionProperty(element, property) {
  try {
    const modeling = getModeler.getModeling();
    const prefix = getProcessEngine();

    const businessObject = getRelevantBusinessObject(element);

    // 判断 extensionElements
    let extensionElements = businessObject.get("extensionElements");
    if (!extensionElements) {
      extensionElements = createModdleElement("bpmn:ExtensionElements", { values: [] }, businessObject);
      modeling.updateModdleProperties(element, businessObject, { extensionElements });
    }
    // 判断 extensionElements 是否有 properties
    let properties = getProperties(businessObject);
    if (!properties) {
      properties = createModdleElement(`${prefix}:Properties`, { values: [] }, extensionElements);
      modeling.updateModdleProperties(element, extensionElements, {
        values: [...extensionElements.get("values"), properties]
      });
    }
    // 创建新属性并添加
    const newProperty = createModdleElement(`${prefix}:Property`, property, properties);
    modeling.updateModdleProperties(element, properties, {
      values: [...properties?.get("values"), newProperty]
    });
  } catch (e) {
    console.log(e);
  }
}

export function getExecutionProperties(element, currentUseType) {
  return [
    {
      name: "task-component",
      desc: "组件名称",
      explain: "对应@TaskComponent注解的name属性，指定组件类位置，允许为空",
      contentType: "text",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "nodeProperty"
    },
    {
      name: "task-service",
      desc: "服务名称",
      explain: "对应@TaskService注解的name属性，指定服务节点对应的节点方法，使用指令时允许为空",
      contentType: "text",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "nodeProperty"
    },
    {
      name: "task-property",
      desc: "节点属性",
      explain:
        "自定义当前服务节点/指令的属性值，在节点方法中可通过ScopeDataOperator对象的getTaskProperty方法获取。需要多个属性时可以定义JSON",
      contentType: "textarea",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task", "bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "nodeProperty"
    },
    {
      name: "task-params",
      desc: "节点入参",
      explain: "必须是JSON格式。在注解方式解析完节点方法入参之后生效，用来自定义节点方法入参",
      contentType: "textarea",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task", "bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "nodeProperty"
    },
    {
      name: "allow-absent",
      desc: "允许服务为空",
      explain: "是否允许在代码中没有与配置中的服务节点相对应的节点方法，默认false，意指必须存在匹配的节点方法",
      contentType: "boolean",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task", "bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "nodeProperty"
    },
    {
      name: "midway-start-id",
      desc: "中途开始节点Id",
      explain: "指定中途开始节点Id。当request中指定的midwayStartId属性与该值相同时，流程会从当前包含网关开始运行",
      contentType: "text",
      allowElements: ["bpmn:InclusiveGateway"],
      useType: "nodeProperty"
    },
    {
      name: "task-demotion",
      desc: "节点降级",
      explain: "指定服务节点执行失败（包含重试）或超时后的降级处理表达式。异常发生后会根据表达式从容器中找到对应的服务节点执行降级。表达式举例： 1> pr:risk-control@check-img  服务组件名是 risk-control 且 服务名是 init-base-info 的服务节点   2> pr:risk-control@check-img@triple  服务组件名是 risk-control 且 服务名是 init-base-info 且 能力点名是 triple 的服务节点",
      contentType: "text",
      allowElements:  ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "nodeProperty"
    },
    {
      name: "task-demotion",
      desc: "节点降级",
      explain: "指定指令执行失败（包含重试）或超时后的降级处理表达式。异常发生后会根据表达式从容器中找到对应的服务节点执行降级。表达式举例： 1> pr:risk-control@check-img  服务组件名是 risk-control 且 服务名是 init-base-info 的服务节点   2> pr:risk-control@check-img@triple  服务组件名是 risk-control 且 服务名是 init-base-info 且 能力点名是 triple 的服务节点",
      contentType: "text",
      allowElements:  ["bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "nodeProperty"
    },
    {
      name: "not-skip-task",
      desc: "条件表达式",
      explain:
        "控制当前服务节点及指令是否不执行直接跳过的条件表达式，默认为空，代表不跳过",
      contentType: "text",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "runProperty"
    },
    {
      name: "not-skip-task",
      desc: "条件表达式",
      explain:
        "控制指令是否不执行直接跳过的条件表达式，默认为空，代表不跳过",
      contentType: "text",
      allowElements: ["bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "runProperty"
    },
    {
      name: "retry-times",
      desc: "重试次数",
      explain:
        "节点方法执行失败或者超时后进行重试的次数。默认：0 意指执行失败不重试。假设设置2，代表执行失败或者超时后再重试两次",
      contentType: "text",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task", "bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "runProperty"
    },
    {
      name: "timeout",
      desc: "超时时间",
      explain: "节点方法执行时被给予的超时时间，默认：-1 意指不设置超时时间。时间单位：ms",
      contentType: "text",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task", "bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "runProperty"
    },
    {
      name: "strict-mode",
      desc: "严格模式",
      explain:
        "节点方法彻底执行失败（重试、降级均失败）之后是否跳过当前错误节点继续执行，默认true，意指不会跳过错误节点，直接抛出异常结束",
      contentType: "boolean",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task", "bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "runProperty"
    },
    {
      name: "timeout",
      desc: "超时时间",
      explain: "子流程执行时被给予的超时时间，默认：-1 意指不设置超时时间。时间单位：ms",
      contentType: "text",
      allowElements: ["bpmn:SubProcess", "bpmn:CallActivity"],
      useType: "runProperty"
    },
    {
      name: "strict-mode",
      desc: "严格模式",
      explain: "子流程执行失败之后是否跳过当前子流程继续执行，默认true，意指不会跳过错误子流程，直接抛出异常结束",
      contentType: "boolean",
      allowElements: ["bpmn:SubProcess", "bpmn:CallActivity"],
      useType: "runProperty"
    },
    {
      name: "open-async",
      desc: "开启并发",
      explain:
        "包含网关后面的线路是否开启并发，默认：false 不开启。改为true时网关后面的线路会被逐一包装成异步任务提交给线程池执行。直到并发任务遇到包含网关、并行网关、结束事件时才会重新聚合成串行",
      contentType: "boolean",
      allowElements: ["bpmn:InclusiveGateway"],
      useType: "runProperty"
    },
    {
      name: "open-async",
      desc: "开启并发",
      explain:
        "并行网关后面的线路是否开启并发，默认：false 不开启。改为true时网关后面的线路会被逐一包装成异步任务提交给线程池执行。直到并发任务遇到包含网关、并行网关、结束事件时才会重新聚合成串行",
      contentType: "boolean",
      allowElements: ["bpmn:ParallelGateway"],
      useType: "runProperty"
    },
    {
      name: "completed-count",
      desc: "完成任务数",
      explain:
        "当完成包含网关前的completed-count指定的任务数时，流程将继续向后执行，后面再有完成的任务到达时会被包含网关终止",
      contentType: "text",
      allowElements: ["bpmn:InclusiveGateway"],
      useType: "runProperty"
    },
    {
      name: "strict-mode",
      desc: "严格模式",
      explain:
        "执行时是否严格限制实际到达并行网关的线路数量与配置侧并行网关入度数量必须一致，默认true，意指并行网关实际入度与配置中入度数量必须一致，否则将报错结束",
      contentType: "boolean",
      allowElements: ["bpmn:ParallelGateway"],
      useType: "runProperty"
    },
    {
      name: "limit-name",
      desc: "限流器名称",
      explain:
        "当前服务节点及指令使用的限流器名称，默认值：local_single_node_rate_limiter 代表使用框架默认的本地单实例限流器，支持定制化",
      contentType: "text",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "rateLimitProperty"
    },
    {
      name: "limit-permits",
      desc: "令牌数量",
      explain:
        "当前服务节点及指令运行过程中每秒可获得的令牌数量，支持小数，默认值：-1 代表不限流，设置为 0 时代表熔断当前服务节点",
      contentType: "text",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "rateLimitProperty"
    },
    {
      name: "limit-warmup-period",
      desc: "预热时间",
      explain:
        "当前服务节点及指令限流器生效前的预热时间，单位ms，默认值：0 代表无预热",
      contentType: "text",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "rateLimitProperty"
    },
    {
      name: "limit-acquire-timeout",
      desc: "等待时长",
      explain:
        "当前服务节点及指令运行过程中获取令牌的最大等待时长，单位ms，默认值：0 代表不等待",
      contentType: "text",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "rateLimitProperty"
    },
    {
      name: "limit-fail-strategy",
      desc: "失败策略",
      explain:
        "当前服务节点及指令运行过程中获取令牌失败后所执行的策略，默认值：exception 代表抛出异常。框架另外提供了两种策略：【demotion】执行服务节点降级方法（如果配置了）、【ignore】跳过当前节点继续向下执行。支持定制化",
      contentType: "text",
      allowElements:["bpmn:ServiceTask", "bpmn:Task"],
      useType: "rateLimitProperty"
    },
    {
      name: "limit-expression",
      desc: "条件表达式",
      explain:
        "当前服务节点及指令限流器是否生效的条件表达式，默认为空，代表直接生效",
      contentType: "text",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "rateLimitProperty"
    },
    {
      name: "limit-name",
      desc: "限流器名称",
      explain:
        "指令使用的限流器名称，默认值：local_single_node_rate_limiter 代表使用框架默认的本地单实例限流器，支持定制化",
      contentType: "text",
      allowElements: ["bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "rateLimitProperty"
    },
    {
      name: "limit-permits",
      desc: "令牌数量",
      explain:
        "指令运行过程中每秒可获得的令牌数量，支持小数，默认值：-1 代表不限流，设置为 0 时代表熔断当前服务节点",
      contentType: "text",
      allowElements: ["bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "rateLimitProperty"
    },
    {
      name: "limit-warmup-period",
      desc: "预热时间",
      explain:
        "指令限流器生效前的预热时间，单位ms，默认值：0 代表无预热",
      contentType: "text",
      allowElements: ["bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "rateLimitProperty"
    },
    {
      name: "limit-acquire-timeout",
      desc: "等待时长",
      explain:
        "指令运行过程中获取令牌的最大等待时长，单位ms，默认值：0 代表不等待",
      contentType: "text",
      allowElements: ["bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "rateLimitProperty"
    },
    {
      name: "limit-fail-strategy",
      desc: "失败策略",
      explain:
        "指令运行过程中获取令牌失败后所执行的策略，默认值：exception 代表抛出异常。框架另外提供了两种策略：【demotion】执行服务节点降级方法（如果配置了）、【ignore】跳过当前节点继续向下执行。支持定制化",
      contentType: "text",
      allowElements: ["bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "rateLimitProperty"
    },
    {
      name: "limit-expression",
      desc: "条件表达式",
      explain:
        "指令限流器是否生效的条件表达式，默认为空，代表直接生效",
      contentType: "text",
      allowElements: ["bpmn:InclusiveGateway", "bpmn:ExclusiveGateway"],
      useType: "rateLimitProperty"
    },
    {
      name: "ite-source",
      desc: "迭代资源",
      explain:
        "指定StoryBus中某个集合字段的位置表达式，集合获取成功且不为空时，会对集合中每一个元素依次执行节点方法。节点方法中可通过直接定义IterDataItem入参来获取迭代信息。迭代步长大于1时节点方法返回值必须是List类型。迭代结果与入参顺序相同，同样是List类型",
      contentType: "text",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "iterateProperty"
    },
    {
      name: "ite-async",
      desc: "迭代开启并发",
      explain: "遍历集合资源时是否开启并发，默认false 意指不开启",
      contentType: "boolean",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "iterateProperty"
    },
    {
      name: "ite-strategy",
      desc: "迭代策略",
      explain:
        "迭代资源时所采用的策略。支持all(每一项都必须成功)、any(只要有一项能成功，就结束迭代)、best(执行失败会被忽略，尽量多的拿到成功项)三种迭代策略",
      contentType: "strategyEnums",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "iterateProperty"
    },
    {
      name: "ite-stride",
      desc: "单次迭代步长",
      explain:
        "单次迭代时要处理的元素个数。默认为1，意指每次处理一个元素。当指定大于1的数值时，单次迭代项将变成装载了元素的List，List元素数与指定步长相同，末位List元素数可能会小于步长",
      contentType: "text",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "iterateProperty"
    },
    {
      name: "ite-align-index",
      desc: "出入参对齐",
      explain:
        "迭代时，是否将返回值、入参两集合中的索引进行一一对应，比如：入参5个元素，经过处理后出参有三个元素有值，另外两个为null。选择出入参对齐，返回结果将是包含有null的5个元素的List，反之将返回包含有三个结果元素的List。默认false意指不进行索引对齐",
      contentType: "boolean",
      allowElements: ["bpmn:ServiceTask", "bpmn:Task"],
      useType: "iterateProperty"
    },
    {
      name: "ite-source",
      desc: "迭代资源",
      explain: "设置子流程中所有服务节点的ite-source属性",
      contentType: "text",
      allowElements: ["bpmn:SubProcess", "bpmn:CallActivity"],
      useType: "iterateProperty"
    },
    {
      name: "ite-async",
      desc: "迭代开启并发",
      explain: "设置子流程中所有服务节点的ite-async属性",
      contentType: "boolean",
      allowElements: ["bpmn:SubProcess", "bpmn:CallActivity"],
      useType: "iterateProperty"
    },
    {
      name: "ite-strategy",
      desc: "迭代策略",
      explain: "设置子流程中所有服务节点的ite-strategy属性",
      contentType: "strategyEnums",
      allowElements: ["bpmn:SubProcess", "bpmn:CallActivity"],
      useType: "iterateProperty"
    },
    {
      name: "ite-stride",
      desc: "单次迭代步长",
      explain: "设置子流程中所有服务节点的ite-stride属性",
      contentType: "text",
      allowElements: ["bpmn:SubProcess", "bpmn:CallActivity"],
      useType: "iterateProperty"
    },
    {
      name: "ite-align-index",
      desc: "出入参对齐",
      explain: "设置子流程中所有服务节点的ite-align-index属性",
      contentType: "boolean",
      allowElements: ["bpmn:SubProcess", "bpmn:CallActivity"],
      useType: "iterateProperty"
    }
  ].filter((p) => p.useType === currentUseType && p.allowElements.filter((e) => is(element, e)).length > 0);
}

export function removeExtensionProperty(element, property) {
  const businessObject = getRelevantBusinessObject(element);
  const extensionElements = businessObject.get("extensionElements");
  const properties = getProperties(businessObject);
  if (!properties) return;

  const modeling = getModeler.getModeling();

  const values = without(properties.get("values"), property);
  modeling.updateModdleProperties(element, properties, { values });

  if (!values || !values.length) {
    modeling.updateModdleProperties(element, extensionElements, {
      values: without(extensionElements.get("values"), properties)
    });
  }
}

///// helpers
function getRelevantBusinessObject(element) {
  const businessObject = getBusinessObject(element);
  if (is(element, "bpmn:Participant")) {
    return businessObject.get("processRef");
  }
  return businessObject;
}

function getPropertiesList(bo) {
  const properties = getProperties(bo);
  return properties && properties.get("values");
}

function getProperties(bo) {
  return getExtensionElementsList(bo)[0];
}
