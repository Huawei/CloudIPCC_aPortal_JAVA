define(['tac' , 'tacCfg'], function(tac , tacCfg) {
	
	var setAutoAnswer = function(_param, __callback){
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.system.autoAnswer,
			type : 'PUT',
			contentType : 'application/json',
			data : JSON.stringify(_param),
			loading : true,
			success : function(__data){
				typeof __callback === 'function' && __callback(__data);
			}
		});
	};
	return {
		setAutoAnswer : setAutoAnswer
	};
});
