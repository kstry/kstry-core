import { catchError, catchWarning } from "./printCatch";

export function initXML(key, name) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" id="Definitions_0001" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="${key}" name="${name}" isExecutable="true">
    <bpmn:startEvent id="startEventId001" name="开始事件">
      <bpmn:outgoing>Flow_0sfx9m5</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:serviceTask id="Activity_1arlfr7" name="服务节点">
      <bpmn:extensionElements>
        <camunda:properties>
          <camunda:property name="task-component" value="className" />
          <camunda:property name="task-service" value="methodName" />
        </camunda:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0sfx9m5</bpmn:incoming>
      <bpmn:outgoing>Flow_0in4yk6</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:sequenceFlow id="Flow_0sfx9m5" sourceRef="startEventId001" targetRef="Activity_1arlfr7" />
    <bpmn:endEvent id="Event_1bizken">
      <bpmn:incoming>Flow_0in4yk6</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_0in4yk6" sourceRef="Activity_1arlfr7" targetRef="Event_1bizken" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${key}">
      <bpmndi:BPMNShape id="Event_1hogd19_di" bpmnElement="startEventId001">
        <dc:Bounds x="472" y="302" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="468" y="345" width="44" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1arlfr7_di" bpmnElement="Activity_1arlfr7">
        <dc:Bounds x="560" y="280" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_1bizken_di" bpmnElement="Event_1bizken">
        <dc:Bounds x="712" y="302" width="36" height="36" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_0sfx9m5_di" bpmnElement="Flow_0sfx9m5">
        <di:waypoint x="508" y="320" />
        <di:waypoint x="560" y="320" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0in4yk6_di" bpmnElement="Flow_0in4yk6">
        <di:waypoint x="660" y="320" />
        <di:waypoint x="712" y="320" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;
}

export async function createNewDiagram(modeler, newXml, settings) {
  try {
    const timestamp = Date.now();
    const { processId, processName } = settings || {};
    const newId = processId ? processId : `Process_${timestamp}`;
    const newName = processName || `业务流程_${timestamp}`;
    const xmlString = newXml || initXML(newId, newName);
    const { warnings } = await modeler.importXML(xmlString);
    if (warnings && warnings.length) {
      warnings.forEach(catchWarning);
    }
  } catch (e) {
    catchError(e);
  }
}
