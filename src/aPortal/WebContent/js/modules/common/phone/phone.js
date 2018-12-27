define(['tac' ,'tacCfg' , 'text!../../../../html/modules/common/phone/phone.html',
	'modules/common/phone/phoneService',
	'modules/common/status/status',
	'modules/common/phone/phoneCall',
	'modules/common/phone/phoneTransfer',
	'modules/common/phone/phoneLinkingCall',
	'modules/common/phone/phoneAddCall', 
	'modules/common/phone/phoneCallAlert'], 
	function(tac, tacCfg, html, phoneService, status, phoneCall , phoneTransfer, phoneLinkingCall, phoneAddCall, phoneCallAlert) {
	
	var _phoneCurrentHoldCall = null;
	var _phoneCurrentCallInfo = null;
	var _actionWaitFlag = false, _actionWaitTime = 500;
	var _phoneInfoPopBoxShow = 0;
	var _currentCallOutCallId = null; //当前呼出的callId;
	var _currentHelpCall = null;
	
	var _isTransferOut = false;
	var _allPopBoxHide = function(){
		phoneCall.hide();
		phoneTransfer.hide();
		phoneLinkingCall.hide()
		phoneAddCall.hide();
		currentShowPopBox = null;
	};
	
	var _CallStatus = {
		"Talking" : 0,
		"Ringing" : 1,
		"Phoneing": 2,
		"Keeping" : 3
	};
	
	var isMute = false; //是否静音了	
	
	/*
	 * 初始化
	 */
	var init = function(__cfg){
		if(__cfg){
			if(__cfg.el){
				el = __cfg.el;
				phoneCall.init().render();
				phoneTransfer.init().render();
				phoneLinkingCall.init().render();
				phoneAddCall.init().render();
				phoneCallAlert.init().render();
			}
		}
		return module;
	};

	/*
	 * 初始化DOM
	 */
	var _initElement = function(){
		domMap = {
			workBox : $('#main_work'),
			phoneBox : $('#top_phone'),
			answerBtn : $('#top_phone_answer_btn'),
			hangupBtn : $('#top_phone_hangup_btn'),
			keepBtn : $('#top_phone_keep_btn'),
			transferBtn : $('#top_phone_transfer_btn'),
			helpBtn : $("#top_phone_help_btn"),
			callBtn : $('#top_phone_call_btn'),
			topPhonePopboxBtn : $('#top_phone_popbox_btn'),
			topPhoneShowScore : $('#top_phone_show_score'),
			phoneSelf : $('#top_phone_self'),
			phoneTarget : $('#top_phone_target'),
			phoneNumArea : $('#top_phone_num_area'),
			phoneNumSelf : $('#top_phone_num_self'),
			phoneNumTarget : $('#top_phone_num_target'),
			keepBtnTxt : $('#top_phone_keep_btn_txt'),
			phoneBoxClose : $('#top_phone_show_hide_btn'),
			muteBtn : $('#top_phone_mute_btn'),
			addCallBtn : $('#top_phone_addcall_btn'),
			fetchBackBtn : $('#top_phone_fetchBack_btn')
			
		};
		topPhoneArriveAudio = document.getElementById('top_phone_arrive_audio');
	};

	
	/*
	 * 事件列表
	 */
	var eventList = [
		{
			target : 'top_phone_answer_btn',
			attach : 'on',
			type : 'click',
			action : function(){
				_answerBtnClick();
			}
		},
		{
			target : 'top_phone_hangup_btn',
			attach : 'on',
			type : 'click',
			action : function(){
				_releaseBtnClick();
			}
		},
		{
			target : 'top_phone_keep_btn',
			attach : 'on',
			type : 'click',
			action : function(){
				_holdBtnClick();
			}
		},
		{
			target : 'top_phone_transfer_btn',
			attach : 'on',
			type : 'click',
			action : function(){
				_transferBtnClick();
			}
		},{
			target : 'top_phone_help_btn',
			attach : 'on',
			type : 'click',
			action : function(){
				_helpBtnClick();
			}
		},
		{
			target : 'top_phone_fetchBack_btn',
			attach : 'on',
			type : 'click',
			action : function(){
				_fetchBackBtnClick();
			}
		},
		{
			target : 'top_phone_call_btn',
			attach : 'on',
			type : 'click',
			action : function (){
				_callBtnClick();
			}
		},
		{
			target : 'top_phone_hide_btn',
			attach : 'on',
			type : 'click',
			action : function(){
				_topPhoneBoxHide();
			}
		},
		{
			target : 'top_phone_show_btn',
			attach : 'on',
			type : 'click',
			action : function(){
				_topPhoneBoxShow();
			}
		},
		{
			target : 'top_phone_mute_btn',
			attach : 'on',
			type : 'click',
			action : function(){
				_muteBtnClick();
			}
		},
		{
			target : 'top_phone_addcall_btn',
			attach : 'on',
			type : 'click',
			action : function(){
				_addCallBtnClick();
			}
		}
	];
	
	
	
	
	
	/**
	 * 点击应答按钮
	 */
	var _answerBtnClick = function(){
		if(!domMap.answerBtn.hasClass('btn-phone-disabled'))
		{
			if(_actionWaitFlag){
				return;
			}
			_actionWaitFlag = true;
			window.setTimeout(function(){
				_actionWaitFlag = false;
			}, _actionWaitTime);
			document.getElementById("home_phoneRing").pause();
			phoneService.answer(function(__data){
				if (RestRetCode.SUCCESS == __data.retcode)
				{
					domMap.answerBtn.addClass('btn-phone-disabled').removeClass('animated tada infinite');
				}
				else
				{
					domMap.answerBtn.addClass('btn-phone-disabled').removeClass('animated tada infinite');
					tac.tips({type:'failed',content:$.i18n.prop('phone.msg.answerFailure'),timeout:3000});
				}
			});
		}
		
	}
	
	/**
	 * 点击释放按钮
	 */
	var _releaseBtnClick = function(){
		if(!domMap.hangupBtn.hasClass('btn-phone-disabled'))
		{
			if(_actionWaitFlag){
				return;
			}
			_actionWaitFlag = true;
			window.setTimeout(function(){
				_actionWaitFlag = false;
			}, _actionWaitTime);
			if (_isTransferOut)
			{
				phoneService.cancelTransfer();
			}
			else
			{
				phoneService.hangup();
			}
			_isTransferOut = false;
		}
	}

	/**
	 * 点击保持/恢复按钮
	 */
	var _holdBtnClick = function(){
		if(!domMap.keepBtn.hasClass('btn-phone-disabled'))
		{
			if(_actionWaitFlag){
				return;
			}
			_actionWaitFlag = true;
			window.setTimeout(function(){
				_actionWaitFlag = false;
			}, _actionWaitTime);
			
			if (_phoneCurrentHoldCall == null)
			{
				phoneService.keep(function(__data){
					if (RestRetCode.SUCCESS == __data.retcode)
					{
						//接口调用成功
						_phoneCurrentHoldCall = _phoneCurrentCallInfo.callId;
						domMap.transferBtn.addClass('btn-phone-disabled');//保持状态不可转移
						domMap.helpBtn.addClass('btn-phone-disabled'); //内部求助不可用
						domMap.hangupBtn.addClass('btn-phone-disabled');//保持状态不可释放
						domMap.muteBtn.addClass('btn-phone-disabled');//保持状态不可静音
						domMap.keepBtn.attr('title' , $.i18n.prop('top.phone.recovery'));
						domMap.keepBtn.removeClass('icon-keep').addClass('icon-recover');
						domMap.addCallBtn.addClass('btn-phone-disabled');//二次拨号不可用
						tac.modules.status.change2Keep();
						
					}
					else
					{
						tac.tips({type:'failed',content:$.i18n.prop('phone.msg.keepFailure'),timeout:3000});
					}
				});
			}
			else
			{
				phoneService.resume(_phoneCurrentHoldCall, function(__data){
					if (RestRetCode.SUCCESS == __data.retcode)
					{
						//接口调用成功
						_phoneCurrentHoldCall = null;
						domMap.keepBtn.removeClass('icon-recover').addClass('icon-keep');
						domMap.keepBtn.attr('title' , $.i18n.prop('top.phone.keep'));
						tac.modules.status.continueCall();
					}
					else
					{
						tac.tips({type:'failed',content:$.i18n.prop('phone.msg.resumeFailure'),timeout:3000});
					}
				});
			}
			
		}
	}
	
	/**
	 * 点击转移按钮
	 */
	var _transferBtnClick = function(){
		if(!domMap.transferBtn.hasClass('btn-phone-disabled'))
		{
			if(_actionWaitFlag){
				return;
			}
			_actionWaitFlag = true;
			window.setTimeout(function(){
				_actionWaitFlag = false;
			}, _actionWaitTime);
			
			phoneTransfer.show();
		}
	}
	
	/**
	 * 内部求助
	 */
	var _helpBtnClick = function(){
		if(!domMap.helpBtn.hasClass('btn-phone-disabled'))
		{
			if(_actionWaitFlag){
				return;
			}
			_actionWaitFlag = true;
			window.setTimeout(function(){
				_actionWaitFlag = false;
			}, _actionWaitTime);
			
			phoneLinkingCall.show();
		}
	}

	/**
	 * 取回呼叫
	 */
	var _fetchBackBtnClick = function(){
		if(domMap.fetchBackBtn.hasClass('btn-phone-disabled'))
		{
			return;
		}
		
		if (_currentHelpCall)
		{
			if (_currentHelpCall.mode == 1)
			{
				//两方求助取回呼叫
				phoneService.dropCall({"callId" : _currentHelpCall.callId}, function(__data){
					if (RestRetCode.SUCCESS != __data.retcode)
					{
						tac.tips({type:'failed',content:$.i18n.prop('phone.msg.fetchBackFailure'),timeout:3000});
					}
				});
			}
			else
			{
				//三方求助取回呼叫
				if (_currentHelpCall.partner)
				{
					phoneService.disconnect({"number" : _currentHelpCall.partner.split(',')[1]}, function(__data){
						if (RestRetCode.SUCCESS != __data.retcode)
						{
							tac.tips({type:'failed',content:$.i18n.prop('phone.msg.fetchBackFailure'),timeout:3000});
						}
					});
				}
				
			}
		}
		else
		{
			//转外部电话，取回呼叫
			phoneService.cancelTransfer(function(__data){
				if (RestRetCode.SUCCESS != __data.retcode)
				{
					tac.tips({type:'failed',content:$.i18n.prop('phone.msg.fetchBackFailure'),timeout:3000});
				}
			});
		}
	}
	
	/**
	 * 点击呼出按钮
	 */
	var _callBtnClick = function(){
		if(!domMap.callBtn.hasClass('btn-phone-disabled'))
		{
			if(_actionWaitFlag){
				return;
			}
			_actionWaitFlag = true;
			window.setTimeout(function(){
				_actionWaitFlag = false;
			}, _actionWaitTime);
			
			phoneCall.show();
		}
	} 
	
	/**
	 * 静音/取消静音
	 */
	var _muteBtnClick = function(){
		if(domMap.muteBtn.hasClass('btn-phone-disabled'))
		{
			return;
		}
		
		if(_actionWaitFlag){
			return;
		}
		_actionWaitFlag = true;
		window.setTimeout(function(){
			_actionWaitFlag = false;
		}, _actionWaitTime);
		
		if (!isMute)
		{
			phoneService.beginMute(function(__data){
				if (RestRetCode.SUCCESS == __data.retcode)
				{
					//接口调用成功
					_phoneCurrentHoldCall = null;
					_change2Mute();
				}
				else
				{
					tac.tips({type:'failed',content:$.i18n.prop('common.msg.box.mutefailed'),timeout:3000});
				}
			});
		}
		else
		{
			phoneService.endMute(function(__data){
				if (RestRetCode.SUCCESS == __data.retcode)
				{
					//接口调用成功
					_change2UnMute();
				}
				else
				{
					tac.tips({type:'failed',content:$.i18n.prop('common.msg.box.unmutefailed'),timeout:3000});
				}
			});
		}
	}
	
	/**
	 * 变为静音, 除了取消和挂断能用，其他都不能使用
	 */
	var _change2Mute = function(){
		isMute = true;
		domMap.hangupBtn.addClass('btn-phone-disabled');
		domMap.keepBtn.addClass('btn-phone-disabled');
		domMap.keepBtn.removeClass('icon-recover').addClass('icon-keep');
		domMap.transferBtn.addClass('btn-phone-disabled');
		domMap.helpBtn.addClass('btn-phone-disabled');
		domMap.callBtn.addClass('btn-phone-disabled').removeClass('bg-primary-x');
		domMap.muteBtn.removeClass('icon-mute').addClass('icon-unmute');
		domMap.muteBtn.attr('title' , $.i18n.prop('top.phone.recovery'));
		domMap.addCallBtn.addClass('btn-phone-disabled');//二次拨号不可用
	}
	
	/**
	 * 取消静音
	 */
	var _change2UnMute = function(){
		isMute = false;
		domMap.hangupBtn.removeClass('btn-phone-disabled');
		domMap.muteBtn.removeClass('icon-unmute').addClass('icon-mute');
		domMap.muteBtn.attr('title' , $.i18n.prop('top.phone.mute'));
		domMap.keepBtn.removeClass('btn-phone-disabled');
		domMap.helpBtn.removeClass('btn-phone-disabled');
		domMap.transferBtn.removeClass('btn-phone-disabled');
		if (_phoneCurrentCallInfo.feature == 7)
		{
			//外呼时可以进行二次拨号
			if (_currentCallOutCallId == _phoneCurrentCallInfo.callId)
			{
				domMap.addCallBtn.removeClass('btn-phone-disabled');
			}
		}
	}
	
	/**
	 * 二次拨号
	 */
	var _addCallBtnClick = function(){
		if(!domMap.addCallBtn.hasClass('btn-phone-disabled'))
		{
			if(_actionWaitFlag){
				return;
			}
			_actionWaitFlag = true;
			window.setTimeout(function(){
				_actionWaitFlag = false;
			}, _actionWaitTime);
			
			phoneAddCall.show(function(__num){
				if(__num){
					phoneService.secondDial({"number" : __num}, function(__data){
						phoneAddCall.hide();
						if (RestRetCode.SUCCESS != __data.retcode){
							tac.tips({type:'failed',content:$.i18n.prop('phone.msg.addCallFailure'),timeout:3000});
						}
					});
				}else{
					tac.tips({type:'failed',content:$.i18n.prop('phone.msg.addCallIsNull'),timeout:3000});
				}
			});
		}
	}
	
	
	
	/**
	 * 隐藏语音栏
	 */
	var _topPhoneBoxHide = function(){
		domMap.phoneBox.slideUp(500);
		domMap.workBox.animate({
			'margin-top' : '15px'
		}, 500);
	}
	
	/**
	 * 显示语音栏
	 */
	var _topPhoneBoxShow = function(){
		domMap.phoneBox.slideDown(500);
		domMap.workBox.animate({
			'margin-top' : '67px'
		}, 500);
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
			el.find('li > p').each(function(){
				if($(this).attr('i18nKey')){
					$(this).html($.i18n.prop($(this).attr('i18nKey')));
				}
			});
			el.find('p > span').each(function(){
				if($(this).attr('i18nKey')){
					$(this).html($.i18n.prop($(this).attr('i18nKey')));
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
			el.append($.tmpl(html));
			_initEvent();
			_initElement();
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

	
	var showPhoneNum = function(__from , __to , __currentCall){
		if(__from && __to){
			domMap.phoneNumSelf.html(__from);
			domMap.phoneNumTarget.html(__to);
		}else{
			domMap.phoneNumSelf.html((phoneMode == 'main' ? mainCall.from : subCall.from) || $.i18n.prop('top.content.from.noinformation'));
			domMap.phoneNumTarget.html((phoneMode == 'main' ? mainCall.to : subCall.to) || $.i18n.prop('top.content.to.noinformation'));
		}
		domMap.phoneNumArea.show();
		_topPhoneBoxShow();
	};
	
	var _showPhoneNum = function(__from , __to , __feature){
		domMap.phoneNumSelf.html(__from);
		domMap.phoneNumTarget.html(__to);
		domMap.phoneNumArea.show();
		_topPhoneBoxShow();
	};
	
	
	
	var hidePhoneNum = function(){
		domMap.phoneNumArea.hide();
	};
	
	
	/**
	 * 坐席久不应答
	 */
	var setPopupButtonsDisable = function(event) {
		phoneCallAlert.setButtonsDisable();
	}
	
	/**
	 * 正在转移中
	 */
	var isTransfering = function(isTransferOut) {
		domMap.answerBtn.addClass('btn-phone-disabled');
		_isTransferOut = isTransferOut;
		if (isTransferOut)
		{
			//转外部电话过程中，可以点击挂断
			domMap.hangupBtn.removeClass('btn-phone-disabled');
		}
		domMap.keepBtn.addClass('btn-phone-disabled');
		domMap.keepBtn.removeClass('icon-recover').addClass('icon-keep');
		domMap.keepBtn.attr('title' , $.i18n.prop('top.phone.keep'));
		domMap.transferBtn.addClass('btn-phone-disabled');
		domMap.helpBtn.addClass('btn-phone-disabled');
		domMap.callBtn.addClass('btn-phone-disabled');
		domMap.muteBtn.addClass('btn-phone-disabled');
		domMap.addCallBtn.addClass('btn-phone-disabled');
	}
	
	/**
	 * 正在求助中
	 */
	var isHelping = function (mode, innerHelpCallId){
		domMap.answerBtn.addClass('btn-phone-disabled');
		//转外部电话过程中，可以点击挂断
		domMap.hangupBtn.removeClass('btn-phone-disabled');
		domMap.keepBtn.addClass('btn-phone-disabled');
		domMap.keepBtn.removeClass('icon-recover').addClass('icon-keep');
		domMap.keepBtn.attr('title' , $.i18n.prop('top.phone.keep'));
		domMap.transferBtn.addClass('btn-phone-disabled');
		domMap.helpBtn.addClass('btn-phone-disabled');
		domMap.callBtn.addClass('btn-phone-disabled');
		domMap.muteBtn.addClass('btn-phone-disabled');
		domMap.addCallBtn.addClass('btn-phone-disabled');
		_currentHelpCall = {
				"callId" : innerHelpCallId,
				"mode" : mode,
				"originalCallId" : _phoneCurrentCallInfo.callid,
				"partner" : null
		};
	}
	
	/**
	 * 空闲可以外呼
	 */
	var change2Free = function() {
		domMap.answerBtn.addClass('btn-phone-disabled').removeClass('animated tada infinite');
		domMap.hangupBtn.addClass('btn-phone-disabled');
		domMap.keepBtn.addClass('btn-phone-disabled');
		domMap.keepBtn.removeClass('icon-recover').addClass('icon-keep');
		domMap.keepBtn.attr('title' , $.i18n.prop('top.phone.keep'));
		domMap.transferBtn.addClass('btn-phone-disabled');
		domMap.helpBtn.addClass('btn-phone-disabled');
		domMap.callBtn.removeClass('btn-phone-disabled').addClass('bg-primary-x');
		
		domMap.muteBtn.addClass('btn-phone-disabled');
		domMap.muteBtn.removeClass('icon-unmute').addClass('icon-mute');
		domMap.muteBtn.attr('title' , $.i18n.prop('top.phone.mute'));
		domMap.addCallBtn.addClass('btn-phone-disabled');
		domMap.fetchBackBtn.addClass('btn-phone-disabled');
		_currentCallOutCallId = null;
		_currentHelpCall = null;
		_phoneCurrentHoldCall = null;
		isMute = false;
		_isTransferOut = false;
	}
	
	/**
	 * 自动应答
	 */
	var change2AutoAnswer = function(event){
		domMap.answerBtn.addClass('btn-phone-disabled');
		domMap.hangupBtn.removeClass('btn-phone-disabled');
		domMap.keepBtn.addClass('btn-phone-disabled');
		domMap.keepBtn.removeClass('icon-recover').addClass('icon-keep');
		domMap.keepBtn.attr('title' , $.i18n.prop('top.phone.keep'));
		domMap.transferBtn.addClass('btn-phone-disabled');
		domMap.helpBtn.addClass('btn-phone-disabled');
		domMap.callBtn.addClass('btn-phone-disabled').removeClass('bg-primary-x');

		domMap.muteBtn.addClass('btn-phone-disabled');
		domMap.muteBtn.removeClass('icon-unmute').addClass('icon-mute');
		domMap.muteBtn.attr('title' , $.i18n.prop('top.phone.mute'));
		_showPhoneNum(event.content.caller, event.content.called,  event.content.feature);
		_allPopBoxHide();
		
	}
	
	/**
	 * 来电话了，只显示挂断、接听
	 */
	var change2Ring = function(event){
		domMap.answerBtn.removeClass('btn-phone-disabled').addClass('animated tada infinite');
		domMap.hangupBtn.removeClass('btn-phone-disabled');
		domMap.keepBtn.addClass('btn-phone-disabled');
		domMap.keepBtn.removeClass('icon-recover').addClass('icon-keep');
		domMap.keepBtn.attr('title' , $.i18n.prop('top.phone.keep'));
		domMap.transferBtn.addClass('btn-phone-disabled');
		domMap.helpBtn.addClass('btn-phone-disabled');
		domMap.callBtn.addClass('btn-phone-disabled').removeClass('bg-primary-x');

		domMap.muteBtn.addClass('btn-phone-disabled');
		domMap.muteBtn.removeClass('icon-unmute').addClass('icon-mute');
		domMap.muteBtn.attr('title' , $.i18n.prop('top.phone.mute'));
		_showPhoneNum(event.content.caller, event.content.called,  event.content.feature);
		_allPopBoxHide();
		
		//显示来电弹窗
		phoneCallAlert.show(event);
		
	}
	
	

	/**
	 * 变成通话中，只显示挂断和保持、转移、静音
	 */
	var change2Talking = function(event){
		domMap.answerBtn.addClass('btn-phone-disabled').removeClass('animated tada infinite');
		domMap.hangupBtn.removeClass('btn-phone-disabled');
		domMap.keepBtn.removeClass('icon-recover').addClass('icon-keep');
		domMap.fetchBackBtn.addClass('btn-phone-disabled');
		if (event.content.feature == 6 
				|| event.content.feature == 51
				|| event.content.feature == 52
				|| event.content.feature == 53)
		{
			//内部呼叫\内部两方咨询\内部三方咨询\多方通话\二次拨号 不能进行保持和静音
			domMap.keepBtn.addClass('btn-phone-disabled');
			domMap.transferBtn.addClass('btn-phone-disabled');
			domMap.helpBtn.addClass('btn-phone-disabled');
			domMap.muteBtn.addClass('btn-phone-disabled');
			domMap.addCallBtn.addClass('btn-phone-disabled');
		}
		else
		{
			domMap.keepBtn.removeClass('btn-phone-disabled');
			domMap.transferBtn.removeClass('btn-phone-disabled');
			domMap.helpBtn.removeClass('btn-phone-disabled');
			domMap.muteBtn.removeClass('btn-phone-disabled');
			if (event.content.feature == 7)
			{
				//外呼时可以进行二次拨号
				if (_currentCallOutCallId == event.content.callid)
				{
					domMap.addCallBtn.removeClass('btn-phone-disabled');
				}
			}
		}
		
		if (event.content.feature == 51)
		{
			if (_currentHelpCall && _currentHelpCall.callId == event.content.callid)
			{
				//为内部两方求助, 可以使用取回呼叫
				domMap.fetchBackBtn.removeClass('btn-phone-disabled');
			}
		}
		
		
		domMap.callBtn.addClass('btn-phone-disabled').removeClass('bg-primary-x');
		domMap.keepBtn.attr('title' , $.i18n.prop('top.phone.keep'));
		_showPhoneNum(event.content.caller, event.content.called,  event.content.feature);
		_phoneCurrentCallInfo = {
			"id" : event.content.callid,
			"callId" : event.content.callid,
			"to" : event.content.called,
			"from" : event.content.caller,
			"feature" : event.content.feature,
			"status" : _CallStatus.Talking,
			"direction" : getDirection(event.content.feature, event.content.called, event.content.caller)
		};
	}
	
	var getDirection = function(feature, called, caller){
		if (feature == 6)
		{
			//内部呼叫
			if (caller == tac.data.get("AGENT_ID"))
			{
				return 1;
			}
			else
			{
				return -1;
			}
		}
		else if (feature == 7){
			//呼出
			return 1; //主叫
		}
		else
		{
			//呼入
			return -1; //被叫
		}
	}
	

	/**
	 * 话机摘机事件
	 */
	var phoneOffHook = function(event){
		domMap.hangupBtn.removeClass('btn-phone-disabled');
		domMap.callBtn.addClass('btn-phone-disabled').removeClass('bg-primary-x');
		isMute = false;
		_isTransferOut = false;
	}
	
	/**
	 * 呼叫释放事件
	 */
	var callReleaseEvent = function(event){
		var callId = event.content;
		if (callId == _phoneCurrentHoldCall)
		{
			_phoneCurrentHoldCall = null;
		}
		
		if (null != _phoneCurrentCallInfo && _phoneCurrentCallInfo.callId == callId)
		{
			_phoneCurrentCallInfo = null;
		}
		
		
		phoneTransfer.hide();
		phoneLinkingCall.hide();
		
		_currentHelpCall = null;
		domMap.fetchBackBtn.addClass('btn-phone-disabled');
	}
	
	/**
	 * 会议事件
	 */
	var conferenceEvent = function (event){
		domMap.answerBtn.addClass('btn-phone-disabled').removeClass('animated tada infinite');
		domMap.hangupBtn.removeClass('btn-phone-disabled');
		domMap.keepBtn.addClass('btn-phone-disabled');
		domMap.keepBtn.removeClass('icon-recover').addClass('icon-keep');
		domMap.transferBtn.addClass('btn-phone-disabled');
		domMap.helpBtn.addClass('btn-phone-disabled');
		domMap.callBtn.addClass('btn-phone-disabled').removeClass('bg-primary-x');
		domMap.keepBtn.attr('title' , $.i18n.prop('top.phone.keep'));
		domMap.addCallBtn.addClass('btn-phone-disabled');
		domMap.fetchBackBtn.removeClass('btn-phone-disabled');
		if (_currentHelpCall && _currentHelpCall.callId == event.content.callid)
		{
			//为内部三方求助, 可以使用取回呼叫
			_currentHelpCall.partner = event.content.partner;
			_currentHelpCall.originalCallId = _phoneCurrentCallInfo != null ? _phoneCurrentCallInfo.callId: null;
		}
		domMap.fetchBackBtn.removeClass('btn-phone-disabled');
	}
	
	/**
	 * 呼叫失败事件
	 */
	var callOutFailedEvent = function (){
		tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
    			$.i18n.prop('phone.msg.callFailure'), 
				$.i18n.prop('common.msg.box.btn.ok'));
    	domMap.hangupBtn.addClass('btn-phone-disabled');
		$('#phone_call_btm_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phonecall.call'));
	}
	
	/**
	 * 内部呼叫失败事件
	 */
	var callInnerFailedEvent = function(){
		tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
    			$.i18n.prop('phone.msg.callInnerFailure'), 
				$.i18n.prop('common.msg.box.btn.ok'));
    	domMap.hangupBtn.addClass('btn-phone-disabled');
		$('#phone_call_btm_submit').removeClass('btn-gary').addClass('btn-success').html($.i18n.prop('phone.phonecall.call'));
	}
	
	/**
	 * 成功转结果通知事件
	 */
	var succTransferResultEvent = function(event){
		if (event.content.result == 0)
		{
			//转移成功
			phoneCall.hide();
		}
		else
		{
			//转移失败
			_transferFailed();
			phoneTransfer.resetBtn();
			tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
	    			$.i18n.prop('phone.msg.transferFailure'), 
					$.i18n.prop('common.msg.box.btn.ok'));
		}
	}
	
	/**
	 * 转外部电话结果事件
	 */
	var transOutResultEvent = function(event){

		if (event.content.result == 0)
		{
			//转移成功
			phoneTransfer.hide();
			domMap.fetchBackBtn.removeClass('btn-phone-disabled');
		}
		else
		{
			//转移失败
			_transferFailed();
			phoneTransfer.resetBtn();
			tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
	    			$.i18n.prop('phone.msg.transferFailure'), 
					$.i18n.prop('common.msg.box.btn.ok'));
		}
		_isTransferOut = false;
	}
	
	/**
	 * 转移失败
	 */
	var _transferFailed = function(){
		domMap.answerBtn.addClass('btn-phone-disabled').removeClass('animated tada infinite');
		domMap.hangupBtn.removeClass('btn-phone-disabled');
		domMap.keepBtn.removeClass('icon-recover').addClass('icon-keep');
		domMap.keepBtn.removeClass('btn-phone-disabled');
		domMap.keepBtn.attr('title' , $.i18n.prop('top.phone.keep'));
		domMap.transferBtn.removeClass('btn-phone-disabled');
		domMap.helpBtn.removeClass('btn-phone-disabled');
		domMap.muteBtn.removeClass('btn-phone-disabled');
		if (_phoneCurrentCallInfo.feature == 7)
		{
			//外呼时可以进行二次拨号
			if (_currentCallOutCallId == _phoneCurrentCallInfo.callId)
			{
				domMap.addCallBtn.removeClass('btn-phone-disabled');
			}
		}
		
		domMap.callBtn.addClass('btn-phone-disabled').removeClass('bg-primary-x');
	}
	
	/**
	 * 内部求助失败
	 */
	var innerHelpFailedEvent = function(){
		_currentHelpCall = null;
		tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
    			$.i18n.prop('phone.msg.callHelpFailure'), 
				$.i18n.prop('common.msg.box.btn.ok'));
		domMap.fetchBackBtn.addClass('btn-phone-disabled');
	}
	
	
	var setPhoneInfoPopBoxShow = function(__val){
		_phoneInfoPopBoxShow = __val;
	};
	
	var setCurrentCallOutCallId = function(__val)
	{
		_currentCallOutCallId = __val;
	}
	/**
	 * 暴露接口
	 */
	tac.modules.phone = module = {
		init : init,
		render : render,
		show : show,
		hide : hide,
		destory : destory,
		change2Free : change2Free,
		change2AutoAnswer:change2AutoAnswer,
		change2Ring : change2Ring,
		change2Talking : change2Talking,
		phoneOffHook : phoneOffHook,
		callReleaseEvent : callReleaseEvent,
		conferenceEvent : conferenceEvent,
		callOutFailedEvent : callOutFailedEvent,
		succTransferResultEvent : succTransferResultEvent,
		transOutResultEvent : transOutResultEvent,
		callInnerFailedEvent : callInnerFailedEvent,
		showPhoneNum : showPhoneNum,
		phoneAddCall : phoneAddCall,
		setPhoneInfoPopBoxShow : setPhoneInfoPopBoxShow,
		setCurrentCallOutCallId : setCurrentCallOutCallId,
		isTransfering : isTransfering,
		innerHelpFailedEvent: innerHelpFailedEvent,
		isHelping : isHelping,
		setPopupButtonsDisable : setPopupButtonsDisable
	}
	return module;
});
