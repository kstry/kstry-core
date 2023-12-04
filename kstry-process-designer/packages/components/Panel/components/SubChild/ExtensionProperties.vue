<template>
  <el-collapse-item v-if="!!properties && properties.length > 0" :name="'element-extension-' + useType">
    <template #title>
      <collapse-title :title="collapseTitle">
        <lucide-icon :name="fileIcon" />
      </collapse-title>
      <number-tag :value="properties.length" margin-left="12px" />
    </template>
    <div class="element-extension-properties">
      <el-table :data="properties" border style="width: 100%">
        <el-table-column label="属性名" show-overflow-tooltip>
          <template v-slot="scope">
            <el-tooltip :content="scope.row.explain" class="item" effect="light" placement="top">
              <lucide-icon name="HelpCircle" style="margin-right: 10px; vertical-align: middle" />
            </el-tooltip>
            <span>{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="属性值" prop="value" show-overflow-tooltip />
        <el-table-column label="编辑" width="140">
          <template v-slot="{ row, $index }">
            <el-button type="text" @click="openPropertyModel($index, row)">
              <lucide-icon :size="13" name="Settings" style="vertical-align: bottom" />
              编辑
            </el-button>
            <el-button v-if="!!row.value" class="copy-value-content" type="text" @click="copyValueContent(row.value)">
              <lucide-icon :size="13" name="Copy" style="vertical-align: bottom" />
              复制
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :visible.sync="modelVisible" append-to-body destroy-on-close title="属性编辑" width="640px">
      <el-form ref="formRef" :model="editProperty" :rules="formRules" aria-modal="true" class="need-filled">
        <el-form-item :label="editProperty.desc + '（' + editProperty.name + '）'" path="class" prop="value">
          <el-alert :closable="false" :title="editProperty.explain" style="margin-bottom: 5px" type="success" />
          <el-input
            v-if="editProperty.contentType === 'text' || editProperty.contentType === 'textarea'"
            v-model="editProperty.value"
            :rows="6"
            :type="editProperty.contentType"
            @keydown.enter.prevent
          />
          <el-select v-if="editProperty.contentType === 'boolean'" v-model="editProperty.value">
            <el-option v-for="{ label, value } in boolOptions" :key="value" :label="label" :value="value" />
          </el-select>
          <el-select v-if="editProperty.contentType === 'strategyEnums'" v-model="editProperty.value">
            <el-option v-for="{ label, value } in strategyOptions" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modelVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveExecutionProperty">确 认</el-button>
      </template>
    </el-dialog>
  </el-collapse-item>
</template>

<script>
import {
  addExtensionProperty,
  getExecutionProperties,
  getExtensionProperties,
  removeExtensionProperty
} from "@packages/bo-utils/extensionPropertiesUtil";
import EventEmitter from "@utils/EventEmitter";
import { getActive } from "@packages/bpmn-utils/BpmnDesignerUtils";
import Clipboard from "clipboard";

export default {
  name: "ExtensionProperties",
  props: ["collapseTitle", "useType", "fileIcon"],
  data() {
    return {
      modelVisible: false,
      properties: [],
      boolOptions: [
        { label: "default", value: "default" },
        { label: "true", value: "true" },
        { label: "false", value: "false" }
      ],
      strategyOptions: [
        { label: "default", value: "default" },
        { label: "all", value: "all" },
        { label: "any", value: "any" },
        { label: "best", value: "best" }
      ],
      editProperty: {},
      formRules: {},
      tableHeight2: 0
    };
  },
  mounted() {
    this.reloadExtensionProperties();
    EventEmitter.on("element-update", this.reloadExtensionProperties);
  },
  methods: {
    copyValueContent(val) {
      let codeContent = val;
      let clipboard = new Clipboard(".copy-value-content", {
        text: () => {
          return codeContent;
        }
      });
      clipboard.on("success", () => {
        this.$message.success("复制成功！");
        clipboard.destroy();
      });
      clipboard.on("error", () => {
        this.$message.error("该浏览器不支持自动复制,请手动复制！");
        clipboard.destroy();
      });
    },
    reloadExtensionProperties() {
      this.modelVisible = false;
      let _extensionsRaw = getExtensionProperties(getActive());
      this.properties = getExecutionProperties(getActive(), this.useType);
      this.properties.forEach((l) => {
        _extensionsRaw.forEach((e) => {
          if (l.name === e.name) {
            l.value = e.value;
          }
        });
      });
    },

    async saveExecutionProperty() {
      await this.$refs.formRef.validate();
      let _extensionsRaw = getExtensionProperties(getActive());
      _extensionsRaw.forEach((e) => {
        if (this.editProperty && e.name === this.editProperty.name) {
          removeExtensionProperty(getActive(), e);
        }
      });

      let isDef =
        (this.editProperty.contentType === "boolean" || this.editProperty.contentType === "strategyEnums") &&
        this.editProperty.value === "default";
      if (isDef || this.editProperty.value === "") {
        this.reloadExtensionProperties();
        return;
      }
      addExtensionProperty(getActive(), { name: this.editProperty.name, value: this.editProperty.value });
      this.reloadExtensionProperties();
    },

    async openPropertyModel(index, prop) {
      this.activeIndex = index;
      this.modelVisible = true;
      prop && (this.editProperty = JSON.parse(JSON.stringify(prop)));
      await this.$nextTick();
      this.$refs.formRef && this.$refs.formRef.clearValidate();
    }
  }
};
</script>
