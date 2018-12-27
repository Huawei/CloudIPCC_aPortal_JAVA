define(['tac' ,'tacCfg' ,'text!../../../../html/modules/common/debugbox/debugbox.html'], function(tac, tacCfg , html) {
	var module = null;
	/*
	 * 定义变量
	 */
	var el = null;
	var infoList = [];
	var domMap = null;
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
	 * 渲染
	 */
	var render = function(){
		if(el){
			if(tacCfg.debugbox){
				el.append($.tmpl(html));
				domMap = {
					info : $('#debugbox_info')
				};
			}
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
	var pushInfo = function(__text){
		if(domMap){
			infoList.unshift(__text);
			if(infoList.length > 20){
				infoList.pop();
			}
			domMap.info.html(infoList.join('</br>'));	
		}
	};
	/*
	 * 暴露接口
	 */
	tac.modules.debugbox = module = {
		init : init,
		render : render,
		show : show,
		pushInfo : pushInfo
	};
	return module;
});
