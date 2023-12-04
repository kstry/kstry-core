<template>
  <el-collapse-item :class="'element-extension-' + this.useType">
    <template #title>
      <collapse-title :title="titleName">
        <lucide-icon :name="fileIcon" />
      </collapse-title>
      <number-tag :value="instructions.length" margin-left="12px" />
    </template>
    <div class="element-extension-properties">
      <el-table :data="instructions" border height="290px" style="width: 100%">
        <el-table-column label="No" type="index" width="50" />
        <el-table-column label="Name" prop="name" show-overflow-tooltip />
        <el-table-column label="Content" prop="value" show-overflow-tooltip />
        <el-table-column label="操作" width="140">
          <template v-slot="{ row, $index }">
            <el-button type="text" @click="openPropertyModel($index, row)">
              <lucide-icon :size="13" name="Settings" style="vertical-align: bottom" />
              编辑
            </el-button>
            <el-button type="text" @click="removeProperty($index)">
              <lucide-icon :size="13" name="Trash2" style="vertical-align: bottom" />
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button class="inline-large-button" icon="el-icon-plus" type="primary" @click="openPropertyModel(-1)">
        添加指令
      </el-button>
    </div>

    <el-dialog
      :title="(this.activeIndex === -1 ? '添加' : '修改') + titleName"
      :visible.sync="modelVisible"
      append-to-body
      destroy-on-close
      width="640px"
    >
      <el-form ref="formRef" :model="editProperty" :rules="rules" aria-modal="true">
        <el-form-item label="指令名称( Name )" path="name" prop="name">
          <el-input v-model="editProperty.name" @keydown.enter.prevent />
        </el-form-item>
        <el-form-item label="指令值( Content )" path="value">
          <el-input v-model="editProperty.value" :rows="6" type="textarea" @keydown.enter.prevent />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modelVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveProperty">确 认</el-button>
      </template>
    </el-dialog>
  </el-collapse-item>
</template>

<script>
import {
  addExtensionProperty,
  getExtensionProperties,
  removeExtensionProperty
} from "@packages/bo-utils/extensionPropertiesUtil";
import EventEmitter from "@utils/EventEmitter";
import { getActive } from "@packages/bpmn-utils/BpmnDesignerUtils";

export default {
  name: "ExtensionInstructions",
  props: ["prefix", "titleName", "useType", "fileIcon"],
  data() {
    return {
      activeIndex: -1,
      instructions: [],
      editProperty: { name: "", value: "" },
      rules: {
        name: { required: true, message: "指令名称不能为空", trigger: ["blur", "change"] }
      },
      modelVisible: false
    };
  },
  mounted() {
    this.reloadExtensionProperties();
    EventEmitter.on("element-update", this.reloadExtensionProperties);
  },
  methods: {
    async reloadExtensionProperties() {
      this.modelVisible = false;
      await this.$nextTick();
      this.editProperty = { name: "", value: "" };
      this._extensionsRaw = getExtensionProperties(getActive()).filter((r) => r.name.startsWith(this.prefix));
      this.instructions = JSON.parse(JSON.stringify(this._extensionsRaw));
    },
    removeProperty(propIndex) {
      removeExtensionProperty(getActive(), this._extensionsRaw[propIndex]);
      this.reloadExtensionProperties();
    },
    async saveProperty() {
      await this.$refs.formRef.validate();
      if (this.activeIndex === -1) {
        addExtensionProperty(getActive(), {
          name: this.prefix + this.editProperty.name,
          value: this.editProperty.value
        });
      } else {
        for (let i = 0; i < this.instructions.length; i++) {
          let inst = this.instructions[i];
          this.removeProperty(i);
          if (i !== this.activeIndex) {
            addExtensionProperty(getActive(), {
              name: inst.name,
              value: inst.value
            });
          } else {
            addExtensionProperty(getActive(), {
              name: this.prefix + this.editProperty.name,
              value: this.editProperty.value
            });
          }
        }
      }
      await this.reloadExtensionProperties();
    },
    async openPropertyModel(index, prop) {
      this.activeIndex = index;
      this.modelVisible = true;
      if (index !== -1) {
        prop && (this.editProperty = JSON.parse(JSON.stringify(prop)));
        this.editProperty.name = this.editProperty.name.substring(this.prefix.length);
      } else {
        this.editProperty = { name: "", value: "" };
      }
      await this.$nextTick();
      this.$refs.formRef && this.$refs.formRef.clearValidate();
    }
  }
};
</script>
