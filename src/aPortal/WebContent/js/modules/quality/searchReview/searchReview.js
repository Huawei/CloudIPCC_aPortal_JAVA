define(['tac' ,'tacCfg','text!../../../../html/modules/quality/searchReview/searchReview.html'], function(tac ,tacCfg, html) {
	var module = null;
	var getCallData_url = "";
	var myPage =1;
	
	var callfrom = "";
	var callTarget = "";
	var agentId = "";
	var startTime = "";
	var endTime = "";
	var callTimeMin = "";
	var callTimeMax = "";
	var callResult = "";
	var note_masking = $("#note_masking");
	var pop = null;
	var data_size = false;
	var hwaSearch = 'searchreview_search',hwaPlay = 'searchreview_search_voice',hwaDownload = 'searchreview_search_download';
	
	var _actionWaitFlag = false, _actionWaitTime = 500;
	
	var isMasking = false; //定义遮罩
	
	var voice_map = {
		Z : "voice",
		H : "voice1",
		I : "voice2",
		J : "voice3",
		K : "voice4",
		L : "voice5",
		M : "voice6",
		N : "voice7",
		O : "voice8"
	};
	//初始化加载下拉框
	var searchReviewI18N=function(){
		var languageValue = tac.util.cookie.get();
		if(languageValue==null||languageValue=="null"){
            languageValue= window.navigator.language || window.navigator.userLanguage;
        }
		$("#searchReview_callResult").empty();
			if(languageValue=="zh-CN"){
				$("#searchReview_callResult").prepend('<option value="">全部</option>')
				$("#searchReview_callResult").append('<option value="1">正常通话</option>');
				$("#searchReview_callResult").append('<option value="0">未接通</option>');
			}else{
				$("#searchReview_callResult").prepend('<option value="">All</option>')
				$("#searchReview_callResult").append('<option value="1">Normal call</option>');
				$("#searchReview_callResult").append('<option value="0">Not connected</option>');
			}
		}
	
	var defaultResultInit = function()
	{
		var myDate = new Date();
		keyWords = "";
		time = tac.util.getDateTimeAll(new Date(myDate.getTime()));
		
		start = time.date+" 00:00:00";
		end = time.date+" " + time.time;
		
		$("#searchReview_startTime").val(start);
		$("#searchReview_endTime").val(end);
		
		getData(1);
	}
	
	var callTypeValueLanaguage = function (callType){
		switch (parseInt(callType)) {
		case 0:			
			return $.i18n.prop('record.info.calltype.in');
		case 1:					
			return $.i18n.prop('record.info.calltype.out');
		case 3:			
			return $.i18n.prop('record.info.calltype.obsout');	
		default:			
			return "";
		}
	};
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
	var eventList = [
         	{
    			target : 'sr_searchBtn',//查找
    			attach : 'on',
    			type : 'click',
    			action : function(e) {
    				startTime = $("#searchReview_startTime").val()+"";
    				endTime = $("#searchReview_endTime").val()+"";
    				callfrom = $("#callfrom").val()+"";
    				callTarget = $("#callTarget").val()+"";
    				agentId = $("#searchReview_agentId").val()+"";
    				
    				callTimeMin = $("#searchReview_MinTime").val();
    				callTimeMax = $("#searchReview_MaxTime").val();
    				callResult = $("#searchReview_callResult").val();
    				
    				
    				
    				if(!isNaN(callTimeMin))
    					if(!isNaN(callTimeMin))
    						if(parseInt(callTimeMax)<=parseInt(callTimeMin)){
    							tac.tips({type:'failed',content:$.i18n.prop('common.query.maximumduration'),timeout:2000});
    	    					return;
    						}
    							
    				if(!isNaN(callTimeMin))callTimeMin = parseInt(callTimeMin);
    				if(!isNaN(callTimeMax))callTimeMax = parseInt(callTimeMax);
    				
    				if(callfrom.length > 24 )
					{
    					tac.tips({type:'failed',content:$.i18n.prop('record.msg.callerTooLong'),timeout:3000});
    					return;
					}
    				
    				if(callTarget.length > 24 )
					{
    					tac.tips({type:'failed',content:$.i18n.prop('record.msg.calledTooLong'),timeout:3000});
    					return;
					}
    				
    				if(agentId.length > 5 )
					{
    					tac.tips({type:'failed',content:$.i18n.prop('record.msg.agentIdTooLong'),timeout:3000});
    					return;
					}
    				if(startTime.length==0 || endTime.length==0 )
					{
    					tac.tips({type:'failed',content:$.i18n.prop('record.msg.inputTime'),timeout:3000});
    					return;
					}
    				
    				
    				if(tac.util.compare(startTime,endTime)){
	    				getData(1);
    				}else{
    					tac.tips({type:'failed',content:$.i18n.prop('record.msg.startLTend'),timeout:3000});
    				}
    				
    			}
    		},{
    			target : 'startTime',
    			attach : 'on',
    			type : 'click',
    			action : function(e) {
    				   $('body,html').animate({scrollTop:0},400);
    			}
    		},{
    			target : 'endTime',
    			attach : 'on',
    			type : 'click',
    			action : function(e) {
    				   $('body,html').animate({scrollTop:0},400);
    			}
    		},{
    			target : 'searchReview_MinTime',
    			attach : 'on',
    			type : 'input propertychange',
    			action : function(e) {
    				$(this).val($(this).val().replace(/[^\d]/g, ''));
    			}
    		},{
    			target : 'searchReview_MaxTime',
    			attach : 'on',
    			type : 'input propertychange',
    			action : function(e) {
    				$(this).val($(this).val().replace(/[^\d]/g, ''));
    			}
    		},{
    			target : 'callfrom',//主叫
    			attach : 'on',
    			type : 'input propertychange',
    			action : function(e) {
    				$(this).val($(this).val().replace(/[^\d]/g, ''));
    			}
    		},
    		{
    			target : 'searchReview_agentId',//坐席号
    			attach : 'on',
    			type : 'input propertychange',
    			action : function(e) {
    				$(this).val($(this).val().replace(/[^\d]/g, ''));
    			}
    		},
    		{
    			target : 'callTarget',//被叫
    			attach : 'on',
    			type : 'input propertychange',
    			action : function(e) {
    				$(this).val($(this).val().replace(/[^\d*#]/g, ''));
    			}
    		},{
    			target : 'sr_resetBtn',//重置
    			attach : 'on',
    			type : 'click',
    			action : function(e) {
    				callfrom = "";
    				startTime = "";
    				endTime = "";
    				callTarget = "";
    				agentId = "";
    				
    				minTime = "";
    				maxTime = "";
    				callResult = "";
    				
    				if (tac.data.get("IS_CENSOR") == true)
					{
    					$("#callfrom").val("");
    					$("#searchReview_agentId").val("");
    					$("#callTarget").val("");
					}
    				else
    				{
    					$("#callfrom").val("");
    					$("#callTarget").val("");
    				}
    				
    				setTime();
    			}
    		},
    		{
    			target : 'searchReview_startTime',//开始时间
    			attach : 'on',
    			type : 'focus',
    			action : function(e) {
    				var lang = 'zh-cn';
    				var languageValue = tac.util.cookie.get();
    				if(languageValue==null||languageValue=="null"){
    		            languageValue= window.navigator.language || window.navigator.userLanguage;
    		        }
					if(languageValue !="zh-CN"){
						lang = 'en';
					}
    				WdatePicker({
    					el:this,
    					isShowClear:true,
    					readOnly:true,
    					dateFmt:'yyyy-MM-dd HH:mm:ss',
    					minDate:'%y-%M-{%d-90} 00:00:00',    //开始时间最小日期为当前时间前90天的0点
    					//minDate:'%y-%M-%d 10:38:00',    
    					maxDate:'#F{$dp.$D(\'searchReview_endTime\',{s:-1})||\'%y-%M-{%d-90} %H:%s:{%s-1}\'}',   //开始时间最大日期为结束时间前一秒
    					lang:lang
    				});
    			}
    		},
    		{
    			target : 'searchReview_endTime',//结束时间
    			attach : 'on',
    			type : 'focus',
    			action : function(e) {
    				var lang = 'zh-cn';
    				var languageValue = tac.util.cookie.get();
    				if(languageValue==null||languageValue=="null"){
    		            languageValue= window.navigator.language || window.navigator.userLanguage;
    		        }
					if(languageValue !="zh-CN"){
						lang = 'en';
					}
    				WdatePicker({el:this,
    					isShowClear:true,
    					readOnly:true,
    					dateFmt:'yyyy-MM-dd HH:mm:ss',
    					minDate:'#F{$dp.$D(\'searchReview_startTime\',{s:1})||\'%y-%M-{%d-90} 00:00:01\'}',  //结束时间最小日期为开始时间后一天
						maxDate:'%y-%M-%d 23:59:59',     //结束时间最大日期为当天23点59分59秒
    					lang:lang
    				});
    			}
    		}
	];
	
	var validStartTime = function() {
		var target 
		if (condition) {
			
		}
	}
	
	var subTime = function(time){
		if(time){
			var _timeStr = "";
			_timeStr = time.substring(0,19);
			return _timeStr;
		}
	};
	
	
	var conversionTime = function(time){
		if(time){
			time = parseInt(time);
			var h = parseInt(time / 3600 , 10),
			    _second = time % 3600,
			    m = parseInt(_second / 60 , 10),
			    s = _second % 60,
			    timeStr = '';
			if(h){
				if(h>1){
					timeStr += h + $.i18n.prop('record.common.hour')+' ';
				}else{
					timeStr += h + $.i18n.prop('record.common.hour')+' ';
				}
			}
			if(m){
				if(m>1){
					timeStr += m + $.i18n.prop('record.common.minute')+' ';
				}else{
					timeStr += m + $.i18n.prop('record.common.minute')+' ';
				}
				
			}
			if(s >= 0)
			{
				timeStr += s + $.i18n.prop('record.common.second');
			}
			return timeStr;
		}else{
			return time;
		}
	};
	
	/**
	 * 获取数据
	 * state  状态
	 * pageSize 每页大小
	 * currentPage 当前页
	 */
	var getData = function(currentPage){
		var __startTime = new Date().getTime();
		$("#searchReview_content").empty();
		$("#searchReview_pagination").empty();
		
		var callData_url=tacCfg.url.agentRestServer+tacCfg.service.record.getRecordInfo;
		
		//此处为ajax获取数据
		var params = {
				"requestParam": {
					"caller": callfrom,
					"called": callTarget,
					"agentId": agentId,
					"begin":startTime, 
					"end":endTime,
					"callTimeMin":callTimeMin,
					"callTimeMax":callTimeMax
				},
				"page": {
				"curPage": currentPage,
				"pageSize": "10"
				}
		 };
		

		tac.ajax({
			url : callData_url,
			type:"post",
			async : true,
			loading:true,
			contentType : 'application/json;charset=utf-8',
			data : JSON.stringify(params),
			success : function(data){
				
				if (data.returnCode=="000-002") {
					var str = '<tr><td colspan="10" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '+$.i18n.prop('record.common.noRecords')+'</td></tr>';
					$(str).appendTo($("#searchReview_content"));
					data_size = false;
					tac.tips({type:'failed',content: data.returnDesc, timeout:3000});
					return;
				}
				
				if(data.returnCode=="1" || data.returnData==null || !data.returnData.result){
					var str = '<tr><td colspan="10" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '+$.i18n.prop('record.common.noRecords')+'</td></tr>';
					 $(str).appendTo($("#searchReview_content"));
					 data_size = false;
					 return;
				}
								
				var notes = data.returnData.result;
				var size = notes.length;
				if(size==0)
				{
					var str = '<tr><td colspan="10" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '+$.i18n.prop('record.common.noRecords')+'</td></tr>';
					 $(str).appendTo($("#searchReview_content"));
					 data_size = false;
					return;
				}
				else
				{
					var buffer;
					data_size = true;
					for (var i = 0; i < size; i++) 
					{
						buffer = [];
						buffer.push('<tr>');
						buffer.push('<td>');
						buffer.push(i+1);
						buffer.push('</td>');
						buffer.push('<td>');
						buffer.push(notes[i].callerId);
						buffer.push('</td>');
						buffer.push('<td>');
						buffer.push(notes[i].calledId);
						buffer.push('</td>');
						buffer.push('<td>');
						buffer.push(notes[i].agentId||"");
						buffer.push('</td>');
						buffer.push('<td>');
						buffer.push(callTypeValueLanaguage(notes[i].callType||""));
						buffer.push('</td>');
						buffer.push('<td>');
						buffer.push(subTime(notes[i].beginTime));
						buffer.push('</td>');
						buffer.push('<td>');
						buffer.push(subTime(notes[i].endTime));
						buffer.push('</td>');
						buffer.push('<td>');
						buffer.push(conversionTime(notes[i].callTimeCic));
						buffer.push('</td>');
						buffer.push('<td>');	
						
						//试听
						buffer.push('<span>');						
						var voiceUrl = "";
						if(-1 != navigator.userAgent.indexOf("Trident"))
						{
							buffer.push('<button type="button" name="audio_a" tabindex="0" class="fa fa-volume-up bg-transparent border-none text-info padding-r-10px play-btn" id="searchView_btn_listen_');
							buffer.push(i);
							buffer.push('"></button>');
						}	
						else
						{
							buffer.push('<button type="button"  name="audio_a" tabindex="0" class="fa fa-volume-up bg-transparent border-none text-info padding-r-10px play-btn" id="searchView_btn_listen_');
							buffer.push(i);	
							buffer.push('"></button>');
						}
						buffer.push('</span>');
						
						//下载
						buffer.push('<span>');						
						buffer.push('<a  href="javascript:;" ><span class="fa fa-download text-success download-icon" id="searchReview_downloadRecord_');
						buffer.push(i);
						buffer.push('" ></span></a>');	
						buffer.push('</span>');
						buffer.push('</td>');
						buffer.push('</tr>');
						$("#searchReview_content").append(buffer.join(''));	
						
						//绑定下载点击事件
						$('#searchReview_downloadRecord_' + i).click({recordId : notes[i].fileName},function(e) {
							_downloadRecord(e.data.recordId);
						});					
						
						//绑定试听点击事件
						$('#searchView_btn_listen_' + i).click({ recordId : notes[i].fileName},function(e) {
							_actionWaitFlag = true;
							window.setTimeout(function(){
								_actionWaitFlag = false;
							}, _actionWaitTime);
							
							pop = $(this);			
							
							var recordId =  e.data.recordId;
	
							//请求生成临时录音文件的接口
							tac.ajax({
								url : tacCfg.url.agentRestServer + tacCfg.service.record.recordfilepath + '?recordId=' + e.data.recordId,
								type :"post",
								async : true,
								loading : true,
								contentType : 'application/json;charset=utf-8',
								data : {},
								success : function(data){
									if (data.retcode == RestRetCode.SUCCESS) {
					
										var voiceUrl = tacCfg.url.agentRestServer + tacCfg.service.record.recordfile + "?agentId="+tac.data.get("AGENT_ID") + "&desFilePath=" + encodeURI(data.tempFilePath);
										
										//设置弹框页
										pop.attr('data-toggle','popover');
										pop.attr('data-placement','left');
										pop.attr('data-html','popover');
										pop.attr('data-trigger','manual');										
										//设置data-content
										var buffer = [];										
										if(-1 != navigator.userAgent.indexOf("Trident"))
										{
											buffer.push('<div class="fa-2x pointer close" name="popover_close">&times;</div><embed  width="300" height="45" type="audio/x-wav" src="');
											buffer.push(voiceUrl);
											buffer.push('"></embed>');
										}	
										else
										{
											buffer.push('<div class="fa-2x pointer close" name="popover_close">&times;</div><audio controls autoplay width="300" height="45" type="audio/x-wav" src="');
											buffer.push(voiceUrl);
											buffer.push('"></audio>');
										}
										pop.attr('data-content',buffer.join(''));										
										
										//显示遮罩
										note_masking.show();										
										//显示播放popup
										pop.popover('show');											
										//绑定遮罩双击事件
										note_masking.dblclick(function(){
											//pop.click();
											pop.popover('hide');
											note_masking.hide();
											note_masking.unbind();
											//$("#"+pop.attr("aria-describedby")).remove();
											if(-1 != navigator.userAgent.indexOf("Trident")){
												$("embed")[0].stop();
											}
										});					
										//绑定audio控件关闭按钮点击事件
										$("div[name=popover_close]").each(function(){
											$(this).click(function(){
												//pop.click();
												pop.popover('hide');
												note_masking.hide();
												note_masking.unbind();
												if(-1 != navigator.userAgent.indexOf("Trident")){
													$("embed")[0].stop();
												}
											});
										});
									}
								}
							});					
						});	
					}
				}
				
				tac.initPage(data.returnData.pageVO.totalRows,data.returnData.pageVO.totalPages,"searchReview_pagination",currentPage,getData);		
			},
			error : function(data){
				data_size = false;
				tac.prompt($.i18n.prop('common.msg.box.title.notice'),$.i18n.prop('common.msg.box.netexception'),$.i18n.prop('common.msg.box.btn.ok'));
			}
		});			
	};
	
	/**
	 * 生成录音临时文件
	 */
	var _genRecordTempFile = function(recordId) {
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.record.recordfilepath + '?recordId=' + recordId,
			type :"post",
			async : true,
			loading : true,
			contentType : 'application/json;charset=utf-8',
			data : {},
			success : function(data){
				if (data.retcode == RestRetCode.SUCCESS) {
					return data.tempFilePath;
				}
			},
			error : function(){
				return '';
			}
		});	
	}
	
	/**
	 * 下载录音文件
	 */
	var _downloadRecord = function(recordId) {
		//生成录音临时文件
		tac.ajax({
			url : tacCfg.url.agentRestServer + tacCfg.service.record.recordfilepath + '?recordId=' + recordId,
			type :"post",
			async : true,
			loading : true,
			contentType : 'application/json;charset=utf-8',
			data : {},
			success : function(data){
				if (data.retcode == RestRetCode.SUCCESS) {
					//提交form表单，下载文件 
					$('#searchReview_filePath').val(data.tempFilePath); 
					$('#searchReview_agentId').val(tac.data.get("AGENT_ID")); 
					
					$('#searchReview_form').submit();
					
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
			
			$("#searchReview_content").empty();
			$("#searchReview_pagination").empty();
			
			$("#searchReview_content").append('<tr><td colspan="10" class="text-center"><span class="fa fa-exclamation-circle fa-lg text-info"></span> '+$.i18n.prop('record.common.noRecords')+'</td></tr>');
			if (tac.data.get("IS_CENSOR") == false)
			{
				agentId = tac.data.get("AGENT_ID");
				$("#searchReview_agentId").val(tac.data.get("AGENT_ID"));			
				$("#searchReview_agentId").attr("readonly","readonly");
			}
			setTime();
			getData(1);
			searchReviewI18N();
			_i18nInit();
		}
		return module;
	};
	
	
	var setTime = function(){
		
		var myDate = new Date();
		callfrom = "";
		startTime = tac.util.getDateTimeAll(new Date(myDate.getTime()));
		endTime = tac.util.getDateTimeAll(new Date(myDate.getTime()));
		
		startTime = startTime.date+" 00:00:00";
		endTime = endTime.date+" " + endTime.time;
		
		$("#searchReview_startTime").val(startTime);
		$("#searchReview_endTime").val(endTime);
		
	}
	
	/**
	 * 绑定回车查询
	 */
	var _bindingEnterEvent = function(){
		
		$("#searchReview_startTime").keyup(function(event){
			if(event.keyCode===13)$("#sr_searchBtn").click();
		});
		
		$("#searchReview_endTime").keyup(function(event){
			if(event.keyCode===13)$("#sr_searchBtn").click();
		});
		
		$("#callfrom").keyup(function(event){
			if(event.keyCode===13)$("#sr_searchBtn").click();
		});
		
		$("#callTarget").keyup(function(event){
			if(event.keyCode===13)$("#sr_searchBtn").click();
		});
		
		
		$("#searchReview_agentId").keyup(function(event){
			if(event.keyCode===13)$("#sr_searchBtn").click();
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
