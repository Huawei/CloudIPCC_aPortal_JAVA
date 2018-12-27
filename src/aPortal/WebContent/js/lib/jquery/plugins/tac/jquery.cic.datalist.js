/*
 * (1)入参说明：
 * ※onInput：当输入框有输入时触发
 * ※onSelect：当选中一条记录时触发
 * ※onPage：当页码切换时触发
 * ※data：要显示的数据
 *   结构：{
 * 				title:[
 * 					'title0','title1'
 * 				],
 * 				list:[
 * 					{
 * 						val : {},
 * 						tds : [
 * 								{
 *	 								val:0,
 * 									txt:0
 * 								},
 * 								{
 *	 								val:0,
 * 									txt:0
 * 								}
 * 						]
 * 					}
 * 				],
 * 				page:{
 * 					now : 1,
 * 					all : 5,
 * 					row : 100
 * 				},
 * 				info : '',
 * 				formatter : []
 * 			}
 * (2)方法说明：
 * ※setData
 *   设置数据
 * ※hideList
 *   隐藏列表
 * (3)调用示例
 * ※$('#ccr_create_basic_info_sr_type_input_' + this.idPostfix).cicdatalist({
			onInput : function(){
				if(this.val()){
				this.setData({
					title : [
						'title1',
						'title2'
					],
					list : [
								{
									val : 'text',
									tds : [
											{
												val : 0,
												txt : 'aaaaaaaaa'
											},
											{
												val : 1,
												txt : 'bbbbbbbbb'
											}
										]
								},
								{
									val : 'text',
									tds : [
											{
												val : 0,
												txt : 'aaaaaaaaa'
											},
											{
												val : 1,
												txt : 'bbbbbbbbb'
											}
										]
								},
								{
									val : 'text',
									tds : [
											{
												val : 0,
												txt : 'aaaaaaaaa'
											},
											{
												val : 1,
												txt : 'bbbbbbbbb'
											}
										]
								},
								{
									val : 'text',
									tds : [
											{
												val : 0,
												txt : 'aaaaaaaaa'
											},
											{
												val : 1,
												txt : 'bbbbbbbbb'
											}
										]
								}
							],
					formatter : [
						function(__data){
							return '<span style="color:red;">'+__data.txt+'</span>';
						}
					],
					info : 'info_info'
				});
				}else{
					this.setData();
				}
			},
			onSelect : function(__data){
				$(this).val(__data.txt);
			}
		});
 */
define(function(){
		var masking = null;//遮罩
		var inputTimer = null, inputTimerDelay = 500;
		$.fn.cicdatalist = function(__options){
			if(__options){
				//DOM实体
				var inputBox = this;//输入框
				var dataContainerBox = null;//容器
				var dataTableBoxContainer = null; //表格容器
				var dataTableBox = null;//表格
				var dataTitleBox = null;//标题
				var dataListBox = null;//列表
				var dataPageBox = null;//翻页
				var dataBtmInfoBox = null;//底部信息
				var dataNoBox = null;//无数据提示
				//基本信息
				var _width = __options.width;//宽度
				var _height = __options.height == null ? "160px" : __options.height;//高度
				var _tableCondensed = __options.tableCondensed == null ? false : __options.tableCondensed;//表格间距是否缩减, 默认不缩减 false
				var _isHideTable = __options.isHideTable == null ? true : __options.isHideTable;//联想框是否消失, 默认消失 true
				var _isDefaultWidth = __options.isDefaultWidth==true ? true : false;//继承input的宽度
				var _isDefaultMasking = __options.isDefaultMasking==null ? null : __options.isDefaultMasking;//弹出层时，已有蒙版，点击无效时，传入dialog对象Id，点击关闭
				var __width = inputBox.outerWidth();
				var _data = null;//数据
				var _onInput = __options.onInput;//当输入时触发
				var _onSelect = __options.onSelect;//当选择时触发
				var _onPage = __options.onPage;//当页码切换时触发
				/*
				 * SR 建单页面 查询联系人联想框
				 * add by ywx311594 2017-01-20
				 * 每一行的宽度 ，是个数组分别放置每一个列的宽度集合,可以不传，但是如果传递，则必须和title的个数一直，每个值一一对应相应title的列宽
				 * */
				var _tdWidthArr = __options.tdWidthArr;
				//提示语信息
				var _noDataInfo = __options.noDataInfo == null ? "no data!" : __options.noDataInfo;
				
				//定义样式
				var dataContainerBoxCss = {
					'position':'absolute',
					'border' : '1px solid #ddd',
					'background' : '#ffffff',
					'z-index' : '10000',
					'width' : _width ? (_width + 'px') : __width + 'px',
					'display' : 'none'
				};
				var dataTableBoxContainerCss={
					'max-height' : _height,
					'overflow': 'auto'
				}
				var dataTableBoxCss = {
					'width' : '100%',
					'background' : '#ffffff',
					'border' : 'none'
				};
				var dataTitleTrCssFixed = {
					// height : '20px',
					// background : '#bbd9ef',
					position : 'fixed',
					'z-index': 1
				};
				var dataTitleTrCss = {
					// height : '20px',
					background : '#bbd9ef',
				};
				var dataTitleTrTdCss = {
					// padding : '0px 0px 0px 10px'
				};
				var dataListTrCss = {
					'cursor' : 'pointer',
					// 'border-bottom' : '1px solid #ddd'
				};
				var dataListTrTdCss = {
					// padding : '0px 0px 0px 10px'
				};
				var dataPageBoxCss = {
					'background' : '#ffffff',
					'padding' : '0px 20px 0px 20px'
				};
				var dataBtmInfoBoxCss = {
					'background' : '#efefef',
					'border-top' : '1px solid #ddd',
					'padding-left' : '10px',
					'color' : '#990000'
				};
				var dataNoBoxCss = {
					'background' : '#eeeeee',
					'border-top' : '1px solid #ddd',
					'padding-left' : '10px'
				};
				//初始化
				dataContainerBox = $('<div id="associational_div" class="cic-datalist" />').css(dataContainerBoxCss);
				dataTableBoxContainer = $('<div />').css(dataTableBoxContainerCss);
				inputBox.after(dataContainerBox);
				if(!masking){
					masking = $('<div id="associational_masking" class="masking" style="background:transparent;opacity:0;"/>').hide().appendTo(document.body);
				}
				masking.bind('click' , function(){
					masking.hide();
					dataContainerBox.hide();
				});
				if(_isDefaultMasking){
					_isDefaultMasking.bind('click' , function(){
						dataContainerBox.hide();
					});
				}
				var callOnInput = function(){
					window.clearTimeout(inputTimer);
					inputTimer = window.setTimeout(function(){
						typeof _onInput == 'function' && _onInput.call(inputBox);
						if(!$.trim(inputBox.val())){
							dataContainerBox.hide();
							masking.hide();
						}
					}, inputTimerDelay);
				};
				inputBox.bind('click' , callOnInput);
				inputBox.bind('input' , callOnInput);
				inputBox.bind('propertychange' , callOnInput);
				dataContainerBox.attr("isDefaultWidth",_isDefaultWidth);
				inputBox.setData = function(__data){
					dataTableBox = null;//表格
					dataTitleBox = null;//标题
					dataListBox = null;//列表
					dataPageBox = null;//翻页
					dataBtmInfoBox = null;//底部信息
					dataContainerBox.empty();
					_data = __data;
					if(_data && _data.list && _data.list.length > 0){//有数据
						if(_tableCondensed){
							dataTableBox = $('<table class="table table-striped table-hover table-condensed" cellspacing="0" cellpadding="0"/>').css(dataTableBoxCss);
						}else{
							dataTableBox = $('<table class="table table-striped table-hover" cellspacing="0" cellpadding="0"/>').css(dataTableBoxCss);
						}
						dataContainerBox.append(dataTableBoxContainer);
						dataTableBoxContainer.empty();
						dataTableBoxContainer.append(dataTableBox);
						// if(_data.title){//有标题
						// 	dataTitleBox = $('<thead/>');
						// 	var dataTitleBoxTr = $('<tr class="info" />').css(dataTitleTrCssFixed);
						// 	var dataTitleBoxTrTd = null;
						// 	for(var i = 0 ; i < _data.title.length ; i++){
						// 		/*dataTitleBoxTrTd = $('<th>'+_data.title[i]+'</th>').css(dataTitleTrTdCss);*/
						// 		if(typeof _tdWidthArr!== 'undefined'){
						// 			dataTitleBoxTrTd = $('<th>'+_data.title[i]+'</th>').css({'width':_tdWidthArr[i]+'px'});
						// 		}else{
						// 			dataTitleBoxTrTd = $('<th>'+_data.title[i]+'</th>').css(dataTitleTrTdCss);
						// 		}
						// 		dataTitleBoxTr.append(dataTitleBoxTrTd);
						// 	}
						// 	dataTableBox.append(dataTitleBox.append(dataTitleBoxTr));
						// }
						
						if(_data.list){//有数据
							dataListBox = $('<tbody/>');
							var dataListBoxTr = null;
							var dataListBoxTrTd = null;
							var dataListBoxTrTdContent = null;
							for(var i = 0 ,ll = _data.list.length>80?80 : _data.list.length; i < ll ; i++){
								if(_data.title){
									if(i==0){
										dataTitleBox = $('<thead/>');
										var dataTitleBoxTr = $('<tr class="info" />').css(dataTitleTrCss);
										var dataTitleBoxTrTd = null;
										for(var j = 0 ; j < _data.title.length ; j++){
											/*dataTitleBoxTrTd = $('<th>'+_data.title[i]+'</th>').css(dataTitleTrTdCss);*/
											if(typeof _tdWidthArr!== 'undefined'){
												dataTitleBoxTrTd = $('<th>'+_data.title[j]+'</th>').css({'width':_tdWidthArr[j]+'px'});
											}else{
												dataTitleBoxTrTd = $('<th>'+_data.title[j]+'</th>').css(dataTitleTrTdCss);
											}
											dataTitleBoxTr.append(dataTitleBoxTrTd);
										}
										dataTableBox.append(dataTitleBox.append(dataTitleBoxTr));
									}
								}
								dataListBoxTr = $('<tr idx="'+i+'"/>').css(dataListTrCss).click(function(){
									if(typeof $(this).attr('idx') != 'undefined'){
										var idx = parseInt($(this).attr('idx'), 10);
										typeof _onSelect == 'function' && _onSelect.call(inputBox, _data.list[idx]);
										if(_isHideTable){
											dataContainerBox.hide();
											masking.hide();
										}
									}
								})
								if( _data.list[i]){
									for(var j = 0 ; j < _data.list[i].tds.length ; j++){
										dataListBoxTrTdContent = _data.list[i].tds[j].txt;
										if(_data.formatter && typeof _data.formatter[j] == 'function'){
											dataListBoxTrTdContent = _data.formatter[j].call(this, _data.list[i].tds[j]);
										}
										var dataListBoxTrTdTitle = "";
										if(dataListBoxTrTdContent!=null && dataListBoxTrTdContent.length>0){
											dataListBoxTrTdTitle = dataListBoxTrTdContent.replace(/<[^>]+>/g,"");
										}
										//dataListBoxTrTd = $('<td title="'+dataListBoxTrTdTitle+'">'+dataListBoxTrTdContent+'</td>').css(dataListTrTdCss);
										if(!dataListBoxTrTdContent)dataListBoxTrTdContent = '';
										dataListBoxTrTd = $('<td title="'+dataListBoxTrTdTitle+'">'+dataListBoxTrTdContent+'</td>').css(dataListTrTdCss);
										dataListBoxTr.append(dataListBoxTrTd);
									}
								}
								dataListBox.append(dataListBoxTr);
							}
							dataTableBox.append(dataListBox);
						}
						
						if(_data.page){//有分页
							var dataPageBoxId = 'cic_datalist_page_'+getRandomString();
							dataPageBox = $('<div id="'+dataPageBoxId+'"/>').css(dataPageBoxCss);
							dataContainerBox.append(dataPageBox);
							initDataListPage(_data.page.row, _data.page.all, dataPageBoxId, _data.page.now || 1,function(__page){
								_onPage.call(inputBox, __page);
							});
						}
						
						/**if(_data.info){
							dataBtmInfoBox = $('<div/>').css(dataBtmInfoBoxCss).html(_data.info);
							dataContainerBox.append(dataBtmInfoBox);
						}*/
						if(_data.list.length > 80){
							dataBtmInfoBox = $('<div/>').css(dataBtmInfoBoxCss).html("Only 80 of all the results are displayed. Try more keywords.");
							dataContainerBox.append(dataBtmInfoBox);
						}
						
					}else{
						dataNoBox = $('<div/>').css(dataNoBoxCss).html(_noDataInfo);
						dataContainerBox.append(dataNoBox);
					}
					var left = inputBox.offset().left;
					if(dataContainerBox.attr("isDefaultWidth")=="true"){
						$(dataContainerBox).css("width",inputBox.parent().width());
					}
					dataContainerBox.show();
					dataContainerBox.offset({left:left});
					masking.show();
				};
				inputBox.hideList = function(){
					dataContainerBox.hide();
					masking.hide();
				};
			}
			return this;
		};
		var getRandomString = function() {
			return (new Date).getTime().toString(36) + Math.floor(Math.random() * 1e8).toString(36);
		};
		var initDataListPage = function(totalSize,tatalPage,page_type,myPage,_getData){
			if(tatalPage<=1)return;
			
			myPage = parseInt(myPage);
			
			var gong = $.i18n.prop('common.system.total_1');
			var tiao = $.i18n.prop('common.system.total_2');
			var str = '<div class="pull-right" style="margin-top: 16px; margin-left: 20px;">'+gong +(totalSize || 0) + tiao+'</div>';
			str += '<div class="pull-right input-group input-group-sm" style="width: 80px; margin: 10px 0;">';
			str += '<input type="text" class="form-control" id="'+page_type+'page_number" style="text-align: center;">';
			str += '<span class="input-group-addon pointer" style="background-color: #ECF7FD;" id="'+page_type+'go_page_number">Go</span> </div>';
			str +='<ul class="pagination" style="margin: 10px 0;">';
			str += '<li  id="'+page_type+'first_page"><a style="cursor:pointer;"><span aria-hidden="true" class="fa fa-step-backward"></span></a></li>';
			str += '<li  id="'+page_type+'previous_page"><a style="cursor:pointer;" aria-label="Previous" ><span aria-hidden="true" class="fa fa-chevron-left"></span></a></li>';
			str +='<li ><a name="page_numbers">'+myPage+' / '+tatalPage+'</a></li>';
			str += '<li id="'+page_type+'next_page"><a style="cursor:pointer;" aria-label="Next"><span aria-hidden="true" class="fa fa-chevron-right"></span></a></li>';
			str += '<li id="'+page_type+'last_page"><a style="cursor:pointer;"><span aria-hidden="true" class="fa fa-step-forward"></span></a></li> ';
			str +='</ul>';
			
			
			$(str).appendTo($("#"+page_type));
			str=null;
			
			if(myPage==1){
				$("#"+page_type+"previous_page").attr("class","disabled");
				$("#"+page_type+"first_page").attr("class","disabled");
			}else{
				$("#"+page_type+"previous_page").attr("class","");
				$("#"+page_type+"previous_page").click(function(){_getData(myPage-1);});
				$("#"+page_type+"first_page").click(function(){_getData(1);});
			}
			
			if(myPage==tatalPage){
				$("#"+page_type+"next_page").attr("class","disabled");
				$("#"+page_type+"last_page").attr("class","disabled");
			}
			else{
				$("#"+page_type+"next_page").attr("class","");
				$("#"+page_type+"next_page").click(function(){_getData(myPage+1);});
				$("#"+page_type+"last_page").click(function(){_getData(tatalPage);});
			}
			
			$("#"+page_type+"go_page_number").click(function(){
				num = $("#"+page_type+"page_number").val();
				if(!isNaN(num) && num>=1 && num<=tatalPage){
					_getData(parseInt(num));
				}else{
					tips({
						type : "warning",
						content : $.i18n.prop('common.system.range')
					});
				}
			});
			
			$("#"+page_type+"page_number").keyup(function(event){
				if(event.keyCode==13)
					$("#"+page_type+"go_page_number").click();
			});
	        $("#"+page_type+"page_number").on('input' , function(){
				$(this).val($(this).val().replace(/[^\d]/g, ''));
			});
			
			$("#"+page_type+"page_number").on('propertychange' , function(){
				$(this).val($(this).val().replace(/[^\d]/g, ''));
			});
		};
		
		
		//测试使用，联想框(MSR使用)
		$.fn.autoCompleteMSR = function(__options,callback){
			if(__options){
				//DOM实体
				var inputBox = this;//输入框
				var dataContainerBox = null;//容器
				var dataTableBox = null;//表格
				var dataTitleBox = null;//标题
				var dataListBox = null;//列表
				var dataPageBox = null;//翻页
				var dataBtmInfoBox = null;//底部信息
				var dataNoBox = null;//无数据提示
				//基本信息
				var _width = __options.width;//宽度
				var _height= __options.height;//高度
				var _data = null;//数据
				var _onInput = __options.onInput;//当输入时触发
				var _onSelect = __options.onSelect;//当选择时触发
				var _onPage = __options.onPage;//当页码切换时触发
				var _columns =__options.columns;  //显示的列数据
				var _removeData  =__options.removeData;   //需要去除的数据
				var _displayPaging =__options.displayPaging;  //是否显示分页,true显示，false不显示
				var _displayTitle  =__options.displayTitle;   //是否显示标题
				//定义样式
				var dataContainerBoxCss = {
					'position':'absolute',
					'border' : '1px solid #ddd',
					'background' : '#ffffff',
					'z-index' : '10000',
					'width' : _width ? (_width + 'px') : '100%',
					'display' : 'none'
				};
				var dataTableBoxCss = {
					'width' : '100%',
					'background' : '#ffffff',
					'border' : 'none'
				};
				var dataTitleTrCss = {
					height : '30px',
					background : '#bbd9ef'
				};
				var dataTitleTrTdCss = {
					'padding' : '0px 0px 0px 10px',
				};
				var dataListTrCss = {
					cursor : 'pointer',
					height : '30px'
				};
				var dataListTrTdCss = {
					padding : '0px 0px 0px 10px',
					'width':'50%',
					'white-space': 'nowrap'
				};
				var dataPageBoxCss = {
					'background' : '#ffffff',
					'padding' : '0px 20px 0px 20px'
				};
				var dataBtmInfoBoxCss = {
					'background' : '#eeeeee',
					'border-top' : '1px solid #ddd',
					'padding-left' : '10px'
				};
				var dataNoBoxCss = {
					'background' : '#eeeeee',
					'border-top' : '1px solid #ddd',
					'padding-left' : '10px'
				};
				//初始化
				dataContainerBox = $('<div class="cic-datalist"/>').css(dataContainerBoxCss);
				inputBox.after(dataContainerBox);
				if(!masking){
					masking = $('<div class="masking" style="background:transparent;opacity:0;"/>').hide().appendTo(document.body);
				}
				masking.bind('click' , function(){
					masking.hide();
					dataContainerBox.hide();
				});
				$(".ccr_modal_masking").bind('click' , function(){
					dataContainerBox.hide();
				});
				var callOnInput = function(){
					window.clearTimeout(inputTimer);
					inputTimer = window.setTimeout(function(){
						typeof _onInput == 'function' && _onInput.call(inputBox);
						if(!$.trim(inputBox.val())){
							dataContainerBox.hide();
							masking.hide();
						}
					}, inputTimerDelay);
				};
				inputBox.bind('click' , callOnInput);
				inputBox.bind('input' , callOnInput);
				inputBox.bind('propertychange' , callOnInput);
				inputBox.setData = function(__data){
					dataTableBox = null;//表格
					dataTitleBox = null;//标题
					dataListBox = null;//列表
					dataPageBox = null;//翻页
					dataBtmInfoBox = null;//底部信息
					dataDivBox = null;//div
					dataContainerBox.empty();
					_data = __data;
					if(_data && _data.returnData && _data.returnData.length > 0){//有数据
						//动态计算高度
						var tempLength=_data.returnData.length;
						if(tempLength>=10){
							tempLength=10;
						}
						var tempHeight = tempLength*30;
						if(_columns&&_displayTitle){
							tempHeight+=30;
							if(tempLength==10){
								tempHeight+=10;
							}
						}
						tempHeight++; //高度多增加一个像素可以去掉滚动条
						
						if(!_height){ //如果没有给定高度
							_height=tempHeight;
						}else{ //如果给定了高度，需要判断计算的和给定的，取最小的那个
							if(_height > tempHeight){
								_height = tempHeight;
							}
						}
						dataDivBox=$('<div id="ccr_dataDivBox_msr" style="height:'+_height+'px;overflow-y:auto">');
						dataTableBox = $('<table class="table table-striped table-hover" />').css(dataTableBoxCss);
						dataDivBox.append(dataTableBox);
						dataContainerBox.append(dataDivBox);
						if(_columns&&_displayTitle){   //有标题
							dataTitleBox = $('<thead/>');
							var dataTitleBoxTr = $('<tr/>').css(dataTitleTrCss);
							var dataTitleBoxTrTd = null;
							for(var i = 0 ; i < _columns.length ; i++){
								if(_columns[i].width!=undefined||_columns[i].width!='undefined'||_columns[i].width!=null||_columns[i].width!=""){
									dataTitleBoxTrTd = $('<td class="text-nowrap" style="width:'+_columns[i].width+'">'+_columns[i].header+'</td>').css(dataTitleTrTdCss);
								}else{
									dataTitleBoxTrTd = $('<td class="text-nowrap">'+_columns[i].header+'</td>').css(dataTitleTrTdCss);
								}
								dataTitleBoxTr.append(dataTitleBoxTrTd);
							}
							dataTableBox.append(dataTitleBox.append(dataTitleBoxTr));
						}
						
						
						
						if(_data.returnData){//有数据
							dataListBox = $('<tbody/>');
							var dataListBoxTr = null;
							var dataListBoxTrTd = null;
							var dataListBoxTrTdContent = null;
							sss:
							for(var i = 0 ; i < _data.returnData.length ; i++){
								//移除不符合条件的数据
								if( _data.returnData[i]&&_columns&&(_removeData!=null&&_removeData!="")){
										for(var jj = 0 ; jj < _columns.length ; jj++){
											//移除不符合条件的数据
											if(((_data.returnData[i])[_columns[jj].field])==_removeData){
												//需要将div ccr_dataDivBox_msr的高度去掉一行
												var ccr_dataDivBox_msr_height=$("#ccr_dataDivBox_msr").css("height");
												$("#ccr_dataDivBox_msr").css("height",parseInt(ccr_dataDivBox_msr_height)-30);
												continue sss;   //跳出整个外层的for循环，继续下一次循环
											}
										}
								}
								
								
								dataListBoxTr = $('<tr idx="'+i+'"/>').css(dataListTrCss).click(function(){
									if(typeof $(this).attr('idx') != 'undefined'){
										var idx = parseInt($(this).attr('idx'), 10);
										typeof _onSelect == 'function' && _onSelect.call(inputBox, _data.returnData[idx]);
										inputBox.hideList();
									}
								});
								if( _data.returnData[i]){
									//获取列头
									if(_columns){
										for(var j = 0 ; j < _columns.length ; j++){
											dataListBoxTrTdContent = (_data.returnData[i])[_columns[j].field];  //动态获取对象里面的值
											if(_data.formatter && typeof _data.formatter[j] == 'function'){
												dataListBoxTrTdContent = _data.formatter[j].call(this, _data.returnData[i]);
											}
											if(_columns[j].width!=undefined||_columns[j].width!='undefined'||_columns[j].width!=null||_columns[j].width!=""){
												dataListBoxTrTd = $('<td class="text-nowrap" style="width:'+_columns[j].width+'">'+$.trim(dataListBoxTrTdContent)+'</td>').css(dataListTrTdCss);
											}else{
												dataListBoxTrTd = $('<td class="text-nowrap">'+$.trim(dataListBoxTrTdContent)+'</td>').css(dataListTrTdCss);
											}
											
											dataListBoxTr.append(dataListBoxTrTd);
										}
									}
									
									
								}
								dataListBox.append(dataListBoxTr);
							}
							dataTableBox.append(dataListBox);
						}
						
						if(_displayPaging){//有分页
							var pageVO=_data.pageVO;
							var dataPageBoxId = 'cic_datalist_page_'+getRandomString();
							dataPageBox = $('<div id="'+dataPageBoxId+'"/>').css(dataPageBoxCss);
							dataContainerBox.append(dataPageBox);
							initDataListPage(pageVO.pageSize, pageVO.totalRows, dataPageBoxId, pageVO.curPage || 1,function(pageVO){
								_onPage.call(inputBox, pageVO);
							});
						}
						
						//改造提示信息，如果数据大于80行，显示提示信息
						if(_data.returnData.length>=80){//有提示   //_data.info
							dataBtmInfoBox = $('<div/>').css(dataBtmInfoBoxCss).html("Only 80 of all the results are displayed. Try more keywords.");
							dataContainerBox.append(dataBtmInfoBox);
						}
						
					}else{
						dataNoBox = $('<div/>').css(dataNoBoxCss).html('no data!');
						dataContainerBox.append(dataNoBox);
					}
					dataContainerBox.show();
					masking.show();
				};
				inputBox.hideList = function(){
					dataContainerBox.hide();
					masking.hide();
					typeof callback === 'function'&& callback();  //执行回调函数
				};
			}
			return this;
		};
		
		//测试使用，联想框(MSR使用)
		$.fn.autoCompleteMSR_NoResult = function(__options,callback){
			if(__options){
				//DOM实体
				var inputBox = this;//输入框
				var dataContainerBox = null;//容器
				var dataTableBox = null;//表格
				var dataTitleBox = null;//标题
				var dataListBox = null;//列表
				var dataPageBox = null;//翻页
				var dataBtmInfoBox = null;//底部信息
				var dataNoBox = null;//无数据提示
				//基本信息
				var _width = __options.width;//宽度
				var _height= __options.height;//宽度
				var _data = null;//数据
				var _onInput = __options.onInput;//当输入时触发
				var _onSelect = __options.onSelect;//当选择时触发
				var _onPage = __options.onPage;//当页码切换时触发
				var _columns =__options.columns;  //显示的列数据
				var _removeData  =__options.removeData;   //需要去除的数据
				var _displayPaging =__options.displayPaging;  //是否显示分页,true显示，false不显示
				var _displayTitle  =__options.displayTitle;   //是否显示标题
				//定义样式
				var dataContainerBoxCss = {
					'position':'fixed',
					'border' : '1px solid #ddd',
					'background' : '#ffffff',
					'z-index' : '10000',
					'width' : _width ? (_width + 'px') : '100%',
					'display' : 'none'
				};
				var dataTableBoxCss = {
					'width' : '100%',
					'background' : '#ffffff',
					'border' : 'none'
				};
				var dataTitleTrCss = {
					height : '30px',
					background : '#bbd9ef'
				};
				var dataTitleTrTdCss = {
					'padding' : '0px 0px 0px 10px',
				};
				var dataListTrCss = {
					cursor : 'pointer',
					height : '30px'
				};
				var dataListTrTdCss = {
					padding : '0px 0px 0px 10px',
					'width':'50%',
					'white-space': 'nowrap'
				};
				var dataPageBoxCss = {
					'background' : '#ffffff',
					'padding' : '0px 20px 0px 20px'
				};
				var dataBtmInfoBoxCss = {
					'background' : '#eeeeee',
					'border-top' : '1px solid #ddd',
					'padding-left' : '10px'
				};
				var dataNoBoxCss = {
					'background' : '#eeeeee',
					'border-top' : '1px solid #ddd',
					'padding-left' : '10px'
				};
				//初始化
				dataContainerBox = $('<div/>').css(dataContainerBoxCss);
				inputBox.after(dataContainerBox);
				if(!masking){
					masking = $('<div class="masking" style="background:transparent;opacity:0;"/>').hide().appendTo(document.body);
				}
				masking.bind('click' , function(){
					masking.hide();
					dataContainerBox.hide();
				});
				$(".ccr_modal_masking").bind('click' , function(){
					dataContainerBox.hide();
				});
				var callOnInput = function(){
					window.clearTimeout(inputTimer);
					inputTimer = window.setTimeout(function(){
						typeof _onInput == 'function' && _onInput.call(inputBox);
						if(!$.trim(inputBox.val())){
							dataContainerBox.hide();
							masking.hide();
						}
					}, inputTimerDelay);
				};
				inputBox.bind('click' , callOnInput);
				inputBox.bind('input' , callOnInput);
				inputBox.bind('propertychange' , callOnInput);
				inputBox.setData = function(__data){
					dataTableBox = null;//表格
					dataTitleBox = null;//标题
					dataListBox = null;//列表
					dataPageBox = null;//翻页
					dataBtmInfoBox = null;//底部信息
					dataDivBox = null;//div
					dataContainerBox.empty();
					_data = __data;
					//特殊处理，针对风险级别(Severity)中的Root Cause，返回的JSON串中没有result值 
					if(_data && (_data ==undefined||_data =='undefined')){//有数据
						//动态计算高度
						var tempLength=_data.length;
						if(tempLength>=10){
							tempLength=10;
						}
						_height=tempLength*30;
						if(_columns&&_displayTitle){
							_height+=30;
							if(tempLength==10){
								_height+=10;
							}
						}
						dataDivBox=$('<div  id="ccr_dataDivBox_NoResult" style="height:'+_height+'px;overflow-y:auto">');
						dataTableBox = $('<table   class="table table-striped table-hover" />').css(dataTableBoxCss);
						dataDivBox.append(dataTableBox);
						dataContainerBox.append(dataDivBox);
						if(_columns&&_displayTitle){   //有标题
							dataTitleBox = $('<thead/>');
							var dataTitleBoxTr = $('<tr/>').css(dataTitleTrCss);
							var dataTitleBoxTrTd = null;
							for(var i = 0 ; i < _columns.length ; i++){
								if(_columns[i].width!=undefined||_columns[i].width!='undefined'||_columns[i].width!=null||_columns[i].width!=""){
									dataTitleBoxTrTd = $('<td class="text-nowrap" style="width:'+_columns[i].width+'">'+_columns[i].header+'</td>').css(dataTitleTrTdCss);
								}else{
									dataTitleBoxTrTd = $('<td class="text-nowrap">'+_columns[i].header+'</td>').css(dataTitleTrTdCss);
								}
								dataTitleBoxTr.append(dataTitleBoxTrTd);
							}
							dataTableBox.append(dataTitleBox.append(dataTitleBoxTr));
						}
						
						
						
						if(_data){//有数据
							dataListBox = $('<tbody/>');
							var dataListBoxTr = null;
							var dataListBoxTrTd = null;
							var dataListBoxTrTdContent = null;
							sss:
							for(var i = 0 ; i < _data.length ; i++){
								//移除不符合条件的数据
								if( _data[i]&&_columns&&(_removeData!=null&&_removeData!="")){
										for(var jj = 0 ; jj < _columns.length ; jj++){
											//移除不符合条件的数据
											if(((_data[i])[_columns[jj].field])==_removeData){
												//需要将div ccr_dataDivBox_NoResult的高度去掉一行
												var ccr_dataDivBox_NoResult_height=$("#ccr_dataDivBox_NoResult").css("height");
												$("#ccr_dataDivBox_NoResult").css("height",parseInt(ccr_dataDivBox_NoResult_height)-30);
												continue sss;   //跳出整个外层的for循环，继续下一次循环
											}
										}
								}
								
								dataListBoxTr = $('<tr idx="'+i+'"/>').css(dataListTrCss).click(function(){
									if(typeof $(this).attr('idx') != 'undefined'){
										var idx = parseInt($(this).attr('idx'), 10);
										typeof _onSelect == 'function' && _onSelect.call(inputBox, _data[idx]);
										inputBox.hideList();
									}
								});
								if( _data[i]){
									//获取列头
									if(_columns){
										for(var j = 0 ; j < _columns.length ; j++){
											dataListBoxTrTdContent = (_data[i])[_columns[j].field];  //动态获取对象里面的值
											if(_data.formatter && typeof _data.formatter[j] == 'function'){
												dataListBoxTrTdContent = _data.formatter[j].call(this, _data[i]);
											}
											if(_columns[j].width!=undefined||_columns[j].width!='undefined'||_columns[j].width!=null||_columns[j].width!=""){
												dataListBoxTrTd = $('<td class="text-nowrap" style="width:'+_columns[j].width+'">'+$.trim(dataListBoxTrTdContent)+'</td>').css(dataListTrTdCss);
											}else{
												dataListBoxTrTd = $('<td class="text-nowrap">'+$.trim(dataListBoxTrTdContent)+'</td>').css(dataListTrTdCss);
											}
											
											dataListBoxTr.append(dataListBoxTrTd);
										}
									}
									
									
								}
								dataListBox.append(dataListBoxTr);
							}
							dataTableBox.append(dataListBox);
						}
						
						if(_displayPaging){//有分页
							var pageVO=_data.pageVO;
							var dataPageBoxId = 'cic_datalist_page_'+getRandomString();
							dataPageBox = $('<div id="'+dataPageBoxId+'"/>').css(dataPageBoxCss);
							dataContainerBox.append(dataPageBox);
							initDataListPage(pageVO.pageSize, pageVO.totalRows, dataPageBoxId, pageVO.curPage || 1,function(pageVO){
								_onPage.call(inputBox, pageVO);
							});
							
							//改造提示信息，如果数据大于80行，显示提示信息
							if(_data.length>=80){//有提示   //_data.info
								dataBtmInfoBox = $('<div/>').css(dataBtmInfoBoxCss).html("Only 80 of all the results are displayed. Try more keywords.");
								dataContainerBox.append(dataBtmInfoBox);
							}
							
						}
					}else{
						dataNoBox = $('<div/>').css(dataNoBoxCss).html('no data!');
						dataContainerBox.append(dataNoBox);
					}
					
					dataContainerBox.show();
					masking.show();
				};
				inputBox.hideList = function(){
					dataContainerBox.hide();
					masking.hide();
					typeof callback === 'function'&& callback();  //执行回调函数
				};
			}
			return this;
		};
		
		
});