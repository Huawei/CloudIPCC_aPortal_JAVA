define(function() {
	var Infobox = function(__cfg) {
		if (__cfg) {
			this.$html = __cfg.html || '';
			this.$sel = __cfg.el || null;
			this.$top = __cfg.top || 0;
			this.$left = __cfg.left || 0;
			this.$width = __cfg.width || 0;
			this.$height = __cfg.height || 0;
			this.$appendTo = __cfg.appendTo;
			this.$maskFlag = __cfg.mask != false;
			this.$model = __cfg.model;
			this.$drag = __cfg.drag;
			this.$onClose = __cfg.onClose;
			this.$color = __cfg.color;
			this.$zIndex = __cfg.zIndex || '9999';
		}
		this.$masking = null;
		this.$el = null;
		this.$render();
		this.$isClosed = false;
	};
	Infobox.prototype.show = function() {
		var me = this;
		if (this.$masking) {
			this.$masking.fadeIn('fast');
		}
		if (this.$el) {
			this.$el.fadeIn('fast', function(){
				me.$isClosed = false;
			});
		}
	};
	Infobox.prototype.hide = function(__callback) {
		if(!this.$isClosed){
			var me = this;
			if (this.$masking) {
				this.$masking.fadeOut('fast');
			}
			if (this.$el) {
				this.$el.fadeOut('fast' , function(){
						typeof __callback === 'function' && __callback();
						typeof me.$onClose === 'function' && me.$onClose();
						me.$isClosed = true;
					}
				);
			}
		}
	};
	Infobox.prototype.destory = function() {
		this.$masking && this.$masking.unbind('click', this.$masking.action);
		if (this.$masking) {
			!this.$model && this.$masking.unbind('click', this.$masking.action);
			this.$masking.hide(function() {
				$(this).detach();
			});
		}
		if (this.$el) {
			this.$el.unbind('click', this.$el.stopPropagation);
			this.$el.hide(function() {
				$(this).detach();
			});
		}
		for (var property in this) {
			if (this.hasOwnProperty(property)) {
				this[property] = null;
			}
		}
	};
	Infobox.prototype.$render = function() {
		var me = this;
		if(this.$maskFlag){
			this.$masking = $('<div class="masking"/>');
			if(this.$model){
				this.$masking.appendTo(document.body).hide();
			}else{
				this.$masking.action = function() {
					me.hide();
					if($(".offset-left-cicdatalist").length>0){
						$(".offset-left-cicdatalist").hide();
					}
				};
				this.$masking.bind("click", this.$masking.action).appendTo(document.body).hide();
			}
		}
		var css = {
			'position' : 'absolute',
			'left' : '0px',
			'top' : '0px',
			'display' : 'none',
			'z-index': this.$zIndex
		};
		if(this.$top || this.$left){
			css.top = this.$top;
			css.left = this.$left;
		}
		if(this.$width || this.$height){
			css.width = this.$width;
			css.height = this.$height;
		}
		if(this.$color){
			css.color = this.$color;
		}
		this.$el = $('<div/>').css(css);
		if(this.$html){
			this.$el.html(this.$html)
		}else if(this.$sel){
			this.$el.append(this.$sel);
		}
		this.$el.appendTo(this.$appendTo ? this.$appendTo : document.body).hide();
		if(!this.$appendTo){
			if(!this.$top && !this.$left){
				this.$el.css({
					top : '50%',
					left : '50%',
					margin : '-' + (this.$el.height() / 2) + "px 0px 0px -" +  (this.$el.width() / 2) + "px"
				});
			}
		}
		this.$el.stopPropagation = function(e) {
			e.stopPropagation();
			if($(".offset-left-cicdatalist").length>0){
				$(".offset-left-cicdatalist").hide();
			}
			/*if($(".modal-dialog.modal-sm").length>0){
				var dialog = $(".modal-dialog.modal-sm");
				for(var di=0;di<dialog.length;di++){
					if(!$(dialog[di]).is(":hidden")){
						
					}
				}
			}*/
		};
		this.$el.bind('click', this.$el.stopPropagation);
		if(this.$drag){
			this.$el.tacdrag({
				dragTarget : this.$el.find('.modal-title')
			});
		}
	};
	Infobox.prototype.isHide = function() {
		return this.$el.css('display') == 'none';
	};
	return Infobox;
});
