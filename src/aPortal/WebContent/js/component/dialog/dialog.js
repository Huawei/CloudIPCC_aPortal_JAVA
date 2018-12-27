define(['text!../../../html/component/dialog/dialog.html'], function(html) {
	var Dailog = function(__cfg) {
		if (__cfg) {
			this.$title = __cfg.title || '';
			this.$message = __cfg.message || '';
			this.$top = __cfg.top || 0;
			this.$left = __cfg.left || 0;
			this.$width = __cfg.width || 0;
			this.$height = __cfg.height || 0;
			this.$button = __cfg.button;
			this.$maskFlag = __cfg.mask != false;
			this.$model = __cfg.model;
			this.$drag = __cfg.drag;
			this.$prompt = __cfg.prompt;
			this.$icon = __cfg.icon;
			this.$isContinue = __cfg.isContinue;
		}
		this.$masking = null;
		this.$el = null;
		this.$render();
	};
	Dailog.prototype.show = function() {
		if (parseInt(this.$el.css('left'), 10) + this.$el.width() > $(window).width()) {
			this.$el.css('left', $(window).width() - this.$el.width() - 50);
		}
		if (parseInt(this.$el.css('top'), 10) + this.$el.height() > $(window).height()) {
			this.$el.css('top', $(window).height() - this.$el.height() - 20);
		}
		if(this.$button && this.$button[0] && this.$button[0]['element'] && !this.$model){
			$(document).keypress(this.$button[0]['onEnter']);
		}
		if (this.$masking) {
			this.$masking.fadeIn('fast');
		}
		if (this.$el) {
			this.$el.fadeIn('fast');
		}
	};
	Dailog.prototype.hide = function() {
		//设置是否弹出
		if(!this.$isContinue){
			tac && tac.setPromptFlag();
		}
		if (this.$button && Object.prototype.toString.call(this.$button) == '[object Array]') {
			for (var i = 0; i < this.$button.length; i++) {
				this.$button[i]['element'].unbind('click', this.$button[i]['onClick']);
			}
		}
		if (this.$masking) {
			!this.$model && this.$masking.unbind('click', this.$masking.action);
			this.$masking.fadeOut('fast', function() {
				$(this).detach();
			});
		}
		if (this.$el) {
			this.$el.unbind('click', this.$el.stopPropagation);
			this.$el.fadeOut('fast', function() {
				$(this).detach();
			});
		}
		if(this.$button && this.$button[0] && this.$button[0]['element'] && !this.$model){
			$(document).unbind('keypress' , this.$button[0]['onEnter']);
		}
		for (var property in this) {
			if (this.hasOwnProperty(property)) {
				this[property] = null;
			}
		}
	};
	Dailog.prototype.$render = function() {
		var me = this;
		if (this.$maskFlag) {
			this.$masking = $('<div class="masking"/>').hide();
			if (this.$model) {
				this.$masking.appendTo(document.body);
			}else{
				this.$masking.action = function(){
					me.hide();
				};
				this.$masking.bind("click", this.$masking.action).appendTo(document.body);
			}
		}
		var css = {
			'position' : 'fixed',
			'display' : 'none',
			'z-index': '99999'
		};
		if(!this.$left && !this.$top){
			css.left = (this.$left = '50%');
			css.top = (this.$top = '50%');
			css.margin = '-' + (this.$height / 2) + "px 0px 0px -" +  (this.$width / 2) + "px";
		}
		this.$el = $.tmpl(html, null).css(css).appendTo(document.body);
		this.$el.stopPropagation = function(e) {
			e.stopPropagation();
		};
		this.$el.bind('click', this.$el.stopPropagation);
		this.$width && this.$el.css('width', this.$width);
		this.$height && this.$el.css('min-height', this.$height);
		this.$title && this.$el.find('.modal-title').html(this.$title);
		var $content = this.$el.find('div[aid=txt].text-center'),
			conHTML = '',isShow = 'none',margin = '8px 0px 0 15px;';
			$content.empty();
		if(this.$prompt){
			isShow = 'block';
			margin = '15px 0px 0 15px;';
		}
		if(this.$icon){
			conHTML =   '<div style="float: left; width: 45px; margin: '+margin+'">' + 
						'<span class="fa fa-exclamation-circle fa-3x text-info"></span>' + 
						'</div>' + 
						'<div style="float: left; width: 180px; padding: 0 10px 0 10px">' + 
							'<div style="width: 100%;word-wrap: break-word;word-break: break-word; margin-top: 15px;" class="text-left">'+this.$message+'</div>' + 
							'<div class="text-left checkbox checkbox-primary checkbox-inline"' +
								'style="width: 100%; margin-top: 5px; color: #999; display: '+isShow+'">' + 
								'<input type="checkBox"' + 
									'style="cursor: pointer;"> <label ' + 
									'class="dialog_prompt_label">'+ this.$prompt +'</label> '+
							'</div>' + 
							'<div></div>' + 
						'</div>';
		}else{
			conHTML =   '<div style="width: 100%;word-wrap: break-word;word-break: break-word;" class="text-left">'+this.$message+'</div>' + 
						'<div class="text-left checkbox checkbox-primary checkbox-inline"' + 
							'style="width: 100%; margin-top: 10px; color: #999; display: '+isShow+'">'+
							'<input type="checkBox" style="cursor: pointer;">' +
							'<label class="dialog_prompt_label">'+ this.$prompt +'</label>' +
						'</div>';
		}
		$content.append(conHTML);
		//默认勾选
		$content.find('input').check();
		$('.dialog_prompt_label').click(function(){
			$(this).prev().click();
		});
		this.$el.find('.modal-body').css('height' , this.$height - 37);
	
		this.$el.find('button.close').bind("click", function(){
			me.hide();
		});
		
		if (this.$button && Object.prototype.toString.call(this.$button) == '[object Array]') {
			var btnArea = this.$el.find('div[aid=btn].text-center');
			for (var i = 0; i < this.$button.length; i++) {
				(function(__i) {
					var action = me.$button[__i]['onClick'];
					me.$button[__i]['onClick'] = function(e) {
						typeof action === 'function' && action(me.$el);
						e.stopPropagation();
						me.hide();
					}
					if(!me.$model){
						me.$button[__i]['onEnter'] = function(e) {
							if(e.which == 13){
								typeof action === 'function' && action(me.$el);
								e.stopPropagation();
								me.hide();
							}
						}
					}
					if(i > 0){
						btnArea.append("<span>　</span>");
					}
					btnArea.append(me.$button[__i]['element'] = $('<button type="button" data-dismiss="modal" />').html(me.$button[__i]['text']).addClass(me.$button[__i]['style'] === 'submit' ? 'btn btn-sm btn-success' : me.$button[__i]['style'] === 'cancel' ? 'btn btn-sm btn-secondary' : 'btn').bind('click', me.$button[__i]['onClick']));
				})(i);
			}
		}
		if(this.$drag){
			this.$el.tacdrag({
				dragTarget : this.$el.find('.modal-title')
			});
		}
	};
	return Dailog;
});
