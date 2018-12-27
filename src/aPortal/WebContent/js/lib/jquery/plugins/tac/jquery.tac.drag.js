define(function(){
		$.fn.tacdrag = function(__options){
			if(__options && __options.dragTarget){
				var mT = this;
				var dT = __options.dragTarget;
				var startMove = false;
				var clicked = false;
		        var mausx = "0";
		        var mausy = "0";
		        var winx = "0";
		        var winy = "0";
		        var win_w=0;
		        var win_h=0;
		        var min_left=0;
		        var max_left=0;
		        
		        var min_top = 0;
		        var max_top = 0;
		        
		        var difx = mausx - winx;
		        var dify = mausy - winy;
				var mousemove = function (event) {
		            mausx = event.pageX;
		            mausy = event.pageY;
		            winx = mT.offset().left;
		            winy = mT.offset().top;
		            
		            win_w = mT.width();
		            win_h = mT.height();
		            
		            min_left = win_w/2;
		            max_left = $(document).width()-min_left;
		            
		            min_top = win_h/2;
		            max_top = $(document). height()-min_top;
		            
		            
		            if (!clicked || !startMove) {
		                difx = mausx - winx;
		                dify = mausy - winy;
		            }
		            var newx = event.pageX - difx - mT.css("marginLeft").replace('px', '');
		            var newy = event.pageY - dify - mT.css("marginTop").replace('px', '');
		            
		            if(newx<min_left)newx = min_left;
		            if(newx>max_left)newx = max_left;
		            
		            if(newy<min_top)newy=min_top;
		            if(newy>max_top)newy=max_top;
		            
		            
					if(startMove){
						mT.css({ top: newy, left: newx });
					}else{
						startMove = true;
					}
		        };
				dT.mousedown(function (event) {
            		clicked = true;
					dT.css('cursor' , 'move');
					$("html").bind("mousemove" , mousemove);
        		});
				$("html").mouseup(function (event) {
					startMove = false;
            		clicked = false;
					dT.css('cursor' , 'default');
					$("html").unbind("mousemove" , mousemove);
       			});
			}
			return this;
		};
});