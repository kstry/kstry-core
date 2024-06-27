<template>
  <extension-instructions :file-icon="fileIcon" :prefix="prefix" :title-name="titleName" :use-type="useType" />
</template>

<script>
import ExtensionInstructions from "@packages/components/Panel/components/SubChild/ExtensionInstructions";

export default {
  name: "ElementPrevInstructions",
  data() {
    return {
      useType: "prevInstruct",
      fileIcon: "ArrowRightToLine",
      titleName: "前置指令",
      prefix: "^c-"
    };
  },
  components: {
    ExtensionInstructions
  }
};
</script>
