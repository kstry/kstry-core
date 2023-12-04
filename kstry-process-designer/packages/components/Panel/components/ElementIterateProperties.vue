<template>
  <extension-properties
    :collapseTitle="collapseTitle"
    :fileIcon="fileIcon"
    :tableHeight="tableHeight"
    :useType="useType"
  />
</template>

<script>
import ExtensionProperties from "@packages/components/Panel/components/SubChild/ExtensionProperties";

export default {
  name: "ElementIterateProperties",
  data() {
    return {
      collapseTitle: "资源遍历",
      useType: "iterateProperty",
      fileIcon: "Recycle"
    };
  },
  components: {
    ExtensionProperties
  }
};
</script>
