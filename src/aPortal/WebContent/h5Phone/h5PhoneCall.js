var global_cloudIPCC_SDK;
var global_userPhoneNumber;
var global_tupDeamonIsOk;
var global_isCanReConnectToDeamon = true;
var global_isInitTupLogin = false;
var global_isInitTupCall = false;
var global_TupDeamonReadyTime;

function onTupDeamonReady() {
    console.info(new Date());
    console.info(new Date() + "TUP Deamon is Ready");
    global_tupDeamonIsOk = true;
    global_isCanReConnectToDeamon = false;
    setTimeout(function(){
    	//做一个保护，防止出现TupDeamon启动了，而没有收到serviceStartUp导致不连接TupCall和TupLogin
    	if (!global_isInitTupLogin && !global_isInitTupCall)
		{
    		console.error(new Date() + "Call doServiceStartUp");
    		global_cloudIPCC_SDK.tupDeamon.doServiceStartUp();
		}
    }, 4000);
}


function onTupDeamonClose() {
	
	if (global_isCanReConnectToDeamon)
	{
		console.error(new Date() + "TUP Deamon Connect Failed, reConnect to http");
		global_isCanReConnectToDeamon = false;
		global_cloudIPCC_SDK = new CloudIPCC_SDK({
			onTupDeamonReady: onTupDeamonReady,
			onTupDeamonClose: onTupDeamonClose,
			serviceStartUp: serviceStartUp,
			serviceShutDown: serviceShutDown,
			onTupLoginReady: onTupLoginReady,
			onTupLoginClose: onTupLoginClose,
			onTupCallReady: onTupCallReady,
			onTupCallClose: onTupCallClose,
			onVersionInfoNotify : onVersionInfoNotify,
			isHttp : true
		});
		return;
	}
	global_tupDeamonIsOk = false;
    console.error(new Date() + "TUP Deamon is Closed,please restart it");
    //没有安装或启动本地TUP服务
    tac.promptEx({
    	title :  $.i18n.prop('common.msg.box.title.notice'),
    	content : $.i18n.prop('common.msg.box.phone.initFailed'), 
    	width : 400,
		height : 200,
		submitText : $.i18n.prop('common.msg.box.btn.ok')
    });
}


function serviceStartUp() {
    console.info(new Date() + "TUP Service StartUp");
}

function serviceShutDown() {
    console.error(new Date() + "TUP Service is shutdown,please restart it");
}


/**
 * TupCall的连接断开了
 */
function onTupCallClose() {
    console.error(new Date() + "onTupCallClose");
}


function onTupCallReady() {
    console.info(new Date() + "onTupCallReady");
    global_isInitTupCall = true;
    if (global_isInitTupCall && global_isInitTupLogin)
    {
    	initTupCall();
    }
}



function initTup() {
	global_cloudIPCC_SDK = new CloudIPCC_SDK({
		onTupDeamonReady: onTupDeamonReady,
		onTupDeamonClose: onTupDeamonClose,
		serviceStartUp: serviceStartUp,
		serviceShutDown: serviceShutDown,
		onTupLoginReady: onTupLoginReady,
		onTupLoginClose: onTupLoginClose,
		onTupCallReady: onTupCallReady,
		onTupCallClose: onTupCallClose,
		onVersionInfoNotify : onVersionInfoNotify
	});
}

function onVersionInfoNotify(data)
{
	console.info("version is");
	console.info(data);
}

function onTupLoginReady() {
    console.info(new Date() + "onTupLoginReady");
    global_isInitTupLogin = true;
    if (global_isInitTupCall && global_isInitTupLogin)
    {
    	initTupCall();
    }
}


function onTupLoginClose() {
    console.info(new Date() + "onTupLoginClose");
}

function initTupCall() {
	if (null == global_cloudIPCC_SDK.tupCall)
	{
		$('#login_phone_password_input').val('');
		tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
    			$.i18n.prop('common.msg.box.phone.startfialed'), 
				$.i18n.prop('common.msg.box.btn.ok'));
    	tac.modules.msg.showShortMsg({
			txt : $.i18n.prop('common.msg.box.phone.startfialedAndOperate'),
			type : 2,
			level : 2
		});
		return;
	}
    global_cloudIPCC_SDK.tupCall.setBasicCallEvent({
        onCallIncoming: onCallIncoming
    });
    startLog();
    getLocalIp();
}



function startLog() {
    global_cloudIPCC_SDK.tupCall.logStart(
        3, 10240, 5, "./tuplog", { response: logStartResponse });
}

function logStartResponse(data) {
    if (data.result == 0) {
        console.info("logStart Success");
    } else {
        console.error("logStart Failed");
        console.error(data);
    }
}

/**
 * 获取本地IP
 */
function getLocalIp() {
    global_cloudIPCC_SDK.tupLogin.setGetBestLocalIp(TUPConfig.sipIp, {
        response: getLocalIpResponse
    });
}

function getLocalIpResponse(data) {
    console.info(data);
    if (data.result == 0) {
        console.info("GetLocalIp success");
        
    } else {
        console.error("GetLocalIp failed");
    }
    sipBasicCfg(data.local_ip);
}

function sipBasicCfg(local_ip) {
	var localIP = local_ip;
	if (localIP == undefined)
	{
		local_ip = '127.0.0.1';
	}
    global_cloudIPCC_SDK.tupCall.config({
        sip: {
            user_type: 0,      
            tls_anonymous_enable: 1,   
            trans_mode: 0   
        },
        call: {
            call_ipcall_enable: 0       
        },
        account: {
            account_pwd_type: 0
        },
        network: {
            sip_svr_addr: TUPConfig.domain,
            sip_svr_port: TUPConfig.sipPort,
            local_ipv4: localIP,
            local_sip_port: TUPConfig.localPort,
            user_agent: TUPConfig.user_agent,
			proxy_addr: TUPConfig.sipIp,
            proxy_port: TUPConfig.sipPort

        },
        media: {
            srtp_mode: 1
        },
        audio: {
            audio_codec: "112,98,18,9,8,0" ,         
            dtmf_mode: 0 
        }
    }, { response: configResponse });
}



/**
 * 配置结果
 * @param {*} data 
 */
function configResponse(data) {
    if (data.result == 0) {
        console.info("Config Success");
    } else {
        console.error("Config Failed");
        console.error(data);
    }
}


/**
 * 注册
 */
function register(phoneNumber) {
	if ($('.welcome-page').length == 0)
    {
		return;
    }
	
	if (global_cloudIPCC_SDK && global_cloudIPCC_SDK.tupCall)
	{
		//var phoneNumber = $.trim($("#login_phone_num_input").val());
	    var password = $("#login_phone_password_input").val();
	    $('.welcome-page').remove();
	    var global_userPhoneNumber = phoneNumber + "@" + TUPConfig.domain;
	    global_cloudIPCC_SDK.tupCall.register(global_userPhoneNumber, global_userPhoneNumber, password, {
	        onRegStatusUpdate: onRegStatusUpdate,
	        onForceUnReg: onForceUnRegInfo,
	        response: registerResponse
	    });
	}
	else
	{
		$('.welcome-page').remove();
		$('#login_phone_password_input').val('');
		tac.modules.msg.showShortMsg({
			txt : $.i18n.prop('common.msg.box.phone.startfialed'),
			type : 2,
			level : 2
		});
	
		tac.promptEx({
	    	title :  $.i18n.prop('common.msg.box.title.notice'),
	    	content : $.i18n.prop('common.msg.box.phone.startfialedAndOperate'), 
	    	width : 330,
			height : 200,
			submitText : $.i18n.prop('common.msg.box.btn.ok')
	   });
	}
}

function onForceUnRegInfo(data) {
	if (tac.isLogin)
	{
		//如果座席还没有签出，而话机已经签出了，则提示
		tac.modules.msg.showShortMsg({
			txt : $.i18n.prop('common.msg.box.phone.foreceUnRegister'),
			type : 2,
			level : 2
		});
		tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
				$.i18n.prop('common.msg.box.phone.foreceUnRegister'), 
				$.i18n.prop('common.msg.box.btn.ok'));
	}
}
/**
 * 注册结果
 * @param {*} data 
 */
function registerResponse(data) {
    if (data.result == 0) {
        console.info("Register Operation Success");

    } else {
        console.error("Register Operation Failed");
        console.error(data);
        tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
    			$.i18n.prop('common.msg.box.phone.register.callfailed'), 
				$.i18n.prop('common.msg.box.btn.ok'));
    }

}

/**
 * 注销
 */
function deRegister() {
	if (this.global_cloudIPCC_SDK && this.global_cloudIPCC_SDK.tupCall)
	{
		this.global_cloudIPCC_SDK.tupCall.deRegister(global_userPhoneNumber, {
	        response: deRegisterResponse
	    });
	}
    
}

/**
 * 注销结果
 * @param {*} data 
 */
function deRegisterResponse(data) {
    if (data.result == 0) {
        console.info("DeRegister Success");
        userPhoneNumber = "";
        tupCurrentCallId = "";
    } else {
        console.error("DeRegister Failed");
        console.error(data);
    }

}


/**
 * 注册结果上报
 * @param {*} data 
 */
function onRegStatusUpdate(data) {
	console.log("onRegStatusUpdate");
    console.log(data);
    var reason = data.param.reason_code;
    if (reason != 0) {
    	 if (reason == 408) {
    		tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
        			$.i18n.prop('common.msg.box.phone.status.408'), 
    				$.i18n.prop('common.msg.box.btn.ok'));
        	tac.modules.msg.showShortMsg({
    			txt : $.i18n.prop('common.msg.box.phone.status.408'),
    			type : 2,
    			level : 2
    		});
    	} else {
    		tac.prompt($.i18n.prop('common.msg.box.title.notice'), 
        			($.i18n.prop('common.msg.box.phone.registerfailed') + reason + "。"), 
    				$.i18n.prop('common.msg.box.btn.ok'));
        	tac.modules.msg.showShortMsg({
    			txt : ($.i18n.prop('common.msg.box.phone.registerfailed') + reason + "。"),
    			type : 2,
    			level : 2
    		});
    	}
    	
    	return;
    }
}

/**
 * 呼入事件
 * @param {*} data 
 */
function onCallIncoming(data) {
    var tupCurrentCallId = data.param.call_id;
    var tupCurrentCallType = data.param.call_type;
    global_cloudIPCC_SDK.tupCall.acceptCall(tupCurrentCallId, tupCurrentCallType, {response: onAcceptCallReponse });
}

function onAcceptCallReponse(data) {
    if (data.result == 0) {
        console.info("AcceptCall success. ");
    } else {
        console.error("AcceptCall failed. The ErrorCode is " + data.result);
        console.error(data);
    }
}


/**
 * 呼出事件
 * @param {*} data 
 */
function onCallOutGoing(data) {
    console.info(data);
}

/**
 * 本方发起呼叫后，本端收到回铃事件
 * @param {*} data 
 */
function onCallRingBack(data) {
    console.info(data);

}

/**
 * 呼叫建立事件
 * @param {*} data 
 */
function onCallConnected(data) {
    console.info(data);
}

/**
 * 呼叫结束事件
 * @param {*} data 
 */
function onCallEnded(data) {
    console.info(data);
}

/**
 * 呼叫失败事件
 * @param {*} data 
 */
function onCallEndedFailed(data) {
    console.info(data);
}

/**
 * RTP通道建立事件
 * @param {*} data 
 */
function onCallRtpCreated(data) {
    console.info(data);
}




