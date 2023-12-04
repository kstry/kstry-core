import PaletteProvider from "bpmn-js/lib/features/palette/PaletteProvider";
import { assign } from "min-dash";
import { createAction } from "../utils";

class RewritePaletteProvider extends PaletteProvider {
  constructor(palette, create, elementFactory, spaceTool, lassoTool, handTool, globalConnect) {
    super(palette, create, elementFactory, spaceTool, lassoTool, handTool, globalConnect, 2000);
    this._create = create;
    this._elementFactory = elementFactory;
    this._lassoTool = lassoTool;
    this._handTool = handTool;
    this._globalConnect = globalConnect;
  }

  getPaletteEntries() {
    const actions = {},
      create = this._create,
      elementFactory = this._elementFactory,
      lassoTool = this._lassoTool,
      handTool = this._handTool,
      globalConnect = this._globalConnect;

    function createParticipant(event) {
      create.start(event, elementFactory.createParticipantShape());
    }

    function createSubprocess(event) {
      const subProcess = elementFactory.createShape({
        type: "bpmn:SubProcess",
        x: 0,
        y: 0,
        isExpanded: true
      });

      const startEvent = elementFactory.createShape({
        type: "bpmn:StartEvent",
        x: 40,
        y: 82,
        parent: subProcess
      });

      create.start(event, [subProcess, startEvent], {
        hints: {
          autoSelect: [startEvent]
        }
      });
    }

    assign(actions, {
      "hand-tool": {
        group: "tools",
        className: "bpmn-icon-hand-tool",
        title: "手型工具",
        action: {
          click: function (event) {
            handTool.activateHand(event);
          }
        }
      },
      "lasso-tool": {
        group: "tools",
        className: "bpmn-icon-lasso-tool",
        title: "套索工具",
        action: {
          click: function (event) {
            lassoTool.activateSelection(event);
          }
        }
      },
      "global-connect-tool": {
        group: "tools",
        className: "bpmn-icon-connection-multi",
        title: "全局连线",
        action: {
          click: function (event) {
            globalConnect.toggle(event);
          }
        }
      },
      "tool-separator": {
        group: "tools",
        separator: true
      },
      "create.start-event": createAction(
        elementFactory,
        create,
        "bpmn:StartEvent",
        "events",
        "bpmn-icon-start-event-none",
        "开始事件"
      ),
      "create.end-event": createAction(
        elementFactory,
        create,
        "bpmn:EndEvent",
        "events",
        "bpmn-icon-end-event-none",
        "结束事件"
      ),
      "events-separator": {
        group: "events",
        separator: true
      },
      "create.exclusive-gateway": createAction(
        elementFactory,
        create,
        "bpmn:ExclusiveGateway",
        "gateway",
        "bpmn-icon-gateway-xor",
        "排他网关"
      ),
      "create.parallel-gateway": createAction(
        elementFactory,
        create,
        "bpmn:ParallelGateway",
        "gateway",
        "bpmn-icon-gateway-parallel",
        "并行网关"
      ),
      "create.inclusive-gateway": createAction(
        elementFactory,
        create,
        "bpmn:InclusiveGateway",
        "gateway",
        "bpmn-icon-gateway-or",
        "包含网关"
      ),
      "gateway-separator": {
        group: "gateway",
        separator: true
      },
      "create.service-task": createAction(
        elementFactory,
        create,
        "bpmn:ServiceTask",
        "activity",
        "bpmn-icon-service-task",
        "服务任务"
      ),
      "create.user-task": createAction(
        elementFactory,
        create,
        "bpmn:CallActivity",
        "activity",
        "bpmn-icon-call-activity",
        "子任务调用"
      ),
      "create.subprocess-expanded": {
        group: "activity",
        className: "bpmn-icon-subprocess-expanded",
        title: "子流程",
        action: {
          dragstart: createSubprocess,
          click: createSubprocess
        }
      },
      "create.participant-expanded": {
        group: "collaboration",
        className: "bpmn-icon-participant",
        title: "任务泳道",
        action: {
          dragstart: createParticipant,
          click: createParticipant
        }
      },
      "create.group": createAction(elementFactory, create, "bpmn:Group", "artifact", "bpmn-icon-group", "分组")
    });
    return actions;
  }
}

RewritePaletteProvider.$inject = [
  "palette",
  "create",
  "elementFactory",
  "spaceTool",
  "lassoTool",
  "handTool",
  "globalConnect"
];

export default RewritePaletteProvider;
