define(
		[ 'tac', 'tacCfg',
				'text!../../../html/modules/report/agentWorkReport.html' ],
		function(tac, tacCfg, html) {
			var module = null;
			var _actionWaitFlag = false, _actionWaitTime = 500;
			/*
			 * 定义变量
			 */
			var el = null;
			/*
			 * 初始化
			 */
			var init = function(__cfg) {
				if (__cfg) {
					if (__cfg.el) {
						el = __cfg.el;
					}
				}
				return module;
			};
			/*
			 * 事件列表
			 */
			var eventList = [ {
				target : 'agentWorkReport_searchBtn',// 查询
				attach : 'on',
				type : 'click',
				action : function(e) {
					_actionWaitFlag = true;
					window.setTimeout(function() {
						_actionWaitFlag = false;
					}, _actionWaitTime);					
					_checkInputParams();
				}
			}, {
				target : 'agentWorkReport_resetBtn',// 重置
				attach : 'on',
				type : 'click',
				action : function(e) {
					$("#agentWorkReport_startTime").val("");
					$("#agentWorkReport_endTime").val("");
					_resetDisplayInfo();
				}
			}, {
				target : 'agentWorkReport_exportBtn',// export
				attach : 'on',
				type : 'click',
				action : function(e) {
					if (_checkParam())
					{
						var selectedAgents = _getAgentSelected();
						$("#agentWorkReport_agentId").val(selectedAgents);
						var myForm = document.getElementById('agentWorkReport_form');
						myForm.submit();
					};
				}
			}, 
			{
				target : 'agentWorkReport_startTime',// 开始时间
				attach : 'on',
				type : 'focus',
				action : function(e) {
					var lang = 'zh-cn';
					WdatePicker({
						el : this,
						isShowClear : true,
						readOnly : true,
						dateFmt : 'yyyy-MM-dd',
						minDate:'%y-%M-{%d-90}',      //开始时间最小日期为当前时间前90天
						maxDate:'#F{$dp.$D(\'agentWorkReport_endTime\',{d:-1})||\'%y-%M-{%d-1}\'}',  //开始时间最大日期为结束时间前一天
						llang : lang
					});
				}
			}, {
				target : 'agentWorkReport_endTime',// 结束时间
				attach : 'on',
				type : 'focus',
				action : function(e) {
					var lang = 'zh-cn';
					WdatePicker({
						el : this,
						isShowClear : true,
						readOnly : true,
						dateFmt : 'yyyy-MM-dd',
						minDate:'#F{$dp.$D(\'agentWorkReport_startTime\',{d:1})||\'%y-%M-{%d-90}\'}',  //结束时间最小日期为开始时间后一天
						maxDate:'%y-%M-%d',   //结束时间最大日期为当天
						lang : lang
					});
				}
			} ];
			
			//重置技能队列和报表显示信息	
			function _resetDisplayInfo() {
				//重置技能队列				
				$('#agentWork_agents').empty();
				$('#agentWork_agents').multiselect("destroy");
				_queryAllAgent();
				//重置报表信息
				initEmptyReport();
			}
			
			//重置报表显示信息
			function initEmptyReport() {
				$('#agentWorkReport_content').empty();
				$('#agentWorkReport_content').append(
						'<tr><td colspan="27" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '
								+ $.i18n.prop('record.common.noRecords') + '</td></tr>');
			}

			//查询所有的坐席信息
			function _queryAllAgent() {
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
								//填充坐席工号多选框数据
								var buffer = [];
								for (var i = 0; i < result.length; i++) {									
									buffer.push('<option value = ');
									buffer.push(result[i]['workno']);
									buffer.push('>');
									buffer.push(result[i]['name']);
									buffer.push('</option>');
								}
								$('#agentWork_agents').append(buffer.join(''));	
								
							}
						}
						//设置坐席多选
						$("#agentWork_agents").multiselect({
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
				var all = $("#agentWork_agents option").length;
				var _agents = $("#agentWork_agents").val();
				if (all == _agents.length) { 
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
					var selectedAgents = $('#agentWork_agents').val();
					var buffer = selectedAgents.join(',');
					return buffer;
				}
			}
			
			function _checkParam()
			{

				_startTime = $("#agentWorkReport_startTime").val()+"";
				_endTime = $("#agentWorkReport_endTime").val()+"";
				_rptType = $('#agentWorkReport_type').val();
				_agents = $('#agentWork_agents').val();
				
				//检查坐席参数是否为空
				if((_agents == null) || (_agents.length == 0)){
					tac.tips({type:'failed',content:$.i18n.prop('record.msg.inputAgents'),timeout:3000});
					return false;
				}
				
				//检查选择的坐席数是否超限
				if ( (_agents.length >50) ) {
					//超限情况下仅支持全选
					if (!_isAllAgentsSelected()) {
						tac.tips({type:'failed',content:$.i18n.prop('record.msg.agentsOutOfBounds'),timeout:3000});
						return;
					}					
				}
				
				if(_startTime.length == 0 || _endTime.length == 0 ){
					tac.tips({type:'failed',content:$.i18n.prop('record.msg.inputTime'),timeout:3000});
					return false;
				}	
				
				//校验起止时间是否有效    				
				if(!tac.util.compareForInterval(_startTime,_endTime))
				{
					tac.tips({type:'failed', content:$.i18n.prop('report.msg.box.startEndDay.outOfLimit'), timeout:3000});
					return false;
				}
				
				return true;
			}
			
			//校验参数,请求数据
			function _checkInputParams() {
				_startTime = $("#agentWorkReport_startTime").val()+"";
				_endTime = $("#agentWorkReport_endTime").val()+"";
				_rptType = $('#agentWorkReport_type').val();
				_agents = $('#agentWork_agents').val();
				
				//检查坐席参数是否为空
				if((_agents == null) || (_agents.length == 0)){
					tac.tips({type:'failed',content:$.i18n.prop('record.msg.inputAgents'),timeout:3000});
					return;
				}
				
				//检查选择的坐席数是否超限
				if ( (_agents.length >50) ) {
					//超限情况下仅支持全选
					if (!_isAllAgentsSelected()) {
						tac.tips({type:'failed',content:$.i18n.prop('record.msg.agentsOutOfBounds'),timeout:3000});
						return;
					}					
				}
				
				//校验时间格式
				if(_startTime.length == 0 || _endTime.length == 0 ){
					tac.tips({type:'failed',content:$.i18n.prop('record.msg.inputTime'),timeout:3000});
					return;
				}	
				
				//校验起止时间是否有效    				
				if(tac.util.compareForInterval(_startTime,_endTime))
				{
					_queryReport();
				}else {
					tac.tips({type:'failed', content:$.i18n.prop('report.msg.box.startEndDay.outOfLimit'), timeout:3000});
				}
				
			}

			var _queryReport = function() {
				var selectedAgents = _getAgentSelected();	
				
				var requestParam = {
					'startTime' : $('#agentWorkReport_startTime').val(),
					'endTime' : $('#agentWorkReport_endTime').val(),
					'reportType' : $('#agentWorkReport_type').val(),
					'agentIds' : selectedAgents,
					'languageType' : 0
				}
				
				tac.ajax({
							url : tacCfg.url.agentRestServer
									+ tacCfg.service.report.agentWork,
							type : 'POST',
							data : JSON.stringify(requestParam),
							contentType : 'application/json',
							loading : true,
							success : function(__data) {
								if (__data.retcode == RestRetCode.SUCCESS) {
									var result = __data.result;
									if (result && result.length > 0) {
										var buffer;
										$('#agentWorkReport_content').empty();
										for (var i = 0; i < result.length; i++) {
											buffer = [];
											buffer.push('<tr>');
											buffer.push('<td>');
											buffer.push(result[i]['agentId']);  //1.工号
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['agentName']);   //2.姓名
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['skillName']);   //3.时间段
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['loginTimes']);   //4.签入次数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['loginDuration']);   //5.签入时长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['callinTalkTimes']);     //6.呼入-通话次数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['callinTalkDuration']);   //7.呼入-通话时长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['calloutTalkTimes']);   //8.呼出-通话次数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['calloutTalkDuration']);     //9.呼出-通话时长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['arrangeTimes']);   //10.整理-次数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['arrangeDuration']);    //11.整理-时长 
											buffer.push('</td>');
											//buffer.push('<td>');
											//buffer.push(result[i]['restTimes']);     //12.休息-次数
											//buffer.push('</td>');
											//buffer.push('<td>');
											//buffer.push(result[i]['restDuration']);    //13.休息-时长
											//buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['holdTimes']);   //14.保持-次数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['holdDuration']);     //15.保持-时长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['busyTimes']);  //16.示忙-次数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['busyDuration']);  //17.示忙-时长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['ringDuration']);  //18.应答时长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['idelTime']);  //19.空闲时长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['workTimeUseRateWithACW']);  //20.工时利用率-含整理时长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['workTimeUseRateWithoutACW']);  //21.工时利用率-不含整理时长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['internalTransferTimes']);  //22.转移数-内部转移
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['transferOutTimes']);  //23.转移数-转出
											buffer.push('</td>');
											//buffer.push('<td>');
											//buffer.push(result[i]['hangUpToIVRTimes']);  //24.转移数-挂起转IVR
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['threePartyCalls']);  //25.三方通话数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['internalCalls']);  //26.内部呼叫数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['internalHelpTimes']);  //27.内部求助数
											buffer.push('</td>');
											buffer.push('</tr>');
											$('#agentWorkReport_content').append(buffer.join(''));
										}
									} else {
										initEmptyReport();
										return;
									}
								} else {
									tac.tips({
										type : 'failed',
										content : __data.retcode,
										timeout : 3000
									});
								}
							}
						});
			}

			/*
			 * 私有方法：初始化事件
			 */
			var _initEvent = function() {
				for (var i = 0; i < eventList.length; i++) {
					$('#' + eventList[i].target)[eventList[i].attach](
							eventList[i].type, eventList[i].action);
				}
			};

			/* 国际化 */
			var _i18nInit = function() {
				if (el) {
					el.find(".i18n").each(function() {
						if ($(this).attr("i18nKey")) {
							$(this).html($.i18n.prop($(this).attr("i18nKey")));
						}
					});
				}
				;
			};

			/*
			 * 渲染
			 */
			var render = function() {
				if (el) {
					el.empty().html($.tmpl(html));
					_initEvent();
					_bindingEnterEvent();
					//初始化报表显示
					initEmptyReport();
					_i18nInit();
					_queryAllAgent();	
					var myForm = document.getElementById('agentWorkReport_form');
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
			var _bindingEnterEvent = function() {
				$("#agentWorkReport_type").keyup(function(event) {
					if (event.keyCode === 13)
						$("#agentWorkReport_type").click();
				});

				$("#agentWorkReport_startTime").keyup(function(event) {
					if (event.keyCode === 13)
						$("#agentWorkReport_type").click();
				});

				$("#agentWorkReport_endTime").keyup(function(event) {
					if (event.keyCode === 13)
						$("#agentWorkReport_type").click();
				});

			}

			/*
			 * 销毁
			 */
			var destory = function() {
				if (el) {
					el.empty();
				}
				return module;
			};
			/*
			 * 显示
			 */
			var show = function() {
				if (el) {
					el.show();
				}
				return module;
			};
			/*
			 * 隐藏
			 */
			var hide = function() {
				if (el) {
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
