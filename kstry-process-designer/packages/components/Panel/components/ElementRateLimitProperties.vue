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
  name: "ElementRateLimitProperties",
  data() {
    return {
      collapseTitle: "限流配置",
      useType: "rateLimitProperty",
      fileIcon: "FilterX"
    };
  },
  components: {
    ExtensionProperties
  }
};
</script>
