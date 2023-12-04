import { getBusinessObject, is, isAny } from "bpmn-js/lib/util/ModelUtil";
import { getEventDefinition } from "@packages/bpmn-utils/BpmnEventDefinition";
import { createModdleElement } from "@packages/bpmn-utils/BpmnExtensionElements";
import { getModeler } from "@packages/bpmn-utils/BpmnDesignerUtils";

///////////////////////////////// 配置项可见性
const CONDITIONAL_SOURCES = [
  "bpmn:StartEvent",
  "bpmn:Activity",
  "bpmn:ExclusiveGateway",
  "bpmn:InclusiveGateway",
  "bpmn:SubProcess",
  "bpmn:Task",
  "bpmn:ServiceTask"
];

const INSTRUCTIONS_SOURCES = ["bpmn:ExclusiveGateway", "bpmn:InclusiveGateway", "bpmn:Task", "bpmn:ServiceTask"];

const defaultConditionTypeOptions = [
  { label: "无条件( None )", value: "none" },
  { label: "条件表达式( Expression )", value: "expression" }
];

// 父节点符合条件的连线
export function isConditionalSource(element) {
  return isAny(element, CONDITIONAL_SOURCES);
}

// 是否是 定义条件的事件 （ 控制变量 Variables 配置 ）
export function isConditionEventDefinition(element) {
  return is(element, "bpmn:Event") && !!getEventDefinition(element, "bpmn:ConditionalEventDefinition");
}

// 元素 是否符合 可以设置条件 的情况
export function isCanbeConditional(element) {
  return (
    (is(element, "bpmn:SequenceFlow") && isConditionalSource(element?.source)) || isConditionEventDefinition(element)
  );
}

export function isInstructionsSource(element) {
  return isAny(element, INSTRUCTIONS_SOURCES);
}

// 3. 元素条件类型
export function getConditionTypeValue(element) {
  const conditionExpression = getConditionExpression(element);
  if (conditionExpression) {
    return conditionExpression.get("language") === undefined ? "expression" : "script";
  }
  if (element.source?.businessObject?.default === element.businessObject) return "default";
  return "none";
}

export function setConditionTypeValue(element, value) {
  if (!value || value === "none" || value === "default") {
    updateCondition(element);
    return setDefaultCondition(element, value === "default");
  }
  const attributes = {
    // body: '',
    language: value === "script" ? "" : undefined
  };
  const parent = is(element, "bpmn:SequenceFlow") ? getBusinessObject(element) : getConditionalEventDefinition(element);
  const formalExpressionElement = createModdleElement("bpmn:FormalExpression", attributes, parent);
  updateCondition(element, formalExpressionElement);
}

// 4. 元素条件表达式
export function getConditionExpressionValue(element) {
  const conditionExpression = getConditionExpression(element);
  if (conditionExpression) {
    return conditionExpression.get("body");
  }
}

export function setConditionExpressionValue(element, body) {
  const parent = is(element, "bpmn:SequenceFlow") ? getBusinessObject(element) : getConditionalEventDefinition(element);
  const formalExpressionElement = createModdleElement("bpmn:FormalExpression", { body }, parent);
  updateCondition(element, formalExpressionElement);
}

///////// helpers
// 获取事件的条件定义
export function getConditionTypeOptions(element) {
  if (is(element, "bpmn:SequenceFlow")) {
    return defaultConditionTypeOptions;
  }
  return defaultConditionTypeOptions.filter((condition) => condition.value !== "default");
}

function getConditionalEventDefinition(element) {
  if (!is(element, "bpmn:Event")) return false;
  return getEventDefinition(element, "bpmn:ConditionalEventDefinition");
}

//获取给定元素的条件表达式的值
function getConditionExpression(element) {
  const businessObject = getBusinessObject(element);
  if (is(businessObject, "bpmn:SequenceFlow")) {
    return businessObject.get("conditionExpression");
  }
  if (getConditionalEventDefinition(businessObject)) {
    return getConditionalEventDefinition(businessObject).get("condition");
  }
}

//
function updateCondition(element, condition) {
  const modeling = getModeler.get("modeling");
  if (is(element, "bpmn:SequenceFlow")) {
    modeling.updateProperties(element, { conditionExpression: condition });
  } else {
    modeling.updateModdleProperties(element, getConditionalEventDefinition(element), { condition });
  }
}

//
function setDefaultCondition(element, isDefault) {
  const modeling = getModeler.get("modeling");
  modeling.updateProperties(element.source, { default: isDefault ? element : undefined });
}
