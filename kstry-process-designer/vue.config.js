const { defineConfig } = require("@vue/cli-service");
const htmlWebpackPlugin = require("html-webpack-plugin");

const path = require("path");

const isProd = process.env.NODE_ENV === "production";
let externals = {};
if (isProd) {
  externals = {
    vue: "Vue",
    "element-ui": "ELEMENT"
  };
}

function resolve(dir) {
  return path.join(__dirname, dir);
}

module.exports = defineConfig({
  publicPath: isProd ? "http://cdn.kstry.cn/stastic/configuration/" : "/",
  pages: {
    index: {
      entry: "playground/main.js",
      template: "public/index.html"
    }
  },
  transpileDependencies: false,
  runtimeCompiler: true,
  parallel: true,
  productionSourceMap: false,
  configureWebpack: {
    resolve: {
      alias: {
        "@": resolve("src"),
        "@packages": resolve("packages"),
        "@utils": resolve("utils")
      }
    },
    externals
  },
  chainWebpack(config) {
    config.module.rule("svg").exclude.add(resolve("packages/bpmn-icons")).end();
    config.module
      .rule("icons")
      .test(/\.svg$/)
      .include.add(resolve("packages/bpmn-icons"))
      .end()
      .use("svg-sprite-loader")
      .loader("svg-sprite-loader")
      .options({
        symbolId: "[name]"
      })
      .end();
  }
});
