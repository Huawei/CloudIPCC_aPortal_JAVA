	
define(['tac' ,'tacCfg',
		'modules/system/system.service' ,
		'text!../../../html/modules/system/system.html'
		],function(tac ,tacCfg, service, html) {
	
	var _actionWaitFlag = false, _actionWaitTime = 500;
	/*
	 * 事件列表
	 */
	var eventList = [
				{
					target : 'system_config_maxWorkTime',
					attach : 'on',
					type : 'input propertychange',
					action : function(e) {
						if($(this).is(':focus')){
							var pos = $(this)[0].selectionStart;
							var inputValue = $(this).val();
							$(this).val(inputValue.replace(/[^\d]/g, ''));
							$(this)[0].setSelectionRange(pos,pos);
						}
						if ($(this).val() <= 0){
							$('#system_config_maxWorkTime').addClass('input_error').nextAll('div:first').text($.i18n.prop('system.config.maxWorkTime.range')).show();
							return;
						} else {
							$('#system_config_maxWorkTime').removeClass('input_error').nextAll('div:first').text('').hide();
						}
					}
				}, {
					target : 'system_config_searchBtn',
					attach : 'on',
					type : 'click',
					action : function(e) {
						if(_actionWaitFlag){
							return;
						}
						_actionWaitFlag = true;
						window.setTimeout(function(){
							_actionWaitFlag = false;
						}, _actionWaitTime);
						_submit();
					}
			}
	];
	
	/**
	 * 初始化注释
	 */
	var _initExplanation = function(){
		$('#system_div').find(".explanation").mouseenter(function () {
		    $(this).find(".explanation_txt_box").show();
		});
		$('#system_div').find(".explanation").mouseleave(function () {
		    $(this).find(".explanation_txt_box").hide();
		});
	}
	
	/*
	 * 私有方法：初始化事件
	 */
	var _initEvent = function(){
		for (var i = 0; i < eventList.length; i++) {
			$('#' + eventList[i].target).off(eventList[i].type)[eventList[i].attach](eventList[i].type, eventList[i].action);
		}
	};
	var el = null;
	
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
	
	/**
	 * 提交保存
	 */
	var _submit = function() {
		var _isAutoAnswer = false;
		if ($('input[name="system_config_autoAnswer"]:checked').val() == "1"){
			 _isAutoAnswer = true;
		}
		var _maxWorkTime = 0;
		if ($('input[name="system_config_maxWorkTime_radio"]:checked').val() == "1"){
			_maxWorkTime = $.trim($('#system_config_maxWorkTime').val());
			if (_maxWorkTime <= 0){
				$('#system_config_maxWorkTime').addClass('input_error').nextAll('div:first').text($.i18n.prop('system.config.maxWorkTime.range')).show();
				return;
			}
		}
		$('#system_config_maxWorkTime').removeClass('input_error').nextAll('div:first').text('').hide();
		
		var _outCallerNo = $("#system_callerNo").val();
		service.setAutoAnswer({"autoAnswer" : _isAutoAnswer}, function(__data){
			if(RestRetCode.SUCCESS == __data.retcode){
				tac.data.set("CallerNoDesc",  $("#system_callerNo option:checked").text());
				tac.data.set("IS_AUTOANSWER", _isAutoAnswer);
				tac.data.set("MAX_WORKTIME", _maxWorkTime);
				tac.data.set("OUTCALLERNO",_outCallerNo);
				_modifyAgentConfigure(_isAutoAnswer, _outCallerNo, _maxWorkTime);
			}else{
				tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
						$.i18n.prop('common.msg.box.btn.systemConfigFailed'), 
						$.i18n.prop('common.msg.box.btn.ok'));
			}
		});
	}
	
	
	/**
	 *修改座席登录配置
	 *
	 */
	var _modifyAgentConfigure = function(_isAutoAnswer, _outCallerNo, _maxWorkTime) {
		var _isAutoAnswer = false;
		if ($('input[name="system_config_autoAnswer"]:checked').val() == "1"){
			 _isAutoAnswer = true;
		}
		
		var _maxWorkTime = 0;
		if ($('input[name="system_config_maxWorkTime_radio"]:checked').val() == "1"){
			_maxWorkTime = $.trim($('#system_config_maxWorkTime').val());
		}
		
		var _outCallerNo =  $("#system_callerNo").val();
		var params = {			
			"isAutoAnswer": _isAutoAnswer,
			"maxWorkTime": _maxWorkTime,
			"outCallerNo": _outCallerNo
		};
		var agentConfigure_url = tacCfg.url.agentRestServer+tacCfg.service.agentconfigure.setAgentConfigure;
		tac.ajax({
			url : agentConfigure_url,
			type:"PUT",
			async : true,
			loading:true,
			contentType : 'application/json;charset=utf-8',
			data : JSON.stringify(params),
			success : function(data) {
				if(data.retcode == 0) {
					tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
							$.i18n.prop('common.msg.box.btn.systemConfigSuccess'), 
							$.i18n.prop('common.msg.box.btn.ok'));
				} else {
					tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
							$.i18n.prop('system.config.systemConfigure.fail'), 
							$.i18n.prop('common.msg.box.btn.ok'));
				}
			}
		});
		
	}
	
	
	var _initCallerNo = function() {		
		var buffer = [];
		//添加默认的主叫号码
		buffer.push("<option value=''>");
		buffer.push($.i18n.prop('system.config.systemCallerNo.default'));
		buffer.push("</option>")
		//追加从aConsole获取的主叫号码的值
		var _callerList = tac.data.get("CALLERNO_LIST");
		if(_callerList)
		{
			var _callerListLength = _callerList.length;
			for(var i = 0; i < _callerListLength; i++)
			{
				buffer.push('<option value="');
				buffer.push(tac.util.htmlEncode(_callerList[i].phoneNumber));
				buffer.push('">');
				if (_callerList[i].remark) {
					buffer.push(tac.util.htmlEncode(_callerList[i].remark));
					buffer.push(' (');
					buffer.push(tac.util.htmlEncode(_callerList[i].phoneNumber));
					buffer.push(')');
				}else {
					buffer.push(tac.util.htmlEncode(_callerList[i].phoneNumber));
				}								
				buffer.push('</option>');
			}		
		}
		$("#system_callerNo").append(buffer.join(''));
		
		
		var val = $("#system_callerNo").val();
		$("#system_callerNo").attr('title', val);
		
		$("#system_callerNo").children("option").each(function(){
            $(this).attr('title', $(this).text());
        });
		
	};
	
	/**
	 * 初始化radiao按钮事件
	 */
	var _initRadioClick = function() {
		$('input[name="system_config_maxWorkTime_radio"]').on('click', function(){
			if ($(this).val() == "1") {
				$('#system_config_maxWorkTime').removeAttr('readonly');
			} else {
				$('#system_config_maxWorkTime').val('');
				$('#system_config_maxWorkTime').attr('readonly', 'readonly');
				$('#system_config_maxWorkTime').removeClass('input_error').nextAll('div:first').text('').hide();
			}
		});
	}
	
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
		if(el) {
			el.show();
		}
		var autoAnswer = tac.data.get('IS_AUTOANSWER');
		if (autoAnswer == 'true') {
			$('input[name="system_config_autoAnswer"][value="1"]').attr('checked', 'checked');
		}
		
		var maxWorkTime = tac.data.get('MAX_WORKTIME');
		if (maxWorkTime && maxWorkTime > 0) {
			$('input[name="system_config_maxWorkTime_radio"][value="1"]').attr('checked', 'checked');
			$('#system_config_maxWorkTime').removeAttr('readonly');
			$('#system_config_maxWorkTime').val(maxWorkTime);
		}

		//如果选过了主叫号码，下次登陆从缓存取出来
		var systemCallerNo = tac.data.get('OUTCALLERNO');
		if(systemCallerNo) {
			$('#system_callerNo').val(systemCallerNo);
			
		}
		_initRadioClick();
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
	
	var init = function(__cfg){
		if(__cfg){
			if(__cfg.el){
				el = __cfg.el;
			}
		}
		return module;
	};
	
	/*
	 * 渲染
	 */
	var render = function(){
		if(el){
			el.empty().html($.tmpl(html));
			_initEvent();
			_i18nInit();
			_initCallerNo();
			_initExplanation();
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
