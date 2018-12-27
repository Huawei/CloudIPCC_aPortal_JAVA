define(['tac' , 'tacCfg' , 'text!../../../../html/modules/common/menu/menu.html'], function(tac , tacCfg ,html) {
	var module = null;
	/*
	 * 定义变量
	 */
	var el = null;
	var cache = {
		calldata : {},
		sr : {},
		home : {},
		report : {},
		quality : {},
		flushT1 : null
	};
	/*
	 * 初始化
	 */
	var init = function(__cfg){
		if(__cfg){
			if(__cfg.el){
				el = __cfg.el;
			}
		}
		return module;
	};
	/*
	 * 事件列表
	 */
	var eventList = [{
			target : 'left_menu_quality_sub',
			attach : 'on',
			type : 'click',
			action : function(e) {
				location.href = '#quality_search_review';
			}
		},{
			target : 'left_menu_report_vdnTraffic',
			attach : 'on',
			type : 'click',
			action : function(e) {
				location.href = '#report_vdnTraffic';
			}
		},{
			target : 'left_menu_report_skillTraffic',
			attach : 'on',
			type : 'click',
			action : function(e) {
				location.href = '#report_skillTraffic';
			}
		},{
			target : 'left_menu_report_agentTraffic',
			attach : 'on',
			type : 'click',
			action : function(e) {
				location.href = '#report_agentTraffic';
			}
		},{
			target : 'left_menu_report_agentWork',
			attach : 'on',
			type : 'click',
			action : function(e) {
				location.href = '#report_agentWork';
			}
		},{
			target : 'left_menu_report_agentCalloutTraffic',
			attach : 'on',
			type : 'click',
			action : function(e) {
				location.href = '#report_agentCalloutTraffic';
			}
		},{
			target : 'left_menu_report_skillTrafficBySkill',
			attach : 'on',
			type : 'click',
			action : function(e) {
				location.href = '#report_skillTrafficBySkill';
			}
		}
	];
	
	/*
	 * 私有方法：初始化事件
	 */
	var _initEvent = function(){
		for (var i = 0; i < eventList.length; i++) {
			$('#' + eventList[i].target).off(eventList[i].type)[eventList[i].attach](eventList[i].type, eventList[i].action);
		}
	};
	

	
	/*
	 * 私有方法：国际化
	 */
	var _i18nInit = function(){
		if(el){
			el.find('li > span').each(function(){
				if($(this).attr('i18nKey')){
					$(this).html($.i18n.prop($(this).attr('i18nKey')));
				}
			});
			el.find(".i18n").each(function(){
				if($(this).attr("i18nKey")){
					$(this).html($.i18n.prop($(this).attr("i18nKey")));
				}
			});
			el.find(".i18ntitle").each(function(){
				if($(this).attr("i18nKey")){
					$(this).attr("title",$.i18n.prop($(this).attr("i18nKey")));
				}
				
			});
		}
	};
	
	/*
	 * 渲染
	 */
	var render = function(){
		if(el){
			el.empty().html($.tmpl(html));
			//绑定事件 
			_initEvent();
			if (tac.data.get("IS_CENSOR") == false)
			{
				$("#left_menu_report_sub").hide();
			}
			_i18nInit();
		}
		return module;
	};
	/*
	 * 销毁
	 */
	var destory = function() {
		if(el){
			el.empty();
		}
		return module;
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
	/*
	 * 隐藏
	 */
	var hide = function(){
		if(el){
			el.hide();
		}
		return module;
	};
	
	
	/*
	 * 暴露接口
	 */
	return module = {
		init : init,
		render : render,
		show : show,
		hide : hide,
		destory : destory
	};
});
