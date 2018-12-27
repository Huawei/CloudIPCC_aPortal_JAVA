define(function() {
	var config = {
		name : 'icare-cic',
		env : '${package.env}',
		debugbox : false,
		debugconsole : true,
		loginByAccount : true,
		cookieKey : {
			agentId : 'TAC_AGENT_ID',
			tacLanguageValue : 'zh-CN',
			autoAnswer : "autoAnswer",
			maxWorkTime : "maxWorkTime",
			systemCallerNo : 'systemCallerNo',
			systemCallerNoDesc : 'systemCallerNoDesc'
		},
		url : {
			agentRestServer : '/aPortal'
		},
		service : {
			event : {
				event : '/resource/agentevent/event'
			},
			sign : {
				login : '/resource/onlineagent/login',
				forceLogin : '/resource/onlineagent/forcelogin',
				loginByAccount : '/resource/onlineagent/loginbyaccount',
				logout : '/resource/onlineagent/logout',
				verifyCode : '/resource/onlineagent/verifycode'
			},
			status : {
				sayBusy : '/resource/onlineagent/saybusy',
				sayFree : '/resource/onlineagent/sayfree'
			},
			phone : {
				answer : '/resource/voicecall/answer',
				hangup : '/resource/voicecall/release',
				keep : '/resource/voicecall/hold',
				resume : '/resource/voicecall/gethold',
				transfer : '/resource/voicecall/transfer',
				callOut : '/resource/voicecall/callout',
				callInner : '/resource/voicecall/callinner',
				secondDial : '/resource/voicecall/seconddial',
				beginMute : '/resource/voicecall/beginmute',
				endMute : '/resource/voicecall/endmute',
				cancelTransfer : '/resource/voicecall/canceltransfer',
				innerHelp : '/resource/voicecall/innerhelp',
				dropCall : '/resource/voicecall/dropcall',
				disconnect : '/resource/voicecall/disconnect',
				getTrialCalled : '/resource/voicecall/trialcalled'
			},
			record : {
				getRecordInfo : '/resource/record/recordinfo',
				recordfilepath : '/resource/record/recordfilepath',
				recordfile : '/resource/record/recordfile'
			},
			agentgroup : {
				agentIdleList : '/resource/agentgroup/allidleagent',
				allAgent : '/resource/agentgroup/allagent',
				allAgentGroups : '/resource/agentgroup/allagentgroups'
				
			},
			queuedevice : {
				voiceskills : '/resource/queuedevice/voiceskills',
				voiceSkillStatistic : '/resource/queuedevice/voiceskillstatistic'
			},
			system : {
				autoAnswer : '/resource/onlineagent/autoanswer'
			},
			agentconfigure : {
				getAgentConfigure : '/resource/agentconfigure/configure',
				setAgentConfigure : '/resource/agentconfigure/configure'
			},
			report : {
				vdnTraffic : '/resource/report/vdntraffic',
				skillTraffic : '/resource/report/skilltraffic',
				agentCallOutBrief : '/resource/report/agentcalloutbrief',
				agentTraffic : '/resource/report/agenttraffic',
				agentWork : '/resource/report/agentwork',
				skillTrafficfilepath : '/resource/report/vdntrafficfilepath',
				skilltrafficbyskillfilepath : '/resource/report/skilltrafficbyskillfilepath',
			},
			callinfo : {
				callers : '/resource/callinfo/callers',
			}
		},
		time : {
			listAutoRefresh : 5000
		}

	};

	return config;
});
