define(['tac' ,
		'tacCfg' ,
		'modules/common/sign/signService',
		'component/vmenu/vmenu',
		'modules/common/msg/msg',
		'modules/common/menu/menu',
		'modules/common/status/status',
		'modules/common/event/eventHandle',
		'modules/system/system.service'
		], function(tac, tacCfg,signService,VMenu,msg,menu,status, eventHandle, system) {
	var module = null;
	var actionWaitFlag = false, actionWaitTime = 500;
	var _login_page_last_agentId = "";
    var _verifyCode_timerInterval = null;
    
	/*
	 * 定义变量
	 */
	var el = null;
	/*
	 * 初始化
	 */
	var init = function(__cfg){
		if(__cfg){
			if(__cfg.el){
				el = __cfg.el;
				$('#login_phone_num_submit').off("click").on('click', _bindlogin);
				$(document).off('keypress').on('keypress',function(e){
					keyEnterHandler(e)
				});
			}
		}
		$('#login_phone_verifycode_div').hide();
		return module;
	};
	var reportEventList = [];
	/*
	 * 事件列表
	 */
	var eventList = [
		{
			target : 'top_sign_img',
			attach : 'on',
			type : 'click',
			action : function(){
				_signOutAsk();
			}
		},
		{
			target : 'login_phone_num_agent_input',
			attach : 'on',
			type : 'blur',
			action : function(){
				var agentId = $('#login_phone_num_agent_input').val().trim();
		        if (agentId != _login_page_last_agentId) 
		        {
		        	_login_page_last_agentId = agentId;
		        	_getVerifyCode();
		        }
		       // $('#login_phone_num_agent_input_tip').hide();
			}
		},
		{
			target : 'login_phone_num_agent_input',
			attach : 'on',
			type : 'input propertychange',
			action : function(e) {
				if($(this).is(':focus')){
					var pos = $(this)[0].selectionStart;
					var inputValue = $(this).val();
					$(this).val(inputValue.replace(/[^\d]/g, ''));
					$(this)[0].setSelectionRange(pos,pos);
				}
			}
		},
		{
			target : 'login_phone_num_account_input',
			attach : 'on',
			type : 'blur',
			action : function(){
				var agentId = $('#login_phone_num_account_input').val().trim();
		        if (agentId != _login_page_last_agentId) 
		        {
		        	_login_page_last_agentId = agentId;
		        	_getVerifyCode();
		        }
			}
		},
		{
			target : 'login_phone_num_account_input',
			attach : 'on',
			type : 'input propertychange',
			action : function(e) {
				if($(this).is(':focus')){
					var pos = $(this)[0].selectionStart;
					var inputValue = $(this).val();
					//暂时注释，与工号一致
					//$(this).val(inputValue.replace(/[^0-9a-zA-Z\-_]/g, ''));
					$(this).val(inputValue.replace(/[^\d]/g, ''));
					$(this)[0].setSelectionRange(pos,pos);
				}
			}
		},		
		{
			target : 'login_phone_verifycode_img_span',
			attach : 'on',
			type : 'click',
			action : function(){
				if(actionWaitFlag){
					return;
				}
				actionWaitFlag = true;
				window.setTimeout(function(){
					actionWaitFlag = false;
				}, actionWaitTime);
				_getVerifyCode();
			}
		},
		{
			target : 'login_phone_num_input',
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
		}
	];
	
	/*
	 * 按回车登录
	 */
	var keyEnterHandler = function(e){
		if(e.which == 13 && $('#login_phone_num_submit').length>0 && !$('#login_phone_num_submit').is(':hidden')){
			$('#login_phone_num_submit').click();
		}
	};

	var _bindlogin = function(){
		if(actionWaitFlag){
			return;
		}
		actionWaitFlag = true;
		window.setTimeout(function(){
			actionWaitFlag = false;
		}, actionWaitTime);
		$('#login_phone_num_agent_input_tip').hide();
		$('#login_phone_num_input_tip').hide();
		$('#login_phone_num_account_tip').hide();
		$('#login_phone_password_input_tip').hide();
		$('#login_phone_verifycode_input_tip').hide();
		
		
		if(LoginMode.loginByAccount){
			var agentAccount = $.trim($('#login_phone_num_account_input').val());
			if(!agentAccount || agentAccount.length < 3){
				$('#login_phone_num_account_tip').show();
				$('#login_phone_num_account_tip').text($.i18n.prop('loginframe.sign.phonenum.accountFormatIsInvalid'));
				$('#login_phone_num_account_input').focus();
				return;
			}
			var reg = new RegExp();     
			//reg.compile("^[a-zA-Z\-][a-zA-Z0-9\-_]{4,31}$");     
			reg.compile("^[1-9][\\d]{2,3}$|^[1-5][\\d]{4}$"); 
		    if (!reg.test(agentAccount) || agentAccount == 100)
		    {
		    	$('#login_phone_num_account_tip').show();
				$('#login_phone_num_account_tip').text($.i18n.prop('loginframe.sign.phonenum.accountFormatIsInvalid'));
				$('#login_phone_num_account_input').focus();
		    	return;
		    }
			
			var password = $('#login_phone_password_input').val();
			if(!password || password.length < 8){
				$('#login_phone_password_input_tip').show();
				$('#login_phone_password_input_tip').text($.i18n.prop('loginframe.sign.phonenum.pwdLenIsInvalid'));
				$('#login_phone_password_input').focus();
				return;
			}
			
			_checkVerify();
			var loginParam = {
					"agentAccount" : agentAccount,
					"password" : password,
					"verifyCode" : $('#login_phone_verifycode_input').val().trim()
					
				};
			_loginByAccount(loginParam);
		}
		else
		{
			var agentId = $.trim($('#login_phone_num_agent_input').val());
			if(!agentId){
				$('#login_phone_num_agent_input_tip').show();
				$('#login_phone_num_agent_input_tip').text($.i18n.prop('loginframe.sign.phonenum.agentIdFormatIsInvalid'));
				$('#login_phone_num_agent_input').focus();
				return;
			}
			
			var reg = new RegExp();     
			reg.compile("^[1-9][\\d]{2,3}$|^[1-5][\\d]{4}$");     
		    if (!reg.test(agentId) || agentId == 100){
		    	$('#login_phone_num_agent_input_tip').show();
				$('#login_phone_num_agent_input_tip').text($.i18n.prop('loginframe.sign.phonenum.agentIdFormatIsInvalid'));
				$('#login_phone_num_agent_input').focus();
		    	return;
		    }
		    
		    reg.compile("^[\\d]{5,24}$");     
			var phonenum = $.trim($('#login_phone_num_input').val());
			if(!phonenum || !reg.test(phonenum)){
				$('#login_phone_num_input_tip').show();
				$('#login_phone_num_input_tip').text($.i18n.prop('loginframe.sign.phonenum.phoneLenIsInvalid'));
				$('#login_phone_num_input').focus();
				return;
			}
			var password = $('#login_phone_password_input').val();
			if(!password || password.length < 8){
				$('#login_phone_password_input_tip').show();
				$('#login_phone_password_input_tip').text($.i18n.prop('loginframe.sign.phonenum.pwdLenIsInvalid'));
				$('#login_phone_password_input').focus();
				return;
			}
			
			_checkVerify();
			var loginParam = {
					"agentId" : agentId,
					"password" : password,
					"phonenum" : phonenum,
					"verifyCode" : $('#login_phone_verifycode_input').val().trim()
					
				};
			_login(loginParam);
		}
	}
	
	var _checkVerify = function(){
		
		if (!$('#login_phone_verifycode_div').is(":hidden"))
		{
			if ($('#login_phone_verifycode_input').val().trim() == '')
			{
				$('#login_phone_verifycode_input_tip').show();
				$('#login_phone_verifycode_input_tip').text($.i18n.prop('loginframe.sign.phonenum.verifycodeIsNull'));
				$('#login_phone_verifycode_input').focus();
				return;
			}
		}
	}
	var _login = function (loginParam){
		signService.login(loginParam, function (__data){
			_callback(loginParam, __data);
		});
	}
	
	var _loginByAccount = function (loginParam){
		signService.loginByAccount(loginParam, function (__data){
			_callbackByAccount(loginParam, __data);
		});
	}
	
	var _forceLogin = function (loginParam){
		signService.forceLogin(loginParam, function (__data){
			_callback(loginParam, __data);
		});
	}
	
	
	var _changeToAutoAnswer = function(isAutoAnswer){
		var autoAnswer = tac.data.get("IS_AUTOANSWER");
		if (autoAnswer == "true")
		{
			system.setAutoAnswer({"autoAnswer" : autoAnswer});
		}
	}
	
	var _updateSysCallerNo = function(__outCallerNo) {
		var isExist = false;
		var _callerList = tac.data.get("CALLERNO_LIST");
		var _curSysCallerNo = __outCallerNo;
		var _curRemark = tac.data.get("CallerNoDesc");
		if(_callerList != null && _curSysCallerNo != null) {
			var _callerListLength = _callerList.length;
			for(var i = 0; i < _callerListLength; i++) {
				if(_curSysCallerNo == _callerList[i].phoneNumber) {
					isExist = true;
					var buffer = [];
					buffer.push(_callerList[i].remark);
					buffer.push(' (');
					buffer.push(_callerList[i].phoneNumber);
					buffer.push(')');
					tac.data.set("CallerNoDesc", buffer.join(''));
					break;
				}
			}
			//如果缓存中的被叫号码已经被后台删除，那么在列表中则不存在，此时将原有缓存内的号码置空，
			//置为默认主叫号码
			if(!isExist) {
				tac.data.set("CallerNoDesc", '');
				tac.data.set("OUTCALLERNO", '');
			}
		} else {
			tac.data.set("OUTCALLERNO", '');
		}
	}
	var _callback =  function (loginParam, __data){
		$(document).off('keypress');
		if (RestRetCode.SUCCESS == __data.retcode)
		{
			//登录成功
			clearTimeout(_verifyCode_timerInterval);
			tac.data.set("AGENT_ID", loginParam.agentId);
			tac.data.set("IS_CENSOR",__data.result.is_censor);
			tac.data.set("CALLERNO_LIST", __data.result.callerList);
			tac.data.set("isTrial", __data.result.isTrial);
			tac.isLogin = true;
			if (global_tupDeamonIsOk
					&& global_cloudIPCC_SDK
					&& !global_cloudIPCC_SDK.tupCall){
				//TUPCall还没有初始化好
				setTimeout(function(){
					_loginSuccess(loginParam);
				}, 3000);
			}else{
				_loginSuccess(loginParam);
			}
		}
		else if (RestRetCode.AGENT_LOGIN_ALREADY_IN == __data.retcode)
		{
			//已经登录，提示是否强制登录
			tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
					$.i18n.prop('loginframe.sign.phonenum.haslogined'), 
					$.i18n.prop('common.msg.box.btn.ok'), function(){	
				_forceLogin(loginParam);
				}, true, function(){
			}, $.i18n.prop('common.msg.box.btn.cancel'));
		} 
		else if (RestRetCode.AGENT_LOGIN_INVALID_PHONE_NOT_CONFIG == __data.retcode)
		{
			$('#login_phone_num_input_tip').show();
			$('#login_phone_num_input_tip').text($.i18n.prop('loginframe.sign.phonenum.phoneinvalid'));
			$('#login_phone_num_input').focus();
			_getVerifyCode();
			$(document).keypress(keyEnterHandler);
		}
		else if (RestRetCode.AGENT_LOGIN_USERNAME_OR_PWD_INVALID == __data.retcode)
		{
			$('#login_phone_num_agent_input_tip').show();
			$('#login_phone_num_agent_input_tip').text($.i18n.prop('loginframe.sign.phonenum.agentorpwdisfailed'));
			$('#login_phone_num_agent_input').focus();
			_getVerifyCode();
			$(document).keypress(keyEnterHandler);
		}
		else if (RestRetCode.AGENT_LOGIN_INVALID_PARAMETER_ERROR == __data.retcode)
		{
			$('#login_phone_num_input_tip').show();
			$('#login_phone_num_input_tip').text($.i18n.prop('loginframe.sign.phonenum.phonehasused'));
			$('#login_phone_num_input').focus();
			_getVerifyCode();
			$(document).keypress(keyEnterHandler);
		}
		else if (RestRetCode.AGENT_REST_INVALID == __data.retcode)
		{
			tac.prompt($.i18n.prop('common.msg.box.title.notice'), $.i18n.prop('common.msg.box.param.invalid'), 
					$.i18n.prop('common.msg.box.btn.ok'), function(){
				$(document).keypress(keyEnterHandler);
			}, false);
		}
		else if ('000-000-006' == __data.retcode)
		{
			$('#login_phone_verifycode_input_tip').show();
			$('#login_phone_verifycode_input_tip').text($.i18n.prop('loginframe.sign.phonenum.verifycodeIsValid'));
			$('#login_phone_verifycode_input').focus();
			_getVerifyCode();
			$(document).keypress(keyEnterHandler);
		}
		else
		{
			tac.prompt($.i18n.prop('common.msg.box.title.notice'), $.i18n.prop('common.msg.box.system.error'), 
					$.i18n.prop('common.msg.box.btn.ok'), function(){
				$(document).keypress(keyEnterHandler);
			}, false);
		}
	}
	var _callbackByAccount =  function (loginParam, __data){
		$(document).off('keypress');
		if (RestRetCode.SUCCESS == __data.retcode)
		{
			//登录成功
			clearTimeout(_verifyCode_timerInterval);
			tac.data.set("AGENT_ID", __data.result.workno);
			tac.data.set("IS_CENSOR",__data.result.is_censor);
			tac.data.set("CALLERNO_LIST", __data.result.callerList);
			tac.data.set("isTrial", __data.result.isTrial);
			tac.isLogin = true;
			if (global_tupDeamonIsOk
					&& global_cloudIPCC_SDK
					&& !global_cloudIPCC_SDK.tupCall){
				//TUPCall还没有初始化好
				setTimeout(function(){
					_loginSuccessByAccount(loginParam,__data.result.phoneNum);
				}, 3000);
			}else{
				_loginSuccessByAccount(loginParam,__data.result.phoneNum);
			}
		}
		else if (RestRetCode.AGENT_LOGIN_ALREADY_IN == __data.retcode)
		{
			//已经登录，提示是否强制登录
			tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
					$.i18n.prop('loginframe.sign.phonenum.haslogined'), 
					$.i18n.prop('common.msg.box.btn.ok'), function(){	
				_forceLogin(loginParam);
				}, true, function(){
			}, $.i18n.prop('common.msg.box.btn.cancel'));
		} 
		else if (RestRetCode.AGENT_LOGIN_INVALID_PHONE_NOT_CONFIG == __data.retcode)
		{
			$('#login_phone_num_input_tip').show();
			$('#login_phone_num_input_tip').text($.i18n.prop('loginframe.sign.phonenum.phoneinvalid'));
			$('#login_phone_num_input').focus();
			_getVerifyCode();
			$(document).keypress(keyEnterHandler);
		}
		else if (RestRetCode.AGENT_LOGIN_USERNAME_OR_PWD_INVALID == __data.retcode)
		{
			$('#login_phone_num_agent_input_tip').show();
			$('#login_phone_num_agent_input_tip').text($.i18n.prop('loginframe.sign.phonenum.agentorpwdisfailed'));
			$('#login_phone_num_agent_input').focus();
			_getVerifyCode();
		}
		else if (RestRetCode.AGENTACCOUNT_USERNAME_OR_PWD_INVALID == __data.retcode)
		{
			$('#login_phone_num_account_tip').show();
			$('#login_phone_num_account_tip').text($.i18n.prop('loginframe.sign.account.accountOrPwdInvalid'));
			$('#login_phone_num_account_tip').focus();
			_getVerifyCode();
			$(document).keypress(keyEnterHandler);
		}
		else if (RestRetCode.AGENTACCOUNT_LOCKED == __data.retcode)
		{
			$('#login_phone_num_account_tip').show();
			$('#login_phone_num_account_tip').text($.i18n.prop('loginframe.sign.account.accountLocked'));
			$('#login_phone_num_account_tip').focus();
			_getVerifyCode();
			$(document).keypress(keyEnterHandler);
		}
		else if (RestRetCode.AGENTACCOUNT_OTHEREXCEPTION == __data.retcode)
		{
			$('#login_phone_num_account_tip').show();
			$('#login_phone_num_account_tip').text($.i18n.prop('loginframe.sign.account.other'));
			$('#login_phone_num_account_tip').focus();
			_getVerifyCode();
			$(document).keypress(keyEnterHandler);
		}
		else if (RestRetCode.AGENT_LOGIN_INVALID_PARAMETER_ERROR == __data.retcode)
		{
			$('#login_phone_num_input_tip').show();
			$('#login_phone_num_input_tip').text($.i18n.prop('loginframe.sign.phonenum.phonehasused'));
			$('#login_phone_num_input').focus();
			_getVerifyCode();
			$(document).keypress(keyEnterHandler);
		}
		else if (RestRetCode.AGENT_REST_INVALID == __data.retcode)
		{
			tac.prompt($.i18n.prop('common.msg.box.title.notice'), $.i18n.prop('common.msg.box.param.invalid'), 
					$.i18n.prop('common.msg.box.btn.ok'), function(){
				$(document).keypress(keyEnterHandler);
			}, false);
		}
		else if ('000-000-006' == __data.retcode)
		{
			$('#login_phone_verifycode_input_tip').show();
			$('#login_phone_verifycode_input_tip').text($.i18n.prop('loginframe.sign.phonenum.verifycodeIsValid'));
			$('#login_phone_verifycode_input').focus();
			_getVerifyCode();
			$(document).keypress(keyEnterHandler);
		}
		else
		{
			tac.prompt($.i18n.prop('common.msg.box.title.notice'), $.i18n.prop('common.msg.box.system.error'), 
					$.i18n.prop('common.msg.box.btn.ok'), function(){
				$(document).keypress(keyEnterHandler);
			}, false);
		}
	}
	
	var _loginSuccess = function(loginParam){
		
		_queryAgentConfigure();
		register(loginParam.phonenum);
		tac.modules.msg.showShortMsg({
			txt : $.i18n.prop('common.msg.box.loginsuccess'),
			type : 2,
			level : 1
		});
		$('#work-page').show();
		$('.welcome-page').fadeOut(function(){
		});
		$('#top_system_img').click();
		$('#top_sign_agent_info_showname').text($.i18n.prop('top.sign.agentid'));
		$('#top_sign_agent_info_text').text(loginParam.agentId);
		$('#top_sign_phone_info_text').text(loginParam.phonenum);
		$('#top_sign_img').text($.i18n.prop('top.sign.signout'));
		hide();
		menu.render();
		tac.modules.phone.change2Free();
		tac.modules.event.startEventListener();
        window.onbeforeunload = function(){
		    return $.i18n.prop('common.msg.box.leavepage');
	    };
	}
	
	var _loginSuccessByAccount = function(loginParam,phoneNum){
		
		_queryAgentConfigure();
		register(phoneNum);
		tac.modules.msg.showShortMsg({
			txt : $.i18n.prop('common.msg.box.loginsuccess'),
			type : 2,
			level : 1
		});
		$('#work-page').show();
		$('.welcome-page').fadeOut(function(){
		});
		$('#top_system_img').click();
		$('#top_sign_agent_info_showname').text($.i18n.prop('loginframe.sign.account'))
		$('#top_sign_agent_info_text').text(loginParam.agentAccount);
		$('#top_sign_phone_info_text').text(phoneNum);
		$('#top_sign_img').text($.i18n.prop('top.sign.signout'));
		hide();
		menu.render();
		tac.modules.phone.change2Free();
		tac.modules.event.startEventListener();
        window.onbeforeunload = function(){
		    return $.i18n.prop('common.msg.box.leavepage');
	    };
	}
	
	
	var _queryAgentConfigure = function()
	{
		var getAgentConfigure_url = tacCfg.url.agentRestServer+tacCfg.service.agentconfigure.getAgentConfigure;
		tac.ajax({
			url : getAgentConfigure_url,
			type:"GET",
			async : true,
			loading:true,
			contentType : 'application/json;charset=utf-8',
			success : function(_data)
			{
				if(_data.retcode == 0)
				{
					if (_data.result.isAutoAnswer != null)
					{
						tac.data.set("IS_AUTOANSWER", _data.result.isAutoAnswer.propValue);
						_changeToAutoAnswer();
					}
					if (_data.result.maxWorkTime != null && _data.result.maxWorkTime.propValue <= LoginMode.maximumWorkTime && _data.result.maxWorkTime.propValue >= LoginMode.minimumWorkTime)
					{
						tac.data.set("MAX_WORKTIME", _data.result.maxWorkTime.propValue);
					}
					if (_data.result.outCallerNo != null)
					{
						tac.data.set("OUTCALLERNO", _data.result.outCallerNo.propValue);
						//更新系统接入码
						_updateSysCallerNo(_data.result.outCallerNo.propValue);
					}
					
				}
			},
		});
		
		
	}
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
		}
	};
	
	var _signOutAsk = function(){
		tac.prompt($.i18n.prop('common.msg.box.title.notice') , $.i18n.prop('top.sign.confirm.signout') , $.i18n.prop('common.msg.box.btn.ok'), function(){
			_signOut();
		});
	};
	
	
	var _signOut = function(){
		deRegister();
		window.onbeforeunload = null;

		tac.data.remove('IS_CENSOR');
		signService.logout();
		
	}
	
	
	var _getVerifyCode = function(){
		clearTimeout(_verifyCode_timerInterval);
		if ('' == _login_page_last_agentId)
		{
			//没有输入工号，不需要验证码校验
			$('#login_phone_verifycode_input').val('');
			return;
		}
		signService.verifyCode(_login_page_last_agentId, function (__data){
			if (RestRetCode.SUCCESS == __data.retcode){
				if (__data.result.needVerifyCode == true)
				{
					//需要进行验证码校验
					$('#login_phone_verifycode_input').val('');
					$('#login_phone_verifycode_img').attr('src', 'data:image/jpeg;base64,' + __data.result.imageData);
					$('#login_phone_verifycode_div').show();
					clearTimeout(_verifyCode_timerInterval);
					_verifyCode_timerInterval = setTimeout(_getVerifyCode, 300000); //每隔5分钟刷新一次验证码
				}
				else
				{
					//不需要验证码校验
					$('#login_phone_verifycode_input').val('');
					$('#login_phone_verifycode_div').hide();
				}
			}
			else
			{
				$('#login_phone_verifycode_input').val('');
				$('#login_phone_verifycode_div').hide();
			}
		});
	}
	
	/*
	 * 渲染
	 */
	var render = function(){
		if(el){
			_initEvent();
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
	tac.modules.sign = module = {
		init : init,
		render : render,
		show : show,
		hide : hide,
		destory : destory
	};
	return module;
});
