define(['tac' ,'tacCfg',
        'text!../../../html/modules/report/agentCallOutBrief.html'], 
        function(tac ,tacCfg, html) {
	var module = null;
	var _actionWaitFlag = false, _actionWaitTime = 500;
	var _startTime = "";
	var _endTime = "";
	var _rptType = "";
	var _agentIdsAll = null;
	
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
			}
		}
		return module;
	};
	/*
	 * 事件列表
	 */
	var eventList = [{
				target : 'agentCallOutBriefReport_searchBtn',//查询
				attach : 'on',
				type : 'click',
				action : function(e) {
					_actionWaitFlag = true;
					window.setTimeout(function(){
						_actionWaitFlag = false;
					}, _actionWaitTime);
					
					_startTime = $("#agentCallOutBriefReport_startTime").val()+"";
    				_endTime = $("#agentCallOutBriefReport_endTime").val()+"";
    				_rptType = $('#agentCallOutBriefReport_type').val();
    				_agentIds = $('#agentCallOutBriefReport_agentIds').val();
    				
    				//检查座席工号是否为空
    				if((!_agentIds) || _agentIds.length == 0)
    				{
    					tac.tips({type:'failed',content:$.i18n.prop('report.msg.inputAgentIds'),timeout:3000});
                        return;
    				}
    				
    				//检查座席工号数目是否超限
    				if(_agentIds.length > 50)
    				{
    					//检查座席工号数目是否全选
    					if(!_isAllAgentsSelected())
    					{
    						tac.tips({type:'failed',content:$.i18n.prop('record.msg.agentsOutOfBounds'),timeout:3000});
    						return;
    					}
    				}
    				
    				//检查时间段
    				if(_startTime.length == 0 || _endTime.length == 0 )
					{
    					tac.tips({type:'failed', content:$.i18n.prop('record.msg.inputTime'), timeout:3000});
    					return;
					}
    				
    				//校验起止时间是否有效    				
    				if(tac.util.compareForInterval(_startTime,_endTime))
    				{
    					_queryReport();
    				}
    				else
        			{
    					tac.tips({type:'failed', content:$.i18n.prop('report.msg.box.startEndDay.outOfLimit'), timeout:3000});
    				}

				}
			},{
    			target : 'agentCallOutBriefReport_resetBtn',//重置
    			attach : 'on',
    			type : 'click',
    			action : function(e) {
    				$('#agentCallOutBriefReport_startTime').val('');
    				$('#agentCallOutBriefReport_endTime').val('');
    				$('#agentCallOutBriefReport_type').val(2);
    				_resetDisplayInfo();
    			}
    		},{
				target : 'agentCallOutBriefReport_exportBtn',// export
				attach : 'on',
				type : 'click',
				action : function(e) {
					if (_checkParam())
					{
						var selectedAgents = _getAgentSelected();
						$("#agentCallOutBriefReport_agentId").val(selectedAgents);
						var myForm = document.getElementById('agentOutBriefReport_form');
						myForm.submit();
					};
				}
			},{
    			target : 'agentCallOutBriefReport_startTime',//开始时间
    			attach : 'on',
    			type : 'focus',
    			action : function(e) {
    				var lang = 'zh-cn';
    				WdatePicker({
    					el:this, isShowClear:true, 
    					readOnly:true, 
    					dateFmt:'yyyy-MM-dd',
    					minDate:'%y-%M-{%d-90}',      //开始时间最小日期为当前时间前90天
						maxDate:'#F{$dp.$D(\'agentCallOutBriefReport_endTime\',{d:-1})||\'%y-%M-{%d-1}\'}',  //开始时间最大日期为结束时间前一天
						lang:lang});
    			}
    		},{
    			target : 'agentCallOutBriefReport_endTime',//结束时间
    			attach : 'on',
    			type : 'focus',
    			action : function(e) {
    				var lang = 'zh-cn';
    				WdatePicker({
    					el:this, 
    					isShowClear:true,
    					readOnly:true, 
    					dateFmt:'yyyy-MM-dd', 
    					minDate:'#F{$dp.$D(\'agentCallOutBriefReport_startTime\',{d:1})||\'%y-%M-{%d-90}\'}',  //结束时间最小日期为开始时间后一天
						maxDate:'%y-%M-%d',   //结束时间最大日期为当天
						lang:lang
    				});
    			}
    		}
	];
	
	//重置技能队列和报表显示信息	
	function _resetDisplayInfo() {
		//重置座席工号
		$('#agentCallOutBriefReport_agentIds').empty();
		$('#agentCallOutBriefReport_agentIds').multiselect("destroy");
		_queryAgentIds();
		//重置报表信息
		$('#agentCallOutBriefReport_content').empty();
		$('#agentCallOutBriefReport_content').append(
				'<tr><td colspan="25" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '
						+ $.i18n.prop('record.common.noRecords') + '</td></tr>');
	}
	
	function _checkParam()
	{

		_startTime = $("#agentCallOutBriefReport_startTime").val()+"";
		_endTime = $("#agentCallOutBriefReport_endTime").val()+"";
		_rptType = $('#agentCallOutBriefReport_type').val();
		_agents = $('#agentCallOutBriefReport_agentIds').val();
		
		//检查坐席参数是否为空
		if((_agents == null) || (_agents.length == 0)){
			tac.tips({type:'failed',content:$.i18n.prop('record.msg.inputAgents'),timeout:3000});
			return false;
		}
		
		//检查选择的坐席数是否超限
		if ( (_agents.length >50) ) {
			var all = $("#agentCallOutBriefReport_agentIds option").length;
			//超限情况下仅支持全选
			if (!(all == _agents.length) ) {
				tac.tips({type:'failed',content:$.i18n.prop('record.msg.agentsOutOfBounds'),timeout:3000});
				return false;;
			}					
		}
		
		//校验时间是否有效
		if(_startTime.length == 0 || _endTime.length == 0 ){
			tac.tips({type:'failed',content:$.i18n.prop('record.msg.inputTime'),timeout:3000});
			return false;
		}
		
		//校验起止时间是否有效    				
		if(!tac.util.compareForInterval(_startTime,_endTime)){
			tac.tips({type:'failed', content:$.i18n.prop('report.msg.box.startEndDay.outOfLimit'), timeout:3000});
			return false;
		}
		
		return true;
	}
	
	//查询座席所在vdn上的所有座席信息
	var _queryAgentIds = function() {
		tac.ajax({
			url : tacCfg.url.agentRestServer
					+ tacCfg.service.agentgroup.allAgent,
			type : 'GET',
			// data : JSON.stringify(requestParam),
			contentType : 'application/json',
			loading : false,
			success : function(__data) {
				if (__data.retcode == RestRetCode.SUCCESS) {
					var result = __data.result;
					if (result && result.length > 0){
						_agentIdsAll = result.length;
						$('#agentCallOutBriefReport_agentIds').empty();
						var buffer = [];
						for (var i = 0; i < result.length; i++) {									
							buffer.push('<option value = ');
							buffer.push(result[i]['workno']);
							buffer.push('>');
							buffer.push(result[i]['name']);
							buffer.push('</option>');
						}
						$('#agentCallOutBriefReport_agentIds').append(buffer.join(''));	
						
					}
				}
				//设置座席工号多选
				$("#agentCallOutBriefReport_agentIds").multiselect({
					includeSelectAllOption: true,
					maxHeight: 200, 
					buttonWidth: '168px',
					delimiterText: '; ',
					numberDisplayed: 0,
					selectAllText: $.i18n.prop('report.input.multiselect.selectAll'),
					allSelectedText : $.i18n.prop('report.input.multiselect.selectAll'),
					selectAllValue: 'select-all-value',
					nonSelectedText : $.i18n.prop('report.input.multiselect.selectAgents')
				});
			}
		});
	}
	

	//判断坐席是否全选
	var _isAllAgentsSelected = function() {
		var all = $("#agentCallOutBriefReport_agentIds option").length;
		var _agentIds = $("#agentCallOutBriefReport_agentIds").val();
		if (all == _agentIds.length) { 
			return true;
		}else {
			return false;
		}
	}
	
	// 获取选中的坐席ID
	var _getAgentSelected = function() {
		if (_isAllAgentsSelected()) {
			return null;
		}else {
			var selectedAgents = $('#agentCallOutBriefReport_agentIds').val();
			var buffer = selectedAgents.join(',');
			return buffer;
		}
	}
	
	
	var _queryReport = function(){
		_agentIds = _getAgentSelected();
		var requestParam = {
			'startTime' : $('#agentCallOutBriefReport_startTime').val(),
			'endTime' : $('#agentCallOutBriefReport_endTime').val(),
			'reportType' : $('#agentCallOutBriefReport_type').val(),
			'languageType' : 0,
			'agentIds' : _agentIds,
		}
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.report.agentCallOutBrief,
			type : 'POST',
			data : JSON.stringify(requestParam),
			contentType : 'application/json',
			loading : true,
			success : function(__data){
				if (__data.retcode == RestRetCode.SUCCESS){
					var result = __data.result;
					if (result && result.length > 0){
						var buffer;
						$('#agentCallOutBriefReport_content').empty();
						for (var i = 0; i < result.length; i++) {
							buffer = [];
							buffer.push('<tr>');
							buffer.push('<td>');
							buffer.push(result[i]['agentId']);
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['agentName']);
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['timeSegment']);
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['outBoundCalls']);
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['outBoundAnswered']);
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['outBoundAbandoned']);
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['outBoundAnswerRate']);
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['outBoundTalkTime']);
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['avgOutBoundTalkTime']);
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['maxOutBoundTalkTime']);
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['minOutBoundTalkTime']);
							buffer.push('</td>');
							buffer.push('</tr>');
							$('#agentCallOutBriefReport_content').append(buffer.join(''));
						}
					}else{
						$('#agentCallOutBriefReport_content').empty();
						$('#agentCallOutBriefReport_content').append('<tr><td colspan="11" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '+$.i18n.prop('record.common.noRecords')+'</td></tr>');
						return;
					}
				}else{
					tac.tips({type:'failed',content: __data.retcode, timeout:3000});
				}
			}
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

	/*国际化*/
	var _i18nInit = function(){
		if(el){
			el.find(".i18n").each(function(){
				if($(this).attr("i18nKey")){
					$(this).html($.i18n.prop($(this).attr("i18nKey")));
				}
			});
		};
	};
	
	/*
	 * 渲染
	 */
	var render = function(){
		if(el){
			el.empty().html($.tmpl(html));
			_initEvent();
			_bindingEnterEvent();
			$("#agentCallOutBriefReport_content").empty();
			$("#agentCallOutBriefReport_content").append('<tr><td colspan="13" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '+$.i18n.prop('record.common.noRecords')+'</td></tr>');
			_i18nInit();
			_queryAgentIds();
			var myForm = document.getElementById('agentOutBriefReport_form');
			var buffer = [];
			buffer.push(myForm.action);
			buffer.push("?agentId=");
			buffer.push(tac.data.get("AGENT_ID"));
			//替换action的url里面加入agentId
			myForm.action= buffer.join('');
		}
		return module;
	};
	
	

	
	/**
	 * 绑定回车查询
	 */
	var _bindingEnterEvent = function(){
		$("#agentCallOutBriefReport_type").keyup(function(event){
			if(event.keyCode===13)$("#agentCallOutBriefReport_searchBtn").click();
		});
		
		$("#agentCallOutBriefReport_startTime").keyup(function(event){
			if(event.keyCode===13)$("#agentCallOutBriefReport_searchBtn").click();
		});
		
		$("#agentCallOutBriefReport_endTime").keyup(function(event){
			if(event.keyCode===13)$("#agentCallOutBriefReport_searchBtn").click();
		});
		
		
	}
	
	
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
	return module = {
		init : init,
		render : render,
		show : show,
		hide : hide,
		destory : destory
	};
});
