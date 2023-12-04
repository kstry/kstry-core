<template>
  <el-button-group>
    <el-button v-r-popover:copyXmlProcessContent class="el-button__no-padding copy-xml-content" @click="copyXmlProcess">
      <lucide-icon :size="16" name="Copy" />
      <el-popover
        ref="copyXmlProcessContent"
        content="复制流程文件至粘贴板"
        placement="bottom"
        popper-class="button-popover"
        trigger="hover"
      />
    </el-button>
    <el-button
      v-if="getEditorConfig.useMock"
      v-r-popover:processMock
      class="el-button__no-padding"
      @click="mockSimulationToggle"
    >
      <lucide-icon :size="16" name="Bot" />
      <el-popover
        ref="processMock"
        content="开启/关闭流程模拟"
        placement="bottom"
        popper-class="button-popover"
        trigger="hover"
      />
    </el-button>
    <el-button
      v-if="getEditorConfig.useMinimap"
      v-r-popover:minimapToggle
      class="el-button__no-padding"
      @click="minimapToggle"
    >
      <lucide-icon :size="16" name="Map" />
      <el-popover
        ref="minimapToggle"
        content="展开/收起小地图"
        placement="bottom"
        popper-class="button-popover"
        trigger="hover"
      />
    </el-button>
    <el-button v-if="getEditorConfig.useLint" v-r-popover:lintToggle class="el-button__no-padding" @click="lintToggle">
      <lucide-icon :size="16" name="FileCheck" />
      <el-popover
        ref="lintToggle"
        content="开启/关闭流程校验"
        placement="bottom"
        popper-class="button-popover"
        trigger="hover"
      />
    </el-button>
    <!--    <el-button v-r-popover:eventToggle class="el-button__no-padding" @click="eventModelVisible = true">-->
    <!--      <lucide-icon name="Podcast" :size="16" />-->
    <!--      <el-popover-->
    <!--        ref="eventToggle"-->
    <!--        content="查看bpmn事件"-->
    <!--        placement="bottom"-->
    <!--        trigger="hover"-->
    <!--        popper-class="button-popover"-->
    <!--      />-->
    <!--    </el-button>-->
    <el-button v-r-popover:keyboard class="el-button__no-padding" @click="keyboardModelVisible = true">
      <lucide-icon :size="16" name="Keyboard" />
      <el-popover
        ref="keyboard"
        content="键盘快捷键"
        placement="bottom"
        popper-class="button-popover"
        trigger="hover"
      />
    </el-button>
    <el-button v-r-popover:gotoHomePage class="el-button__no-padding" @click="gotoHomePage()">
      <lucide-icon :size="16" name="Home" />
      <el-popover
        ref="gotoHomePage"
        content="跳转主页"
        placement="bottom"
        popper-class="button-popover"
        trigger="hover"
      />
    </el-button>
    <el-dialog
      :visible.sync="eventModelVisible"
      append-to-body
      destroy-on-close
      title="Bpmn.js 当前已注册事件"
      width="560px"
    >
      <div class="event-listeners-box">
        <div class="listener-search">
          <el-input v-model="listenerFilter" clearable placeholder="事件名称关键字" />
        </div>
        <div class="event-listeners-box">
          <p v-for="(name, index) in visibleListeners" :key="name" class="listener-item">
            {{ `${index + 1}：${name}` }}
          </p>
        </div>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="keyboardModelVisible" append-to-body destroy-on-close title="键盘快捷键" width="560px">
      <div class="shortcut-keys-model">
        <p>Undo</p>
        <p>Ctrl + Z</p>
        <p>Redo</p>
        <p>Ctrl + Shift + Z / ctrl + Y</p>
        <p>Select All</p>
        <p>Ctrl + A</p>
        <p>Zoom</p>
        <p>Ctrl + Mouse Wheel</p>
        <p>Scrolling (Vertical)</p>
        <p>Mouse Wheel</p>
        <p>Scrolling (Horizontal)</p>
        <p>Shift + Mouse Wheel</p>
        <p>Direct Editing</p>
        <p>E</p>
        <p>Hand Tool</p>
        <p>H</p>
        <p>Lasso Tool</p>
        <p>L</p>
        <p>Space Tool</p>
        <p>S</p>
      </div>
      <div v-if="templateChooser" class="shortcut-keys-model">
        <p>Replace Tool</p>
        <p>R</p>
        <p>Append anything</p>
        <p>A</p>
        <p>Create anything</p>
        <p>N</p>
      </div>
    </el-dialog>
  </el-button-group>
</template>

<script>
import { mapGetters } from "vuex";
import { catchError } from "@utils/printCatch";
import Clipboard from "clipboard";

export default {
  name: "BpmnExternals",
  data() {
    return {
      listenerFilter: "",
      listeners: [],
      eventModelVisible: false,
      keyboardModelVisible: false
    };
  },
  computed: {
    ...mapGetters(["getModeler", "getEditorConfig"]),
    visibleListeners() {
      return this.listeners.filter((i) => i.includes(this.listenerFilter));
    },
    templateChooser() {
      return this.getEditorConfig.templateChooser;
    }
  },
  watch: {
    getModeler: {
      immediate: true,
      handler() {
        if (this.getModeler) {
          this.listeners = Object.keys(this.getModeler.get("eventBus")?._listeners || {}).sort();
        }
      }
    }
  },
  methods: {
    gotoHomePage() {
      window.open("http://kstry.cn", "_blank");
    },
    mockSimulationToggle() {
      this.getModeler?.get("toggleMode")?.toggleMode();
    },
    minimapToggle() {
      this.getModeler?.get("minimap")?.toggle();
    },
    lintToggle() {
      this.getEditorConfig.useLint && this.getModeler?.get("linting")?.toggle();
    },
    async copyXmlProcess() {
      try {
        if (!this.getModeler) return this.$message.error("流程图引擎初始化失败");
        this.codeLanguage = "xml";
        this.codeModelVisible = true;
        const { xml } = await this.getModeler.saveXML({ format: true, preamble: true });
        let clipboard = new Clipboard(".copy-xml-content", {
          text: () => {
            return xml;
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
      } catch (e) {
        catchError(e);
      }
    }
  }
};
</script>
