
package com.huawei.agentconsole.common.constant;

public interface ParamPatternConstant
{
    /**
     * 座席工号的正则表达式
     */
    String WORKNO_PATTERN = "[1-9][\\d]{0,3}|[1-5][\\d]{4}";
    
    /**
     * 账号的正则表达式
     */
    String ACCOUNT_PATTERN = "^[a-zA-Z\\-][a-zA-Z0-9_\\-]{4,31}$";
    
    /**
     * 外呼的被叫号码表达式
     */
    String CALLED_PATTERN = "[\\d*#]{1,24}";
    
    /**
     * 主叫号码
     */
    String CALLER_PATTERN = "[\\d*#]{0,24}";
    
    /**
     * 电话号码字段
     */
    String PHONE_PATTERN = "[\\d]{1,24}";
    
    String DEVICETYPE_PATTERN = "[1-5]";
    
    String MODE_PATTERN = "[0-4]";
    
    String VDNID_PATTERN = "[1-512]";
    
    String REPORT_TIME_PATTERN = "([1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]))";
    
    int REPORT_MAX_SKILLS = 50;
    
    int REPORT_MAX_AGENTIDS = 50;
    
    String MONTHRTYPEREGEX = "[1-9]\\d{3}-(0[1-9]|1[0-2])";
    
    String WEEKORDAYTYPEREREGX = "[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])";
    
    String SKILLREGEX = "[1-9][\\d]{0,4}";;
    
    /**
     * CALLID表达式
     */
    String CALLID_PATTERN = "[\\d\\-]{1,24}";
    
    /**
     * 座席工号id
     */
    String AGENTID_PATTERN = "[1-9][\\d]{0,3}|[1-5][\\d]{4}";
    
    /**
     * 最大工作时长字段
     */
    String MAXWORKTIME_PATTERN = "[\\d]{0,4}";
    //-------------录音相关功能校验使用pattern-----------------
    //--------------------start-------------------------
    
    String RECORDQUERY_CALLERNO_PATTERN = "[0-9]{0,24}";
    
    String RECORDQUERY_CALLEDNO_PATTERN = "[0-9*#]{0,24}";
    
    String RECORDQUERY_AGENTID_PATTERN = "[1-9][0-9]{0,4}";
    
    String RECORDQUERY_TIME_PATTERN = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";
    
    String RECORDQUERY_PAGENO_PATTERN = "[1-9][0-9]{0,6}";
    
    String RECORDQUERY_PAGESIZE_PATTERN = "^(10|20|50)$";
}
