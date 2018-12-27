
package com.huawei.agentconsole.common.constant;

/**
 * 
 * <p>Title: 常量配置 </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年7月24日
 * @since
 */
public interface CommonConstant
{
    String UTF_8 = "utf-8";
    
    /**
     * 电话号码长度最大24个字符
     */
    int MAX_PHONE_NUMBER = 24;
    
    /**
     * 密码最小长度为8个字符
     */
    int MIN_PASSWORD_NUMBER = 8;
    
    /**
     * 密码最大长度32个字符
     */
    int MAX_PASSWORD_NUMBER = 32;
    
    /**
     * 电话最小长度为5个字符
     */
    int MIN_PHONE_NUMBER = 5;
    
    /**
     * 登录后状态为工作态
     */
    int WORK_STATUS_AFTER_LOGIN = 5;
    
    /**
     * 设备ID地址长度最大24个字符
     */
    int MAX_ADDRESS_NUMBER = 24;
    
    /**
     * 通话结束后，进入工作态
     */
    boolean IS_IDLE_AFTER_TALKING = false;
    
    String HTTPS = "https";
    
    String COOKIE_PORTAL_TOKEN_NAME = "JSESSIONID";
    
    String COOKIE_SERVER_IP = "AgentConsoleIP";
    
    /**
     * 数据库类型为sqlserver
     */
    String DB_TYPE_SQLSERVER = "sqlserver";
    
    /**
     * 数据库类型为oracle
     */
    String DB_TYPE_ORACLE = "oracle";
    /**
     * 数据库类型为mysql
     */
    String DB_TYPE_MYSQL = "mysql";
    
    /**
     * 数据库类型为db2
     */
    String DB_TYPE_DB2 = "db2";
    
    
    /**
     * oracle驱动字符串
     */
    String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver"; 
    
    /**
     * sqlsever驱动
     */
    String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    
    /**
     * db2驱动
     */
    String DB2_DRIVER = "com.ibm.db2.jcc.DB2Driver";    
    
    /**
     * mysql驱动字符串
     */
    String MYSQL_DRIVER = "com.mysql.jdbc.Driver"; 
    
    /**
     * cluster id
     */
    String CLUSTER_ID = "CLUSTER_ID"; 
    
    /**
     * 服务器状态更新间隔 10s
     */
    int SERVER_STATUS_UPDATE_INTERVAL = 10000;

    
    String RETURNCODE_SUCCESS = "0";
    
    String RETURNCODE_FAIL = "1";
    
    String RETURNDESC_SUCCESS = "SUCCESS";
    
    String RETURNDESC_FAIL = "FAIL";
    
    /**
     * 文字质检
     */
    String MEDIA_TYPE_TEXT = "1";
    
    /**
     * 语音质检
     */
    String MEDIA_TYPE_VOICE = "5";
    
    /**
     * call center id 
     */
    String CCID = "CALLCENTER_ID";
    
    /**
     * vdn 用户名
     */
    String VDNUSERNAME = "VDN_USERNAME";
    
    /**
     * 周报表类型
     */
    int REPROTTYPE_WEEK = 4;
    
    /**
     * 月报表类型
     */
    int REPROTTYPE_MONTH = 3;
    
    /**
     * 日报表类型
     */
    int REPROTTYPE_DAY = 2;
    
    /**
     * 报表TimeUnit
     */
    int REPORT_TIMEUNIT = 0;
    
    /**
     * 报表语言类型为英文
     */
    int LANGUAGE_ENGLISH = 1;
    
    /**
     * 报表语言类型为中文
     */
    int LANGUAGE_CHINESE = 0;
    
    /**
     * 报表查询最大时间范围（单位：天）
     */
    int REPORT_MAX_INTERVAL_DAYS = 91;    

    
    /**
     * 话务员外呼报表的查询类型  
     */
    int REPORT_AGENT_CALLOUT_BREIF_QUERYTYPE = 0;
    
    int MAX_RECORDID_LENGTH = 129; 
    
    /**
     * EVENT_POLL_WAIT_INTERVAL 最小值
     */
    long EVENT_POLL_WAIT_INTERVAL_MIN = 0;
    
    /**
     * EVENT_POLL_WAIT_INTERVAL 最大值
     */
    long EVENT_POLL_WAIT_INTERVAL_MAX = 10000;
    
    /**
     * 录音转换临时目录的文件夹名称
     */
    String TEMPFILE_FOLDER_NAME = "tempfile";

    
    String IS_NEED_VERIFY = "_needVerifyCode";

    
    /**
     * 访问报表的rest接口路径中带的关键字
     */
    String REPORT_REST_PATH = "/report/";
    
    /**
     * vdn话务量报表模板相对路径
     */
    String TEMPLATE_VDNTRAFFIC_PATH = "/WEB-INF/template/VDNTRAFFIC.xls";
    
    String TEMPLATE_AGENTTRAFFIC_PATH = "/WEB-INF/template/AGENTTRAFFIC.xls";
    
    String TEMPLATE_AGENTWORK_PATH = "/WEB-INF/template/AGENTWORK.xls";
    
    String TEMPLATE_SKILLTRAFFIC_PATH = "/WEB-INF/template/SKILLTRAFFIC.xls";

    String TEMPLATE_SKILLTRAFFICBYSKILL_PATH = "/WEB-INF/template/SKILLTRAFFICBYSKILL.xls";
    
    String TEMPLATE_AGENTOUTBRIEF_PATH = "/WEB-INF/template/AGENTOUTBRIEF.xls";
    /**
     * 转换临时excel文件的目录
     */
    String TEMPFILE_EXCEL_PATH = "tempfile/excel";
    /**
     * 转换临时录音文件的目录
     */
    String TEMPFILE_RECORD_PATH = "tempfile/record";
    
    String TEMPFOLDER_RECORD_PATH = "record";
    
    String TEMPFOLDER_EXCEL_PATH = "excel";
    
    String EXCEL_NAME_VDNTRAFFIC = "VDNTRAFFIC";
    
    String EXCEL_NAME_AGENTTRAFFIC = "AGENTTRAFFIC";
    
    String EXCEL_NAME_AGENTWORK = "AGENTWORK";
    
    String EXCEL_NAME_SKILLTRAFFIC = "SKILLTRAFFIC";
    
    String EXCEL_NAME_SKILLTRAFFICBYSKILL = "SKILLTRAFFICBYSKILL";
    
    String EXCEL_NAME_AGENTOUTBRIEF = "AGENTOUTBRIEF";
    
    String EXCEL_SUFFIX = ".xls";

    /**
     * 为不同的报表编号，用于导出Excel时候判断
     */
    int REPORTNAME_VDNTRAFFIC = 1;
    
    int REPORTNAME_SKILLTRAFFIC = 2;
    
    int REPORTNAME_SKILLTRAFFICBYSKILL = 3;
    
    int REPORTNAME_AGENTTRAFFIC = 4;
    
    int REPORTNAME_AGENTTRAFFICBYSKILL = 5;
    
    int REPORTNAME_AGENTWORK = 6;
    
    int REPORTNAME_AGENTCALLOUTBRIEF = 7;
    
    /**
     * 账户最大长度
     */
    int MAX_ACCOUNT_LENGTH = 32;
    
    /**
     * 账号最小长度
     */
    int MIN_ACCOUNT_LENGTH = 5;

    /**
     * 试用租户
     */
    int TRIAL_TENANT = 1;

    
    /**
     * 座席个人配置的key
     */
    String PROPKEY_AUTOANSWER = "isAutoAnswer";
    
    String PROPKEY_MAXWORKTIME = "maxWorkTime";
    
    String PROPKEY_OUTCALLERNO = "outCallerNo";
    
    /**
     * 日期格式化
     */
    String DATE_YYYY_MM_DD = "yyyy-MM-dd";
    
    /**
     * 日期格式化
     */
    String DATE_FULL = "yyyy-MM-dd HH:mm:ss";
}
