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
  name: "ElementServiceTaskNodeProperties",
  data() {
    return {
      collapseTitle: "节点属性",
      useType: "nodeProperty",
      fileIcon: "FileCog"
    };
  },
  components: {
    ExtensionProperties
  }
};
</script>
