package com.huawei.agentconsole.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentConfigureInfoBean;
import com.huawei.agentconsole.bean.DBOperResult;
import com.huawei.agentconsole.common.config.ConfigList;
import com.huawei.agentconsole.common.config.ConfigProperties;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.dao.service.AgentConfigureDaoService;
import com.huawei.agentconsole.ws.param.AgentConfigureParam;
import com.huawei.agentconsole.ws.param.RestResponse;
import com.huawei.agentconsole.bean.AgentBaseInfoBean;

public class AgentConfigureService
{
    private static final Logger LOG = LoggerFactory.getLogger(OnlineAgentService.class);
    
    private AgentConfigureDaoService agentConfigureDaoService;
    
    private AgentBaseInfoBean agentBaseInfoBean;
    
    private String agentId;
    
    public AgentConfigureService(String agentId)
    {
        this.agentId = agentId;
        this.agentBaseInfoBean = GlobalObject.getAgentBaseInfo(agentId);
        agentConfigureDaoService = new AgentConfigureDaoService(agentId, 
                Integer.valueOf(ConfigProperties.getKey(ConfigList.BASIC, "CALLCENTER_ID")),
                Integer.valueOf(agentBaseInfoBean.getVdnId()));
    }
    
    /**
     * 设置座席个人登录配置
     * @param agentId
     */
   public Map<String, Object> setAgentConfiguration(AgentConfigureParam agentConfigureParam)
   {
       RestResponse restResponse = new RestResponse();
       //判断是否启用数据库
       if (!ConfigProperties.getKey(ConfigList.BASIC, "EBABLE_SYSDB").equals("true"))
       {
           restResponse.setReturnCode(AgentErrorCode.SUCCESS);
           return restResponse.returnResult();
       }
       //更新数据库
       DBOperResult<Integer> dbOperResult = agentConfigureDaoService.setAgentConfigure(agentConfigureParam);      
       if (dbOperResult.isSucess())
       {
           restResponse.setReturnCode(AgentErrorCode.SUCCESS);
           Map<String, Object> result = new HashMap<String, Object>();
           result.put("dbOperResult", dbOperResult.getResult());
           restResponse.setRetObject("result", result);
       }
       else
       {
           LOG.error(LogUtils.AGENT_ID  + " setAgentConfiguration database error. ", LogUtils.encodeForLog(agentId));
       }
       return restResponse.returnResult();
   }
    
    /**
     * 获取坐席登录配置
     * @param 
     */
    public Map<String, Object> getAgentConfiguration(String agentId) 
    {
        DBOperResult<Map<String, AgentConfigureInfoBean>> dbOperResult = agentConfigureDaoService.queryAgentConfigure();
        RestResponse restResponse = new RestResponse();
        if (dbOperResult.isSucess())
        {
            restResponse.setReturnCode(AgentErrorCode.SUCCESS);
            restResponse.setRetObject("result", dbOperResult.getResult());
        }
        else
        {
            LOG.error(LogUtils.AGENT_ID  + " getAgentConfiguration database error. ", LogUtils.encodeForLog(agentId));
        }
        return restResponse.returnResult();
        
        
    }
}
