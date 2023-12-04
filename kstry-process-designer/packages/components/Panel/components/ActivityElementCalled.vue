<template>
  <el-collapse-item name="activity-called">
    <template #title>
      <collapse-title title="流程关联">
        <lucide-icon name="Cable" />
      </collapse-title>
    </template>

    <edit-item label="Called">
      <el-input
        v-model="calledElement"
        maxlength="200"
        placeholder="请输入需要关联的子流程ID"
        @change="updateCalledElement"
      />
    </edit-item>
  </el-collapse-item>
</template>

<script>
import { getCalledElement, updateCalledElement } from "@packages/bo-utils/nameUtil";
import EventEmitter from "@utils/EventEmitter";
import { getActive } from "@packages/bpmn-utils/BpmnDesignerUtils";

export default {
  name: "ActivityElementCalled",
  data() {
    return {
      calledElement: ""
    };
  },

  mounted() {
    this.reloadGenerationData();
    EventEmitter.on("element-update", this.reloadGenerationData);
  },
  methods: {
    reloadGenerationData() {
      this.calledElement = getCalledElement(getActive()) || "";
    },
    updateCalledElement(value) {
      updateCalledElement(getActive(), value);
    }
  }
};
</script>
