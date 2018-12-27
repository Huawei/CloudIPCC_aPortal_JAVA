define(['tac' ,
		'tacCfg',
		'text!../../../../html/modules/common/phone/phoneCall.html',
		'text!../../../../html/modules/common/phone/phoneCallAgentList.html',
		'text!../../../../html/modules/common/phone/phoneCallAgentListEN.html',//英文模板
		'component/infobox/infobox', 
		'modules/common/phone/phoneService'], function(tac ,tacCfg, html, agentListHtml ,agentListHtmlEN,Infobox, phoneService) {
	var module = null;
	var statusList = [];
	var infobox = null;
	/*
	 * 定义变量
	 */
	var el = null;
	var currentInputType = 0;
	var currentInputTelVal = '';
	var currentInputAgentVal = '';

	var agentQueryAutoFlag = false;
	var agentQueryAutoTimer = 0;
	var maxPhoneNum = 24;
	var tempParam = null;
	var isLoading = false;
	var agentListPage = null;
	var actionWaitFlag = false, actionWaitTime = 500;

	/*
	 * 初始化
	 */
	var init = function(__cfg){
		return module;
	};
	var domMap = null;

	/*设置值*/
	var inputNumber = function(number){
		if(number && domMap.telInput.val().length < maxPhoneNum){
			var pos = domMap.telInput[0].selectionStart;
			var _value = domMap.telInput.val().substring(0,pos);
			var __value = domMap.telInput.val().substring(pos);
			_value = _value + (number + '') + __value;
			domMap.telInput.val(_value);
			pos++;
			domMap.telInput.focus();
			domMap.telInput[0].setSelectionRange(pos,pos);
		}
	};
	/*
	 * 事件列表
	 * 
	 */
	var eventList = [
		{
			target : 'phone_call_infobox_close',
			attach : 'on',
			type : 'click',
			action : function(e) {
				hide();
			}
		},
		{
			target : 'phone_call_infobox_tab_num',
			attach : 'on',
			type : 'click',
			action : function(e) {
				currentInputType = 0;
				$('#phone_call_infobox_tab_num').addClass('active');
				$('#phone_call_infobox_tab_agent').removeClass('active');
				$('#phone_call_infobox_top_self_phone_li').hide();
				$('#phone_call_infobox_tab_num_content').show();
				$('#phone_call_infobox_tab_agent_content').hide();
				$('#phone_call_infobox_tab_num_content_input').focus();
				window.clearTimeout(agentQueryAutoTimer);
				isLoading = false;
			}
		},
		{
			target : 'phone_call_infobox_tab_agent',
			attach : 'on',
			type : 'click',
			action : function(e) {
				currentInputType = 1;
				$('#phone_call_infobox_tab_num').removeClass('active');
				$('#phone_call_infobox_tab_agent').addClass('active');
				$('#phone_call_infobox_top_self_phone_li').hide();
				$('#phone_call_infobox_tab_num_content').hide();
				$('#phone_call_infobox_tab_agent_content').show();
				_loadAgentList();
				
			}
		},
		{
			target : 'phone_call_btm_submit',
			attach : 'on',
			type : 'click',
			action : function(e) {
				if($(this).hasClass('btn-gary') || actionWaitFlag){
					return;
				}
				actionWaitFlag = true;
				window.setTimeout(function(){
					actionWaitFlag = false;
				}, actionWaitTime);

				if(currentInputType == 0){
					//呼出				
					currentInputTelVal = $.trim(domMap.telInput.val());
					if(currentInputTelVal == ""){
						$('#top_phone_hangup_btn').addClass('btn-phone-disabled');
						$(document).off('keypress')
						tac.prompt($.i18n.prop('common.msg.box.title.notice') , $.i18n.prop('common.msg.box.title.callnumberisnull') , $.i18n.prop('common.msg.box.btn.ok'), function(){
							$(document).keypress(keyEnterHandler);
						});
					}
				
					if (currentInputTelVal == $('#top_sign_phone_info_text').text())
					{
						tac.tips({type:'failed',content:$.i18n.prop('phone.msg.canNotCallSelf'),timeout:3000});
						return;
					}
					var systemCallerNo = tac.data.get("OUTCALLERNO");
					var param = {
							"caller" : systemCallerNo,
							"called" : currentInputTelVal
					};
					phoneService.callOut(param, function(__data){
						if(__data.retcode == 0){
							hide();
							tac.modules.phone.setCurrentCallOutCallId(__data.result);
							$('#top_phone_hangup_btn').removeClass('btn-phone-disabled');
							$('#phone_call_btm_submit').addClass('btn-gary').removeClass('btn-success').html($.i18n.prop('phone.btn.calling'));
						}
						else if (__data.retcode == RestRetCode.AGENT_CALLOUT_CALLERNOINVALID)
						{
							tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
					    			$.i18n.prop('phone.msg.callFailure.callernoInvalid'), 
									$.i18n.prop('common.msg.box.btn.ok'));
							resetBtn();
						}
						else{
							tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
					    			$.i18n.prop('phone.msg.callFailure'), 
									$.i18n.prop('common.msg.box.btn.ok'));
							resetBtn();
						}
					});
					
				}else{
					currentInputAgentVal = $.trim(currentInputAgentVal);
					if(currentInputAgentVal){
						var param = {
								"called" : currentInputAgentVal
						};
						phoneService.callInner(param , function(__data){
							if(__data.retcode == 0){
								hide();
								$('#top_phone_hangup_btn').removeClass('btn-phone-disabled');
								$('#phone_call_btm_submit').addClass('btn-gary').removeClass('btn-success').html($.i18n.prop('phone.btn.calling'));
							}else{
								tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
						    			$.i18n.prop('phone.msg.callInnerFailure'), 
										$.i18n.prop('common.msg.box.btn.ok'));
								resetBtn();
							}
						});
					}else{
						$('#top_phone_hangup_btn').addClass('btn-phone-disabled');
						$(document).off('keypress');
						tac.prompt($.i18n.prop('common.msg.box.title.notice') , $.i18n.prop('phone.msg.agentNotSelect') , $.i18n.prop('common.msg.box.btn.ok'), function(){
							$(document).keypress(keyEnterHandler);
						});
					}
				}
			}
		},
		{
			target : 'phone_call_btm_cancel',
			attach : 'on',
			type : 'click',
			action : function(e) {
				hide();
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_delete',
			attach : 'on',
			type : 'click',
			action : function(e) {
				currentInputTelVal = domMap.telInput.val();
				var pos = domMap.telInput[0].selectionStart;
				domMap.telInput.val(currentInputTelVal.substr(0,pos-1) + currentInputTelVal.substr(pos));
				pos--;
				domMap.telInput.focus();
				domMap.telInput[0].setSelectionRange(pos,pos);
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_btn_1',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(1);
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_btn_2',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(2);
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_btn_3',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(3);
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_btn_4',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(4);
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_btn_5',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(5);
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_btn_6',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(6);
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_btn_7',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(7);
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_btn_8',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(8);
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_btn_9',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(9);
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_btn_0',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(0+"");
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_btn_x',
			attach : 'on',
			type : 'click',
			action : function(e) {
				if(domMap.telInput.val().length < maxPhoneNum){
					inputNumber('*');
				}
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_btn_j',
			attach : 'on',
			type : 'click',
			action : function(e) {
				if(domMap.telInput.val().length < maxPhoneNum){
					inputNumber('#');
				}
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_common_0',
			attach : 'on',
			type : 'click',
			action : function(e) {
				currentInputTelVal = $('#phone_call_infobox_tab_num_content_common_0_tel').html();
				domMap.telInput.focus().val(currentInputTelVal);
				domMap.callSubmit.trigger("click");//点击后直接呼出
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_common_1',
			attach : 'on',
			type : 'click',
			action : function(e) {
				currentInputTelVal = $('#phone_call_infobox_tab_num_content_common_1_tel').html();
				domMap.telInput.focus().val(currentInputTelVal);
				domMap.callSubmit.trigger("click");//点击后直接呼出
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_common_2',
			attach : 'on',
			type : 'click',
			action : function(e) {
				currentInputTelVal = $('#phone_call_infobox_tab_num_content_common_2_tel').html();
				domMap.telInput.focus().val(currentInputTelVal);
				domMap.callSubmit.trigger("click");//点击后直接呼出
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_common_3',
			attach : 'on',
			type : 'click',
			action : function(e) {
				currentInputTelVal = $('#phone_call_infobox_tab_num_content_common_3_tel').html();
				domMap.telInput.focus().val(currentInputTelVal);
				domMap.callSubmit.trigger("click");//点击后直接呼出
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_common_4',
			attach : 'on',
			type : 'click',
			action : function(e) {
				currentInputTelVal = $('#phone_call_infobox_tab_num_content_common_4_tel').html();
				domMap.telInput.focus().val(currentInputTelVal);
				domMap.callSubmit.trigger("click");//点击后直接呼出
			}
		},
		{
			target : 'phone_call_infobox_tab_num_content_input',
			attach : 'on',
			type : 'input propertychange',
			action : function(e) {
				if($(this).is(':focus')){
					var pos = $(this)[0].selectionStart;
					var inputValue = $(this).val();
					$(this).val(inputValue.replace(/[^\d|\*}\#]/g, ''));
					$(this)[0].setSelectionRange(pos,pos);
					if($(this).val().length > 14){
						$(this).css('font-size' , '20px');
					}else{
						$(this).css('font-size' , '22px');
					}
				}
			}
		},
		{
			target : 'phone_call_infobox_top_self_phone',
			attach : 'on',
			type : 'input',
			action : function(e) {
				var inputValue = $(this).val();
				$(this).val(inputValue.replace(/[^\d|\*}\#]/g, ''));
			}
		},
		{
			target : 'phone_call_infobox_top_self_phone',
			attach : 'on',
			type : 'propertychange',
			action : function(e) {
				var inputValue = $(this).val();
				$(this).val(inputValue.replace(/[^\d|\*}\#]/g, ''));
			}
		}
	];
	
	var getNewTelInputVal = function(__type, __val){
		var val0 = domMap.telInput.val(), val1 = '';
		var idx = domMap.telInput[0].selectionStart;
		console.log('idx:' + idx );
		console.log('val0.length:' + val0.length );
		if(__type > 0){//添加
			if(idx == 0){
				val1 = __val + val0;
			}else if(idx == val0.length){
				val1 = val0 + __val;
			}else{
				val1 = val0.substr(0, idx) + __val + val0.substr(idx, val0.length);
			}
		}else{//删除
			
		}
		return val1;
	};
	
	/*
	 * 私有方法：初始化事件
	 */
	var _initEvent = function(){
		for (var i = 0; i < eventList.length; i++) {
			$('#' + eventList[i].target)[eventList[i].attach](eventList[i].type, eventList[i].action);
		}
		
	};
	
	var _initElement = function(){
		domMap = {
			telInput : $('#phone_call_infobox_tab_num_content_input'),
			selfTelInput : $('#phone_call_infobox_top_self_phone'),
			agentContent : $('#phone_call_infobox_tab_agent_content'),
			callSubmit : $('#phone_call_btm_submit')
		};
	};
	/*
	 * 私有方法：国际化
	 */
	var _i18nInit = function(){
		if(el){
			el.find(".i18n").each(function(){
				if($(this).attr("i18nKey")){
					$(this).html($.i18n.prop($(this).attr("i18nKey")));
				}
			});
			el.find(".i18nplaceholder").each(function(){
				if($(this).attr("i18nKey")){
					$(this).attr('placeholder',$.i18n.prop($(this).attr("i18nKey")));
				}
			});
		}
	};
	
	/**
	 * 加载试呼的被叫号码
	 */
	var _loadTrialCalledList = function(){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.phone.getTrialCalled,
			type : 'GET',
			contentType : 'application/json',
			loading : true,
			success : function(__data){
				if(__data.retcode == RestRetCode.SUCCESS){
					var calledList = __data.result;
					if (calledList)
					{
						var buffer=[];
						for (var i = 0; i < calledList.length; i++)
						{
							buffer.push('<option value="');
							buffer.push(calledList[i]);
							buffer.push('">');
							buffer.push(calledList[i]);
							buffer.push('</option>');
						}
						$('#phone_call_infobox_call_trialCalled').append(buffer.join(""));
					}
				}
			}
		});
	}
	
	
	var	_loadAgentList = function(){
		isLoading = true;
		window.clearTimeout(agentQueryAutoTimer);
		if(!agentQueryAutoFlag){//自动刷新
			agentQueryAutoFlag = false;
		}
		
		phoneService.getIdleAgentList(function(__data){
			isLoading = false;
			if(__data.retcode == RestRetCode.SUCCESS){
				var cookieLanguage=tac.util.cookie.get();
				if(cookieLanguage==null||cookieLanguage==""){
					cookieLanguage = window.navigator.language || window.navigator.userLanguage;
				}
				__data = _removeSelf(__data);
				if(cookieLanguage=="zh-CN"){
            		domMap.agentContent.empty().append($.tmpl(agentListHtml , __data));
            	}else{
            		domMap.agentContent.empty().append($.tmpl(agentListHtmlEN , __data));
            	}
				_addAgentListEvent();
				if(__data.result.length > 0){
					var isExist = false;
					$('#phone_call_infobox_tab_agent_content').find('input[type=radio]').each(function(){
						jdom = $(this);
						if(jdom.attr('val') == currentInputAgentVal){
							jdom.prop('checked' , true);
							isExist = true;
						}
					});
					if (!isExist)
					{
						currentInputAgentVal = "";
					}
				}
			}
			agentQueryAutoTimer = window.setTimeout(function(){
				_loadAgentList();
			} , tacCfg.time.listAutoRefresh);
		});
	};
	
	/**
	 * 删除自己这个座席工号
	 */
	var _removeSelf = function(__data) {
		var length = __data.result.length;
		if (0 == length) {
			return __data;
		}
		var new_resultArray = [];
		var resultObject;
		var j = 0;
		for (var i = 0; i < length; i++) {
			resultObject = __data.result[i];
			if (resultObject["workno"] != tac.data.get('AGENT_ID')) {
				new_resultArray[j] = resultObject;
				j++;
			}
		}
		__data.result = new_resultArray;
		return __data;
		
		
	}
	
	var _addAgentListEvent = function(){
		$('#phone_call_infobox_tab_agent_content').find('tr').each(function(){
			$(this).click(function(){
				$('#phone_call_infobox_tab_agent_content').find('input[type=radio]').removeProp('checked');
				var r = $(this).find('input[type=radio]').prop('checked' , true);
				currentInputAgentVal = r.attr('val');
			});
		});
	};
	
	/*
	 * 渲染
	 */
	var render = function(){
		infobox = new Infobox({
			el : el = $.tmpl(html),
			drag : true,
			model : true,
			onClose : hide
		});
		_initEvent();
		_initElement();
		_i18nInit();
		return module;
	};
	
	/*
	 * 销毁
	 */
	var destory = function() {
		if(infobox){
			infobox.destory();
		}
		return module;
	};
	
	/*
	 * 按回车呼出
	 */
	var keyEnterHandler = function(e){
		if(e.which == 13){
			domMap.callSubmit.trigger("click");
			e.preventDefault();
		}
	};
	
	/*
	 * 显示
	 */
	var show = function(__mode, __param){
		if(infobox){
			tempParam = __param;
			infobox.show();
			currentInputType = 0;
			currentInputTelVal = '';
			currentInputAgentVal = '';
			$('#phone_call_infobox_call_trialCalled').empty();

			//$('#phone_call_infobox_call_phoneBox_div').show();
			
			domMap.telInput.val(tempParam ? (tempParam.called || '') : '');
			isLoading = false;
			agentListPage = null;
			actionWaitFlag = false;
			$('#phone_call_infobox_tab_num').addClass('active');
			$('#phone_call_infobox_tab_agent').removeClass('active');
			$('#phone_call_infobox_top_self_phone_li').hide();
			$('#phone_call_infobox_tab_num_content').show();
			$('#phone_call_infobox_tab_agent_content').hide();
			$('#phone_call_infobox_tab_num_content_input').focus();
			//如果没设置外呼号码，或者设置了默认号码，则显示 默认主叫号码
			if(null == tac.data.get("OUTCALLERNO") 
					|| tac.data.get("OUTCALLERNO") == '')
			{
				$('#phone_call_SystemCallerNo').text($.i18n.prop('system.config.systemCallerNo.default'));
			}
			else
			{
				$('#phone_call_SystemCallerNo').text(tac.util.htmlEncode(tac.data.get("CallerNoDesc")));
			}
			$('#phone_call_btm_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phonecall.call'));
			$(document).keypress(keyEnterHandler);
			if(__mode == 'out'){
				$('#phone_call_infobox_tab_agent').hide();
			}else{
				$('#phone_call_infobox_tab_agent').show();
			}
		}
		return module;
	};
	/*
	 * 隐藏
	 */
	var hide = function(){
		if(infobox){
			infobox.hide(function(){
				window.clearTimeout(agentQueryAutoTimer);
			});
			$(document).off('keypress');
		}
		return module;
	};
	
	var resetBtn = function(){
		$('#phone_call_btm_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phonecall.call'));
		$('#top_phone_hangup_btn').addClass('btn-phone-disabled');
	};

	

	/*
	 * 暴露接口
	 */
	return module = {
		init : init,
		render : render,
		show : show,
		hide : hide,
		destory : destory,
		resetBtn : resetBtn
	};
});
