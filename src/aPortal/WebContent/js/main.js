require.config({
	waitSeconds: 0, 
	paths : {
		'jquery' : 'lib/jquery/jquery-3.1.1.min',
		'jquery.tmpl' : 'lib/jquery/plugins/jquery.tmpl.min',
		'jquery.mousewheel' : 'lib/jquery/plugins/jquery.mousewheel',
		'jquery.i18n.properties' : 'lib/jquery/plugins/jquery.i18n.properties.min',
		'jquert.cic.extend' : 'lib/jquery/plugins/tac/jquery.cic.extend',
		'jquery.tac.drag' : 'lib/jquery/plugins/tac/jquery.tac.drag',
		'jquery.cic.datalist' : 'lib/jquery/plugins/tac/jquery.cic.datalist',
		'tacCfg' : 'config/tacConfig',
		'director' : 'lib/director/director.min',
		'jquery.ui' : 'lib/jquery/plugins/jquery.ui',
		'jquery.ui.core' : 'lib/jquery/plugins/jquery.ui.core',
		'jquery.ui.widget' : 'lib/jquery/plugins/jquery.ui.widget',
		'jquery.ui.mouse' : 'lib/jquery/plugins/jquery.ui.mouse',
		'jquery.ui.sortable' : 'lib/jquery/plugins/jquery.ui.sortable',
		'bootstrap' : 'lib/bootstrap/bootstrap',
		'bootstrap-multiselect' : 'lib/bootstrap/bootstrap-multiselect',
		'retcode' : 'retcode',
		'loginmode' : 'loginmode'
	},
	shim : {
		//使css样式依赖于jquery,页面加载时使用原有css，此处进行重新引入。修复css有增加时,新样式不切换
		'jquery.tmpl' : ['jquery',
		                 'css!../css/bootstrap.css',
		                 'css!../css/font-awesome.css',
		                 'css!../css/style.css',
		                 'css!../css/awesome-bootstrap-checkbox.css',
		                 'css!../css/common.css',
		                 'css!../css/bootstrap-multiselect.css'],                
		'jquery.ui' : ['jquery'],
		'jquery.ui.core' : ['jquery'],
		'jquery.ui.widget' : ['jquery','jquery.ui'],		
		'jquery.ui.mouse' : ['jquery','jquery.ui'],
		'jquery.ui.sortable' : ['jquery','jquery.ui'],
		'jquery.mousewheel' : ['jquery'],
		'jquert.cic.extend' : ['jquery'],
		'jquery.tac.drag' : ['jquery'],
		'jquery.cic.datalist' : ['jquery'],
		'bootstrap' : ['jquery'],
		'jquery.i18n' : ['jquery'],
     	'jquery.i18n.properties' : ['jquery']
	}
});
require([	
         	'jquery.ui',
         	'jquery.ui.core',
         	'jquery.ui.widget',
         	'jquery.ui.mouse',
         	'jquery.ui.sortable',
		 	'jquery.tmpl',
			'jquery.mousewheel',
		 	'jquery.i18n.properties',
			'jquert.cic.extend',
			'jquery.tac.drag',
			'jquery.cic.datalist',
			'director',
			'bootstrap',
			'retcode',
			'loginmode'
		], function(){
	require(['index', 'bootstrap-multiselect'], function(index, multiselect) {
		if(!LoginMode.loginByAccount)
		{
			$("#home_agentid").show();
			$("#home_agentid_tip").show();
			$("#home_phonenum").show();
			$("#home_phonenum_tip").show();
			$("#home_account").hide();
			$("#home_account_tip").hide();
			$("#home_login_body").height(230);
		}
		else 
		{
			$("#home_agentid").hide();
			$("#home_agentid_tip").hide();
			$("#home_phonenum").hide();
			$("#home_phonenum_tip").hide();
			$("#home_account").show();
			$("#home_account_tip").show();
			$("#home_login_body").height(200);
		}
		index.init();
		initTup();
	});
});
