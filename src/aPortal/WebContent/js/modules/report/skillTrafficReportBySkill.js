define(
		[ 'tac', 'tacCfg',
				'text!../../../html/modules/report/skillTrafficReportBySkill.html' ],
		function(tac, tacCfg, html) {
			var module = null;
			var _skillAll = null;
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
				target : 'skillTrafficReportBySkill_searchBtn',// 查询
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
				target : 'skillTrafficReportBySkill_resetBtn',// 重置
				attach : 'on',
				type : 'click',
				action : function(e) {
					$('#skillTrafficReportBySkill_startTime').val('');
					$('#skillTrafficReportBySkill_endTime').val('');
					$('#skillTrafficReportBySkill_type').val(2);
					_resetDisplayInfo();
				}
			},{
    			target : 'skillTrafficBySkillReport_exportBtn',//export
    			attach : 'on',
    			type : 'click',
    			action : function(e) {
    				_actionWaitFlag = true;
					window.setTimeout(function(){
						_actionWaitFlag = false;
					}, _actionWaitTime);
					
					if (_checkParam())
					{
						var selectedSkills = _getSkillsSelected();
						$("#skillTrafficBySkillReport_skills").val(selectedSkills);
						//var myForm = $('#skillTrafficBySkillReport_form');							
						//myForm.submit();
						_exportSkillTrafficBySkillReportFile();
					};
					
					
    			}
    		},{
				target : 'skillTrafficReportBySkill_startTime',// 开始时间
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
						maxDate:'#F{$dp.$D(\'skillTrafficReportBySkill_endTime\',{d:-1})||\'%y-%M-{%d-1}\'}',  //开始时间最大日期为结束时间前一天
						lang : lang
					});
				}
			}, {
				target : 'skillTrafficReportBySkill_endTime',// 结束时间
				attach : 'on',
				type : 'focus',
				action : function(e) {
					var lang = 'zh-cn';
					WdatePicker({
						el : this,
						isShowClear : true,
						readOnly : true,
						dateFmt : 'yyyy-MM-dd',
						minDate:'#F{$dp.$D(\'skillTrafficReportBySkill_startTime\',{d:1})||\'%y-%M-{%d-90}\'}',  //结束时间最小日期为开始时间后一天
						maxDate:'%y-%M-%d',   //结束时间最大日期为当天
						lang : lang
					});
				}
			} ];

			function _resetDisplayInfo() {
				// 重置技能队列
				$('#skillTrafficReportBySkill_queue').val("");
				$('#skillTrafficReportBySkill_queue').multiselect("refresh");
				_queryVoiceSkills();
				// 重置报表信息
				$('#skillTrafficReportBySkill_content').empty();
				$('#skillTrafficReportBySkill_content').append('<tr><td colspan="20" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '
										+ $.i18n.prop('record.common.noRecords')
										+ '</td></tr>');
			}

			// 请求技能队列
			var _queryVoiceSkills = function() {
				tac.ajax({
						url : tacCfg.url.agentRestServer
								+ tacCfg.service.queuedevice.voiceskills,
						type : 'GET',
						// data : JSON.stringify(requestParam),
						contentType : 'application/json',
						loading : false,
						success : function(__data) {
							if (__data.retcode == RestRetCode.SUCCESS) {
								var result = __data.result;
								if (result && result.length > 0) {
									_skillAll = result.length;
									$('#skillTrafficReportBySkill_queue').empty();
									var buffer = [];
									for (var i = 0; i < result.length; i++) {
										buffer.push('<option value = ');
										buffer.push(result[i]['id']);
										buffer.push('>');
										buffer.push(result[i]['name']);
										buffer.push('</option>');
									}
									$('#skillTrafficReportBySkill_queue').append(buffer.join(''));

								}
							}
							// 设置技能队列多选
							$("#skillTrafficReportBySkill_queue").multiselect({
												includeSelectAllOption : true,
												maxHeight : 200,
												buttonWidth : '168px',
												delimiterText : '; ',
												numberDisplayed : 0,
												selectAllText : $.i18n.prop('report.input.multiselect.selectAll'),
												allSelectedText : $.i18n.prop('report.input.multiselect.selectAll'),
												selectAllValue : 'select-all-value',
												nonSelectedText : $.i18n.prop('report.input.multiselect.noSelect')
											});
						}
						});
			}

			function _checkParam() {
				_startTime = $("#skillTrafficReportBySkill_startTime").val()+"";
				_endTime = $("#skillTrafficReportBySkill_endTime").val()+"";
				_rptType = $('#skillTrafficReportBySkill_type').val();
				_skills = $('#skillTrafficReportBySkill_queue').val();
				
				//检查技能是否为空
				if((!_skills) || _skills.length == 0){
					tac.tips({type:'failed',content:$.i18n.prop('record.msg.inputSkills'),timeout:3000});
					return false;
				}
				//检查技能数目是否超限
				if(_skills.length > 50){
					//超限情况下仅支持全选
					if (!_isAllSkillsSelected()) {
						tac.tips({type:'failed',content:$.i18n.prop('record.msg.skillsOutOfBounds'),timeout:3000});
						return false;
					}				
				}
				//检查时间段
				if(_startTime.length == 0 || _endTime.length == 0 ){
					tac.tips({type:'failed',content:$.i18n.prop('record.msg.inputTime'),timeout:3000});
					return false;
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
				
				return true;
				
			}
			// 校验参数，请求数据
			function _checkInputParams() {
				_startTime = $("#skillTrafficReportBySkill_startTime").val()
						+ "";
				_endTime = $("#skillTrafficReportBySkill_endTime").val() + "";
				_rptType = $('#skillTrafficReportBySkill_type').val();
				_skills = $('#skillTrafficReportBySkill_queue').val();
				// 检查技能是否为空
				if ((!_skills) || _skills.length == 0) {
					tac.tips({
						type : 'failed',
						content : $.i18n.prop('record.msg.inputSkills'),
						timeout : 3000
					});
					return;
				}

				// 检查技能数目是否超限
				if (_skills.length > 50) {
					if (!_isAllSkillsSelected()) {
						tac.tips({
							type : 'failed',
							content : $.i18n
									.prop('record.msg.skillsOutOfBounds'),
							timeout : 3000
						});
						return;
					}
				}
				// 检查时间段
				if (_startTime.length == 0 || _endTime.length == 0) {
					tac.tips({
						type : 'failed',
						content : $.i18n.prop('record.msg.inputTime'),
						timeout : 3000
					});
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

			// 判断技能队列是否全选
			var _isAllSkillsSelected = function() {
				var all = $("#skillTrafficReportBySkill_queue option").length;
				if (all == _skills.length) {
					return true;
				} else {
					return false;
				}
			}

			// 获取选中的技能队列
			var _getSkillsSelected = function() {
				if (_isAllSkillsSelected()) {
					return null;
				} else {
					var selectedAgents = $('#skillTrafficReportBySkill_queue').val();
					var buffer = selectedAgents.join(',');
					return buffer;
				}
			}

			var _queryReport = function() {
				var selectedSkills = _getSkillsSelected();
				var requestParam = {
					'startTime' : $('#skillTrafficReportBySkill_startTime')
							.val(),
					'endTime' : $('#skillTrafficReportBySkill_endTime').val(),
					'reportType' : $('#skillTrafficReportBySkill_type').val(),
					'skills' : selectedSkills,
					'languageType' : 0,
					'skillQueryType' : 1
				}

				tac.ajax({
							url : tacCfg.url.agentRestServer + tacCfg.service.report.skillTraffic,
							type : 'POST',
							data : JSON.stringify(requestParam),
							contentType : 'application/json',
							loading : false,
							success : function(__data) {
								if (__data.retcode == RestRetCode.SUCCESS) {
									var result = __data.result;
									if (result && result.length > 0) {
										var buffer;
										$('#skillTrafficReportBySkill_content').empty();
										for (var i = 0; i < result.length; i++) {
											buffer = [];
											buffer.push('<tr>');
											buffer.push('<td>');
											buffer.push(result[i]['skillName']); // 1.时间段
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['offeredCalls']); // 2.请求人工数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['answeredCalls']); // 3.通话数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['answerRate']); // 4.通话率
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['lostCalls']); // 5.呼损数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['lostRate']); // 6.呼损率
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['userAbanInQueue']); // 7.排队呼损-用户放弃数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['sysAbanCallsInQueue']); // 8.排队呼损-平台呼损数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['userAbanInRing']); // 9.应答呼损-用户放弃数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['ringReject']); // 10.应答呼损-坐席挂机数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['sysAbanCallsInRing']); // 11.应答呼损-平台呼损数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['sysSLARate']); // 12.系统服务水平
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['lostInRing']); // 13.应答无通话数
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['ringAnswerRate']); // 14.应答通话率
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['avgAnsweredQueueTime']); // 15.通话排队均长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['avgLostQueueTime']); // 16.呼损排队均长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['avgAnseredRingingTime']); // 17.通话应答均长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['avgLostRingTime']); // 18.呼损应答均长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['talkTime']); // 19.通话均长
											buffer.push('</td>');
											buffer.push('<td>');
											buffer.push(result[i]['avgWaitTime']); // 20.等待均长
											buffer.push('</td>');
											buffer.push('</tr>');
											$('#skillTrafficReportBySkill_content').append(buffer.join(''));
										}
									} else {
										$('#skillTrafficReportBySkill_content').empty();
										$('#skillTrafficReportBySkill_content').append('<tr><td colspan="20" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '
																+ $.i18n.prop('record.common.noRecords') + '</td></tr>');
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
			
			/**
			 * 导出技能话务量报表（按技能）excel文件
			 */
			var _exportSkillTrafficBySkillReportFile = function() {
				
				var selectedSkills = _getSkillsSelected();
				var requestParam = {
					'startTime' : $('#skillTrafficReportBySkill_startTime')
							.val(),
					'endTime' : $('#skillTrafficReportBySkill_endTime').val(),
					'reportType' : $('#skillTrafficReportBySkill_type').val(),
					'skills' : selectedSkills,
					'languageType' : 0,
					'skillQueryType' : 1
				}
				tac.ajax({
					url :  tacCfg.url.agentRestServer + tacCfg.service.report.skilltrafficbyskillfilepath,
					type : 'POST',
					data : JSON.stringify(requestParam),
					contentType : 'application/json',
					loading : true,
					success : function(__data){
						if (__data.retcode == RestRetCode.SUCCESS) {
							$('#skillTrafficBySkillReport_desfilePath').val(__data.desFilePath);
							$('#skillTrafficBySkillReport_agentId').val(tac.data.get("AGENT_ID"));	
							$('#skillTrafficBySkillReport_form').submit();
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
					$("#skillTrafficReportBySkill_content").empty();
					$("#skillTrafficReportBySkill_content").append(
									'<tr><td colspan="20" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '
											+ $.i18n.prop('record.common.noRecords') + '</td></tr>');
					_i18nInit();
					_queryVoiceSkills();
				}
				return module;
			};

			/**
			 * 绑定回车查询
			 */
			var _bindingEnterEvent = function() {
				$("#skillTrafficReportBySkill_type").keyup(function(event) {
					if (event.keyCode === 13)
						$("#skillTrafficReportBySkill_type").click();
				});

				$("#skillTrafficReportBySkill_startTime").keyup(
						function(event) {
							if (event.keyCode === 13)
								$("#skillTrafficReportBySkill_type").click();
						});

				$("#skillTrafficReportBySkill_endTime").keyup(function(event) {
					if (event.keyCode === 13)
						$("#skillTrafficReportBySkill_type").click();
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
