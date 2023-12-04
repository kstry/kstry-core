<template>
  <el-button v-popover:popover type="primary">
    预览文件
    <el-popover ref="popover" placement="bottom" popper-class="button-popover" trigger="hover">
      <div class="button-list_column">
        <el-button type="primary" @click="openXMLPreviewModel">预览 XML</el-button>
      </div>
    </el-popover>

    <el-dialog :title="modelTitle" :visible.sync="codeModelVisible" append-to-body destroy-on-close width="72vw">
      <el-button class="copy-qb" type="text" @click="copyXmlContent()">
        <lucide-icon :size="13" name="Copy" style="vertical-align: bottom" />
        复制
      </el-button>
      <div class="preview-model">
        <highlightjs :code="codeString" :language="codeLanguage" />
      </div>
    </el-dialog>
  </el-button>
</template>

<script>
import { mapGetters } from "vuex";
import { catchError } from "@utils/printCatch";
import Clipboard from "clipboard";

export default {
  name: "BpmnPreviews",
  computed: {
    ...mapGetters(["getModeler"]),
    modelTitle() {
      return this.codeLanguage === "xml" ? "预览 XML" : "预览 JSON";
    }
  },
  data() {
    return {
      codeLanguage: "xml",
      codeString: "",
      codeModelVisible: false
    };
  },
  methods: {
    copyXmlContent() {
      let codeContent = this.codeString;
      let clipboard = new Clipboard(".copy-qb", {
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
    async openXMLPreviewModel() {
      try {
        if (!this.getModeler) return this.$message.error("流程图引擎初始化失败");
        this.codeLanguage = "xml";
        this.codeModelVisible = true;
        const { xml } = await this.getModeler.saveXML({ format: true, preamble: true });
        this.codeString = xml;
      } catch (e) {
        catchError(e);
      }
    },
    async openJsonPreviewModel() {
      try {
        if (!this.getModeler) return this.$message.error("流程图引擎初始化失败");
        this.codeLanguage = "json";
        this.codeModelVisible = true;
        const { xml } = await this.getModeler.saveXML({ format: true, preamble: true });
        const jsonStr = await this.getModeler.get("moddle").fromXML(xml);
        this.codeString = JSON.stringify(jsonStr, null, 2);
      } catch (e) {
        catchError(e);
      }
    }
  }
};
</script>
