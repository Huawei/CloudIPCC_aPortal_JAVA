define(
		[ 'tac', 'tacCfg',
				'text!../../../html/modules/report/agentTrafficReport.html' ],
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
				target : 'agentTrafficReport_searchBtn',// 查询
				attach : 'on',
				type : 'click',
				action : function(e) {
					_actionWaitFlag = true;
					window.setTimeout(function() {
						_actionWaitFlag = false;
					}, _actionWaitTime);					
					_checkInputParams();
					//_queryReport();
				}
			}, {
				target : 'agentTrafficReport_resetBtn',// 重置
				attach : 'on',
				type : 'click',
				action : function(e) {
					$("#agentTrafficReport_startTime").val("");
					$("#agentTrafficReport_endTime").val("");
					_resetDisplayInfo();
				}
			},{
				target : 'agentTrafficReport_exportBtn',// export
				attach : 'on',
				type : 'click',
				action : function(e) {
					if (_checkParam())
					{
						var selectedAgents = _getAgentSelected();
						$("#agentTrafficReport_agentId").val(selectedAgents);
						var myForm = document.getElementById('agentTrafficReport_form');
						myForm.submit();
					};
				}
			}, 
			{
				target : 'agentTrafficReport_startTime',// 开始时间
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
						maxDate:'#F{$dp.$D(\'agentTrafficReport_endTime\',{d:-1})||\'%y-%M-{%d-1}\'}',  //开始时间最大日期为结束时间前一天
						lang : lang
					});
				}
			}, {
				target : 'agentTrafficReport_endTime',// 结束时间
				attach : 'on',
				type : 'focus',
				action : function(e) {
					var lang = 'zh-cn';
					WdatePicker({
						el : this,
						isShowClear : true,
						readOnly : true,
						dateFmt : 'yyyy-MM-dd',
						minDate:'#F{$dp.$D(\'agentTrafficReport_startTime\',{d:1})||\'%y-%M-{%d-90}\'}',  //结束时间最小日期为开始时间后一天
						maxDate:'%y-%M-%d',   //结束时间最大日期为当天
						lang : lang
					});
				}
			} ];
			
			//重置技能队列和报表显示信息	
			function _resetDisplayInfo() {
				//重置技能队列
				$('#agentTraffic_agents').empty();
				$('#agentTraffic_agents').multiselect("destroy");
				_queryAllAgent();
				//重置报表信息
				$('#agentTrafficReport_content').empty();
				$('#agentTrafficReport_content').append(
						'<tr><td colspan="25" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '
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
								$('#agentTraffic_agents').append(buffer.join(''));	
								
							}
						}
						//设置坐席多选
						$("#agentTraffic_agents").multiselect({
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
				var all = $("#agentTraffic_agents option").length;
				var _agents = $("#agentTraffic_agents").val();
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
					var selectedAgents = $('#agentTraffic_agents').val();
					var buffer = selectedAgents.join(',');
					return buffer;
				}
			}
			
			//校验参数,请求数据
			function _checkInputParams() {
				_startTime = $("#agentTrafficReport_startTime").val()+"";
				_endTime = $("#agentTrafficReport_endTime").val()+"";
				_rptType = $('#agentTrafficReport_type').val();
				_agents = $('#agentTraffic_agents').val();
				
				//检查坐席参数是否为空
				if((_agents == null) || (_agents.length == 0)){
					tac.tips({type:'failed',content:$.i18n.prop('record.msg.inputAgents'),timeout:3000});
					return;
				}
				
				//检查选择的坐席数是否超限
				if ( (_agents.length >50) ) {
					var all = $("#agentTraffic_agents option").length;
					//超限情况下仅支持全选
					if (!(all == _agents.length) ) {
						tac.tips({type:'failed',content:$.i18n.prop('record.msg.agentsOutOfBounds'),timeout:3000});
						return;
					}					
				}
				
				if(_startTime.length == 0 || _endTime.length == 0 ){
					tac.tips({type:'failed',content:$.i18n.prop('record.msg.inputTime'),timeout:3000});
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

			function _checkParam()
			{

				_startTime = $("#agentTrafficReport_startTime").val()+"";
				_endTime = $("#agentTrafficReport_endTime").val()+"";
				_rptType = $('#agentTrafficReport_type').val();
				_agents = $('#agentTraffic_agents').val();
				
				//检查坐席参数是否为空
				if((_agents == null) || (_agents.length == 0)){
					tac.tips({type:'failed',content:$.i18n.prop('record.msg.inputAgents'),timeout:3000});
					return false;
				}
				
				//检查选择的坐席数是否超限
				if ( (_agents.length >50) ) {
					var all = $("#agentTraffic_agents option").length;
					//超限情况下仅支持全选
					if (!(all == _agents.length) ) {
						tac.tips({type:'failed',content:$.i18n.prop('record.msg.agentsOutOfBounds'),timeout:3000});
						return false;;
					}					
				}
				
				//校验起止时间是否有效    				
				if(!tac.util.compareForInterval(_startTime,_endTime))
				{
					tac.tips({type:'failed', content:$.i18n.prop('report.msg.box.startEndDay.outOfLimit'), timeout:3000});
					return false;
				}
				return true;
			}
			
			
			var _queryReport = function() {
				var selectedAgents = _getAgentSelected();		
				var requestParam = {
					'startTime' : $('#agentTrafficReport_startTime').val(),
					'endTime' : $('#agentTrafficReport_endTime').val(),
					'reportType' : $('#agentTrafficReport_type').val(),
					'agentIds' : selectedAgents,
					'languageType' : 0
				}
				
				tac.ajax({
							url : tacCfg.url.agentRestServer
									+ tacCfg.service.report.agentTraffic,
							type : 'POST',
							data : JSON.stringify(requestParam),
							contentType : 'application/json',
							loading : true,
							success : function(__data) {
								if (__data.retcode == RestRetCode.SUCCESS) {
									var result = __data.result;
									if (result && result.length > 0) {
										var buffer;
										$('#agentTrafficReport_content').empty();
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
											buffer.push(result[i]['offeredCalls']);   //4.请求数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['answeredCalls']);   //5.通话数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['lostCalls']);     //6.呼损数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['abortInRing']);   //7.应答阶段平台拆线数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['answerRate']);   //8.通话率
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['answerRateInServiceLevel']);     //9.人工服务水平
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['ringOverTime']);   //10.坐席久不应答数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['ringReject']);    //11.坐席挂机数（应答时） 
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['userAbanInSLA']);     //12.用户放弃数(应答时)-服务水平内
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['userAbanOverSLA']);    //13.用户放弃数(应答时)-服务水平外
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['avgRingTime']);   //14.应答均长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['avgTalkTime']);     //15.通话均长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['maxTalkTime']);  //16.最大通话时长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['minTalkTime']);  //17.最小通话时长
											buffer.push('</td>');
											buffer.push('</tr>');
											$('#agentTrafficReport_content').append(buffer.join(''));
										}
									} else {
										$('#agentTrafficReport_content').empty();
										$('#agentTrafficReport_content').append(
														'<tr><td colspan="22" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '
																+ $.i18n.prop('record.common.noRecords')
																+ '</td></tr>');
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
					$("#agentTrafficReport_content").empty();
					$("#agentTrafficReport_content")
							.append(
									'<tr><td colspan="22" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '
											+ $.i18n.prop('record.common.noRecords')
											+ '</td></tr>');
					_i18nInit();
					_queryAllAgent();
					var myForm = document.getElementById('agentTrafficReport_form');
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
				$("#agentTrafficReport_type").keyup(function(event) {
					if (event.keyCode === 13)
						$("#agentTrafficReport_type").click();
				});

				$("#agentTrafficReport_startTime").keyup(function(event) {
					if (event.keyCode === 13)
						$("#agentTrafficReport_type").click();
				});

				$("#agentTrafficReport_endTime").keyup(function(event) {
					if (event.keyCode === 13)
						$("#agentTrafficReport_type").click();
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
