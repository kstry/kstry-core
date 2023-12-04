<template>
  <el-button-group>
    <el-button v-r-popover:undo class="el-button__no-padding" @click="undo">
      <lucide-icon :size="16" name="Undo2" />
      <el-popover ref="undo" content="撤销" placement="bottom" popper-class="button-popover" trigger="hover" />
    </el-button>
    <el-button v-r-popover:redo class="el-button__no-padding" @click="redo">
      <lucide-icon :size="16" name="Redo2" />
      <el-popover ref="redo" content="恢复" placement="bottom" popper-class="button-popover" trigger="hover" />
    </el-button>
    <el-button v-r-popover:restart class="el-button__no-padding" @click="preRestart">
      <lucide-icon :size="16" name="Eraser" />
      <el-popover ref="restart" content="擦除重做" placement="bottom" popper-class="button-popover" trigger="hover" />
    </el-button>
    <el-dialog :visible.sync="dialogVisible" title="警告" width="30%">
      <span>擦除重做会彻底丢失当前正在编辑的流程，请谨慎操作！！！</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="restart()">确 定</el-button>
      </span>
    </el-dialog>
  </el-button-group>
</template>

<script>
import { mapGetters, mapMutations } from "vuex";
import { createNewDiagram } from "@utils/xml";

export default {
  name: "BpmnCommands",
  data() {
    return {
      dialogVisible: false
    };
  },
  computed: {
    ...mapGetters(["getModeler"])
  },
  methods: {
    ...mapMutations("localStoreModule", ["changeLocalBpmnXml"]),
    getCommand() {
      return this.getModeler && this.getModeler.get("commandStack");
    },
    redo() {
      const command = this.getCommand();
      command && command.canRedo() && command.redo();
    },
    undo() {
      const command = this.getCommand();
      command && command.canUndo() && command.undo();
    },
    preRestart() {
      this.dialogVisible = true;
    },
    restart() {
      this.dialogVisible = false;
      const command = this.getCommand();
      command && command.clear();
      this.getModeler && createNewDiagram(this.getModeler);
      setTimeout(() => this.changeLocalBpmnXml(""), 100);
    }
  }
};
</script>
