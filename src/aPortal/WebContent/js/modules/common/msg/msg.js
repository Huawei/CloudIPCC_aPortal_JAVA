define(['tac' ,'tacCfg' ,'text!../../../../html/modules/common/msg/msg.html'], function(tac, tacCfg , html) {
	var module = null;
	/*
	 * 定义变量
	 */
	var el = null;
	var domMap = null;
	var msgText = '';
	var notificationFlag = true;
	var currentIsMajorAccident = false;
	var currentIsImportantMsg = false;
	var currentIsAgentAllBusy = false;
	var livecharMsgRegExp = /^\u60A8\u6709\u672A\u5E94\u7B54\u591A\u5A92\u4F53\u547C\u53EB/;//多媒体便签提醒
	var phoneMsgRegExp = /^\u60a8\u6709\u672a\u63a5\u6765\u7535\u3002/;//语音提醒
	/*
	 * 初始化
	 */
	var init = function(__cfg){
		if(__cfg){
			if(__cfg.el){
				el = __cfg.el;
				tac.modules.debugbox.pushInfo('[SYSTEM]上部消息框初始化完毕！');
			}
		}
		return module;
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
		}
	};
	/*
	 * 事件列表
	 */
	var eventList = [];
	
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
			topMsgNewCount : $('#top_msg_new_count'),
			topMsgNewText : $('#top_msg_new_text'),
			topMsgNewIcon : $('#top_msg_new_icon')
		};
	};
	/*
	 * 渲染
	 */
	var render = function(){
		if(el){
			el.empty().append($.tmpl(html));
			_initEvent();
			_i18nInit();
			_initElement();
			tac.showTopMsg.init({
				txtel:domMap.topMsgNewText
			});
		}
		return module;
	};
	

	var hideNewMsgInfo = function(){
		domMap.topMsgNewCount.removeClass('badge');
		domMap.topMsgNewCount.html('');
		showShortMsg({
			txt : msgText = $.i18n.prop('top.msg.nonewmsg'),
			mov : false
		});
	};
	
	/*
	 * 显示
	 */
	var show = function(){
		if(el){
			el.show();
		}
		return module;
	};
	
	var showShortMsg = function(__params){
		if(__params && __params.txt){
			tac.showTopMsg.show({
				txt : __params.txt,
				mov : __params.mov != false,
				movSecond : __params.movSecond,
				type : __params.type,
				level : __params.level,
				blink : __params.blink
			});
		}
	};

	
	
	/*
	 * 暴露接口
	 */
	tac.modules.msg = module = {
		init : init,
		render : render,
		show : show,
		hideNewMsgInfo : hideNewMsgInfo,
		showShortMsg : showShortMsg
	};
	return module;
});
