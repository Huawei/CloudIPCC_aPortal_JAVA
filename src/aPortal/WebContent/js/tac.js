define(['tacCfg'] , function(tacCfg) {
	var tac = null;
	var tacDataMap = {};
	var loading = null;
	var loginTimeout = false;
	var blinkTitleTaskTimer = 0;
	var oldAnswerMax = null;
	var config = {
		serverIp : ''
	};
	var upload = null;
	var _isPromptFlag = false;//解决小广告的问题
	var TT_RING = {};
	var data = {
		set : function(__key, __value) {
			if (__key) {
				tacDataMap[__key] = __value
			}
		},
		get : function(__key) {
			return __key && tacDataMap[__key];
		},
		remove : function(__key) {
			__key && (tacDataMap[__key] = null);
		}
	};
	
	
	/*Map类
	 *调用方法 var map = new tac.Map(); 
	 * */
	var Map = function(){
		this.keys = new Array();
		this.data =  new Object();
		this.times = new Object();
		this.number = new Object();
		this.beforeData = new Object();
		this.old = new Object();
		this.overFlow = new Object();
		this.transferSR = new Object();
		this.privateData = new Object();
		this.overFlowTime = new Object();
	};
	Map.prototype = {
		/*设置值*/
		put: function(key,value){if (key) {if(!this.containsKey(key))this.keys.push(key);this.data[key]=value;}},
		/*获取元素*/
		get: function(key){if(key) return this.data[key];},
		/*获取map大小*/
		size: function(){return this.keys.length;},
		/*清空map*/
		clear: function(){this.keys=new Array();this.data =  new Object();this.times =  new Object();this.number =  new Object();this.beforeData =  new Object();},
		/*判断是否包含该键*/
		containsKey: function(key){return this.keys.filter(function(v){if(v===key)return key;}).length>0},
		/*删除元素*/
		remove: function(key){for(var i=0;i<this.keys.length;i++){if(this.keys[i]==key){var del_keys = this.keys.splice(i,1);delete this.data[key];if(this.times[key])delete this.times[key];if(this.old[key])delete this.old[key];if(this.overFlow[key])delete this.overFlow[key];if(this.privateData[key])delete this.privateData[key];}}},
		/*获取值*/
		values:function(){return this.data;},
		/*设置时间*/
		setTimes : function(key,times){this.times[key]=times;},
		/*获取时间*/
		getTimes : function(key){if(key) return this.times[key];},
		/*设置是不是老单*/
		setOld : function(key,value){this.old[key]=value;},
		/*设置是不是老单*/
		getOld : function(key){return this.old[key];},
		/*设置未读消息数*/
		setNumber : function(key,value){this.number[key]=value;},
		/*设置未读消息数*/
		getNumber : function(key){return this.number[key];},
		/*设置未读数据*/
		setBeforeData : function(key,value){this.beforeData[key]=value;},
		/*设置未读数据*/
		getBeforeData : function(key){return this.beforeData[key];},
		/*设置是不是溢出单*/
		setOverFlow : function(key,value){this.overFlow[key]=value;},
		/*获取是不是溢出单*/
		getOverFlow : function(key){return this.overFlow[key];},
		/*设置转单SR*/
		setTransferSR : function(key,value){this.transferSR[key]=value;},
		/*获取转单SR*/
		getTransferSR : function(key){return this.transferSR[key];},
		/*获取转单SR*/
		delTransferSR : function(key){delete this.transferSR[key];},
		/*设置进入私有队列的数据*/
		setPrivateData : function(key,value){this.privateData[key]=value;},
		/*获取私有队列的数据*/
		getPrivateData : function(key){return this.privateData[key];},
		/*设置溢出单时间*/
		setOverFlowTime : function(key,value){this.overFlowTime[key]=value;},
		/*获取私溢出单时间*/
		getOverFlowTime : function(key){return this.overFlowTime[key];}
	};
	
	/*用于保存全局的等待应答*/
	var wait_customer = new Map(); //等待会话列表

	wait_customer.setData = function(_key,_data){this.put(_key,_data);this.setTimes(_key,0);};
	wait_customer.removeData = function(_key){if(!$("#lc_wait_list_number").get(0))this.remove(_key);delete this.old[key];delete this.overFlow[key];};

	
	
	
	

	
	var loadingShow = function(__flag,__iconflag){
		if(__flag){
			loading || (function() {
				loading = $('<div style="position:fixed;top:0px;left:0px;width:100%;height:100%;z-index:20000; background-color:rgba(255,255,255,.1);"><div style="position:absolute;top:50%;left:50%;z-index:10000;background:url(images/loading.gif) center center no-repeat;border-radius:18px;width:36px;height:36px;background-color:#ffffff;margin-left: -18px;margin-top: -18px" class="'+__iconflag+'"/></div>').appendTo(document.body)
			})();
			loading && loading.show();
			if(__iconflag && __iconflag=="hide"){
				loading.find("div").hide()
			}else{
				loading.find("div").show()
			}
		}else{
			loading && loading.hide();
		}
	};
	
	var ajax = function(__cfg) {
		if (__cfg && __cfg.url) {
			var showErrorMsg = __cfg.showErrorMsg;
			var showTimeout = __cfg.showTimeout != false;
			var settings ={};
			var iconFlag = null;
			if (__cfg.iconFlag == false){
				iconFlag = "hide";
			}
			if (__cfg.loading !== false) {
				loadingShow(true, iconFlag);
			}
			settings.timeout = __cfg.timeout || 2400000;
			__cfg.contentType && (settings.contentType = __cfg.contentType);
			__cfg.type && (settings.type = __cfg.type);
			settings.async = __cfg.async != false;
			settings.data = __cfg.data || {};
			settings.data.handlerTime = util.getRandomString();
			__cfg.type && (settings.type = __cfg.type);
			typeof __cfg.success === 'function' && (settings.success = function(data) {
				if (__cfg.loading !== false) {
					loadingShow(false);
				}
				var stringData = data;
				if(typeof data != 'string'){
					stringData = JSON.stringify(stringData);
				}
				stringData = $.trim(stringData);
				tac.modules.debugbox.pushInfo('[AJAX]response:' + stringData);
				__cfg.success(data);	
				
			});
			if ( typeof __cfg.error === 'function') {
				settings.error = function(data) {
					var responseObj = null;
					if (__cfg.loading !== false) {
						loadingShow(false);
					}
					__cfg.error(data);
				};
			} else {
				settings.error = function(data) {
					if (__cfg.loading !== false) {
						loadingShow(false);
					}
					//防止报错
					if(__cfg.loading !== false){
						showTimeout && _prompt($.i18n.prop('common.msg.box.title.notice'),$.i18n.prop('common.msg.box.timeout'),$.i18n.prop('common.msg.box.btn.ok'));
					}
					
				}
			}
			
			if (__cfg.url.indexOf('onlineagent/verifycode') == -1)
			{
				if (__cfg.url.indexOf('?') == -1)
				{
					__cfg.url = __cfg.url + '?agentId=' + tac.data.get('AGENT_ID');
				}
				else
				{
					__cfg.url = __cfg.url + '&agentId=' + tac.data.get('AGENT_ID');
				}
			}
			
			$.ajax(__cfg.url, settings);
			tac.modules.debugbox.pushInfo('[AJAX]url:' + __cfg.url);
		}
	};
	
	/*
	 * 提示方法
	 * title : 弹出框的标题
	 * content : 提示信息内容
	 * button : 确认按钮名称
	 * callback : 确认按钮回调函数
	 * ismodel : 是否为模态化窗口
	 * cancel : 默认的取消按钮名称
	 * cancelText : 重命名取消按钮的名称，不传时取默认的cancel
	 * promptText : 有该参数则有不再提示之类的选择框，此值为提示语
	 * icon : 是否显示左边提示图标
	 */
	var __setPromptFlag = function(){
		_isPromptFlag = false;
	};
	var _prompt = function(title,content,button,callback,ismodel,cancel,cancelText,promptText,icon,isContinue){
		if((_isPromptFlag && !isContinue)){
			return;
		}
		if(!isContinue){
			if(!_isPromptFlag){
				_isPromptFlag = true;
			}
		}
		if(typeof callback === 'undefined' || !(typeof callback === 'function')){
			callback = function(){}
		}
		var btnArr = [{
			text : button,
			style : 'submit',
			onClick : callback
		}];
		if(typeof cancel === 'function'){
			btnArr.push({
				text : cancelText || '取消',
				style : 'cancel',
				onClick : cancel
			});
		}
		if (undefined == ismodel)
		{
			ismodel = true;
		}
		require(['component/dialog/dialog'], function(Dialog) {
			new Dialog({
				title : title,
				message : content,
				width : 270,
				height : 175,
				model : ismodel,
				drag : true,
				button : btnArr,
				prompt : promptText,
				icon : icon,
				isContinue : isContinue
			}).show();
		});
	}
	
	var _promptEx = function(param){
		if(typeof param.submitClick === 'undefined' || !(typeof param.submitClick === 'function')){
			param.submitClick = function(){}
		}
		var btnArr = [{
			text : param.submitText,
			style : 'submit',
			onClick : param.submitClick
		}];
		if(typeof cancel === 'function'){
			btnArr.push({
				text : param.cancelText || '取消',
				style : 'cancel',
				onClick : param.cancelClick
			});
		}
		require(['component/dialog/dialog'], function(Dialog) {
			new Dialog({
				title : param.title,
				message : param.content,
				width : param.width,
				height : param.height,
				model : true,
				drag : true,
				button : btnArr,
				prompt : param.promptText,
				icon : param.icon,
				isContinue : param.isContinue
			}).show();
		});
	}
	
	
	/*
	 * 自消失的提示框
	 * param
	 * option{
	 * 		type:("warning","success","failed","info")
	 * 		content:提示内容
	 * 		timeout：多长时间后消失（单位毫秒）
	 * }
	 * 不传默认参数：defaultoption={
					type : "success",
					content : '操作成功',
					timeout : 3000
				}
		用法示例：tac.tips({type:'success',content:'重置成功',timeout:3000});
	 */
	
	var tips = function(option){
		var masking = null;
		if(loginTimeout){
			return;
		}
		if(option.timeout=="hide"){
			$('#tac-common-tips').hide();
			return;
		}
		defaultoption={
			type : "success",
			content : $.i18n.prop('common.msg.tips.success'),
			timeout : 3000
		};
		$.extend(defaultoption,option);
		var that = $(document).find('#tac-common-tips');
		if(that.length == 0){
			that = $('<div class="modal-tips" id="tac-common-tips" style="display:none;position:fixed;">' +
					'<span class="fa fa-times-circle fa-lg text-danger"></span>' +
					'<span style="margin-left: 10px;"></span></div>');
			that.appendTo(document.body);
		}
		if(defaultoption.type === 'info'){
			that.find('span:eq(0)').attr('class','fa fa-info-circle fa-lg text-info');
		}else if(defaultoption.type === 'failed'){
			that.find('span:eq(0)').attr('class','fa fa-times-circle fa-lg text-danger');
		}else if(defaultoption.type === 'warning'){
			that.find('span:eq(0)').attr('class','fa fa-exclamation-triangle fa-lg text-warning');
		}else if(defaultoption.type === 'loading'){
			that.find('span:eq(0)').attr('class','fa fa-spinner fa-pulse fa-lg text-info');
		}else{
			that.find('span:eq(0)').attr('class','fa fa-check-circle fa-lg text-success');
		}
		that.find('span:eq(1)').text(defaultoption.content);
		that.show();
		//that.fadeOut(defaultoption.timeout);
		if(option.timeout=="show"){
			return;
		}
		setTimeout(function(){$('#tac-common-tips').hide()},defaultoption.timeout);
		//点击其他地方消失
		if(option.close){
			masking = $('<div style="position: fixed;height: 100%;width: 100%;z-index:10000;overflow: hidden;"></div>').appendTo(document.body);
			$("#tac-common-tips").click(function (event){
				event.stopPropagation();
			});
			//点击其他地方隐藏
			masking.click(function (){
				$('#tac-common-tips').hide();
				masking.remove();
			});
		}
	};
	
	var blinkTitleTask = function(__txt , __interval , __times){
		if(__txt){
			blinkTitleTaskTimer && window.clearInterval(blinkTitleTaskTimer);
			var temp = 0 , tempTimes = 0;
			var intervalTime = __interval || 500;
			var times = __times || 3;
			blinkTitleTaskTimer = window.setInterval(function(){
				if(temp % 2){
					document.title = '工作台';
				}else{
					document.title = __txt;
					tempTimes++;
				}
				temp++;
				if(tempTimes > times){
					window.clearInterval(blinkTitleTaskTimer);
					document.title = '工作台';
				}
			} , parseInt(intervalTime / 2 , 10));
		}
	};
	
	var showNotification = function(__params){
		var notification = null;
		if(__params && window.Notification){
			var img = '';
			var title = __params.title || '提示';
			var content = __params.content || '内容';
			var timeout = __params.timeout || 0;
			var type = __params.type || 0;//0：消息；1：座席；2：电话；
			var level = __params.level || 0;//0：普通；1：警告；2：错误；
			switch(type){
				case 0:
					switch(level){
						case 0:
							img = 'images/notification/msg.jpg';
						break;
						case 1:
							img = 'images/notification/msg.jpg';
						break;
						case 2:
							img = 'images/notification/msg-important.jpg';
						break;
						case 3:
							img = 'images/notification/warning.jpg';
						break;
					};
				break;
				case 1:
					switch(level){
						case 0:
							img = 'images/notification/new-call.jpg';
						break;
						case 1:
							img = 'images/notification/new-call-unanswer.jpg';
						break;
						case 2:
							img = 'images/notification/new-call-unanswer.jpg';
						break;
					};
				break;
				case 2:
					switch(level){
						case 0:
							img = 'images/notification/new-call.jpg';
						break;
						case 1:
							img = 'images/notification/new-call-unanswer.jpg';
						break;
						case 2:
							img = 'images/notification/new-call-unanswer.jpg';
						break;
					};
				break;
			};
			if(Notification.permission==='granted'){
	            notification = new Notification(
													title,
													{	
														icon:img,
														body:content
													}
												);
				if(timeout){
					window.setTimeout(function(){
						notification.close();
					} , timeout);
				}
	        }else {
	           	Notification.requestPermission();
	        };
		}
		return notification;
	};
	
	
    
    
    
    
    
	
	var showTopMsg = (function(__params){
		var timer = 0;
		var params = {};
		var getStyleByTypeAndLevel = function(__type , __level){
			//__type->0：普通；1：消息；2：座席；3：电话；
			//__level->0：普通；1：成功；2：失败；3：警告；4：禁止
			var blueColor = '#008dda';
			var greenColor = '#8FC31F';
			var redColor = '#EB6100';
			var iconClass = 'fa fa-bell';
			var iconColor = '#ffffff';
			var txtColor = '#ffffff';
			var iconBgColor = '#ffffff';
			var iconMode = 0;
			switch(__type){
				case 0://普通
					switch(__level){
						case 0://普通
							iconClass = 'fa fa-bell';
							iconBgColor = 'transparent';
						break;
						case 1://成功
							iconClass = 'fa fa-check-circle';
							iconColor = greenColor
						break;
						case 2://失败
							iconClass = 'fa fa-times-circle';
							iconColor = redColor;
						break;
						case 3://警告
							iconClass = 'fa fa-exclamation-circle';
							iconColor = redColor;
						break;
						case 4://禁止
							iconClass = 'fa fa-minus-circle';
							iconColor = redColor;
						break;
						default:
							iconClass = 'fa fa-bell';
							iconBgColor = 'transparent';
						break;
					};
				break;
				case 1://消息
					switch(__level){
						case 0://普通
							iconClass = 'fa fa-envelope';
							iconColor = blueColor;
							iconMode = 1;
						break;
						case 1://成功
							iconClass = 'fa fa-check-circle';
							iconColor = greenColor
						break;
						case 2://失败
							iconClass = 'fa fa-times-circle';
							iconColor = redColor;
						break;
						case 3://警告
							iconClass = 'fa fa-envelope';
							iconBgColor = redColor;
							iconMode = 1;
						break;
						case 4://禁止
							iconClass = 'fa fa-minus-circle';
							iconColor = redColor;
						break;
						default:
							iconClass = 'fa fa-bell';
							iconBgColor = 'transparent';
						break;
					};
				break;
				case 2://座席
					switch(__level){
						case 0://普通
							iconClass = 'fa fa-bell';
							iconBgColor = 'transparent';
						break;
						case 1://成功
							iconClass = 'fa fa-check-circle';
							iconColor = greenColor
						break;
						case 2://失败
							iconClass = 'fa fa-times-circle';
							iconColor = redColor;
						break;
						case 3://警告
							iconClass = 'fa fa-exclamation-circle';
							iconColor = redColor;
						break;
						case 4://禁止
							iconClass = 'fa fa-minus-circle';
							iconColor = redColor;
						break;
						default:
							iconClass = 'fa fa-bell';
							iconBgColor = 'transparent';
						break;
					};
				break;
				case 3://电话
					switch(__level){
						case 0://普通
							iconClass = 'fa fa-bell';
							iconBgColor = 'transparent';
						break;
						case 1://成功
							iconClass = 'fa fa-check-circle';
							iconColor = greenColor
						break;
						case 2://失败
							iconClass = 'fa fa-times-circle';
							iconColor = redColor;
						break;
						case 3://警告
							iconClass = 'fa fa-exclamation-circle';
							iconColor = redColor;
						break;
						case 4://禁止
							iconClass = 'fa fa-minus-circle';
							iconColor = redColor;
						break;
						default:
							iconClass = 'fa fa-bell';
							iconBgColor = 'transparent';
						break;
					};
				break;
			};
			return {
				iconClass : iconClass,
				iconColor : iconColor,
				txtColor : txtColor,
				iconBgColor : iconBgColor,
				iconMode : iconMode
			};
		};
		return {
			init : function(__params){
				if(__params){
					params.txtel = __params.txtel;
					if(params.txtel){
						params.txtel.html('<span class="fa fa-bell fa-lg"></span><span>&nbsp;&nbsp;'+$.i18n.prop('top.msg.nonewmsg')+'</span>');
					}
				}
			},
			show : function(__params){
				if(__params){
					params.txt = __params.txt || $.i18n.prop('top.msg.havanewmsg');
					params.mov = __params.mov == true;
					params.movSecond = __params.movSecond || 15000;
					params.type = __params.type || 0;
					params.level = __params.level || 0;
					params.blink = __params.blink == true;
					var styleMap = getStyleByTypeAndLevel(params.type, params.level);
					if(params.txtel){
						window.clearTimeout(timer);
						var imgDom = null;
						if(styleMap.iconMode == 1){
							imgDom = $('<span class="'+styleMap.iconClass+'" style="line-height:18px;border-radius:9px;width:18px; text-align: center; background:'+styleMap.iconBgColor+';"></span>');
						}else{
							imgDom = $('<span class="'+styleMap.iconClass+'" style="line-height:16px;border-radius:8px;width:16px;height:16px;font-size:18px; text-align: center; background:'+styleMap.iconBgColor+';"></span>');
						}
						var txtDom = $('<span>&nbsp;&nbsp;'+params.txt+'</span>');
						var marqueeDom = $('<marquee direction="left" behavior="scroll" scrollamount="4" scrolldelay="0" loop="-1" width="100%" height="100%"></marquee>');
						txtDom.addClass(params.blink ? 'blink' : '');
						if(params.level == 0 && params.type != 1){
							imgDom.css('font-size' , '16px');
						}
						imgDom.css('color' , styleMap.iconColor);
						txtDom.css('color' , styleMap.txtColor);
						if(params.mov){
							marqueeDom.append(imgDom).append(txtDom);
							params.txtel.empty().append(marqueeDom);
							window.clearTimeout(timer);
							if(params.movSecond >=0){
								timer = window.setTimeout(function(){
									window.clearTimeout(timer);
									params.txtel.empty().append(imgDom).append(txtDom.removeClass('blink'));
									imgDom.css('color' , styleMap.iconColor);
									txtDom.css('color' , styleMap.txtColor);
								}, params.movSecond);
							}
						}else{
							params.txtel.empty().append(imgDom).append(txtDom);
						}
					}
				}
			}
		};
	})();
	
	var util = {
		htmlEncode : function(__string){
			if ( __string == undefined || __string == null)
			{
				return "";
			}
			var str = __string + "";
			if(str == '')
			{
				return str;
			}
			str = str.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(new RegExp("\"","g"),"&quot;").
			replace(new RegExp("\'","g"),"&#39;").replace(new RegExp("  ","g")," &nbsp;");
			return str;
		},
		secondToDate : function(__second) {
			var second = __second || 0, hour = 0, minute = 0;
			var timeStringArray = [];
			if (second >= 3600) {
				hour = parseInt(second / 3600, 10);
				timeStringArray.push(hour + ' 小时 ');
				second = second % 3600;
			}
			if (second >= 60) {
				minute = parseInt(second / 60, 10);
				timeStringArray.push(minute + ' 分 ');
				second = second % 60;
			}
			second && timeStringArray.push(parseInt(second, 10) + ' 秒');
			return {
				text : timeStringArray.join(''),
				hour : hour,
				minute : minute,
				second : second
			};
		},
		minuteToDate : function(__minute) {
			var minute = __minute || 0, day = 0, hour = 0;
			var timeStringArray = [];
			if (minute >= 1440) {
				day = parseInt(minute / 1440, 10);
				timeStringArray.push(day + ' 天 ');
				minute = minute % 1440;
			}
			if (minute >= 60) {
				hour = parseInt(minute / 60, 10);
				timeStringArray.push(hour + ' 小时 ');
				minute = minute % 60;
			}
			minute && timeStringArray.push(parseInt(minute, 10) + ' 分');
			return {
				text : timeStringArray.join(''),
				day : day,
				hour : hour,
				minute : minute
			};
		},
		getRandomString : function() {
			return (new Date).getTime().toString(36) + Math.floor(Math.random() * 1e8).toString(36);
		},  getDateTime : function(date) {
			if(!date){
				date = new Date();
			}
			var year = date.getFullYear();
			var month = date.getMonth() + 1;
			var day = date.getDate();
			var hours = date.getHours();
			var minutes = date.getMinutes();
			month <= 9 && ( month = "0" + month);
			day <= 9 && ( day = "0" + day);
			hours <= 9 && ( hours = "0" + hours);
			minutes <= 9 && ( minutes = "0" + minutes);
			return {
				date : year + '-' + month + '-' + day,
				time : hours + ':' + minutes
			};
		},	getDateTimeAll : function(date) {
			if(!date){
				date = new Date();
			}
			var year = date.getFullYear();
			var month = date.getMonth() + 1;
			var day = date.getDate();
			var hours = date.getHours();
			var minutes = date.getMinutes();
			var seconds = date.getSeconds();
			month <= 9 && ( month = "0" + month);
			day <= 9 && ( day = "0" + day);
			hours <= 9 && ( hours = "0" + hours);
			minutes <= 9 && ( minutes = "0" + minutes);
			seconds <= 9 && ( seconds = "0" + seconds);
			return {
				date : year + '-' + month + '-' + day,
				time : hours + ':' + minutes + ':' + seconds
			};
		},	getDateTimeZone : function(flag, zone, date) { //flag为true表示增加时区，反之表示减少时区
			if(!date){
				date = new Date();
			}
			if(flag){
				date.setHours(date.getHours()+zone);
			}else{
				date.setHours(date.getHours()-zone);
			}
			var year = date.getFullYear();
			var month = date.getMonth() + 1;
			var day = date.getDate();
			var hours = date.getHours();
			var minutes = date.getMinutes();
			var seconds = date.getSeconds();
			month <= 9 && ( month = "0" + month);
			day <= 9 && ( day = "0" + day);
			hours <= 9 && ( hours = "0" + hours);
			minutes <= 9 && ( minutes = "0" + minutes);
			seconds <= 9 && ( seconds = "0" + seconds);
			return {
				date : year + '-' + month + '-' + day,
				time : hours + ':' + minutes + ':' + seconds
			};
		}, getDateTimeWeekAll : function(date) {
			if(!date){
				date = new Date();
			}
			var year = date.getFullYear();
			var month = date.getMonth() + 1;
			var day = date.getDate();
			var hours = date.getHours();
			var minutes = date.getMinutes();
			var seconds = date.getSeconds();
			month <= 9 && ( month = "0" + month);
			day <= 9 && ( day = "0" + day);
			hours <= 9 && ( hours = "0" + hours);
			minutes <= 9 && ( minutes = "0" + minutes);
			seconds <= 9 && ( seconds = "0" + seconds);
			var week = date.getDay();
			return {
				date : year + '/' + month + '/' + day,
				week : week,
				cday : day==new Date().getDate()?true:false,
				time : hours + ':' + minutes
			};
		},	getDateByLocalhost : function(date) {
			if(!date){
				return new Date();
			}
			var strDate = date;
			strDate = strDate.replace(/-/g,' ').replace(/:/g,' ').split(' ');
			date = new Date(strDate[0],strDate[1]-1,strDate[2],strDate[3],strDate[4],strDate[5]);
			return date;
		},compare :function(startTime,endTime){
			startTime = startTime.replace(new RegExp('-','gm'),'/');
			endTime = endTime.replace(new RegExp('-','gm'),'/');
			if(startTime && endTime){
				var st = new Date(startTime).getTime();
				var et = new Date(endTime).getTime();
				if(st<et || st===et){
					return true;
				}else{
					return false;
				}
			}
			return true;
		},compareForDay :function(startTime,endTime){
			startTime = startTime.replace(new RegExp('-','gm'),'/');
			endTime = endTime.replace(new RegExp('-','gm'),'/');
			if(startTime && endTime){
				var st = new Date(startTime).getTime();
				var et = new Date(endTime).getTime();
				var timeOfFiveYear = new Date().getTime() - 5*365*24*60*60*1000;
				if(st < et && (st > (et - 31*24*60*60*1000)) && st > timeOfFiveYear){
					return true;
				}else{
					return false;
				}
			}
			return true;
		},compareForWeek :function(startTime,endTime){
			startTime = startTime.replace(new RegExp('-','gm'),'/');
			endTime = endTime.replace(new RegExp('-','gm'),'/');
			var timeOfTenYear = new Date().getTime() - 10*365*24*60*60*1000;
			if(startTime && endTime){
				var st = new Date(startTime).getTime();
				var et = new Date(endTime).getTime();
				if(st < et && (st > (et - 62*24*60*60*1000)) && st > timeOfTenYear ){
					return true;
				}else{
					return false;
				}
			}
			return true;
		},compareForMonth :function(startTime,endTime){
			startTime = startTime.replace(new RegExp('-','gm'),'/');
			endTime = endTime.replace(new RegExp('-','gm'),'/');
			
			var timeOfTenYear = new Date().getTime() - 10*365*24*60*60*1000;
			if(startTime && endTime){
				
				var ds = new Date(startTime);
				var de = new Date(endTime);
				
				var st = ds.getTime();
				var et = de.getTime();
				
				if(ds.getFullYear() > de.getFullYear())
				{
					return false;
				}
				if(ds.getMonth() >= de.getMonth())
				{
					return false;
				}
				
				if(st<et && st > timeOfTenYear && (st > (et - 124*24*60*60*1000))){
					return true;
				}else{
					return false;
				}
			}
			return true;
		},compareForInterval :function(startTime,endTime){  //检查开始时间和结束时间是否有效
			startTime = startTime.replace(new RegExp('-','gm'),'/');
			endTime = endTime.replace(new RegExp('-','gm'),'/');
			
			if(startTime && endTime){
				
				var ds = new Date(startTime);
				var de = new Date(endTime);
				
				var st = ds.getTime();
				var et = de.getTime();
			
				//开始时间必须早于结束时间并且在90天以内
				var timeOfNinetyDays = new Date().getTime() - 91*24*60*60*1000; //90天
				if(st<et && st > timeOfNinetyDays && (st > (et - 91*24*60*60*1000))){
					return true;
				}else{
					return false;
				}
			}
			return true;
		},
		getTimeZone : function(){
			var date = new Date();
			return parseInt(-date.getTimezoneOffset()/60,10);
		},
		cookie : (function() {
			var cookie = {};
			var getSecond = function(__str) {
				var str1 = __str.substring(1, __str.length) * 1;
				var str2 = __str.substring(0, 1);
				if (str2 == "s") {
					return str1 * 1000;
				} else if (str2 == "h") {
					return str1 * 60 * 60 * 1000;
				} else if (str2 == "d") {
					return str1 * 24 * 60 * 60 * 1000;
				}
			};
			cookie.set = function(__name, __value, __time) {
				var strsec = getSecond(__time || "d18300");
				var exp = new Date();
				exp.setTime(exp.getTime() + strsec * 1);
				document.cookie = __name + "=" + escape(__value) + ";expires=" + exp.toGMTString();
			};
			cookie.get = function(__name) {
				var arr, reg = new RegExp("(^| )" + __name + "=([^;]*)(;|$)");
				if ( arr = document.cookie.match(reg)) {
					return (unescape(arr[2]));
				} else {
					return null;
				}
			};
			cookie.del = function(__name) {
				var exp = new Date();
				exp.setTime(exp.getTime() - 1);
				var cval = getCookie(__name);
				if (cval != null) {
					document.cookie = __name + "=" + cval + ";expires=" + exp.toGMTString();
				}
			};
			return cookie;
		})(),
		regExpPattern : {
			num : /^\d{1,10}$/,
			telephone : /^\d{0,20}$/,
			year : /^\d{4}$/,
			email : /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/,
			username : /^[a-zA-Z][a-zA-Z0-9_]{0,19}$/,
			url : /^((https|http)?:\/\/)?(([0-9]{1,3}\.){3}[0-9]{1,3}|([0-9a-z_!~*'()-]+\.)*([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\.[a-z]{2,6})(:[0-9]{1,4})?((\/?)|(\/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+\/?)$/,
			hasChinese : /.*[\u4e00-\u9fa5]+.*$/,
			price : /^\d{1,7}(\.\d{1,2})?$/
		},
		strlen : function(str) {
			var len = 0;
			for (var i = 0; i < (str || '').length; i++) {
				var c = str.charCodeAt(i);
				//单字节加1
				if ((c >= 0x0001 && c <= 0x007e) || (0xff60 <= c && c <= 0xff9f)) {
					len++;
				} else {
					len += 2;
				}
			}
			return len;
		},
		getUrlParam : function(__name) {
    		var reg = new RegExp('(^|&)' + __name + '=([^&]*)(&|$)', 'i');
   	 		var r = window.location.search.substr(1).match(reg);
    		if (r != null) {
        		return unescape(r[2]);
    		}
    		return null;
		},
		bindHandler : (function() {
            if (window.addEventListener) {// 标准浏览器
                return function(elem, type, handler) {// elem:节点    type:事件类型   handler:事件处理程序
                    // 最后一个参数为true:在捕获阶段调用事件处理程序    为false:在冒泡阶段调用事件处理程序
                    elem.addEventListener(type, handler, false);
                }
            } else if (isIE()) {// IE浏览器
                return function(elem, type, handler) {
                    elem.attachEvent("on" + type, handler);
                }
            }
        })(),
		copyToClipboard : function(s){
			
		}
	};
	
	/**
	 * 生成分页信息
	 * totalSize 总条数
	 * page_type 显示分页信息之div之id 
	 * myPage 当前页
	 * tatalPage 总页数
	 * _getData 回调获取数据的函数
	 */
	var initPage = function(totalSize,tatalPage,page_type,myPage,_getData){
		$("#"+page_type).empty();
	//	if(tatalPage<=1)return;
		
		myPage = parseInt(myPage);
		
		var gong = $.i18n.prop('common.system.total_1');
		var tiao = $.i18n.prop('common.system.total_2');
		var str = '<div style="margin: 30px 0;"><div class="pull-right" style="margin-top: 5px; margin-left: 20px;">'+gong +(totalSize || 0) + tiao+'</div>';
		str += '<div class="pull-right input-group input-group-sm" style="width: 80px; margin-left:20px;">';
		str += '<input type="text" class="form-control" id="'+page_type+'page_number" style="text-align: center;">';
		str += '<span class="input-group-addon pointer" style="background-color: #ECF7FD;" id="'+page_type+'go_page_number">Go</span> </div>';
		str +='<ul class="pagination">';
		str += '<li  id="'+page_type+'first_page"><a style="cursor:pointer;"><span aria-hidden="true" class="fa fa-step-backward"></span></a></li>';
		str += '<li  id="'+page_type+'previous_page"><a style="cursor:pointer;" aria-label="Previous" ><span aria-hidden="true" class="fa fa-chevron-left"></span></a></li>';
		str +='<li ><a name="page_numbers">'+myPage+' / '+tatalPage+'</a></li>';
		str += '<li id="'+page_type+'next_page"><a style="cursor:pointer;" aria-label="Next"><span aria-hidden="true" class="fa fa-chevron-right"></span></a></li>';
		str += '<li id="'+page_type+'last_page"><a style="cursor:pointer;"><span aria-hidden="true" class="fa fa-step-forward"></span></a></li> ';
		str +='</ul></div>';
		
		
		$(str).appendTo($("#"+page_type));
		str=null;
		
		if(myPage==1){
			$("#"+page_type+"previous_page").attr("class","disabled");
			$("#"+page_type+"first_page").attr("class","disabled");
		}else{
			$("#"+page_type+"previous_page").attr("class","");
			$("#"+page_type+"previous_page").click(function(){_getData(myPage-1);});
			$("#"+page_type+"first_page").click(function(){_getData(1);});
		}
		
		if(myPage==tatalPage){
			$("#"+page_type+"next_page").attr("class","disabled");
			$("#"+page_type+"last_page").attr("class","disabled");
		}
		else{
			$("#"+page_type+"next_page").attr("class","");
			$("#"+page_type+"next_page").click(function(){_getData(myPage+1);});
			$("#"+page_type+"last_page").click(function(){_getData(tatalPage);});
		}
		
		$("#"+page_type+"go_page_number").click(function(){
			num = $("#"+page_type+"page_number").val();
			if(!isNaN(num) && num>=1 && num<=tatalPage){
				_getData(parseInt(num));
			}else{
				tips({
					type : "warning",
					content : $.i18n.prop('common.system.range')
				});
			}
		});
		
		$("#"+page_type+"page_number").keyup(function(event){
			if(event.keyCode==13)
				$("#"+page_type+"go_page_number").click();
		});
        $("#"+page_type+"page_number").on('input' , function(){
			$(this).val($(this).val().replace(/[^\d]/g, ''));
		});
		
		$("#"+page_type+"page_number").on('propertychange' , function(){
			$(this).val($(this).val().replace(/[^\d]/g, ''));
		});
		
		$("#"+page_type+"page_number").focus(function(){
			$("#"+page_type+"page_number").attr('isfocus' , '1');
		});
		
		$("#"+page_type+"page_number").blur(function(){
			$("#"+page_type+"page_number").attr('isfocus' , '0');
		});
		
		return {
			getGoPageInput : function(){
				return $.trim($("#"+page_type+"page_number").val());
			},
			setGoPageInput : function(__val){
				__val = $.trim(__val).replace(/[^\d]/g, '');
				if(__val){
					$("#"+page_type+"page_number").val(__val);
				}
			},
			isFocus : function(){
				return $("#"+page_type+"page_number").attr('isfocus') == '1';
			},
			setFocus : function(){
				$("#"+page_type+"page_number").attr('isfocus' , '1').focus();
			},
			
		}
	};
	
	
	
	
	var logger = {
		flag : tacCfg.debugconsole,
		info : function(__info){
			logger.flag && console.log(__info);
		},
		warn : function(__info){
			logger.flag && console.warn(__info);
		},
		error : function(__info){
			logger.flag && console.error(__info);
		}
	};
	
	var fullscreen={
			go : function(){
				elem=document.body;
				
				if(elem.webkitRequestFullScreen){
					elem.webkitRequestFullScreen(); 	
				}else if(elem.mozRequestFullScreen){
					elem.mozRequestFullScreen();
				}else if(elem.requestFullScreen){
					elem.requestFullscreen();
				}else if(elem.msRequestFullscreen){
					elem.msRequestFullscreen();
				}else{
					//浏览器不支持全屏API或已被禁用
				}
			},
		cancel : function(){
			var elem=document;
			if(elem.webkitCancelFullScreen){
				elem.webkitCancelFullScreen(); 	
			}else if(elem.mozCancelFullScreen){
				elem.mozCancelFullScreen();
			}else if(elem.cancelFullScreen){
				elem.cancelFullScreen();
			}else if(elem.exitFullscreen){
				elem.exitFullscreen();
			}else if(elem.msExitFullscreen){
				elem.msExitFullscreen();
			}else{
				//浏览器不支持全屏API或已被禁用
			}
		}
	};

	
	//埋码
	var hwa ={
		//事件埋码方法---人工上报互交事件(Event)
		trackEvent : function(param){
			if(param){
				param.dimension5 = tac.data.get('AGENT_SITE_ID') || '';
				logger.info("-----------------事件埋码参数开始-----------------");
				logger.info(param);
				logger.info("-----------------事件埋码参数结束-----------------");
				ha("set",param);
			    ha("trackEvent","click");
			    ha("setPageComplete");
			}
		},
		//主动上报访问(PV)埋码
		trackPageView : function(keyObj,startTime){
			if(keyObj){
				logger.info("-----------------性能埋码参数开始-----------------");
				keyObj.param.dimension1 = tac.data.get("AGENT_ID") || '';
				keyObj.param.dimension5 = tac.data.get('AGENT_SITE_ID') || '';
				logger.info(keyObj.param);
				logger.info("-----------------性能埋码参数结束-----------------");

			}
		},
		//局部刷新页面-性能计算开始时间
		trackPP : function(keyObj){
			keyObj.param.dimension5 = tac.data.get('AGENT_SITE_ID') || '';
			keyObj.param.dimension1 = tac.data.get("AGENT_ID") || '';
			ha("set",keyObj.param);
			ha("trackHistory");
		}
	};
	/**
     * 扩展js方法
     */
    Array.prototype.delByCon = function(content,deleteAll) {
        for (var i = 0; i < this.length; i++) {
            if (this[i] == content) {
                this.splice(i, 1);
                if(!deleteAll){
                    return;
                }
            }
        }
    };
	String.prototype.getBytes = function () {
	    var cArr = this.match(/[^\x00-\xff]/ig);
	    return this.length + (cArr == null ? 0 : cArr.length * 2);
	};
	
	/**
	 * 例子：欢迎{{username}}登陆
	 * 调用:"欢迎{{username}}登陆".displace({'username':'张三'});
	 * 返回:"欢迎张三登陆"
	 */
	String.prototype.displace = function(obj){
		 return this.replace(/\{\{(\w*)\}\}/g, function(t,k){return obj[k] || t;});
	};	
	

	
	/**
	 * is IE
	 */
	var isIE = function(){
		return navigator.userAgent.indexOf("Trident") >=0 ? true : false;
	};
	
	
	var _phoneInitFailedTip = function(){
		
	}
	
	
	
	return window.tac = tac = {
		data : data,
		Map:Map,
		ajax : ajax,
		config : config,
		util : util,
		initPage:initPage,
		prompt : _prompt,
		promptEx : _promptEx,
		tips : tips,
		logger : logger,
		blinkTitleTask : blinkTitleTask,
		showNotification : showNotification,
		showTopMsg : showTopMsg,
		fullscreen : fullscreen,
		loadingShow : loadingShow,
		setPromptFlag : __setPromptFlag,
		isIE : isIE,
		isLogin : false,
		modules : {}
	}
});
