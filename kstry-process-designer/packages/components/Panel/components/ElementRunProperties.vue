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
  name: "ElementRunProperties",
  data() {
    return {
      collapseTitle: "运行时",
      useType: "runProperty",
      fileIcon: "Bike"
    };
  },
  components: {
    ExtensionProperties
  }
};
</script>
