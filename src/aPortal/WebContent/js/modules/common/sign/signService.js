define(['tac', 'tacCfg'], function(tac, tacCfg) {
	var login = function(__params, __callback){
		if(__params){
			tac.ajax({
				url : tacCfg.url.agentRestServer + tacCfg.service.sign.login,
				type : 'PUT',
				contentType : 'application/json;charset=utf-8',
				data : JSON.stringify(__params),
				checkReturnCode : true,
				success : function(__data){
					typeof __callback === 'function' && __callback(__data);
				}
			});
		}
	};
	
	var loginByAccount = function(__params, __callback){
		if(__params){
			tac.ajax({
				url : tacCfg.url.agentRestServer + tacCfg.service.sign.loginByAccount,
				type : 'PUT',
				contentType : 'application/json;charset=utf-8',
				data : JSON.stringify(__params),
				checkReturnCode : true,
				success : function(__data){
					typeof __callback === 'function' && __callback(__data);
				}
			});
		}
	};
	
	var forceLogin = function(__params, __callback){
		if(__params){
			tac.ajax({
				url : tacCfg.url.agentRestServer + tacCfg.service.sign.forceLogin,
				type : 'PUT',
				contentType : 'application/json;charset=utf-8',
				data : JSON.stringify(__params),
				checkReturnCode : true,
				success : function(__data){
					typeof __callback === 'function' && __callback(__data);
				}
			});
		}
	};
	
	var logout = function(){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.sign.logout,
			type : 'DELETE',
			contentType : 'application/json;charset=utf-8',
			checkReturnCode : true,
			success : function(__data){
				window.location = "home.html";
			}
		});
	}
	
	var verifyCode = function(agentId, __callback){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.sign.verifyCode + "?agentId=" + agentId,
			type : 'GET',
			contentType : 'application/json;charset=utf-8',
			checkReturnCode : true,
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	}
	
	/*
	 * 暴露接口
	 */
	return {
		login : login,
		forceLogin : forceLogin,
		loginByAccount : loginByAccount,
		logout : logout,
		verifyCode : verifyCode
	};
});
