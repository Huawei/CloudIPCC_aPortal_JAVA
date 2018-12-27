define(['tac' ,
        'tacCfg'
       ], function(tac , tacCfg) {
	/*
	 * 定义变量
	 */
	var el = null;
	var module = null;
	
	/*
	 * 事件列表
	 */
	var eventList = [
						{
							target : 'onlineHelp', //点击在线帮助
							attach : 'on',
							type : 'click',
							action : function() {
								window.open("WebHelp/web/zh-cn_bookmap_0126441719.htm",
										"WebHelp","menubar=0,height=600,width=1200,left=100,top=100,scrollbars=0,resizable=0," +
												"status=0,titlebar=0,toolbar=0,location=no");
							}
						},
						{
							target : 'downloadHelp', //点击在线帮助
							attach : 'on',
							type : 'click',
							action : function() {
								window.open("WebHelp/pdf/aPortal_webHelp.pdf","_blank");
							}
						},
						{
							target : 'feedback', //点击评价与反馈
							attach : 'on',
							type : 'click',
							action : function() {
								 tac.promptEx({
								    	title :  $.i18n.prop('common.msg.box.title.notice'),
								    	content : $.i18n.prop('common.msg.box.feedback'), 
								    	width : 330,
										height : 200,
										submitText : $.i18n.prop('common.msg.box.btn.ok')
								    });
							}
						}
				];
	
	
	/*
	 * 初始化
	 */
	var init = function(__cfg){
		if(__cfg){
			if(__cfg.el){
				el = __cfg.el;
				_i18nInit();
				_initEvent();
				return module;
			}
		}	
	};

	/*
	 * 私有方法：初始化事件
	 */
	var _initEvent = function() {
		for ( var i = 0; i < eventList.length; i++) {
			$('#' + eventList[i].target)[eventList[i].attach](
					eventList[i].type, eventList[i].action);
		}
	};
	/*
	 * 国际化
	 */
	
	var _i18nInit = function(){
		if(el){
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
	 * 暴露接口
	 */
	tac.modules.tool = module = {
			init : init
		};
	return module;
});
