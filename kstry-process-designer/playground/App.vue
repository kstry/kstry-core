<template>
  <div id="app">
    <bpmn-toolbar v-if="getEditorConfig.toolbar" />
    <div class="main-content">
      <bpmn-designer :xml.sync="xmlString" />
      <bpmn-panel v-if="getEditorConfig.penalMode === 'custom'" />
      <div v-else id="camunda-panel" class="camunda-panel"></div>
    </div>
  </div>
</template>

<script>
import BpmnDesigner from "../packages/components/Designer";
import { mapGetters } from "vuex";
import BpmnToolbar from "../packages/components/Toolbar";
import BpmnPanel from "@packages/components/Panel";

export default {
  name: "App",
  components: { BpmnPanel, BpmnToolbar, BpmnDesigner },
  data() {
    return {
      xmlString: undefined
    };
  },
  computed: {
    ...mapGetters(["getEditorConfig"])
  },
  mounted() {
    document.body.addEventListener("contextmenu", function (ev) {
      ev.preventDefault();
    });
  }
};
</script>
