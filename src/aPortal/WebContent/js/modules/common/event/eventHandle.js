/**
 * 
 */
define(['tac', 'tacCfg'], function(tac , tacCfg) {
	var module = null;
	var startEventListener_timer = null;
	var global_heartBeatValue = 0;
	var global_noAccessNumber = 0;
	var _checkOutFromWorkStatusTaskId = null;
	var _workStatusTimes = 0; //进入工作态次数
	var _currentTimeStatus = 0;// 0初始化，1表示上午；2表示下午；3表示晚上
	
	var getEventListener = function(){
		_changeBackground();
		$.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.event.event + "?agentId=" + tac.data.get("AGENT_ID"),
			async : true,
			contentType : "application/json",
			dataType : "json",
			type : "GET",
			success : function(data, textStatus, jqXHR){
				if(data == undefined || data == "")	
				{
					global_heartBeatValue += 1;
					tac.modules.debugbox.pushInfo("Event result is undefined ! It's " + global_heartBeatValue + " times");
					
					if(global_heartBeatValue > 10){
						tac.modules.debugbox.pushInfo("Event result is undefined ! It's over " + global_heartBeatValue + " times about losing heart");
						global_heartBeatValue = 0;
					}
					else
					{
						startEventListener_timer = setTimeout(function(){
							getEventListener();
						}, 500);
					}
					return;
				}
				
				global_heartBeatValue = 0;
				if(data.retcode != RestRetCode.SUCCESS)
				{
					if (data.retcode == "000-003" || data.retcode == "-3")
					{
						global_noAccessNumber++;
						tac.modules.debugbox.pushInfo("No auth to visit the interface! It's " + global_noAccessNumber + " times");
						if (global_noAccessNumber > 5)
						{
							tac.modules.debugbox.pushInfo("No auth to visit the interface! It's over 5 times");
							_agentLogoutListener();
							return;
						}
					}
					if(data.retcode == "100-006" )
					{
						tac.modules.debugbox.pushInfo("The current agent has not logined!");
						_agentLogoutListener();
					}
					else
					{
						startEventListener_timer = setTimeout(function(){
							getEventListener();
						}, 500);
					}
					return;
				}
				
				global_noAccessNumber = 0;
				
				var agentEvents = data.event;
				if(null == agentEvents || agentEvents.length == 0)
				{
					startEventListener_timer = setTimeout(function(){
						getEventListener();
					}, 500);
					return;
				}
				
				try{
					_agentEventHandle(agentEvents);
				}catch (e) {
					console.error(e);
				}
				getEventListener();
			},
			error : function (data, textStatus, jqXHR){
				global_heartBeatValue += 1;
				tac.modules.debugbox.pushInfo("Get event timeout! It's " + global_heartBeatValue + " times");
				if(global_heartBeatValue > 10){
					global_heartBeatValue = 0;
					tac.modules.debugbox.pushInfo("Get event timeout! It's over 10 times about losing heart");
					_agentLogoutListener();
				}
				else
				{
					startEventListener_timer = setTimeout(function(){
						getEventListener();
					}, 500);
				}
			}
			
		});
		
		
	}
	
	var _changeBackground = function() {
		var hours = new Date().getHours();
		if (hours >=6 && hours <12) {
			//上午
			if (_currentTimeStatus != 1) {
				_currentTimeStatus = 1;
				$("#home_work_home_banner_backgound").removeClass();
				$("#home_work_home_banner_backgound").addClass("home_morning");
				$("#home_work_home_banner_text").text($.i18n.prop('loginframe.sign.morning'));
			}
			
		} else if (hours >= 12 && hours < 18)  {
			//下午
			if (_currentTimeStatus != 2) {
				_currentTimeStatus = 2;
				$("#home_work_home_banner_backgound").removeClass();
				$("#home_work_home_banner_backgound").addClass("home_afternoon");
				$("#home_work_home_banner_text").text($.i18n.prop('loginframe.sign.afternoon'));
			}
		} else {
			//晚上
			if (_currentTimeStatus != 3) {
				_currentTimeStatus = 3;
				$("#home_work_home_banner_backgound").removeClass();
				$("#home_work_home_banner_backgound").addClass("home_night");
				$("#home_work_home_banner_text").text($.i18n.prop('loginframe.sign.night'));
			}
		}
		
	}
	
	
	
	/**
	 * 座席异常签出
	 */
	var _agentLogoutListener = function()
	{
		tac.isLogin = false; 
		window.onbeforeunload = null;
		tac.setPromptFlag()
		tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
				$.i18n.prop('common.msg.box.haslogout'), 
				$.i18n.prop('common.msg.box.btn.ok'), function (){
			window.location = "home.html";
		},true,null,null,null,null,true);
		tac.modules.status.stopTimer();
		tac.modules.msg.showShortMsg({
			txt : $.i18n.prop('common.msg.box.haslogout'),
			type : 2,
			level : 2
		});
		
	}
	
	
	var startEventListener = function ()
	{
		var startEventListener_timer = null;
		var global_heartBeatValue = 0;
		var global_noAccessNumber = 0;
		getEventListener();
	}
	
	
	var _agentEventHandle = function(event)
	{
		tac.modules.debugbox.pushInfo("Receive Event :" + JSON.stringify(event));
		_statusChangeEventHandle(event);
		
	}
	
	var _statusChangeEventHandle = function(event){
		var eventType = event.eventType;
		switch(eventType)
		{
			case 'AgentState_SetNotReady_Success': //Into busy status successfully
			case 'AgentState_Force_SetNotReady':   //Forcibly into busy status
				clearAutoExitFromWorkStatusTimer();
				tac.modules.status.change2Busy();
				break;
			case 'AgentState_Ready':               //Into Idle status
			case 'AgentState_Force_Ready':         //Forcibly Into Idle status
			case 'AgentState_CancelNotReady_Success':  //Cancel busy status successfully, and agent into Idle status
			case 'AgentState_CancelRest_Success':      //Cancel rest status successfully, and agent into Idle status
			case 'AgentState_CancelWork_Success':      //Exit work status successfully, and agent into Idle status
				clearAutoExitFromWorkStatusTimer();
				tac.modules.status.change2Free();
				break;
			case 'AgentState_SetRest_Success':        //Into rest status successfully
				tac.modules.status.change2Rest();
				break;
			case 'AgentState_Rest_Timeout':           //Only tell the agent, rest timeout
				break;
			case 'AgentState_SetWork_Success':       //Into work status successfully
				tac.modules.status.change2Arrange();
				break;
			case 'AgentState_Work':					//After finishing talking, agent into work status
				tac.modules.status.change2Arrange();
				if (_workStatusTimes == 0)
				{
					//登录后首次进入工作态
					_workStatusTimes++;
					break;
				}
				else
				{
					_startTimerToAutoExitFromWorkStatus();
				}
				break;
			case 'AgentState_Busy': 
				clearAutoExitFromWorkStatusTimer();
				tac.modules.status.change2Connecting();
				break;
			case 'AgentEvent_Ringing':    //坐席来电提醒
				document.getElementById("home_phoneRing").play();
				tac.modules.phone.change2Ring(event);
				break;
			case 'AgentEvent_Auto_Answer':   //坐席自动应答
				tac.modules.phone.change2AutoAnswer(event);
				break;
			case 'AgentEvent_Talking':  //座席进入Talking
				document.getElementById("home_phoneRing").pause();
				tac.modules.phone.change2Talking(event);
				tac.modules.status.change2Call();
				break;
			
			case 'AgentEvent_Call_Out_Fail':  //坐席外呼失败
				tac.modules.phone.callOutFailedEvent();
				break;
			case 'AgentEvent_Inside_Call_Fail':  //内部呼叫失败
				tac.modules.phone.callInnerFailedEvent();
				break;
			case 'AgentEvent_Hold':   //保持成功
				break;
			case 'AgentEvent_Call_Release':  //座席退出呼叫
				document.getElementById("home_phoneRing").pause();
				tac.modules.phone.callReleaseEvent(event);
				break;
			case 'AgentEvent_Customer_Release':  //客户退出呼叫
				document.getElementById("home_phoneRing").pause();
				break;	
			case 'AgentEvent_Conference': //三方通话
				tac.modules.phone.conferenceEvent(event);
				break;	
			case 'AgentEvent_TransOutResult': //转外部电话的结果
				tac.modules.phone.transOutResultEvent(event);
				break;	
			case 'AgentEvent_SuccTransResult': //成功转结果
				tac.modules.phone.succTransferResultEvent(event);
				break;	
			case 'AgentOther_PhoneRelease':  
				document.getElementById("home_phoneRing").pause();
				tac.modules.phone.change2Free();
				break;
			case 'AgentOther_PhoneOffhook':
				tac.modules.phone.phoneOffHook();
				break;		
			case 'AgentOther_PhoneBusy':
				document.getElementById("home_phoneRing").pause();
				break;
			case 'AgentEvent_Consult_Fail':  //咨询失败
				tac.modules.phone.innerHelpFailedEvent(event);
				break;
			case 'AgentEvent_No_Answer':  //坐席久不应答
				tac.modules.phone.setPopupButtonsDisable(event);
				break;
		}
	}
	
	var clearAutoExitFromWorkStatusTimer = function(){
		if (_checkOutFromWorkStatusTaskId)
		{
			clearTimeout(_checkOutFromWorkStatusTaskId);		
		}
	}

	var _startTimerToAutoExitFromWorkStatus = function(){
		if (_checkOutFromWorkStatusTaskId)
		{
			clearTimeout(_checkOutFromWorkStatusTaskId);		
		}
		var timeout = tac.data.get("MAX_WORKTIME");
		if (timeout == null || timeout == 0)
		{
			//如果时间为0，则表示不会自动退出整理态
			_checkOutFromWorkStatusTaskId = null;
			return;
		}
		
		_checkOutFromWorkStatusTaskId = setTimeout(function(){
			tac.modules.status.autoExitWork();
		}, timeout * 1000);
	}
	
	
	
	tac.modules.event = module = {
		startEventListener : startEventListener,
		clearAutoExitFromWorkStatusTimer : clearAutoExitFromWorkStatusTimer
	};
	return module; 
});