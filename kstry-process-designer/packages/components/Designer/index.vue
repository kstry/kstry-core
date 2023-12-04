<template>
  <div ref="designerRef" :class="['bpmn-designer', bgClass]"></div>
</template>

<script>
import { debounce } from "min-dash";
import { mapGetters } from "vuex";
import { createNewDiagram } from "@utils/xml";
import { catchError } from "@utils/printCatch";
import moduleAndExtensions from "./moduleAndExtensions";
import initModeler from "./initModeler";

export default {
  name: "BpmnDesigner",
  props: {
    events: {
      type: Array,
      default: () => []
    }
  },
  computed: {
    ...mapGetters(["getEditor", "getModeler", "getModeling"]),
    bgClass() {
      const bg = this.getEditor.bg;
      if (bg === "grid-image") return "designer-with-bg";
      if (bg === "image") return "designer-with-image";
      return "";
    }
  },
  methods: {
    reloadProcess: debounce(async function (setting, oldSetting) {
      const modelerModules = moduleAndExtensions(setting);
      let xmlStr = this.$store.getters["localStoreModule/getLocalBpmnXml"];
      await this.$nextTick();
      const modeler = initModeler(this.$refs.designerRef, modelerModules, this);
      if (!oldSetting || setting.processEngine !== oldSetting.processEngine) {
        await createNewDiagram(modeler, xmlStr);
      } else {
        await createNewDiagram(modeler, xmlStr, setting);
      }
    }, 100)
  },
  watch: {
    getEditor: {
      immediate: true,
      deep: true,
      handler: async function (value, oldValue) {
        try {
          this.reloadProcess(value, oldValue);
        } catch (e) {
          catchError(e);
        }
      }
    }
  }
};
</script>
