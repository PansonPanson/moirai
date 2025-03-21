(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-6b3c38c4"],{"249c":function(t,n,o){},"34bb":function(t,n,o){"use strict";o("5a7f")},"5a7f":function(t,n,o){},"9a73":function(t,n,o){},"9ed6":function(t,n,o){"use strict";o.r(n);var e=function(){var t=this,n=t.$createElement,o=t._self._c||n;return o("div",{staticClass:"login-container"},[o("el-form",{ref:"loginForm",staticClass:"login-form",attrs:{model:t.loginForm,rules:t.loginRules,autocomplete:"on","label-position":"left"}},[o("div",{staticClass:"title-container"},[o("h3",{staticClass:"title"},[t._v("Login Form")])]),t._v(" "),o("el-form-item",{attrs:{prop:"username"}},[o("span",{staticClass:"svg-container"},[o("svg-icon",{attrs:{"icon-class":"user"}})],1),t._v(" "),o("el-input",{ref:"username",attrs:{placeholder:"Username",name:"username",type:"text",tabindex:"1",autocomplete:"on"},model:{value:t.loginForm.username,callback:function(n){t.$set(t.loginForm,"username",n)},expression:"loginForm.username"}})],1),t._v(" "),o("el-tooltip",{attrs:{content:"Caps lock is On",placement:"right",manual:""},model:{value:t.capsTooltip,callback:function(n){t.capsTooltip=n},expression:"capsTooltip"}},[o("el-form-item",{attrs:{prop:"password"}},[o("span",{staticClass:"svg-container"},[o("svg-icon",{attrs:{"icon-class":"password"}})],1),t._v(" "),o("el-input",{key:t.passwordType,ref:"password",attrs:{type:t.passwordType,placeholder:"Password",name:"password",tabindex:"2",autocomplete:"on"},on:{blur:function(n){t.capsTooltip=!1}},nativeOn:{keyup:[function(n){return t.checkCapslock(n)},function(n){return!n.type.indexOf("key")&&t._k(n.keyCode,"enter",13,n.key,"Enter")?null:t.handleLogin(n)}]},model:{value:t.loginForm.password,callback:function(n){t.$set(t.loginForm,"password",n)},expression:"loginForm.password"}}),t._v(" "),o("span",{staticClass:"show-pwd",on:{click:t.showPwd}},[o("svg-icon",{attrs:{"icon-class":"password"===t.passwordType?"eye":"eye-open"}})],1)],1)],1),t._v(" "),o("el-button",{staticStyle:{width:"100%","margin-bottom":"30px"},attrs:{loading:t.loading,type:"primary"},nativeOn:{click:function(n){return n.preventDefault(),t.handleLogin(n)}}},[t._v("Login")])],1),t._v(" "),o("el-dialog",{attrs:{title:"Or connect with",visible:t.showDialog},on:{"update:visible":function(n){t.showDialog=n}}},[t._v("\n    Can not be simulated on local, so please combine you own business simulation! ! !\n    "),o("br"),t._v(" "),o("br"),t._v(" "),o("br"),t._v(" "),o("social-sign")],1)],1)},s=[],i=(o("ac6a"),o("456d"),function(){var t=this,n=t.$createElement,o=t._self._c||n;return o("div",{staticClass:"social-signup-container"},[o("div",{staticClass:"sign-btn",on:{click:function(n){return t.wechatHandleClick("wechat")}}},[o("span",{staticClass:"wx-svg-container"},[o("svg-icon",{staticClass:"icon",attrs:{"icon-class":"wechat"}})],1),t._v("\n    WeChat\n  ")]),t._v(" "),o("div",{staticClass:"sign-btn",on:{click:function(n){return t.tencentHandleClick("tencent")}}},[o("span",{staticClass:"qq-svg-container"},[o("svg-icon",{staticClass:"icon",attrs:{"icon-class":"qq"}})],1),t._v("\n    QQ\n  ")])])}),a=[],r={name:"SocialSignin",methods:{wechatHandleClick:function(t){alert("ok")},tencentHandleClick:function(t){alert("ok")}}},c=r,l=(o("bfec"),o("2877")),u=Object(l["a"])(c,i,a,!1,null,"7309fbbb",null),p=u.exports,d={name:"Login",components:{SocialSign:p},data:function(){var t=function(t,n,o){n.length<6?o(new Error("The password can not be less than 6 digits")):o()};return{loginForm:{username:"",password:""},loginRules:{password:[{required:!0,trigger:"blur",validator:t}]},passwordType:"password",capsTooltip:!1,loading:!1,showDialog:!1,redirect:void 0,otherQuery:{}}},watch:{$route:{handler:function(t){var n=t.query;n&&(this.redirect=n.redirect,this.otherQuery=this.getOtherQuery(n))},immediate:!0}},created:function(){var t=window.location.hostname;"console.hippo4j.cn"===t&&(this.loginForm.username="hippo4j",this.loginForm.password="hippo4j"),console.log(t)},mounted:function(){""===this.loginForm.username?this.$refs.username.focus():""===this.loginForm.password&&this.$refs.password.focus()},destroyed:function(){},methods:{checkCapslock:function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{},n=t.shiftKey,o=t.key;o&&1===o.length&&(this.capsTooltip=!!(n&&o>="a"&&o<="z"||!n&&o>="A"&&o<="Z")),"CapsLock"===o&&!0===this.capsTooltip&&(this.capsTooltip=!1)},showPwd:function(){var t=this;"password"===this.passwordType?this.passwordType="":this.passwordType="password",this.$nextTick((function(){t.$refs.password.focus()}))},handleLogin:function(){var t=this;this.$refs.loginForm.validate((function(n){if(!n)return console.log("error submit."),!1;t.loading=!0,t.$store.dispatch("user/login",t.loginForm).then((function(){t.$cookie.set("userName",t.loginForm.username),console.log("success submit."),t.$router.push({path:t.redirect||"/",query:t.otherQuery}),t.loading=!1})).catch((function(){console.log("error catch."),t.loading=!1}))}))},getOtherQuery:function(t){return Object.keys(t).reduce((function(n,o){return"redirect"!==o&&(n[o]=t[o]),n}),{})}}},h=d,g=(o("d4a4"),o("34bb"),Object(l["a"])(h,e,s,!1,null,"13dcd441",null));n["default"]=g.exports},bfec:function(t,n,o){"use strict";o("9a73")},d4a4:function(t,n,o){"use strict";o("249c")}}]);