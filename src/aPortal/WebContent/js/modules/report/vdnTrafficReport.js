define(['tac' ,'tacCfg',
        'text!../../../html/modules/report/vdnTrafficReport.html'], 
        function(tac ,tacCfg, html) {
	var module = null;
	var _actionWaitFlag = false, _actionWaitTime = 500;
	var _startTime = "";
	var _endTime = "";
	var _rptType = "";
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
				target : 'vdnTrafficReport_searchBtn',//查询
				attach : 'on',
				type : 'click',
				action : function(e) {
					_actionWaitFlag = true;
					window.setTimeout(function(){
						_actionWaitFlag = false;
					}, _actionWaitTime);
					
					_startTime = $("#vdnTrafficReport_startTime").val()+"";
    				_endTime = $("#vdnTrafficReport_endTime").val()+"";
    				_rptType = $('#vdnTrafficReport_type').val()
    				
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
    			target : 'vdnTrafficReport_resetBtn',//重置
    			attach : 'on',
    			type : 'click',
    			action : function(e) {
    				$('#vdnTrafficReport_startTime').val('');
    				$('#vdnTrafficReport_endTime').val('');
    				$('#vdnTrafficReport_type').val(2);
    				_resetDisplayInfo();
    			}
    		},{
    			target : 'vdnTrafficReport_exportBtn',//export
    			attach : 'on',
    			type : 'click',
    			action : function(e) {
    				_actionWaitFlag = true;
					window.setTimeout(function(){
						_actionWaitFlag = false;
					}, _actionWaitTime);
					
					_startTime = $("#vdnTrafficReport_startTime").val()+"";
    				_endTime = $("#vdnTrafficReport_endTime").val()+"";
    				_rptType = $('#vdnTrafficReport_type').val()
    				
    				//var myForm = document.getElementById('vdnTrafficReport_form');
    				var myForm = $('#vdnTrafficReport_form');
    				
    				if(_startTime.length == 0 || _endTime.length == 0 )
					{
    					tac.tips({type:'failed', content:$.i18n.prop('record.msg.inputTime'), timeout:3000});
    					return;
					}
    				
    				//校验起止时间是否有效    				
    				if(tac.util.compareForInterval(_startTime,_endTime))
    				{
    					
    					//myForm.submit();
    					_exportVdnTrafficReportFile();
    				}
    				else
        			{
    					tac.tips({type:'failed', content:$.i18n.prop('report.msg.box.startEndDay.outOfLimit'), timeout:3000});
    				}
    			}
    		},
    		{
    			target : 'vdnTrafficReport_startTime',//开始时间
    			attach : 'on',
    			type : 'focus',
    			action : function(e) {
    				var lang = 'zh-cn';
    				WdatePicker({
    					el:this, 
    					isShowClear:true,
    					readOnly:true, 
    					dateFmt:'yyyy-MM-dd', 
    					minDate:'%y-%M-{%d-90}',    //开始时间最小日期为当前时间前90天
    					maxDate:'#F{$dp.$D(\'vdnTrafficReport_endTime\',{d:-1})||\'%y-%M-{%d-1}\'}',   //开始时间最大日期为结束时间前一天
    					lang:lang
    				});
    			}
    		},{
    			target : 'vdnTrafficReport_endTime',//结束时间
    			attach : 'on',
    			type : 'focus',
    			action : function(e) {
    				var lang = 'zh-cn';
    				WdatePicker({
    					el:this, 
    					isShowClear:true, 
    					readOnly:true, 
    					dateFmt:'yyyy-MM-dd', 
    					minDate:'#F{$dp.$D(\'vdnTrafficReport_startTime\',{d:1})||\'%y-%M-{%d-90}\'}',  //结束时间最小日期为开始时间后一天
						maxDate:'%y-%M-%d',     //结束时间最大日期为当天
    					lang:lang
    				});
    			}
    		}
	];
	
	function _resetDisplayInfo() {
		//重置报表信息
		$('#vdnTrafficReport_content').empty();
		$('#vdnTrafficReport_content').append(
				'<tr><td colspan="25" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '
						+ $.i18n.prop('record.common.noRecords') + '</td></tr>');
	}
	
	var _queryReport = function(){
		var requestParam = {
			'startTime' : $('#vdnTrafficReport_startTime').val(),
			'endTime' : $('#vdnTrafficReport_endTime').val(),
			'reportType' : $('#vdnTrafficReport_type').val(),
			'languageType' : 0
		}
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.report.vdnTraffic,
			type : 'POST',
			data : JSON.stringify(requestParam),
			contentType : 'application/json',
			loading : true,
			success : function(__data){
				if (__data.retcode == RestRetCode.SUCCESS){
					var result = __data.result;
					if (result && result.length > 0){
						var buffer;
						$('#vdnTrafficReport_content').empty();
						for (var i = 0; i < result.length; i++) {
							buffer = [];
							buffer.push('<tr>');
							buffer.push('<td>');
							buffer.push(result[i]['statTime']); //时间段
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['inBoundCalls']);  //总呼入数
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['answerdCalls']);   //总接通数
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['answerRate']);   //总接通数
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['ivrInBoundCalls']);  //IVR呼入
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['ivrAnswerdCalls']);  //IVR接通数
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['ivrAnswerRate']);   //IVR接通率
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['skillInBoundCalls']);   //人工呼入数
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['skillAnswerdCalls']);  //人工接通数
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['skillAnswerRate']);  //人工接通率
							buffer.push('</td>');
//							buffer.push('<td>');
//							buffer.push(result[i]['avgInBoundVdnTime']);
//							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['outBoundCalls']);   //总呼出数
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['outBoundAnsweredCalls']);    //总接通数
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['outBoundIVRAnsweredCalls']);    //IVR接通数
							buffer.push('</td>');
							buffer.push('<td>');
							buffer.push(result[i]['outBoundSkillAnsweredCalls']);    //人工接通数
    						buffer.push('</td>');
//							buffer.push('<td>');
//							buffer.push(result[i]['avgOutBoundVdnTime']);
//							buffer.push('</td>');
							buffer.push('</tr>');
							$('#vdnTrafficReport_content').append(buffer.join(''));
						}
					}else{
						$('#vdnTrafficReport_content').empty();
						$('#vdnTrafficReport_content').append('<tr><td colspan="14" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '+$.i18n.prop('record.common.noRecords')+'</td></tr>');
						return;
					}
				}else{
					tac.tips({type:'failed',content: __data.retcode, timeout:3000});
				}
			}
		});
	}
	

	/**
	 * 导出vdn话务量报表excel文件
	 */
	var _exportVdnTrafficReportFile = function() {
		
		var requestParam = {
			'startTime' : $('#vdnTrafficReport_startTime').val(),
			'endTime' : $('#vdnTrafficReport_endTime').val(),
			'reportType' : $('#vdnTrafficReport_type').val(),
			'languageType' : 0
		}
		tac.ajax({
			url :  tacCfg.url.agentRestServer + tacCfg.service.report.skillTrafficfilepath,
			type : 'POST',
			data : JSON.stringify(requestParam),
			contentType : 'application/json',
			loading : true,
			success : function(__data){
				if (__data.retcode == RestRetCode.SUCCESS) {
					$('#vdnTrafficReport_desfilePath').val(__data.desFilePath);
					$('#vdnTrafficReport_agentId').val(tac.data.get("AGENT_ID"));					
					$('#vdnTrafficReport_form').submit();
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
			$("#vdnTrafficReport_content").empty();
			$("#vdnTrafficReport_content").append('<tr><td colspan="14" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '+$.i18n.prop('record.common.noRecords')+'</td></tr>');
			_i18nInit();

		}
		return module;
	};
	
	

	
	/**
	 * 绑定回车查询
	 */
	var _bindingEnterEvent = function(){
		$("#vdnTrafficReport_type").keyup(function(event){
			if(event.keyCode===13)$("#vdnTrafficReport_searchBtn").click();
		});
		
		$("#vdnTrafficReport_startTime").keyup(function(event){
			if(event.keyCode===13)$("#vdnTrafficReport_searchBtn").click();
		});
		
		$("#vdnTrafficReport_endTime").keyup(function(event){
			if(event.keyCode===13)$("#vdnTrafficReport_searchBtn").click();
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
