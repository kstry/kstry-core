<template>
  <el-button type="primary" @click="openImportWindow">
    打开文件
    <input ref="importRef" accept=".xml,.bpmn" style="display: none" type="file" @change="changeImportFile" />
  </el-button>
</template>

<script>
import { mapMutations } from "vuex";
import { catchError } from "@utils/printCatch";
import { getModeler } from "@packages/bpmn-utils/BpmnDesignerUtils";

export default {
  name: "BpmnImport",
  methods: {
    ...mapMutations("localStoreModule", ["changeLocalBpmnXml"]),
    openImportWindow() {
      this.$refs.importRef && this.$refs.importRef.click();
    },
    changeImportFile() {
      try {
        if (this.$refs.importRef && this.$refs.importRef.files) {
          const file = this.$refs.importRef.files[0];
          const reader = new FileReader();
          reader.readAsText(file);
          let that = this;
          reader.onload = function () {
            const xmlStr = this.result;
            getModeler() && getModeler().importXML(xmlStr) && that.changeLocalBpmnXml(xmlStr);
          };
          this.$refs.importRef.value = null;
          this.$refs.importRef.files = null;
        }
      } catch (e) {
        catchError(e);
      }
    }
  }
};
</script>
