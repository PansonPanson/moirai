(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-d6c1d344"],{3737:function(e,t,i){"use strict";i.d(t,"c",(function(){return s})),i.d(t,"d",(function(){return a})),i.d(t,"a",(function(){return l})),i.d(t,"b",(function(){return o}));var n=i("b775");function s(e){return Object(n["a"])({url:"/hippo4j/v1/cs/item/query/page",method:"post",data:e})}function a(e){return Object(n["a"])({url:"/hippo4j/v1/cs/item/update",method:"post",data:e})}function l(e){return Object(n["a"])({url:"/hippo4j/v1/cs/item/save",method:"post",data:e})}function o(e){return Object(n["a"])({url:"/hippo4j/v1/cs/item/delete/"+e[0]+"/"+e[1],method:"delete"})}},"4d85":function(e,t,i){"use strict";i.d(t,"e",(function(){return s})),i.d(t,"f",(function(){return a})),i.d(t,"d",(function(){return l})),i.d(t,"g",(function(){return o})),i.d(t,"h",(function(){return r})),i.d(t,"j",(function(){return c})),i.d(t,"k",(function(){return u})),i.d(t,"i",(function(){return d})),i.d(t,"b",(function(){return p})),i.d(t,"c",(function(){return m})),i.d(t,"a",(function(){return f}));var n=i("b775");function s(e){return Object(n["a"])({url:"/hippo4j/v1/cs/thread/pool/query/page",method:"post",data:e})}function a(e){return Object(n["a"])({url:"/hippo4j/v1/cs/thread/pool/list/client/instance/"+e.itemId+"?mark="+e.mark,method:"get",data:e})}function l(e){return Object(n["a"])({url:"/hippo4j/v1/cs/configs?tpId="+e.tpId+"&itemId="+e.itemId+"&namespace="+e.tenantId+"&instanceId="+e.identify,method:"get"})}function o(e){return Object(n["a"])({url:"/hippo4j/v1/cs/thread/pool/run/state/"+e.tpId+"?clientAddress="+e.clientAddress,method:"get"})}function r(e){return Object(n["a"])({url:"/hippo4j/v1/cs/thread/pool/run/thread/state/"+e.tpId+"?clientAddress="+e.clientAddress,method:"get"})}function c(e){return Object(n["a"])({url:"/hippo4j/v1/cs/thread/pool/web/run/state?clientAddress="+e.clientAddress,method:"get"})}function u(e){return Object(n["a"])({url:"/hippo4j/v1/cs/thread/pool/web/update/pool",method:"post",data:e})}function d(e){return Object(n["a"])({url:"/hippo4j/v1/cs/thread/pool/save_or_update",method:"post",data:e})}function p(e){return Object(n["a"])({url:"/hippo4j/v1/cs/thread/pool/save_or_update",method:"post",data:e})}function m(e){return Object(n["a"])({url:"/hippo4j/v1/cs/thread/pool/delete",method:"delete",data:e})}function f(e){return Object(n["a"])({url:"/hippo4j/v1/cs/thread/pool/alarm/enable/"+e.id+"/"+e.isAlarm,method:"post"})}},6724:function(e,t,i){"use strict";i("8d41");var n="@@wavesContext";function s(e,t){function i(i){var n=Object.assign({},t.value),s=Object.assign({ele:e,type:"hit",color:"rgba(0, 0, 0, 0.15)"},n),a=s.ele;if(a){a.style.position="relative",a.style.overflow="hidden";var l=a.getBoundingClientRect(),o=a.querySelector(".waves-ripple");switch(o?o.className="waves-ripple":(o=document.createElement("span"),o.className="waves-ripple",o.style.height=o.style.width=Math.max(l.width,l.height)+"px",a.appendChild(o)),s.type){case"center":o.style.top=l.height/2-o.offsetHeight/2+"px",o.style.left=l.width/2-o.offsetWidth/2+"px";break;default:o.style.top=(i.pageY-l.top-o.offsetHeight/2-document.documentElement.scrollTop||document.body.scrollTop)+"px",o.style.left=(i.pageX-l.left-o.offsetWidth/2-document.documentElement.scrollLeft||document.body.scrollLeft)+"px"}return o.style.backgroundColor=s.color,o.className="waves-ripple z-active",!1}}return e[n]?e[n].removeHandle=i:e[n]={removeHandle:i},i}var a={bind:function(e,t){e.addEventListener("click",s(e,t),!1)},update:function(e,t){e.removeEventListener("click",e[n].removeHandle,!1),e.addEventListener("click",s(e,t),!1)},unbind:function(e){e.removeEventListener("click",e[n].removeHandle,!1),e[n]=null,delete e[n]}},l=function(e){e.directive("waves",a)};window.Vue&&(window.waves=a,Vue.use(l)),a.install=l;t["a"]=a},"7f82":function(e,t,i){"use strict";i.r(t);var n=function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("div",{staticClass:"app-container"},[i("div",{staticClass:"filter-container"},[i("el-select",{staticClass:"filter-item",staticStyle:{width:"220px"},attrs:{placeholder:"租户（必填）",filterable:""},on:{change:function(t){return e.tenantSelectList()}},model:{value:e.listQuery.tenantId,callback:function(t){e.$set(e.listQuery,"tenantId",t)},expression:"listQuery.tenantId"}},e._l(e.tenantOptions,(function(e){return i("el-option",{key:e.key,attrs:{label:e.display_name,value:e.key}})})),1),e._v(" "),i("el-select",{staticClass:"filter-item",staticStyle:{width:"220px"},attrs:{placeholder:"项目（必填）",filterable:""},on:{change:function(t){return e.itemSelectList()}},model:{value:e.listQuery.itemId,callback:function(t){e.$set(e.listQuery,"itemId",t)},expression:"listQuery.itemId"}},e._l(e.itemOptions,(function(e){return i("el-option",{key:e.key,attrs:{label:e.display_name,value:e.key}})})),1),e._v(" "),i("el-button",{directives:[{name:"waves",rawName:"v-waves"}],staticClass:"filter-item",attrs:{type:"primary",icon:"el-icon-search"},on:{click:e.fetchData}},[e._v("\n      搜索\n    ")])],1),e._v(" "),i("el-table",{directives:[{name:"loading",rawName:"v-loading",value:e.listLoading,expression:"listLoading"}],attrs:{data:e.list,"element-loading-text":"Loading",border:"",fit:"","max-height":"714","highlight-current-row":""}},[i("el-table-column",{attrs:{label:"序号",fixed:"",width:"95"},scopedSlots:e._u([{key:"default",fn:function(t){return[e._v(e._s(t.$index+1))]}}])}),e._v(" "),i("el-table-column",{attrs:{label:"实例标识",width:"260"},scopedSlots:e._u([{key:"default",fn:function(t){return[i("el-link",{attrs:{type:"primary",underline:!1}},[e._v(e._s(t.row.identify))])]}}])}),e._v(" "),i("el-table-column",{attrs:{label:"Active",width:"120"},scopedSlots:e._u([{key:"default",fn:function(t){return[i("el-tag",{attrs:{type:e._f("statusFilter")(t.row.active)}},[e._v("\n          "+e._s(t.row.active)+"\n        ")])]}}])}),e._v(" "),i("el-table-column",{attrs:{label:"核心线程",width:"120"},scopedSlots:e._u([{key:"default",fn:function(t){return[i("el-link",{attrs:{type:"success",underline:!1}},[e._v(e._s(t.row.coreSize))])]}}])}),e._v(" "),i("el-table-column",{attrs:{label:"最大线程",width:"120"},scopedSlots:e._u([{key:"default",fn:function(t){return[i("el-link",{attrs:{type:"danger",underline:!1}},[e._v(e._s(t.row.maximumSize))])]}}])}),e._v(" "),i("el-table-column",{attrs:{label:"队列类型",width:"260"},scopedSlots:e._u([{key:"default",fn:function(t){return[e._v(e._s(t.row.queueType))]}}])}),e._v(" "),i("el-table-column",{attrs:{label:"队列容量",width:"160"},scopedSlots:e._u([{key:"default",fn:function(t){return[e._v(e._s(t.row.queueCapacity))]}}])}),e._v(" "),i("el-table-column",{attrs:{label:"拒绝策略",width:"260"},scopedSlots:e._u([{key:"default",fn:function(t){return[e._v(e._s(t.row.rejectedName))]}}])}),e._v(" "),i("el-table-column",{attrs:{label:"空闲回收",width:"100"},scopedSlots:e._u([{key:"default",fn:function(t){return[e._v(e._s(t.row.keepAliveTime))]}}])}),e._v(" "),i("el-table-column",{attrs:{label:"操作",width:"90",align:"center",fixed:"right","class-name":"small-padding fixed-width"},scopedSlots:e._u([{key:"default",fn:function(t){var n=t.row;return[i("el-button",{attrs:{type:"text",size:"small"},on:{click:function(t){return e.handleInfo(n)}}},[e._v(" 查看 ")]),e._v(" "),i("el-button",{attrs:{type:"text",disabled:e.isEditDisabled,size:"small"},on:{click:function(t){return e.handleUpdate(n)}}},[e._v("\n          编辑\n        ")])]}}])})],1),e._v(" "),i("el-dialog",{attrs:{title:e.textMap[e.dialogStatus],visible:e.instanceDialogFormVisible,width:"1000px"},on:{"update:visible":function(t){e.instanceDialogFormVisible=t}}},[[i("el-descriptions",{staticClass:"margin-top",attrs:{title:"负载信息",column:3,size:e.size,border:""}},[i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 当前负载 ")]),e._v("\n          "+e._s(e.runTimeTemp.currentLoad)+"\n        ")],2),e._v(" "),i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 峰值负载 ")]),e._v("\n          "+e._s(e.runTimeTemp.peakLoad)+"\n        ")],2),e._v(" "),i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 剩余内存 ")]),e._v("\n          "+e._s(e.runTimeTemp.freeMemory)+"\n        ")],2),e._v(" "),i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 内存占比 ")]),e._v("\n          "+e._s(e.runTimeTemp.memoryProportion)+"\n        ")],2)],1),e._v(" "),i("br"),e._v(" "),i("br"),e._v(" "),i("el-descriptions",{staticClass:"margin-top",attrs:{title:"线程信息",column:3,size:e.size,border:""}},[i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 核心线程 ")]),e._v("\n          "+e._s(e.runTimeTemp.coreSize)+"\n        ")],2),e._v(" "),i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 当前线程 ")]),e._v("\n          "+e._s(e.runTimeTemp.poolSize)+"\n        ")],2),e._v(" "),i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 最大线程 ")]),e._v("\n          "+e._s(e.runTimeTemp.maximumSize)+"\n        ")],2),e._v(" "),i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 活跃线程 ")]),e._v("\n          "+e._s(e.runTimeTemp.activeSize)+"\n        ")],2),e._v(" "),i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 同存最大线程 ")]),e._v("\n          "+e._s(e.runTimeTemp.largestPoolSize)+"\n        ")],2)],1),e._v(" "),i("br"),e._v(" "),i("br"),e._v(" "),i("el-descriptions",{staticClass:"margin-top",attrs:{title:"队列信息",column:3,size:e.size,border:""}},[i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 队列容量 ")]),e._v("\n          "+e._s(e.runTimeTemp.queueCapacity)+"\n        ")],2),e._v(" "),i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 队列元素 ")]),e._v("\n          "+e._s(e.runTimeTemp.queueSize)+"\n        ")],2),e._v(" "),i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 队列剩余容量 ")]),e._v("\n          "+e._s(e.runTimeTemp.queueRemainingCapacity)+"\n        ")],2),e._v(" "),i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 阻塞队列 ")]),e._v("\n          "+e._s(e.runTimeTemp.queueType)+"\n        ")],2)],1),e._v(" "),i("br"),e._v(" "),i("br"),e._v(" "),i("el-descriptions",{staticClass:"margin-top",attrs:{title:"其它信息",column:3,size:e.size,border:""}},[i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 任务总量 ")]),e._v("\n          "+e._s(e.runTimeTemp.completedTaskCount)+"\n        ")],2),e._v(" "),i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 最后更新时间 ")]),e._v("\n          "+e._s(e.runTimeTemp.clientLastRefreshTime)+"\n        ")],2),e._v(" "),i("el-descriptions-item",[i("template",{slot:"label"},[e._v(" 拒绝策略 ")]),e._v("\n          "+e._s(e.runTimeTemp.rejectedName)+"\n        ")],2)],1)],e._v(" "),i("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[i("el-button",{on:{click:function(t){e.instanceDialogFormVisible=!1}}},[i("i",{staticClass:"el-icon-close"}),e._v("\n        关 闭\n      ")]),e._v(" "),i("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.handleInfo()}}},[i("i",{staticClass:"el-icon-refresh-right"}),e._v("\n        刷 新\n      ")])],1)],2),e._v(" "),i("el-dialog",{attrs:{title:e.textMap[e.dialogStatus],visible:e.dialogFormVisible},on:{"update:visible":function(t){e.dialogFormVisible=t}}},[i("el-form",{ref:"dataForm",attrs:{rules:e.rules,model:e.temp,"label-position":"left","label-width":"110px"}},[i("el-form-item",{attrs:{label:"核心线程",prop:"coreSize"}},[[i("el-input-number",{attrs:{"controls-position":"right",min:1,max:9999},model:{value:e.temp.coreSize,callback:function(t){e.$set(e.temp,"coreSize",t)},expression:"temp.coreSize"}})]],2),e._v(" "),i("el-form-item",{attrs:{label:"最大线程",prop:"maximumSize"}},[[i("el-input-number",{attrs:{"controls-position":"right",min:1,max:9999},model:{value:e.temp.maximumSize,callback:function(t){e.$set(e.temp,"maximumSize",t)},expression:"temp.maximumSize"}})]],2),e._v(" "),i("el-form-item",{attrs:{label:"全部修改",prop:"allUpdate"}},[i("el-switch",{model:{value:e.temp.allUpdate,callback:function(t){e.$set(e.temp,"allUpdate",t)},expression:"temp.allUpdate"}})],1)],1),e._v(" "),i("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[i("el-button",{on:{click:function(t){e.dialogFormVisible=!1}}},[e._v(" 取消 ")]),e._v(" "),i("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.updateData()}}},[e._v(" 确认 ")])],1)],1),e._v(" "),i("el-dialog",{attrs:{visible:e.dialogPluginVisible,title:"Reading statistics"},on:{"update:visible":function(t){e.dialogPluginVisible=t}}},[i("el-table",{staticStyle:{width:"100%"},attrs:{data:e.pluginData,border:"",fit:"","highlight-current-row":""}},[i("el-table-column",{attrs:{prop:"key",label:"Channel"}}),e._v(" "),i("el-table-column",{attrs:{prop:"pv",label:"Pv"}})],1),e._v(" "),i("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[i("el-button",{attrs:{type:"primary"},on:{click:function(t){e.dialogPvVisible=!1}}},[e._v("Confirm")])],1)],1)],1)},s=[],a=(i("6762"),i("3737")),l=i("dd71"),o=i("4d85"),r=i("6724"),c={name:"JobProject",directives:{waves:r["a"]},filters:{statusFilter:function(e){var t={DEV:"info",TEST:"success",UAT:"warning",PROD:"danger"};return t[e]}},data:function(){return{isRejectShow:!1,list:[],listLoading:!1,total:0,listQuery:{current:1,size:10,tpId:"",itemId:"",mark:"Jetty"},pluginTypeOptions:["reader","writer"],dialogPluginVisible:!1,pluginData:[],dialogFormVisible:!1,isEditDisabled:!1,runTimeTemp:{},tenantOptions:[],instanceDialogFormVisible:!1,threadPoolOptions:[],itemOptions:[],itemTempOptions:[],queueTypeOptions:[{key:1,display_name:"ArrayBlockingQueue"},{key:2,display_name:"LinkedBlockingQueue"},{key:3,display_name:"LinkedBlockingDeque"},{key:4,display_name:"SynchronousQueue"},{key:5,display_name:"LinkedTransferQueue"},{key:6,display_name:"PriorityBlockingQueue"},{key:9,display_name:"ResizableLinkedBlockingQueue (动态修改队列大小)"}],rejectedOptions:[{key:1,display_name:"CallerRunsPolicy"},{key:2,display_name:"AbortPolicy"},{key:3,display_name:"DiscardPolicy"},{key:4,display_name:"DiscardOldestPolicy"},{key:5,display_name:"RunsOldestTaskPolicy"},{key:6,display_name:"SyncPutQueuePolicy"},{key:99,display_name:"CustomRejectedPolicy（自定义 SPI 策略）"}],alarmTypes:[{key:1,display_name:"报警"},{key:0,display_name:"不报警"}],allowCoreThreadTimeOutTypes:[{key:1,display_name:"超时"},{key:0,display_name:"不超时"}],size:500,dialogStatus:"",textMap:{update:"Edit",create:"Create",info:"Info"},rules:{coreSize:[{required:!0,message:"this is required",trigger:"blur"}],maximumSize:[{required:!0,message:"this is required",trigger:"blur"}]},temp:{id:void 0,tenantId:"",itemId:"",rejectedType:null,allUpdate:"1",customRejectedType:null},visible:!0}},created:function(){this.initSelect()},mounted:function(){this.isEditDisabled="ROLE_ADMIN"!==localStorage.getItem("USER_ROLE")},methods:{onInput:function(){this.$forceUpdate()},fetchData:function(){var e=this;this.listQuery.tenantId?this.listQuery.itemId?(this.listLoading=!0,o["f"](this.listQuery).then((function(t){if(null!=t){for(var i=t,n=[],s=0;s<i.length;s++){var a={},l=t[s];a.identify=l.identify,a.active=l.active,a.clientAddress=l.clientAddress,a.coreSize=l.coreSize,a.maximumSize=l.maximumSize,a.queueType=l.queueType,a.queueCapacity=l.queueCapacity,a.rejectedName=l.rejectedName,a.keepAliveTime=l.keepAliveTime,a.tenantId=l.tenantId,a.itemId=l.itemId,n.push(a)}e.list=n,e.listLoading=!1}else e.listLoading=!1}))):this.$message.warning("项目不允许为空"):this.$message.warning("租户不允许为空")},changeAlarm:function(e){var t=this;o["a"](e).then((function(){t.fetchData(),t.$notify({title:"Success",message:"Update Successfully",type:"success",duration:2e3})}))},initSelect:function(){var e=this;l["c"]({size:this.size}).then((function(t){for(var i=t.records,n=0;n<i.length;n++)e.tenantOptions.push({key:i[n].tenantId,display_name:i[n].tenantId+" "+i[n].tenantName})}))},resetTemp:function(){this.isRejectShow=!1,this.temp={id:void 0,tenantId:"",itemId:"",rejectedType:null,customRejectedType:null}},handleCreate:function(){var e=this;this.resetTemp(),this.dialogStatus="create",this.dialogFormVisible=!0,this.isRejectShow=!1,this.$nextTick((function(){e.$refs["dataForm"].clearValidate()}))},createData:function(){var e=this;this.$refs["dataForm"].validate((function(t){t&&(e.isRejectShow&&(null==e.temp.customRejectedType?e.temp.rejectedType=2:e.temp.rejectedType=e.temp.customRejectedType),o["b"](e.temp).then((function(){e.fetchData(),e.dialogFormVisible=!1,e.$notify({title:"Success",message:"Created Successfully",type:"success",duration:2e3})})))}))},handleUpdate:function(e){var t=this;this.temp=Object.assign({},e);var i=this.temp.rejectedType,n=[1,2,3,4,5,6];n.includes(i)?this.isRejectShow=!1:(this.isRejectShow=!0,this.temp.customRejectedType=this.temp.rejectedType,this.temp.rejectedType=99),this.dialogStatus="update",this.dialogFormVisible=!0,this.$nextTick((function(){t.$refs["dataForm"].clearValidate()}))},updateData:function(){var e=this;this.$refs["dataForm"].validate((function(t){if(t){if(parseInt(e.temp.maximumSize)<parseInt(e.temp.coreSize))return void e.$message({message:"最大线程必须大于等于核心线程",type:"warning"});var i=[],n={corePoolSize:e.temp.coreSize,itemId:e.temp.itemId,tenantId:e.temp.tenantId,maximumPoolSize:e.temp.maximumSize,keepAliveTime:e.temp.keepAliveTime,clientAddressList:i};if(e.temp.allUpdate){n.modifyAll=!0;for(var s=0;s<e.list.length;s++)null!=e.list[s]&&(i[s]=e.list[s].clientAddress)}else n.modifyAll=!1,n.identify=e.temp.identify,i[0]=e.temp.clientAddress;e.updateExecute(n)}}))},updateExecute:function(e){var t=this;o["k"](e).then((function(e){t.dialogFormVisible=!1,t.$notify({title:"Success",message:"Update Successfully",type:"success",duration:2e3}),t.fetchData()})).catch((function(e){console.log(e),t.$message.error("查询失败，请尝试刷新页面")}))},openDelConfirm:function(e){return this.$confirm("此操作将删除 ".concat(e,", 是否继续?"),"提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"})},selectQueueType:function(e){4===e?this.temp.capacity=0:5===e&&(this.temp.capacity=2147483647)},tenantSelectList:function(){var e=this;this.listQuery.itemId=null,this.listQuery.tpId=null,this.temp.itemId=null,this.itemOptions=[],this.itemTempOptions=[],this.threadPoolOptions=[];var t={tenantId:this.listQuery.tenantId,size:this.size};a["c"](t).then((function(t){for(var i=t.records,n=0;n<i.length;n++)e.itemOptions.push({key:i[n].itemId,display_name:i[n].itemId+" "+i[n].itemName})}))},tenantTempSelectList:function(){var e=this;this.itemTempOptions=[],this.temp.itemId&&(this.temp.itemId=null);var t={tenantId:this.temp.tenantId,size:this.size};a["c"](t).then((function(t){for(var i=t.records,n=0;n<i&&i.length;n++)e.itemTempOptions.push({key:i[n].itemId,display_name:i[n].itemId+" "+i[n].itemName})}))},itemSelectList:function(){var e=this;this.listQuery.tpId=null,this.threadPoolOptions=[];var t={itemId:this.listQuery.itemId,size:this.size};o["e"](t).then((function(t){for(var i=t.records,n=0;n<i&&i.length;n++)e.threadPoolOptions.push({key:i[n].tpId,display_name:i[n].tpId})}))},selectRejectedType:function(e){this.isRejectShow=99==e},handleInfo:function(e){this.instanceDialogFormVisible=!0,this.dialogStatus="info","undefined"==typeof e||null==e?e=this.tempRow:this.tempRow={clientAddress:e.clientAddress},this.refresh(e)},refresh:function(e){var t=this,i="",n=e.clientAddress,s=e.clientBasePath;i=null!=s?n+s:n,o["j"]({clientAddress:i}).then((function(e){null!=e&&(t.runTimeTemp=e)})).catch((function(e){console.log(e),t.$message.error("查询失败，请尝试刷新页面")}))}}},u=c,d=i("2877"),p=Object(d["a"])(u,n,s,!1,null,null,null);t["default"]=p.exports},"8d41":function(e,t,i){},dd71:function(e,t,i){"use strict";i.d(t,"c",(function(){return s})),i.d(t,"d",(function(){return a})),i.d(t,"a",(function(){return l})),i.d(t,"b",(function(){return o}));var n=i("b775");function s(e){return Object(n["a"])({url:"/hippo4j/v1/cs/tenant/query/page",method:"post",data:e})}function a(e){return Object(n["a"])({url:"/hippo4j/v1/cs/tenant/update",method:"post",data:e})}function l(e){return Object(n["a"])({url:"/hippo4j/v1/cs/tenant/save",method:"post",data:e})}function o(e){return Object(n["a"])({url:"/hippo4j/v1/cs/tenant/delete/"+e,method:"delete"})}}}]);