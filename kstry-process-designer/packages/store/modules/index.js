const state = {
  localBpmnXml: ""
};

const getters = {
  getLocalBpmnXml: (state) => state.localBpmnXml
};

const mutations = {
  changeLocalBpmnXml(state, xml) {
    state.localBpmnXml = xml;
  }
};

export default {
  namespaced: true,
  state,
  getters,
  mutations
};
