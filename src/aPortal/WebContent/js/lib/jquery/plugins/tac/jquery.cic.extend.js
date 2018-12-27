/*
	 *	扩展jqery的方法 
	 */
	jQuery.fn.extend({
		check: function() {
		  return this.each(function() { this.checked = true; });
		},
		uncheck: function() {
		  return this.each(function() { this.checked = false; });
		}
	});