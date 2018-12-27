define(['tac' , 'text!../../../../html/modules/common/phone/phoneAddCall.html','component/infobox/infobox'], function(tac , html, Infobox) {
	var module = null;
	var infobox = null;
	/*
	 * 定义变量
	 */
	var el = null;
	var onClose = null;
	/*
	 * 初始化
	 */
	var init = function(__cfg){
		return module;
	};
	var domMap = null;
	/*
	 * 事件列表
	 */
	var eventList = [{
			target : 'phone_addcall_tel_input',
			attach : 'on',
			type : 'input propertychange',
			action : function(e) {
				if($(this).is(':focus')){
					var pos = $(this)[0].selectionStart;
					var inputValue = $(this).val();
					$(this).val(inputValue.replace(/[^\d|\*}\#]/g, ''));
					$(this)[0].setSelectionRange(pos,pos);
				}
			}
		},
		{
			target : 'phone_addcall_btm_submit',
			attach : 'on',
			type : 'click',
			action : function(e) {
				typeof onClose === 'function' && onClose($('#phone_addcall_tel_input').val());
			}
		},
		{
			target : 'phone_addcall_btm_cancel',
			attach : 'on',
			type : 'click',
			action : function(e) {
				hide();
			}
		}
	];
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
		}
	};
	/*
	 * 渲染
	 */
	var render = function(){
		if(infobox == null){
			infobox = new Infobox({
				el : el = $.tmpl(html),
				width : 320,
				height : 144
			});
		}
		_initEvent();
		_i18nInit();
		_initElement();
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
			$('#phone_addcall_btm_submit').trigger("click");
		}
	};
	
	/*
	 * 显示
	 */
	var show = function(__onClose){
		if(infobox){
			infobox.show();
			$('#phone_addcall_tel_input').val('').focus();
			onClose = __onClose;
			$(document).keypress(keyEnterHandler);
		}
		return module;
	};
	/*
	 * 隐藏
	 */
	var hide = function(){
		if(infobox){
			infobox.hide();
			$(document).off('keypress')
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
