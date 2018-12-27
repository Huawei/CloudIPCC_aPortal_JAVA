define(['tac' , 'tacCfg'], function(tac , tacCfg) {
	var module = null , params = {"sourceCode":"100"};
	
	/**
	 * 接听电话
	 */
	var answer = function(__callback){
			tac.ajax({
				url : tacCfg.url.agentRestServer + tacCfg.service.phone.answer,
				type : 'PUT',
				contentType : 'application/json',
				loading : false,
				success : function(__data){
					typeof __callback === 'function' && __callback(__data);
				}
			});
	};
	
	/**
	 * 挂断电话
	 */
	var hangup = function(__callback){
			tac.ajax({
				url : tacCfg.url.agentRestServer + tacCfg.service.phone.hangup,
				type : 'PUT',
				contentType : 'application/json',
				loading : false,
				success : function(__data){
					typeof __callback === 'function' && __callback(__data);
				}
			});
	};
	
	/**
	 * 保持电话
	 */
	var keep = function(__callback){
			tac.ajax({
				url : tacCfg.url.agentRestServer + tacCfg.service.phone.keep,
				type : 'PUT',
				contentType : 'application/json',
				loading : false,
				success : function(__data){
					typeof __callback === 'function' && __callback(__data);
				}
			});
	};
	
	/**
	 * 恢复电话
	 */
	var resume = function(__callId, __callback){
		var requestParam = {
				"callId" : __callId
			};
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.phone.resume,
			type : 'PUT',
			data : JSON.stringify(requestParam),
			contentType : 'application/json',
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	};

	
	
	/**
	 * 外呼电话
	 */
	var callOut = function(__params, __callback){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.phone.callOut,
			type : 'POST',
			contentType : 'application/json',
			data : JSON.stringify(__params),
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	}
	
	/**
	 * 内部呼叫
	 */
	var callInner = function(__params, __callback)
	{
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.phone.callInner,
			type : 'POST',
			contentType : 'application/json',
			data : JSON.stringify(__params),
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	}
	
	/**
	 * 转移
	 */
	var transfer = function(__params, __callback){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.phone.transfer,
			type : 'PUT',
			contentType : 'application/json',
			data : JSON.stringify(__params),
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	}
	

	/**
	 * 获取所有技能
	 */
	var getSkillList = function(__callback){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.queuedevice.voiceSkillStatistic,
			type : 'GET',
			contentType : 'application/json',
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		})
	}
	
	

	/**
	 * 获取所有空闲座席
	 */
	var getIdleAgentList = function(__callback){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.agentgroup.agentIdleList,
			type : 'GET',
			contentType : 'application/json',
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	}
	
	/**
	 * 二次拨号
	 */
	var secondDial = function(__params, __callback){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.phone.secondDial,
			type : 'PUT',
			contentType : 'application/json',
			loading : false,
			data : JSON.stringify(__params),
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	}
	
	/**
	 * 静音
	 */
	var beginMute = function(__callback){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.phone.beginMute,
			type : 'PUT',
			contentType : 'application/json',
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	}
	
	/**
	 * 取消静音
	 */
	var endMute = function(__callback){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.phone.endMute,
			type : 'PUT',
			contentType : 'application/json',
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	}
	
	/**
	 * 取消转移
	 */
	var cancelTransfer = function(__callback){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.phone.cancelTransfer,
			type : 'PUT',
			contentType : 'application/json',
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	}
	
	/**
	 * 内部求助
	 */
	var innerHelp = function(__params, __callback){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.phone.innerHelp,
			type : 'PUT',
			contentType : 'application/json',
			data : JSON.stringify(__params),
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	}
	
	var dropCall = function(__params, __callback){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.phone.dropCall,
			type : 'PUT',
			contentType : 'application/json',
			data : JSON.stringify(__params),
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	}
	
	var disconnect = function(__params, __callback){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.phone.disconnect,
			type : 'PUT',
			contentType : 'application/json',
			data : JSON.stringify(__params),
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	}
	
	
	return module = {
		answer : answer,
		hangup : hangup,
		keep : keep,
		resume : resume,
		callOut : callOut,
		transfer: transfer,
		getSkillList : getSkillList,
		getIdleAgentList : getIdleAgentList,
		callInner : callInner,
		secondDial : secondDial,
		beginMute : beginMute,
		endMute : endMute,
		cancelTransfer : cancelTransfer,
		innerHelp : innerHelp,
		dropCall : dropCall,
		disconnect : disconnect
	};
});
