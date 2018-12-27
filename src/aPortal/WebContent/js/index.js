define(['tac',
		'tacCfg',
		'modules/common/menu/menu',
		'modules/common/status/status',
		'modules/common/phone/phone',
		'modules/common/msg/msg',
		'modules/common/sign/sign',
		'modules/common/debugbox/debugbox',
		'modules/common/phone/phone',
		'modules/quality/searchReview/searchReview',
		'component/dialog/dialog',
		'tool/tool'
	], 
	function(
		tac, 
		tacCfg,
		menu,
		status,
		phone,
		msg,
		sign,
		debugbox,
		phone,
		searchReview,
		Dailog,
		tool
	) {
	/*
	 * 私有方法：国际化
	 */
	var _i18nInit = function(){
		$(document).find(".i18n").each(function(){
			if($(this).attr("i18nKey")){
				$(this).html($.i18n.prop($(this).attr("i18nKey")));
			}
		});
		$(document).find(".i18nplaceholder").each(function(){
			if($(this).attr("i18nKey")){
				$(this).attr('placeholder',$.i18n.prop($(this).attr("i18nKey")));
			}
		});
		$(".sysMessage18n").each(function(){
            var _this = $(this);
            var _i18ReplaceChat = _this.attr("i18ReplaceChat");
            if(_i18ReplaceChat){
                _this.html($.i18n.prop(_this.attr("i18nTitle")).replace('\$\{param\}',_i18ReplaceChat));    
            }else{
                _this.html($.i18n.prop(_this.attr("i18nTitle")));    
            }
        });
		//修改 定时签出的title
		tac.modules.autoCheckout.autoCheckoutTipsAction();
	};
	var init = function(){
		
		//取开始时间，如果未取到，则取此处时间
		var startTime = new Date().getTime();
		
		
		//初始化国际化
		try{
			var languageValue = tac.util.cookie.get(tacCfg.cookieKey.tacLanguageValue);
			$("#change_lanaguages").text(languageValue);
			var language = window.navigator.language || window.navigator.userLanguage;
			var cookieLanguage=tac.util.cookie.get(tacCfg.cookieKey.tacLanguage);
			if(cookieLanguage==null||cookieLanguage==""){
				language = window.navigator.language || window.navigator.userLanguage;
				if(language=='zh-CN'){
					$("#change_lanaguages").text("EN");
					tac.data.set("tacLanguage" , "zh-CN");
				}else{
					$("#change_lanaguages").text("中文");
					tac.data.set("tacLanguage" , "en-US");
				}
			}else if(language==cookieLanguage){
				language=window.navigator.language || window.navigator.userLanguage;
				if(language=='zh-CN'){
					$("#change_lanaguages").text("EN");
					tac.data.set("tacLanguage" , "zh-CN");
				}else{
					$("#change_lanaguages").text("中文");
					tac.data.set("tacLanguage" , "en-US");
				}
			}else{
				if(language!='zh-CN'&&language!='en-US'){
					language='en-US';
					$("#change_lanaguages").text("中文");
					tac.data.set("tacLanguage" , "en-US");
				}else{
					language=cookieLanguage;
					if(language=="en-US"){
						$("#change_lanaguages").text("中文");
						tac.data.set("tacLanguage" , "en-US");
					}else{
						$("#change_lanaguages").text("EN");
						tac.data.set("tacLanguage" , "zh-CN");
					}
				}
			}
			$.i18n.properties({
					name: 'strings',
					path: 'i18n/',
					mode: 'map',
					language: language == 'zh-CN' ? 'zh' : 'zh'
			});	
		}catch(e){
		}
		
	
		//初始化调试信息盒子
		debugbox.init({
			el: $(document.body)
		});
		debugbox.render();
		//初始化左侧菜单
		menu.init({
			el: $("#menu")
		});
		menu.render();
		//初始化座席状态
		status.init({
			el: $("#top_status")
		});
		status.render();
		//初始化电话条
		phone.init({
			el: $("#top_phone")
		});
		phone.render();
		//初始化消息提示
		msg.init({
			el: $("#top_msg")
		});
		//初始化工具条
		tool.init({
			el: $("#pull-right-tool")
		});
		msg.render();

		//初始化签入签出
		sign.init({
			el: $("#top_sign")
		});
		sign.render();

		//初始化tooltip
		$('[data-toggle="tooltip"]').tooltip();
		
		//初始化popover
		$('[data-toggle="popover"]').popover({
			'html': true
		});

	
		/*移动*/
		$("#menu_card_left").click(function(){
			deviations_left=deviations_left-70;
			if(deviations_left<0)
				deviations_left=0;
			$("#menu_card_panel").scrollLeft(deviations_left);
		});
		
		$("#menu_card_right").click(function(){
			deviations_left=deviations_left+70;
			if(deviations_left>deviations)
				deviations_left=deviations;
			$("#menu_card_panel").scrollLeft(deviations_left);
		});
		
		$("#top_system_img").click(function(){
			//埋码
			location.href = '#system';
		});
		/*初始化选项卡*/
		
		var deviations=0;//总偏移量
		var deviations_left=0;//每次偏移量
		var isClickButton = false;
		var initCardpanel = function(){
			var w = $("#menu_card_panel").width();
			var c_w = 0;
			$("#menu_card_container").children().each(function(){
				c_w+=$(this).width()+26+7;
			});
			
			if((c_w-w)>4){
				deviations = c_w-w+20;
				$("#menu_card_container").width(c_w+20);
				$("#menu_card_left").show();
				$("#menu_card_right").show();
			}else{
				$("#menu_card_panel").width($(window).width()-90);
				$("#menu_card_container").width($("#menu_card_panel").width()+30);
				$("#menu_card_left").hide();
				$("#menu_card_right").hide();
				deviations_left = 0;
				deviations = 0;
			}
		};
		
		$(window).resize(function(){
			initDocWidth();
		});
		
		var initDocWidth = function(){
			var w = $(window).width();
			var move_div_w = 0;
			var c_w = 0;
			$("#menu_card_container").children().each(function(){
				c_w+=$(this).width()+26+7;
			});
			
			if(c_w>(w-90)){
				move_div_w=80;
			}
			w_h = w-90-move_div_w;
			
			$("#menu_card_panel").width(w_h);
			initCardpanel();
		};
		
		initDocWidth();
		
		var work_div_container="";
		var initCard = function(__module,__title,_i18n){
			work_div_container = "work_20161201"+__module;
			if($("#"+work_div_container).get(0)){
				$("#"+work_div_container+"btn").removeClass("btn-info");
				$("#"+work_div_container+"btn").addClass("btn-primary");
				$("#"+work_div_container+"btn").siblings().removeClass("btn-primary").addClass("btn-info");
				$("#"+work_div_container).show().siblings().hide();
				
				return false;
			}else{
				var btnStr = '<div  class="btn btn-sm btn-primary" ref="'+work_div_container+'" name="work_div_card_btn"  id="'+work_div_container+'btn"><span class="i18n" i18nKey="'+_i18n+'">'+__title+'</span><span class="fa fa-close" ref="'+work_div_container+'" name="work_div_closer" id="'+work_div_container+'closer"></span></div>'
				$("#menu_card_container").append(btnStr);
				
				if("home"==__module)
					$("#menu_card_container").prepend($("#"+work_div_container+"btn"));
				
				$("#work").append("<div id='"+work_div_container+"' name='"+work_div_container+"div'></div>");
				
				
				$("#"+work_div_container+"btn").siblings().removeClass("btn-primary").addClass("btn-info");
				$("#"+work_div_container).show().siblings().hide();
				
				$("#"+work_div_container+"btn").click(function(){
					myid = $(this).attr("ref");
					myid = myid.replace("work_20161201","");

//					if(myid.indexOf("report_")!=-1)
//						myid = myid.replace("report_","report_load/");
					isClickButton = true;
					location.href = "#"+myid;
				});
				
				$("#"+work_div_container+"closer").click(function(e){
					myid = $(this).attr("ref");
					__myid=myid.replace("work_20161201","");
					
					
					$("#"+myid+"btn").remove();
					$("#"+myid).remove();
					
					if(myid==work_div_container){
						if($("#work").children().length>1 &&  $("#work").children()[0]){
							myid = $("#menu_card_container").children().last().attr("ref");
							myid = myid.replace("work_20161201","");
//							if(myid.indexOf("report_")!=-1)
//								myid = myid.replace("report_","report_load/");
							location.href = "#"+myid;
						}else{
							location.href = "#default";
						}
					}
					
					initCardpanel();
					
					
				});
				
				$("#"+work_div_container+"btn").addClass("btn-primary").siblings().removeClass("btn-primary").addClass(" btn-info");
				$("#"+work_div_container).show().siblings().hide();
				initCardpanel();
				initDocWidth();
				return true;
			}
		}
		
			
		//定义路由处理方法
		var routerHandler = {
			loadModule : function(__module){
				var cookieLanguage="zh-CN";
				//定义埋码开始时间
				var startTime = new Date().getTime();
				$("#work").removeClass("top-0");

				switch(__module){
					case 'quality_search_review':
						var title = $.i18n.prop('menu.system.basic.searchreview');
						if(initCard(__module, title, 'menu.system.basic.searchreview')){
							routerHandler.loadQualitySearchReview();
						}
					break;
					case 'report_vdnTraffic':
						var title = $.i18n.prop('menu.report.vdnTraffic');
						if(initCard(__module, title, 'menu.report.vdnTraffic')){
							routerHandler.loadVdnTrafficReport();
						}
					break;
					//技能话务量
					case 'report_skillTraffic':
						var title = $.i18n.prop('menu.report.skillTraffic');
						if(initCard(__module, title, 'menu.report.skillTraffic')){
							routerHandler.loadSkillTrafficReport();
						}
					break;
					//话务员话务量
					case 'report_agentTraffic':
						var title = $.i18n.prop('menu.report.agentTraffic');
						if(initCard(__module, title, 'menu.report.agentTraffic')){
							routerHandler.loadAgentTrafficReport();
						}
					break;
					//话务员接续
					case 'report_agentWork':
						var title = $.i18n.prop('menu.report.agentWork');
						if(initCard(__module, title, 'menu.report.agentWork')){
							routerHandler.loadAngentWorkReport();
						}
					break;
					//话务员呼出
					case 'report_agentCalloutTraffic':
						var title = $.i18n.prop('menu.report.agentCalloutTraffic');
						if(initCard(__module, title, 'menu.report.agentCalloutTraffic')){
							routerHandler.loadAgentCallOutReport();
						}
					break;
					//技能话务量(按技能)
					case 'report_skillTrafficBySkill':
						var title = $.i18n.prop('menu.report.skillTrafficBySkill');
						if(initCard(__module, title, 'menu.report.skillTrafficBySkill')){
							routerHandler.loadSkillTrafficReportBySkill();
						}						
					break;
					//话务员话务量(按技能)
					case 'report_agentTrafficBySkill':
						var title = $.i18n.prop('menu.report.agentTrafficBySkill');
						if(initCard(__module, title, 'menu.report.agentTrafficBySkill')){
							routerHandler.loadAgentSkillTrafficReport();
						}						
					break;
					
					case 'system':
						var title = $.i18n.prop('menu.system.config');
						if(initCard(__module, title, 'menu.system.config')){
							routerHandler.loadSystem();
						}
			        /**
			         * 隐藏报表和录音回放菜单
			         */
					$("#menu").attr("style","display:none");
			    	$("span[name='work_div_closer'").attr("class","fa fa-close");
						
						break;
					case 'default':
						routerHandler.loadDefault();
						break;
				}
				
				$('#work').show();
			},
			
			loadDefault : function(){
				$("#home_work_home_banner").show();
				$("#work").addClass("top-0");
			},
			
			loadQualitySearchReview : function(){
				require(['modules/quality/searchReview/searchReview'], function(quality) {
					quality.init({
						el : $('#'+work_div_container)	
					}).render().show();
				});
			},
			
			loadSystem : function(){
				require(['modules/system/system'], function(system) {
					system.init({
						el : $('#'+work_div_container)
					}).render().show();
				});
			},
			
			loadVdnTrafficReport : function(){
				require(['modules/report/vdnTrafficReport'], function(vdnTrafficReport) {
					vdnTrafficReport.init({
						el : $('#'+work_div_container)
					}).render().show();
				});
			},
			//加载技能话务量报表
			loadSkillTrafficReport : function(){
				require(['modules/report/skillTrafficReport'], function(skillTrafficReport) {
					skillTrafficReport.init({
						el : $('#'+work_div_container)
					}).render().show();
				});
			},
			//加载话务员话务量报表
			loadAgentTrafficReport : function(){
				require(['modules/report/agentTrafficReport'], function(agentTrafficReport) {
					agentTrafficReport.init({
						el : $('#'+work_div_container)
					}).render().show();
				});
			},
			//加载话务员接续报表
			loadAngentWorkReport : function(){
				require(['modules/report/agentWorkReport'], function(agentWorkReport) {
					agentWorkReport.init({
						el : $('#'+work_div_container)
					}).render().show();
				});
			},
			//加载话务员呼出报表
			loadAgentCallOutReport : function(){
				require(['modules/report/agentCallOutBrief'], function(vdnTrafficReport) {
					vdnTrafficReport.init({
						el : $('#'+work_div_container)
					}).render().show();
				});
			},
			//加载技能话务量（按技能）报表
			loadSkillTrafficReportBySkill : function(){
				require(['modules/report/skillTrafficReportBySkill'], function(vdnTrafficReport) {
					vdnTrafficReport.init({
						el : $('#'+work_div_container)
					}).render().show();
				});
			}			
			
		};
		
		//初始化并启动路由
		Router({
			/*'report_load/:index' : routerHandler.loadReport,*/
			':module' : routerHandler.loadModule
		}).init();
		
		isClickButton = true;

	};
	
	

	
	return {
		init : init
	};
});
