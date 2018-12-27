define(['tac', 'tacCfg'], function(tac, tacCfg) {
	var sayBusy = function (__callback, __error){ 
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.status.sayBusy,
			type : 'PUT',
			contentType : 'application/json;charset=utf-8',
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			},
			error : function(__error){
				typeof error === 'function' && error(__error);
			}
		});
		
	}

	var sayFree = function (__callback, __error){ 
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.status.sayFree,
			type : 'PUT',
			contentType : 'application/json;charset=utf-8',
			loading : false,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			},
			error : function(__error){
				typeof error === 'function' && error(__error);
			}
		});
	}
	
	/*
	 * 暴露接口
	 */
	return {
		sayBusy : sayBusy,
		sayFree : sayFree
	};
});
