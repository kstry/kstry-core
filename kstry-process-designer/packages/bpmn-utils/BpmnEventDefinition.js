import { getBusinessObject, is } from "bpmn-js/lib/util/ModelUtil";
import { find } from "min-dash";

export function getEventDefinition(element, eventType) {
  const businessObject = getBusinessObject(element);

  const eventDefinitions = businessObject.get("eventDefinitions") || [];

  return find(eventDefinitions, function (definition) {
    return is(definition, eventType);
  });
}
