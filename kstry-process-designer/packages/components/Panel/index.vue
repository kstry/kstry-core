<template>
  <div ref="panel" class="bpmn-panel">
    <div class="panel-header">
      <bpmn-icon :name="bpmnIconName" />
      <p>{{ bpmnElementName }}</p>
      <p>{{ customTranslate(currentElementType || "Process") }}</p>
    </div>
    <el-collapse>
      <component :is="cp" v-for="cp in this.renderComponents" :key="cp.name" />
    </el-collapse>
  </div>
</template>

<script>
import { debounce } from "min-dash";
import Logger from "@utils/Logger";
import { catchError } from "@utils/printCatch";
import EventEmitter from "@utils/EventEmitter";
import bpmnIcons from "@packages/bpmn-icons";
import getBpmnIconType from "@packages/bpmn-icons/getIconType";
import { customTranslate } from "@packages/additional-modules/Translate";
import { isCanbeConditional, isInstructionsSource } from "@packages/bo-utils/conditionUtil";
import { getModeler } from "@packages/bpmn-utils/BpmnDesignerUtils";
import ElementGenerations from "@packages/components/Panel/components/ElementGenerations";
import ActivityElementCalled from "@packages/components/Panel/components/ActivityElementCalled";
import ElementDocumentations from "@packages/components/Panel/components/ElementDocumentations";
import ElementConditional from "@packages/components/Panel/components/ElementConditional";
import ElementServiceTaskNodeProperties from "@packages/components/Panel/components/ElementServiceTaskNodeProperties";
import ElementIterateProperties from "@packages/components/Panel/components/ElementIterateProperties";
import ElementRunProperties from "@packages/components/Panel/components/ElementRunProperties";
import ElementPrevInstructions from "@packages/components/Panel/components/ElementPrevInstructions";
import ElementPostInstructions from "@packages/components/Panel/components/ElementPostInstructions";
import ElementRateLimitProperties from "@packages/components/Panel/components/ElementRateLimitProperties";
import BpmnIcon from "@packages/components/common/BpmnIcon";
import { is } from "bpmn-js/lib/util/ModelUtil";

export default {
  name: "BpmnPanel",
  components: {
    BpmnIcon,
    ElementGenerations,
    ElementConditional,
    ElementServiceTaskNodeProperties,
    ElementRunProperties,
    ElementPrevInstructions,
    ElementDocumentations,
    ElementRateLimitProperties
  },
  data() {
    return {
      bpmnElementName: "Process",
      bpmnIconName: "Process",
      currentElementType: undefined,
      currentElementId: undefined,
      customTranslate,
      renderComponents: []
    };
  },
  created() {
    EventEmitter.on("modeler-init", (modeler) => {
      // 导入完成后默认选中 process 节点
      modeler.on("import.done", () => {
        this.setCurrentElement(null);
      });
      // 监听选择事件，修改当前激活的元素以及表单
      modeler.on("selection.changed", ({ newSelection }) => {
        this.setCurrentElement(newSelection[0] || null);
      });
      modeler.on("element.changed", ({ element }) => {
        // 保证 修改 "默认流转路径" 等类似需要修改多个元素的事件发生的时候，更新表单的元素与原选中元素不一致。
        if (element && element.id === this.currentElementId) {
          this.setCurrentElement(element);
        }
      });
    });
  },
  mounted() {
    !this.currentElementId && this.setCurrentElement();
  },
  methods: {
    //
    setCurrentElement: debounce(function (element) {
      let activatedElement = element,
        activatedElementTypeName = "";
      if (!activatedElement) {
        const modeler = getModeler();
        activatedElement =
          modeler.get("elementRegistry")?.find((el) => el.type === "bpmn:Process") ||
          modeler.get("elementRegistry")?.find((el) => el.type === "bpmn:Collaboration");

        if (!activatedElement) {
          return catchError("No Element found!");
        }
      }
      activatedElementTypeName = getBpmnIconType(activatedElement);

      this.$store.commit("setElement", { element: activatedElement, id: activatedElement.id });
      this.currentElementId = activatedElement.id;
      this.currentElementType = activatedElement.type.split(":")[1];

      this.bpmnIconName = bpmnIcons[activatedElementTypeName];
      this.bpmnElementName = activatedElementTypeName;

      this.setCurrentComponents(activatedElement);
      EventEmitter.emit("element-update", activatedElement);

      Logger.prettyPrimary("Selected element changed", `ID: ${activatedElement.id} , type: ${activatedElement.type}`);
      Logger.prettyInfo("Selected element businessObject", activatedElement.businessObject);
    }, 100),
    //
    setCurrentComponents(element) {
      // 清空
      this.renderComponents.splice(0, this.renderComponents.length);

      // 重设
      this.renderComponents.push(ElementGenerations);
      this.renderComponents.push(ElementDocumentations);
      isCanbeConditional(element) && this.renderComponents.push(ElementConditional);
      is(element, "bpmn:CallActivity") && this.renderComponents.push(ActivityElementCalled);
      this.renderComponents.push(ElementServiceTaskNodeProperties);
      this.renderComponents.push(ElementRunProperties);
      this.renderComponents.push(ElementRateLimitProperties);
      this.renderComponents.push(ElementIterateProperties);
      isInstructionsSource(element) && this.renderComponents.push(ElementPrevInstructions);
      isInstructionsSource(element) && this.renderComponents.push(ElementPostInstructions);
    }
  }
};
</script>
