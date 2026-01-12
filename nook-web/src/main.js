import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import JSEncrypt from 'jsencrypt';
Vue.use(ElementUI);
const defaultBaseApi = 'http://localhost:8080';
const storedBaseApi = localStorage.getItem('baseApi');
if (!storedBaseApi || storedBaseApi.includes(':8081')) {
  localStorage.setItem('baseApi', defaultBaseApi);
}
Vue.prototype.$getRsaCode = function(str,key){ // 注册方法
  let encryptStr = new JSEncrypt();
  encryptStr.setPublicKey(key); // 设置 加密公钥
  let  data = encryptStr.encrypt(str.toString());  // 进行加密
  return data;
}

window.vueObj = Vue;
new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
