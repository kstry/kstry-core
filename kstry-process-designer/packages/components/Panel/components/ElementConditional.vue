<template>
  <el-collapse-item name="element-conditional">
    <template #title>
      <collapse-title title="条件设置">
        <lucide-icon name="ArrowLeftRight" />
      </collapse-title>
    </template>
    <div class="element-conditional">
      <edit-item key="condition" :label-width="120" label="条件类型">
        <el-select v-model="cType" @change="setElementConditionType">
          <el-option v-for="item in conditionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </edit-item>
      <edit-item v-if="!!cType && cType === 'expression'" key="expression" :label-width="120" label="条件内容">
        <el-input v-model="exp" @change="setConditionExpression" />
      </edit-item>
    </div>
  </el-collapse-item>
</template>

<script>
import * as CU from "@packages/bo-utils/conditionUtil";
import EventEmitter from "@utils/EventEmitter";
import { getActive } from "@packages/bpmn-utils/BpmnDesignerUtils";

export default {
  name: "ElementConditional",
  data() {
    return {
      exp: "",
      cType: null,
      conditionTypeOptions: []
    };
  },

  mounted() {
    this.conditionTypeOptions = CU.getConditionTypeOptions(getActive());
    this.getConditionTypeValue();
    this.getConditionExpression();

    EventEmitter.on("element-update", () => {
      this.conditionTypeOptions = CU.getConditionTypeOptions(getActive());
      this.getConditionTypeValue();
      this.getConditionExpression();
    });
  },
  methods: {
    getConditionExpression() {
      this.exp = CU.getConditionExpressionValue(getActive());
    },
    getConditionTypeValue() {
      this.cType = CU.getConditionTypeValue(getActive());
    },
    setElementConditionType(value) {
      CU.setConditionTypeValue(getActive(), value);
    },
    setConditionExpression(value) {
      CU.setConditionExpressionValue(getActive(), value);
    }
  }
};
</script>
