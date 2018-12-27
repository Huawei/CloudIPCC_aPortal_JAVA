define(['text!../../../html/component/vmenu/vmenu.html'], function(html) {
	var Menu = function(__cfg) {
		this.$el = null;
		if (__cfg) {
			this.$id = __cfg.id || '';
			this.$top = __cfg.top || 0;
			this.$left = __cfg.left || 0;
			this.$border = __cfg.border || '';
			this.$item = __cfg.item;
			this.$animation = __cfg.animation != false;
			this.$onDestroy = __cfg.onDestroy;
			this.$fixed = __cfg.fixed;
		}
		this.$masking = null;
		this.$render();
	};
	Menu.prototype.show = function() {
		var top = parseInt(this.$el.css('top'), 10);
		var left = parseInt(this.$el.css('left'), 10);
//		if (left + this.$el.width() > $(window).width() - 20) {
//			this.$el.css('left', left - this.$el.width() - 10);
//		}
//		if (top + this.$el.height() > $(window).height() - 20) {
//			this.$el.css('top', top - this.$el.height());
//		}
		if (this.$animation) {
			this.$el.show('blind', null, 300);
		} else {
			this.$el.show();
		}

	};
	Menu.prototype.hide = function() {
		if (this.$item && Object.prototype.toString.call(this.$item) == '[object Array]') {
			for (var i = 0; i < this.$item.length; i++) {
				this.$item[i]['element'].unbind('click', this.$item[i]['onClick']);
			}
		}
		if (this.$masking) {
			this.$masking.unbind('click', this.$masking.action);
			this.$masking.fadeOut('fast', function() {
				$(this).detach();
			});
		}
		if (this.$el) {
			this.$el.unbind('click', this.$el.stopPropagation);
			if (this.$animation) {
				this.$el.hide({
					duration : 300,
					complete : function(){
						$(this).detach();
						typeof this.$onDestroy === 'function' && this.$onDestroy();
					}
				});
			} else {
				this.$el.detach();
				this.$masking.detach();
				typeof this.$onDestroy === 'function' && this.$onDestroy();
			}
		}
		for (var property in this) {
			if (this.hasOwnProperty(property)) {
				this[property] = null;
			}
		}
	};
	Menu.prototype.getItem = function(__index) {
		var element = null;
		if (!isNaN(__index) && this.$item[__index]) {
			element = this.$item[__index].element;
		}
		return element;
	};
	Menu.prototype.$render = function() {
		var me = this;
		this.$masking = $('<div class="masking" style="opacity:0;filter:(alpha(opacity=0));"/>');
		this.$masking.action = function() {
			me.hide();
		};
		this.$masking.bind("click", this.$masking.action).appendTo(document.body);
		this.$el = $.tmpl(html, null).css({
			'position' : this.$fixed ? 'fixed' : 'absolute',
			'left' : this.$left,
			'top' : this.$top,
			'display' : 'none'
		}).appendTo(document.body);
		if(this.$id){
			this.$el.attr('id' , this.$id);
		}
		this.$el.stopPropagation = function(e) {
			e.stopPropagation();
		};
		this.$el.bind('click', this.$el.stopPropagation);
		if (this.$item && Object.prototype.toString.call(this.$item) == '[object Array]') {
			var menuArea = this.$el.find('.component-menu');
			for (var i = 0; i < this.$item.length; i++) {
				(function(__i) {
					var action = me.$item[__i]['onClick'];
					me.$item[__i]['onClick'] = function(e) {
						typeof action === 'function' && action(e);
						e.stopPropagation();
						me.hide();
					}
					menuArea.append(me.$item[__i]['element'] = $('<li/>').html(me.$item[__i]['text']));
					if (me.$item[__i].disabled === true) {
						me.$item[__i]['element'].addClass('disabled');
					} else {
						me.$item[__i]['element'].bind('click', me.$item[__i]['onClick'])
					}
				})(i);
			}
			if (this.$border) {
				if (Object.prototype.toString.call(this.$border) == '[object String]') {
					menuArea.css('border', this.$border);
				} else {
					for (var border_attr in this.$border) {
						this.$border[border_attr] && menuArea.css('border-' + border_attr, this.$border[border_attr]);
					}
				}
			}
			menuArea.find('li').hover(function() {
				!$(this).hasClass('disabled') && $(this).addClass("hover");
			}, function() {
				!$(this).hasClass('disabled') && $(this).removeClass("hover");
			});
		}
	};
	return Menu;
});
