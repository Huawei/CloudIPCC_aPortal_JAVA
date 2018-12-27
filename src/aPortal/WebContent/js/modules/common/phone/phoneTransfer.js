define(['tac' ,
		'tacCfg',
		'text!../../../../html/modules/common/phone/phoneTransfer.html',
		'text!../../../../html/modules/common/phone/phoneTransferSkillList.html',
		'text!../../../../html/modules/common/phone/phoneTransferSkillListEN.html',
		'text!../../../../html/modules/common/phone/phoneTransferAgentList.html',
		'text!../../../../html/modules/common/phone/phoneTransferAgentListEN.html',
		'component/infobox/infobox',
		'modules/common/phone/phoneService'], function(tac ,tacCfg, html, skillListHtml,skillListHtmlEN, agentListHtml, agentListHtmlEN, Infobox, phoneService) {
	var module = null;
	var statusList = [];
	var infobox = null;
	/*
	 * 定义变量
	 */
	var el = null;
	var domMap = null;
	var currentInputType = 0;
	var currentInputSkillVal = '';
	var currentInputSkillLoginNum = 0;
	var currentInputAgentVal = '';
	var currentInputAgentTxt = '';

	var currentInputSelfTelVal = '';
	var currentInputTelVal = '';

	var callid = '';


	var queryAutoFlag = false;
	var queryAutoTimer = 0;
	var maxPhoneNum = 24;
	var isLoading = false;
	var actionWaitFlag = false, actionWaitTime = 500;
	var inputQueryDelayTimer = 0;
	/*
	 * 初始化
	 */
	var init = function(__cfg){
		return module;
	};
	

	
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
	 */
	var eventList = [
		{
			target : 'phone_transfer_infobox_close',
			attach : 'on',
			type : 'click',
			action : function(e) {
				hide();
			}
		},
		{
			target : 'phone_transfer_infobox_tab_skill',
			attach : 'on',
			type : 'click',
			action : function(e) {
				currentInputType = 0;
				$('#phone_transfer_infobox_tab_skill').addClass('active');
				$('#phone_transfer_infobox_tab_agent').removeClass('active');
				$('#phone_transfer_infobox_tab_tel').removeClass('active');
				$('#phone_transfer_infobox_tab_skill_content').show();
				$('#phone_transfer_infobox_tab_agent_content').hide();
				$('#phone_transfer_infobox_tab_tel_content').hide();
				$('#phone_transfer_infobox_tab_tel_li').hide();
				
				window.clearTimeout(queryAutoTimer);
				_loadSkillList();
				
				$("#phone_transfer_btm_submit").show();
				$("#phone_transfer_callParty_btm_submit").hide();
				$("#phone_transfer_3Party_btm_submit").hide();
			}
		},
		{
			target : 'phone_transfer_infobox_tab_agent',
			attach : 'on',
			type : 'click',
			action : function(e) {
				currentInputType = 1;
				$('#phone_transfer_infobox_tab_skill').removeClass('active');
				$('#phone_transfer_infobox_tab_agent').addClass('active');
				$('#phone_transfer_infobox_tab_tel').removeClass('active');
				$('#phone_transfer_infobox_tab_skill_content').hide();
				$('#phone_transfer_infobox_tab_agent_content').show();
				$('#phone_transfer_infobox_tab_tel_content').hide();
				$('#phone_transfer_infobox_tab_tel_li').hide();
				$('#phone_transfer_infobox_top_agent_id').focus();
				_loadAgentList();
				
				$("#phone_transfer_btm_submit").show();
				$("#phone_transfer_callParty_btm_submit").hide();
				$("#phone_transfer_3Party_btm_submit").hide();
			}
		},
		{
			target : 'phone_transfer_infobox_tab_tel',
			attach : 'on',
			type : 'click',
			action : function(e) {
				currentInputType = 2;
				$('#phone_transfer_infobox_tab_skill').removeClass('active');
				$('#phone_transfer_infobox_tab_agent').removeClass('active');
				$('#phone_transfer_infobox_tab_tel').addClass('active');
				$('#phone_transfer_infobox_tab_skill_content').hide();
				$('#phone_transfer_infobox_tab_agent_content').hide();
				$('#phone_transfer_infobox_tab_tel_content').show();
				$('#phone_transfer_infobox_tab_tel_li').show();
				$('#phone_transfer_infobox_tel_num').css('font-size','17px');
				$('#phone_transfer_infobox_tel_num').focus();
				window.clearTimeout(queryAutoTimer);
				isLoading = false;
				$("#phone_transfer_btm_submit").hide();
				$("#phone_transfer_callParty_btm_submit").show();
				$("#phone_transfer_3Party_btm_submit").show();
			}
		},
		{
			target : 'phone_transfer_btm_submit',
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
					//转技能，
					currentInputSkillVal = $.trim(currentInputSkillVal);
					if(currentInputSkillVal){
						if (currentInputSkillLoginNum == 0)
						{
							tac.tips({type:'failed',content:$.i18n.prop('phone.msg.nologinAgent'),timeout:3000});
							return;
						}
						var param = {
							"deviceType" : 1,
							"address" : currentInputSkillVal,
							"mode" : 2
						};
						_doTransfer(false, param);
					}else{
						//技能队列名称不能为空
						$(document).off('keypress');
						tac.prompt($.i18n.prop('common.msg.box.title.notice') , $.i18n.prop('phone.msg.skillNotSelect') , $.i18n.prop('common.msg.box.btn.ok'), function(){
							$(document).keypress(keyEnterHandler);
						});
					}
					
				}else if(currentInputType == 1){
					//转座席
					currentInputAgentVal = $.trim(currentInputAgentVal);
					if(currentInputAgentVal){
						var param = {
							"deviceType" : 2,
							"address" : currentInputAgentVal,
							"mode" : 2
						};
						_doTransfer(false, param);
					}else{
						$(document).off('keypress');
						tac.prompt($.i18n.prop('common.msg.box.title.notice') , $.i18n.prop('phone.msg.agentNotSelect') , $.i18n.prop('common.msg.box.btn.ok'), function(){
							$(document).keypress(keyEnterHandler);
						});
					}
				}
			}
		},
		{
			target : 'phone_transfer_callParty_btm_submit',
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
				currentInputTelVal = $.trim(domMap.telInput.val());
				if(currentInputTelVal){
					var param = {
						"deviceType" : 5,
						"address" : currentInputTelVal,
						"mode" : 3
					};
					_doTransfer(true, param);
				}else{
					$(document).off('keypress');
					tac.prompt($.i18n.prop('common.msg.box.title.notice') , $.i18n.prop('phone.msg.numNotInput') , $.i18n.prop('common.msg.box.btn.ok'), function(){
						$(document).keypress(keyEnterHandler);
					});
				}
			}
		},
		{
			target : 'phone_transfer_3Party_btm_submit',
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
				currentInputTelVal = $.trim(domMap.telInput.val());
				if(currentInputTelVal){
					var param = {
						"deviceType" : 5,
						"address" : currentInputTelVal,
						"mode" : 4
					};
					_doTransfer(true, param);
				}else{
					$(document).off('keypress');
					tac.prompt($.i18n.prop('common.msg.box.title.notice') , $.i18n.prop('phone.msg.numNotInput') , $.i18n.prop('common.msg.box.btn.ok'), function(){
						$(document).keypress(keyEnterHandler);
					});
				}
			}
		},
		{
			target : 'phone_transfer_btm_cancel',
			attach : 'on',
			type : 'click',
			action : function(e) {
				hide();
			}
		},
		{
			target : 'phone_transfer_infobox_tab_num_content_delete',
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
			target : 'phone_transfer_infobox_tab_tel_content_btn_1',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(1);
			}
		},
		{
			target : 'phone_transfer_infobox_tab_tel_content_btn_2',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(2);
			}
		},
		{
			target : 'phone_transfer_infobox_tab_tel_content_btn_3',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(3);
			}
		},
		{
			target : 'phone_transfer_infobox_tab_tel_content_btn_4',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(4);
			}
		},
		{
			target : 'phone_transfer_infobox_tab_tel_content_btn_5',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(5);
			}
		},
		{
			target : 'phone_transfer_infobox_tab_tel_content_btn_6',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(6);
			}
		},
		{
			target : 'phone_transfer_infobox_tab_tel_content_btn_7',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(7);
			}
		},
		{
			target : 'phone_transfer_infobox_tab_tel_content_btn_8',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(8);
			}
		},
		{
			target : 'phone_transfer_infobox_tab_tel_content_btn_9',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber(9);
			}
		},
		{
			target : 'phone_transfer_infobox_tab_tel_content_btn_0',
			attach : 'on',
			type : 'click',
			action : function(e) {
				inputNumber("0");
			}
		},
		{
			target : 'phone_transfer_infobox_tab_tel_content_btn_x',
			attach : 'on',
			type : 'click',
			action : function(e) {
				if(domMap.telInput.val().length < maxPhoneNum){
					currentInputTelVal = domMap.telInput.val() + '*';
					domMap.telInput.val(currentInputTelVal);
				}
			}
		},
		{
			target : 'phone_transfer_infobox_tab_tel_content_btn_j',
			attach : 'on',
			type : 'click',
			action : function(e) {
				if(domMap.telInput.val().length < maxPhoneNum){
					currentInputTelVal = domMap.telInput.val() + '#';
					domMap.telInput.val(currentInputTelVal);
				}
			}
		},
		{
			target : 'phone_transfer_infobox_top_agent_id',
			attach : 'on',
			type : 'input',
			action : function(e) {
				var me = this;
				window.clearTimeout(inputQueryDelayTimer);
				inputQueryDelayTimer = window.setTimeout(function(){
					if(agentQueryText != $(me).val()){
						agentQueryText = $(me).val();
						_loadAgentList();
					}
				} , 500);
			}
		},
		{
			target : 'phone_transfer_infobox_top_agent_id',
			attach : 'on',
			type : 'propertychange',
			action : function(e) {
				var me = this;
				window.clearTimeout(inputQueryDelayTimer);
				inputQueryDelayTimer = window.setTimeout(function(){
					if(agentQueryText != $(me).val()){
						agentQueryText = $(me).val();
						_loadAgentList();
					}
				} , 500);
			}
		},
		{
			target : 'phone_transfer_infobox_self_tel_num',
			attach : 'on',
			type : 'input',
			action : function(e) {
				var inputValue = $(this).val();
				$(this).val(inputValue.replace(/[^\d|\*}\#]/g, ''));
			}
		},
		{
			target : 'phone_transfer_infobox_self_tel_num',
			attach : 'on',
			type : 'propertychange',
			action : function(e) {
				var inputValue = $(this).val();
				$(this).val(inputValue.replace(/[^\d|\*}\#]/g, ''));
			}
		},
		{
			target : 'phone_transfer_infobox_tel_num',
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
		}
	];
	
	/**
	 * 执行转移
	 * @param isTransferOut 是否为转外部电话
	 * @param param 
	 */
	var _doTransfer = function (isTransferOut, param) {
		phoneService.transfer(param, function(__data){
			if(__data.retcode == RestRetCode.SUCCESS){
				hide();
				tac.modules.phone.isTransfering(isTransferOut);
				$('#phone_transfer_btm_submit').addClass('btn-gary').removeClass('btn-success').html($.i18n.prop('phone.btn.transfering'));
				$('#phone_transfer_callParty_btm_submit').addClass('btn-gary').removeClass('btn-success').html($.i18n.prop('phone.btn.transfering'));
				$('#phone_transfer_3Party_btm_submit').addClass('btn-gary').removeClass('btn-success').html($.i18n.prop('phone.btn.transfering'));
			}else{
				tac.tips({type:'failed',content:$.i18n.prop('phone.msg.transferFailure'),timeout:3000});
			}
		});
	}
	
	
	/*
	 * 私有方法：初始化DOM对象
	 */
	var _initElement = function(){
		domMap = {
			telInput : $('#phone_transfer_infobox_tel_num'),
			selfTelInput : $('#phone_transfer_infobox_self_tel_num'),
			skillListContent : $('#phone_transfer_infobox_tab_skill_content'),
			agentListContent : $('#phone_transfer_infobox_tab_agent_content')
		};
	};
	/*
	 * 私有方法：初始化事件
	 */
	var _initEvent = function(){
		for (var i = 0; i < eventList.length; i++) {
			$('#' + eventList[i].target)[eventList[i].attach](eventList[i].type, eventList[i].action);
		}
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
	
	
	var	_loadSkillList = function(){
		isLoading = true;
		window.clearTimeout(queryAutoTimer);
		if(!queryAutoFlag){//自动刷新
			queryAutoFlag = false;
		}

		phoneService.getSkillList(function(__data){
			isLoading = false;
			if(__data.retcode == RestRetCode.SUCCESS){
				//473170    根据语言更改模板
                var cookieLanguage=tac.util.cookie.get();
                if(cookieLanguage==null||cookieLanguage==""){
					cookieLanguage = window.navigator.language || window.navigator.userLanguage;                    
                }
                if(cookieLanguage=="zh-CN"){
                	domMap.skillListContent.empty().append($.tmpl(skillListHtml , __data));
                }else{
                	domMap.skillListContent.empty().append($.tmpl(skillListHtmlEN , __data));
                }
				_addSkillListEvent(__data);	
				if(__data.result.length > 0){
					$('#phone_transfer_infobox_tab_skill_content').find('input[type=radio]').each(function(){
						jdom = $(this);
						if(jdom.attr('val') == currentInputSkillVal){
							jdom.prop('checked' , true);
							currentInputSkillLoginNum = jdom.attr('loginNum');
						}
					});
				}
				else
				{
					currentInputSkillLoginNum = 0;
					currentInputSkillVal = '';
				}
			}
			queryAutoTimer = window.setTimeout(function(){
				_loadSkillList();
			}, tacCfg.time.listAutoRefresh);
		});
	};
	
	var	_loadAgentList = function(){
		isLoading = true;
		window.clearTimeout(queryAutoTimer);
		if(!queryAutoFlag){//自动刷新
			queryAutoFlag = false;
		}
		phoneService.getIdleAgentList(function(__data){
			isLoading = false;
			if(__data.retcode == RestRetCode.SUCCESS){
				//473170	根据语言更改模板
				var cookieLanguage=tac.util.cookie.get();
				if(cookieLanguage==null||cookieLanguage==""){
					cookieLanguage = window.navigator.language || window.navigator.userLanguage;                    
                }
            	if(cookieLanguage=="zh-CN"){
            		domMap.agentListContent.empty().append($.tmpl(agentListHtml , __data));
            	}else{
            		domMap.agentListContent.empty().append($.tmpl(agentListHtmlEN , __data));
            	}
            	
				_addAgentListEvent(__data);
				if(__data.result.length > 0){
					$('#phone_transfer_infobox_tab_agent_content').find('input[type=radio]').each(function(){
						jdom = $(this);
						if(jdom.attr('val') == currentInputAgentVal){
							jdom.prop('checked' , true);
						}
					});
				}
				else
				{
					currentInputAgentVal = '';
					currentInputAgentTxt = '';
				}
			}
			queryAutoTimer = window.setTimeout(function(){
				_loadAgentList();
			}, tacCfg.time.listAutoRefresh);
		});
	};
	
	

	var _addSkillListEvent = function(__data){
		$('#phone_transfer_infobox_tab_skill_content').find('tr').each(function(){
			$(this).click(function(){
				$('#phone_transfer_infobox_tab_skill_content').find('input[type=radio]').removeProp('checked');
				var r = $(this).find('input[type=radio]').prop('checked' , true);
				currentInputSkillLoginNum = r.attr('loginNum');
				currentInputSkillVal = r.attr('val');

			});
		});
	};
	
	var _addAgentListEvent = function(__data){
		$('#phone_transfer_infobox_tab_agent_content').find('tr').each(function(){
			$(this).click(function(){
				$('#phone_transfer_infobox_tab_agent_content').find('input[type=radio]').removeProp('checked');
				var r = $(this).find('input[type=radio]').prop('checked' , true);
				currentInputAgentVal = r.attr('val');
				currentInputAgentTxt = r.attr('txt');
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
			$('#phone_transfer_btm_submit').trigger("click");
			e.preventDefault();
		}
	};
	
	/*
	 * 显示
	 */
	var show = function(__callid , __callback, __srNo){
		if(infobox){
			infobox.show();
			currentInputType = 0;
			currentInputSkillVal = '';
			currentInputSkillLoginNum  = 0;
			currentInputAgentVal = '';
			currentInputAgentTxt = '';
			currentInputSelfTelVal = '';
			currentInputTelVal = '';
			$('#phone_transfer_infobox_tab_skill').addClass('active');
			$('#phone_transfer_infobox_tab_agent').removeClass('active');
			$('#phone_transfer_infobox_tab_tel').removeClass('active');
			$('#phone_transfer_infobox_tab_skill_content').show();
			$('#phone_transfer_infobox_tab_agent_content').hide();
			$('#phone_transfer_infobox_tab_tel_content').hide();
			$('#phone_transfer_infobox_tab_tel_li').hide();
			$('#phone_transfer_btm_submit').show();
			$('#phone_transfer_callParty_btm_submit').hide();
			$('#phone_transfer_3Party_btm_submit').hide();
			domMap.telInput.val('');
			domMap.selfTelInput.val('');
			callid = __callid || '';
			isLoading = false;
			actionWaitFlag = false;
			
			$('#phone_transfer_btm_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phoneTransfer.btn.transfer'));
			$('#phone_transfer_callParty_btm_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phoneTransfer.btn.calltransfer'));
			$('#phone_transfer_3Party_btm_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phoneTransfer.btn.threetransfer'));
			_loadSkillList();
			$(document).keypress(keyEnterHandler);

		}
		return module;
	};
	/*
	 * 隐藏
	 */
	var hide = function(){
		if(infobox){
			infobox.hide(function(){
				window.clearTimeout(queryAutoTimer);
			});
			$(document).off('keypress');
		}
		return module;
	};
	
	var resetBtn = function(){
		$('#phone_transfer_callParty_btm_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phoneTransfer.btn.calltransfer'));
		$('#phone_transfer_3Party_btm_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phoneTransfer.btn.threetransfer'));
		$('#phone_transfer_btm_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phoneTransfer.btn.transfer'));
	};
	
	
	/*
	 * 暴露接口
	 */
	return module = {
		init : init,
		render : render,
		_i18nInit:_i18nInit,
		show : show,
		hide : hide,
		destory : destory,
		resetBtn : resetBtn
	};
});
