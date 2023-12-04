<template>
  <extension-instructions :file-icon="fileIcon" :prefix="prefix" :title-name="titleName" :use-type="useType" />
</template>

<script>
import ExtensionInstructions from "@packages/components/Panel/components/SubChild/ExtensionInstructions";

export default {
  name: "ElementPostInstructions",
  data() {
    return {
      useType: "postInstruct",
      fileIcon: "ArrowRightFromLine",
      titleName: "后置指令",
      prefix: "c-"
    };
  },
  components: {
    ExtensionInstructions
  }
};
</script>
