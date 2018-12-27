define(['tac' ,
		'tacCfg',
		'text!../../../../html/modules/common/phone/phoneLinkingCall.html',
		'text!../../../../html/modules/common/phone/phoneLinkingCallSkillList.html',
		'text!../../../../html/modules/common/phone/phoneLinkingCallSkillListEN.html',
		'text!../../../../html/modules/common/phone/phoneLinkingCallAgentList.html',
		'text!../../../../html/modules/common/phone/phoneLinkingCallAgentListEN.html',
		'component/infobox/infobox',
		'modules/common/phone/phoneService'], function(tac ,tacCfg, html,skillListHtml,skillListHtmlEN, agentListHtml,agentListHtmlEN, Infobox, phoneService) {
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
	var queryAutoFlag = false;
	var queryAutoTimer = 0;
	var isLoading = false;
	var skillListPage = null;
	var agentListPage = null;
	var actionWaitFlag = false, actionWaitTime = 500;
	var inputQueryDelayTimer = 0;
	var callid = '';
	/*
	 * 初始化
	 */
	var init = function(__cfg){
		return module;
	};
	/*
	 * 事件列表
	 */
	var eventList = [
		{
			target : 'phone_linkingcall_infobox_close',
			attach : 'on',
			type : 'click',
			action : function(e) {
				hide();
			}
		},
		{
			target : 'phone_linkingcall_infobox_tab_skill',
			attach : 'on',
			type : 'click',
			action : function(e) {
				currentInputType = 0;
				$('#phone_linkingcall_infobox_tab_skill').addClass('active');
				$('#phone_linkingcall_infobox_tab_agent').removeClass('active');
				$('#phone_linkingcall_infobox_tab_skill_content').show();
				$('#phone_linkingcall_infobox_tab_agent_content').hide();
				$('#phone_linkingcall_infobox_tab_skill_li').show();
				$('#phone_linkingcall_infobox_tab_agent_li').hide();
				_loadSkillList();
			}
		},
		{
			target : 'phone_linkingcall_infobox_tab_agent',
			attach : 'on',
			type : 'click',
			action : function(e) {
				currentInputType = 1;
				$('#phone_linkingcall_infobox_tab_skill').removeClass('active');
				$('#phone_linkingcall_infobox_tab_agent').addClass('active');
				$('#phone_linkingcall_infobox_tab_skill_content').hide();
				$('#phone_linkingcall_infobox_tab_agent_content').show();
				$('#phone_linkingcall_infobox_tab_skill_li').hide();
				$('#phone_linkingcall_infobox_tab_agent_li').show();
				$('#phone_linkingcall_infobox_top_agent_id').focus();
				_loadAgentList();
			}
		},
		{
			target : 'phone_linkingcall_btm_2PartyHelp_submit',
			attach : 'on',
			type : 'click',
			action : function(e) {
				if($(this).hasClass('btn-gary') || actionWaitFlag){
					return;
				}
				_innerHelp(1);
			}
		},
		{
			target : 'phone_linkingcall_btm_3PartyHelp_submit',
			attach : 'on',
			type : 'click',
			action : function(e) {
				if($(this).hasClass('btn-gary') || actionWaitFlag){
					return;
				}
				_innerHelp(2);
			}
		},
		{
			target : 'phone_linkingcall_btm_cancel',
			attach : 'on',
			type : 'click',
			action : function(e) {
				hide();
			}
		}
	];
	
	/**
	 * 进行内部求助
	 * @param mode 1为2方；2为3方
	 */
	var _innerHelp = function(mode){
		actionWaitFlag = true;
		window.setTimeout(function(){
			actionWaitFlag = false;
		}, actionWaitTime);
		var param = null;
		if(currentInputType == 0){
			currentInputSkillVal = $.trim(currentInputSkillVal);
			if(currentInputSkillVal){
				if (currentInputSkillLoginNum == 0)
				{
					tac.tips({type:'failed',content:$.i18n.prop('phone.msg.nologinAgent'),timeout:3000});
					return;
				}
				param = {
					"dstAddress" : currentInputSkillVal,
					"deviceType" : 1,
					"mode" : mode
				};
			}else{
				$(document).unbind('keypress' , keyEnterHandler);
				tac.prompt($.i18n.prop('common.msg.box.title.notice') , $.i18n.prop('phone.msg.skillNotSelect') , $.i18n.prop('common.msg.box.btn.ok'), function(){
					$(document).keypress(keyEnterHandler);
				});
				return;
			}
		}else{
			currentInputAgentVal = $.trim(currentInputAgentVal);
			if(currentInputAgentVal){
				 param = {
					"dstAddress" : currentInputAgentVal,
					"deviceType" : 2,
					"mode" : mode
				};
			}else{
				$(document).unbind('keypress' , keyEnterHandler);
				tac.prompt($.i18n.prop('common.msg.box.title.notice') , $.i18n.prop('phone.msg.agentNotSelect') , $.i18n.prop('common.msg.box.btn.ok'), function(){
					$(document).keypress(keyEnterHandler);
				});
				return;
			}
		}
		phoneService.innerHelp(param , function(__data){
			if(__data.retcode == RestRetCode.SUCCESS){ 
				hide();
				tac.modules.phone.isHelping(mode, __data.result);
				$('#phone_linkingcall_btm_2PartyHelp_submit').addClass('btn-gary').removeClass('btn-success').html($.i18n.prop('phone.phoneLinkingCall.btn.doPartyHelp'));
				$('#phone_linkingcall_btm_3PartyHelp_submit').addClass('btn-gary').removeClass('btn-success').html($.i18n.prop('phone.phoneLinkingCall.btn.doPartyHelp'));
			}else{
				tac.tips({
					type: 'failed',
					content: $.i18n.prop('phone.msg.callHelpFailure'),
					timeout: 3000
				});
				resetBtn();
				return;
			}
		});
	}
	
	/*
	 * 私有方法：初始化DOM对象
	 */
	var _initElement = function(){
		domMap = {
			skillListContent : $('#phone_linkingcall_infobox_tab_skill_content'),
			agentListContent : $('#phone_linkingcall_infobox_tab_agent_content'),
			callSubmit : $('#phone_linkingcall_btm_submit')
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
			el.find(".bgi18n").each(function(){
				if($(this).attr("i18nKey")){
					$(this).attr('placeholder',$.i18n.prop($(this).attr("i18nKey")));
				}
			});
		}
		
	};
	
	/**
	 * 获取技能列表
	 */
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
					$('#phone_linkingcall_infobox_tab_skill_content').find('input[type=radio]').each(function(){
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
			} , tacCfg.time.listAutoRefresh);
		});
	};
	
	/**
	 * 获取座席列表
	 */
	var	_loadAgentList = function(){
		isLoading = true;
		window.clearTimeout(queryAutoTimer);
		if(!queryAutoFlag){//自动刷新
			queryAutoFlag = false;
		}
	
		phoneService.getIdleAgentList(function(__data){
			isLoading = false;
			if(__data.retcode == RestRetCode.SUCCESS){
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
					var isExist = false;
					$('#phone_linkingcall_infobox_tab_agent_content').find('input[type=radio]').each(function(){
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
			queryAutoTimer = window.setTimeout(function(){
				_loadAgentList();
			} , tacCfg.time.listAutoRefresh);
		});
	};
	
	var _addSkillListEvent = function(__data){
		$('#phone_linkingcall_infobox_tab_skill_content').find('tr').each(function(){
			$(this).click(function(){
				$('#phone_linkingcall_infobox_tab_skill_content').find('input[type=radio]').removeProp('checked');
				var r = $(this).find('input[type=radio]').prop('checked' , true);
				currentInputSkillLoginNum = r.attr('loginNum');
				currentInputSkillVal = r.attr('val');

			});
		});
	};
	
	var _addAgentListEvent = function(__data){
		$('#phone_linkingcall_infobox_tab_agent_content').find('tr').each(function(){
			$(this).click(function(){
				$('#phone_linkingcall_infobox_tab_agent_content').find('input[type=radio]').removeProp('checked');
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
		}
	};
	
	/*
	 * 显示
	 */
	var show = function(__callid , __srNo){
		if(infobox){
			infobox.show();
			currentInputType = 0;
			currentInputSkillVal = '';
			currentInputSkillLoginNum = 0;
			currentInputAgentVal = '';
			currentInputAgentTxt = '';
			currentInputAgentState = '';
			$('#phone_linkingcall_infobox_tab_skill').addClass('active');
			$('#phone_linkingcall_infobox_tab_agent').removeClass('active');
			$('#phone_linkingcall_infobox_tab_skill_content').show();
			$('#phone_linkingcall_infobox_tab_agent_content').hide();
			$('#phone_linkingcall_infobox_tab_skill_li').show();
			$('#phone_linkingcall_infobox_tab_agent_li').hide();
			isLoading = false;
			actionWaitFlag = false;
			skillListPage = null;
			agentListPage = null;
			$('#phone_linkingcall_btm_2PartyHelp_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phoneLinkingCall.btn.2PartyHelp'));
			$('#phone_linkingcall_btm_3PartyHelp_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phoneLinkingCall.btn.3PartyHelp'));
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
			$(document).unbind('keypress' , keyEnterHandler);
		}
		return module;
	};
	
	var resetBtn = function(){
		$('#phone_linkingcall_btm_2PartyHelp_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phoneLinkingCall.btn.2PartyHelp'));
		$('#phone_linkingcall_btm_3PartyHelp_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phoneLinkingCall.btn.3PartyHelp'));
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
