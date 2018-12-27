
package com.huawei.agentconsole.common.global;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;

public class GlobalObject
{
    /**
     * 座席基本信息集合
     * key : 座席工号 (work no)
     * value ： 鉴权信息  (auth info)
     */
    private static final Map<String, AgentBaseInfoBean> AGENT_BASE_INFO_MAP = new ConcurrentHashMap<String, AgentBaseInfoBean>();
    
    /**
     * key为pushurl的token
     * value为座席工号
     */
    private static final Map<String, String> PUSH_URL_TOKEN = new ConcurrentHashMap<String, String>(); 
    
    
    /**
     *  为了防止登录过程中，收到agentserver推送的该座席事件，则先将座席信息对象先缓存
     *  key为pushurl的token
     */
    private static final Map<String, AgentBaseInfoBean> TEMP_AGENTINFO_BYLOGIN = new ConcurrentHashMap<String, AgentBaseInfoBean>();
    
    /**
     * 保存报表的基本信息  ccid  vdnusername
     */
    private static final Map<String ,String> VDNINFO = new ConcurrentHashMap<String, String>();    
    
    /**
     * AgentServer的接口地址
     */
    private static String agentServerUrl;
    
    /**
     * 接口订阅的地址
     */
    private static String eventPushUrl;
    
    /**
     * callcenter的id编号
     */
    private static String callcenterId;
    
    private static String app_path;

    /**
     * 新增座席鉴权信息 
     * @param agentId 座席工号 
     * @param agentBaseInfoBean 座席信息
     */
    public static void addAgentBaseInfo(String agentId, AgentBaseInfoBean agentBaseInfoBean)
    {
        delAgentBaseInfo(agentId);
        AGENT_BASE_INFO_MAP.put(agentId, agentBaseInfoBean);
        PUSH_URL_TOKEN.put(agentBaseInfoBean.getPushUrlToken(), agentId);
    }
    
    /**
     * 删除指定座席基本信息
     * @param agentId 座席工号
     */
    public static void delAgentBaseInfo(String agentId)
    {
        AgentBaseInfoBean agentBaseInfo = AGENT_BASE_INFO_MAP.remove(agentId);
        if (null != agentBaseInfo)
        {
            PUSH_URL_TOKEN.remove(agentBaseInfo.getPushUrlToken());
        }
    }
    
    /**
     * 新增vdn信息
     * @param key
     * @param value
     */
    public static void addVdnInfo(String key, String value)
    {
        VDNINFO.put(key, value);
    }
    
    /**
     * 获取vdn信息
     * @param key
     * @return
     */
    public static String getVdnInfo(String key)
    {
       return VDNINFO.get(key);
    }

    public static Map<String, AgentBaseInfoBean> getAllAgentBaseInfos()
    {
        return AGENT_BASE_INFO_MAP;
    }
    
    /**
     * 获取座席基本信息
     * @param agentId 座席工号 
     * @return
     */
    public static AgentBaseInfoBean getAgentBaseInfo(String agentId)
    {
        return AGENT_BASE_INFO_MAP.get(agentId);
    }
    
    public static void setAgentServerUrl(String agentServerUrl)
    {
        GlobalObject.agentServerUrl = agentServerUrl;
    }
    
    public static String getAgentServerUrl()
    {
        return agentServerUrl;
    }
    
    
    public static void setEventPushUrl(String eventPushUrl)
    
    {
        GlobalObject.eventPushUrl = eventPushUrl;
    }
    
    public static String getEventPushUrl()
    {
        return eventPushUrl;
    }

    public static String getPushUrlToken(String token)
    {
        return PUSH_URL_TOKEN.get(token);
    }
    
    /**
     * 收到登录结果前，先缓存push的token与座席信息的关系
     * @param pushUrlToken
     * @param agentBaseInfoBean
     */
    public static void addTempAgentInfoByLogin(String pushUrlToken, AgentBaseInfoBean agentBaseInfoBean) 
    {
        TEMP_AGENTINFO_BYLOGIN.put(pushUrlToken, agentBaseInfoBean);
    }
    
    /**
     * 收到登录结果后，从临时登录缓存中删除座席信息
     * @param pushUrlToken
     */
    public static void delTempAgentInfoByLogin(String pushUrlToken)
    {
        TEMP_AGENTINFO_BYLOGIN.remove(pushUrlToken);
    }
    
    /**
     * 从临时登录缓存中获取座席信息
     * @param pushUrlToken
     * @return
     */
    public static AgentBaseInfoBean getAgentInfoFromTempByToken(String pushUrlToken)
    {
        return TEMP_AGENTINFO_BYLOGIN.get(pushUrlToken);
    }

    public static String getCallcenterId()
    {
        return callcenterId;
    }

    public static void setCallcenterId(String callcenterId)
    {
        GlobalObject.callcenterId = callcenterId;
    }

    public static String getApp_path()
    {
        return app_path;
    }

    public static void setApp_path(String app_path)
    {
        GlobalObject.app_path = app_path;
    }

}
