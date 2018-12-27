/**
 * 来电弹窗
 */
define(['tac' ,
		'tacCfg',
		'text!../../../../html/modules/common/phone/phoneCallAlert.html',
		'component/infobox/infobox'],
		function(tac ,tacCfg, html,Infobox) {
	var module = null;
	var infobox = null;
	var tempId = null;
	/*
	 * 定义变量
	 */
	var el = null;
	
	//事件列表
	var eventList = [{
		target : 'phoneCallAlert_infobox_close', //关闭
		attach : 'on',
		type : 'click',
		action : function(e) {
			infobox.hide();
		}
	},{
		target : 'phoneCallAlert_acceptBtn', //接听
		attach : 'on',
		type : 'click',
		action : function(e) {
			$('#top_phone_answer_btn').click();
			infobox.hide();
		}
	},{
		target : 'phoneCallAlert_rejectBtn', //拒接
		attach : 'on',
		type : 'click',
		action : function(e) {
			$('#top_phone_hangup_btn').click();
			infobox.hide();
		}
	}]
	
	/**
	 * 设置接听和拒接按钮不可用
	 */
	var setButtonsDisable = function() {
		$('#phoneCallAlert_callIn_Number_label').text($.i18n.prop('phoneCallAlert.noAnswer.Number'));
		$('#phoneCallAlert_acceptBtn').attr("disabled", true);
		$('#phoneCallAlert_rejectBtn').attr("disabled", true);
		$('#phoneCallAlert_acceptBtn').removeClass("animated pulse infinite");
	}
	
	/**
	 * 初始化弹窗显示的控件
	 */
	var _initPopupDisplay = function() {
		$('#phoneCallAlert_callIn_Number_label').text($.i18n.prop('phoneCallAlert.callIn.Number'));
		$('#phoneCallAlert_acceptBtn').removeAttr("disabled");
		$('#phoneCallAlert_rejectBtn').removeAttr("disabled");
		$('#phoneCallAlert_acceptBtn').addClass("animated pulse infinite");
	}
	
	/*
	 * 初始化事件
	 */
	var _initEvent = function(){
		for (var i = 0; i < eventList.length; i++) {
			$('#' + eventList[i].target)[eventList[i].attach](eventList[i].type, eventList[i].action);
		}			
	};	
	
	
	var initInfobox = function() {
		infobox = new Infobox({
			el : el = $.tmpl(html),
			drag : true,
			model : true,
			onClose : hide
		});
	}
	
	/**
	 * 根据呼入类型设置视图
	 */
	var _initCallInfoView = function(_event) {
		var callInNumber;
		var callInType;
		var feature = _event.content.feature;
		switch (feature) {
		case '0': //普通呼入
			callInType = $.i18n.prop('phoneCallAlert.feature.callIn.ordinary');
			callInNumber = _event.content.caller;
			break;
		case '6': //内部呼叫
			$('#phoneCallAlert_callIn_Number_label').text($.i18n.prop('phoneCallAlert.callIn.internal.agentId'));
			callInType = $.i18n.prop('phoneCallAlert.feature.callIn.intern');
			callInNumber = _event.content.caller;
			break;
		case '7': //普通呼出
			callInType = $.i18n.prop('phoneCallAlert.feature.callOut.ordinary');
			callInNumber = _event.content.called;
			break;
		case '40': //预约呼出
			callInType = $.i18n.prop('phoneCallAlert.feature.callOut.premeet');
			callInNumber = _event.content.called;
			break;
		case '43': //预测式外呼
			callInType = $.i18n.prop('phoneCallAlert.feature.callIn.auto');
			callInNumber = _event.content.called;
			break;
		case '41': //预占式外呼
			callInType = $.i18n.prop('phoneCallAlert.feature.callIn.auto');
			callInNumber = _event.content.called;
			break;
		case '44': //预览式外呼
			callInType = $.i18n.prop('phoneCallAlert.feature.callIn.auto');
			callInNumber = _event.content.called;
			break;
		case '45': //预约回呼
			callInType = $.i18n.prop('phoneCallAlert.feature.callIn.reservation');
			callInNumber = _event.content.called;
			break;
		case '51':  //内部两方咨询
			$('#phoneCallAlert_callIn_Number_label').text($.i18n.prop('phoneCallAlert.callIn.internal.agentId'));
			callInType = $.i18n.prop('phoneCallAlert.feature.callIn.intern.two');
			callInNumber = _event.content.caller;
			break;
		case '52':  //内部三方咨询
			$('#phoneCallAlert_callIn_Number_label').text($.i18n.prop('phoneCallAlert.callIn.internal.agentId'));
			callInType = $.i18n.prop('phoneCallAlert.feature.callIn.intern.three');
			callInNumber = _event.content.caller;
			break;
		case '53': //多方通话
			callInType = $.i18n.prop('phoneCallAlert.feature.callIn.multiparty');
			callInNumber = _event.content.caller;
			break;
		default:
			callInNumber = _event.content.caller;
			break;
		}
		//显示来电类型
		$('#phoneCallAlert_callIn_Type').empty();
		$('#phoneCallAlert_callIn_Type').append(callInType);
		//显示来电号码
		$('#phoneCallAlert_callIn_Number').empty();
		$('#phoneCallAlert_callIn_Number').append(callInNumber); 
		
		_sendNotification(callInType, callInNumber);
	}
	
	/**
	 * 显示弹屏通知
	 */
	var _sendNotification = function (callInType, callInNumber) {
		if (window.Notification) {
			var msg = "呼叫类型：" + callInType + "\r\n来电号码：" + callInNumber;
			if (Notification.permission == "granted") {
				_popNotice(msg);
	        } else if (Notification.permission != "denied") {
	            Notification.requestPermission(function (permission) {
	            	_popNotice(callInType, callInNumber);
	            });
	        }
		}
	}
	
	/**
	 * 填充弹屏视图
	 */
	var _popNotice = function(msg) {
	    if (Notification.permission == "granted") {
	         _notification = new Notification("来电号码", {
	            body: msg,
	            data: msg,
	            tag: 'ipcc.aportal.callincoming',
	            renotify:true,
	            icon: './images/linking.png'
	        });
	        
	        _notification.onclick = function() {
	        	window.focus();
	        	_notification.close();    
	        };
	    }    
	};

	/*
	 * 初始化
	 */
	var init = function(__cfg){
		initInfobox();
		_i18nInit();
		_initEvent();	
		return module;
	};
	
	/*
	 * 显示
	 */
	var show = function(_event){	
		if (_event) {
			_initPopupDisplay();
			_initCallInfoView(_event);
		}		
		if (infobox) {
			infobox.show();	
		}							
		return module;
	};
	
	/*
	 * 隐藏
	 */
	var hide = function(){
		if(infobox){
			infobox.hide();
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
	 * 渲染
	 */
	var render = function(){		
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
	 * 暴露接口
	 */
	return module = {
		init : init,
		render : render,
		show : show,
		hide : hide,
		setButtonsDisable : setButtonsDisable
	};
});
