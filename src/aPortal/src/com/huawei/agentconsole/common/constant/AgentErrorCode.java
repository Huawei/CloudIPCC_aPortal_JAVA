
package com.huawei.agentconsole.common.constant;

public interface AgentErrorCode
{
    String SUCCESS = "0";
    
    /**
     * 网络异常
     */
    String NETWORK_ERROR = "-1";
    
    /**
     * 返回值异常
     */
    String RETURN_CONTENT_ERROR = "-2";
    
    /**
     * 请求消息中没有cookie
     */
    String NO_COOKIES = "-3";
    
    /**
     * 参数异常
     */
    String AGENT_REST_INVALID = "000-002";
    
    /**
     * 未登录
     */
    String AGENT_NOT_LOGIN = "100-006";
    
    /**
     * 鉴权信息不正确
     */
    String AGENT_REST_NORIGHT = "000-003";
    
    /**
     * 座席已经登录
     */
    String AGENT_HAS_LOGIN = "100-002";
    
    /**
     * 没有质检员权限
     */
    String AGENT_REST_NOQCRIGHT = "000-004";
    
    /**
     * 报表查询参数异常
     */
    String REPORT_REST_INVALID = "000-000-002";

    /**
     * sqlSessionFacotory未初始化
     */
    String REPORT_SQLSESSIONFACTORY_UNINIT = "000-000-003";

    /**
     * sql语句查询失败
     */
    String REPROT_QUERY_FAILED = "000-000-004";
    
    
    /**
     * 生成验证码失败
     */
    String CREATE_VERIFYCODE_FAILED = "000-000-005";
    
    /**
     * 验证码不正确
     */
    String VERIFYCODE_ISINVALID = "000-000-006";
    
    
    /**
     * 外呼时，主叫号码不正确
     */
    String CALLER_ISINVALID = "000-000-007";
    
    /**
     * mysql数据库异常
     */
    String SYS_DATABASE_ERROR = "000-000-008";
    
    /**
     * 外呼时，被叫号码不正确
     */
    String CALLED_ISINVALID = "000-000-009";
    
    /**
     * 拷贝报表文件失败
     */
    String REPORT_FILE_COPY_ERROR = "000-000-0010";
    
    /**
     * 生成录音临时文件失败
     */
    String GENERATE_RECORD_TEMPFILE_ERROR = "000-000-011";
}
