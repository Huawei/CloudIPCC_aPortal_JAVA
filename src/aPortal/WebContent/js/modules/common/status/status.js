define(['tac', 'tacCfg' , 'component/vmenu/vmenu' ,
	'text!../../../../html/modules/common/status/status.html' , 
	'modules/common/status/statusService'], 
function(tac, tacCfg ,VMenu ,html, statusService) {
	var statusMap = {
		init : -1,			//未签入
		free : 0,			//空闲态
		busy : 1,			//示忙态
		arrange : 2,		//整理态
		call : 3,		//通话态
		keep : 4, 		//保持中....
		connecting : 5	//连接中....
	};
	
	var statusBeginTime = new Date();//状态开始时间
	var holdBeginTime = null; //保持中
	
	var module = null;
	
	var currentStatus = statusMap.init;//当前状态
	
	var menuStyleHtmlList = ['<span class="fa fa-ban"></span> ','<span class="fa fa-inbox fa-lg"></span> ','<span class="fa fa-coffee"></span> '];
	
	var isCallRequestBusy = false; //是否调用了请求示忙接口
	
	var isCallRequestFree = false; //是否调用了请求空闲接口

	/*
	 * 定义变量
	 */
	var el = null;
	
	var domMap = null;
	
	var intervalTimerId = null;
	
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
	
	/**
	 * 根据当前状态获取状态名称
	 */
	var _getCurrentStatusName = function(){
		switch (currentStatus) {
		case statusMap.free:
			return 'top.status.free';
		case statusMap.busy:
			return 'top.status.busy';
		case statusMap.arrange:
			return 'top.status.arrange';
		case statusMap.call:
			return 'top.status.call';
		case statusMap.keep:
			return 'top.status.keep';
		case statusMap.connecting:
			return 'top.status.connecting';
		default:
			return 'top.status.notchecked';
		}
	}
	
	
	var _startTimer = function ()
	{
		if (null == intervalTimerId)
		{
			intervalTimerId = window.setInterval(_computeStatusDuration, 1000);
		}
		else
		{
			_computeStatusDuration();
		}
		
	}
	/**
	 * 计算当前状态持续时长
	 */
	var _computeStatusDuration =  function(){
		var duration = 0;
		if (null == holdBeginTime){
			duration = parseInt((new Date().getTime() -  statusBeginTime.getTime()) / 1000, 10);
		}
		else{
			duration = parseInt((new Date().getTime() -  holdBeginTime.getTime()) / 1000, 10);
		}
		var h = parseInt(duration / 3600 , 10);
		var _second = duration % 3600;
		var m = parseInt(_second / 60 , 10);
		var s = _second % 60;
		domMap.statusText.html($.i18n.prop(_getCurrentStatusName()));
		domMap.statusTime.html(' ' + (h < 10 ? '0' : '') + h + ':'+ (m < 10 ? '0' : '') + m + ':' + (s < 10 ? '0' : '') + s);
		
	}
	
	
	
	/*
	 * 事件列表
	 */
	var eventList = [
		{
			target : 'top_status_img',
			attach : 'on',
			type : 'click',
			action : function(e) {
				$("#top_status_info").trigger('click');
			}
		},
		{
			target : 'top_status_info',
			attach : 'on',
			type : 'click',
			action : function(e) {
				var menuItems = null;
				if(currentStatus == statusMap.arrange){
					menuItems = [
						{
							text : menuStyleHtmlList[1] + $.i18n.prop('top.status.reqeust_free'),
							onClick : _sayFree
						}
					];
					
					
				}
				else if(currentStatus == statusMap.busy){
					menuItems = [
						{
							text : menuStyleHtmlList[1] + $.i18n.prop('top.status.reqeust_free'),
							onClick : _sayFree
						}
					];
				}
				else if(currentStatus == statusMap.free){
					menuItems = [
						{
							text : menuStyleHtmlList[1] + $.i18n.prop('top.status.request_busy'),
							onClick : _sayBusy
						}
					];
				}
				if (null != menuItems)
				{
					new VMenu({
						id : 'top_status_select_list',
						top : $('#top_status_info_text').offset().top + $('#top_status_info_text').height(),
						left : $('#top_status_info_text').offset().left,
						item : menuItems
					}).show();
				}
				
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
			statusDiv : $('#top_status_info'),
			statusText : $('#top_status_info_text'),
			statusTime : $('#top_status_info_time')
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
	/*
	 * 渲染
	 */
	var render = function(){
		if(el){
			$("#top_status").empty();
			el.append($.tmpl(html));
			_initEvent();
			_initElement();
			_i18nInit();
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
	

	
	//处理消息
	var onMessage = function(__msg){
		if(__msg){
			try{
				__msg = JSON.parse(__msg);
				if(__msg.CTI && __msg.CTI.eventType == 'AGENT_MANAGER_STAUTS'){
					var status = __msg.CTI.agentStatus;
					agentStatusAction(status);
				}
			}catch(e){

			}
		}
	};
	
	
	//设置当前坐席状态
	var setCurrentStatus = function(status){
		return currentStatus = status;
	};
	
	//获取当前坐席状态
	var getCurrentStatus = function(){
		return currentStatus;
	};
	
	
	
	/**
	 * 请求示忙
	 */
	var _sayBusy = function(){
		isCallRequestBusy = true;
		statusService.sayBusy(function(__data){
			if (RestRetCode.SUCCESS != __data.retcode){
				isCallRequestBusy = false;
				tac.tips({type:'failed',content:$.i18n.prop('common.msg.box.requestbusyfailed'),timeout:3000});
			}else{
				tac.modules.event.clearAutoExitFromWorkStatusTimer();
			}
		}, function(){
			isCallRequestBusy = false;
		});
	};
	
	/**
	 * 变成示忙态
	 */
	var change2Busy = function (){
		statusBeginTime = new Date();
		currentStatus = statusMap.busy;
		_startTimer();
		el.find('div.user-face').attr('class' ,'user-face pull-left relative badge-br').addClass('busy');
		if(isCallRequestBusy){
			isCallRequestBusy = false;
		}
	}
	
	
	
	
	/**
	 * 请求空闲
	 */
	var _sayFree = function(){
		isCallRequestFree = true;
		statusService.sayFree(function(__data){
			if (RestRetCode.SUCCESS != __data.retcode){
				isCallRequestFree = false;
				tac.tips({type:'failed',content:$.i18n.prop('common.msg.box.requestfreefailed'),timeout:3000});
			}else{
				tac.modules.event.clearAutoExitFromWorkStatusTimer();
			}
		}, function(){
			isCallRequestFree = false;
		});
	};
	
	
	/**
	 * 变成空闲态
	 */
	var change2Free = function(){
		holdBeginTime = null;
		statusBeginTime = new Date();
		currentStatus = statusMap.free;
		_startTimer();
		el.find('div.user-face').attr('class' ,'user-face pull-left relative badge-br').addClass('free');
		if(isCallRequestFree){
			isCallRequestFree = false;
		}
	}
	
	/**
	 * 整理态
	 */
	var change2Arrange = function(){
		holdBeginTime = null;
		statusBeginTime = new Date();
		currentStatus = statusMap.arrange;
		_startTimer();
		el.find('div.user-face').attr('class' ,'user-face pull-left relative badge-br').addClass('arrange');
	}
	
	/**
	 * 连接中....
	 */
	var change2Connecting = function(){
		holdBeginTime = null;
		statusBeginTime = new Date();
		currentStatus = statusMap.connecting;
		_startTimer();
		el.find('div.user-face').attr('class' ,'user-face pull-left relative badge-br').addClass('busy');
	}
	
	
	
	/**
	 * 通话中
	 */
	var change2Call = function(){
		holdBeginTime = null;
		if (currentStatus != statusMap.call
				&& currentStatus != statusMap.keep)
		{
			//之前的状态不是通话和保持时，通话开始时间重新计算
			statusBeginTime = new Date();
		}
		currentStatus = statusMap.call;
		_startTimer();
		el.find('div.user-face').attr('class' ,'user-face pull-left relative badge-br').addClass('busy');
	}
	
	/**
	 * 保持中
	 */
	var change2Keep = function(){
		holdBeginTime = new Date();
		currentStatus = statusMap.keep;
		_startTimer();
		el.find('div.user-face').attr('class' ,'user-face pull-left relative badge-br').addClass('busy');
	}
	
	/**
	 * 取消保持后，继续通话
	 */
	var continueCall = function(){
		holdBeginTime = null;
		currentStatus = statusMap.call;
		_startTimer();
		el.find('div.user-face').attr('class' ,'user-face pull-left relative badge-br').addClass('busy');
	}
	
	/**
	 * 达到最大整理时长时，退出整理态
	 */
	var autoExitWork= function(){
		isCallRequestFree = true;
		statusService.sayFree(function(__data){
			if (RestRetCode.SUCCESS != __data.retcode)
			{
				isCallRequestFree = false;
			}
		}, function(){
			isCallRequestFree = false;
		});
	};
	
	var stopTimer = function ()
	{
		if (null != intervalTimerId)
		{
			clearInterval(intervalTimerId);
			currentStatus = statusMap.init;
		}
	}
	/*
	 * 暴露接口
	 */
	tac.modules.status = module = {
		init : init,
		render : render,
		show : show,
		stopTimer : stopTimer,
		change2Connecting : change2Connecting,
		change2Free : change2Free,
		change2Busy : change2Busy,
		change2Arrange : change2Arrange,
		change2Call : change2Call,
		change2Keep : change2Keep,
		continueCall : continueCall,
		autoExitWork : autoExitWork
	    
	};
	return module;
});
