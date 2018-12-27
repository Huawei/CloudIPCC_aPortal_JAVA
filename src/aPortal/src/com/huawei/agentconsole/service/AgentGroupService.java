
package com.huawei.agentconsole.service;

import java.util.Map;

import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.http.AgentRequest;

public class AgentGroupService
{
    private String agentId;
    
    public AgentGroupService(String agentId)
    {
        this.agentId = agentId;
    }
    
    public Map<String, Object> getAllIdleAgents()
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/agentgroup/").append(agentId).append("/idleagent");
        return AgentRequest.get(agentId, url.toString());
    }
    
    /**
     * 查询当前坐席所在VDN下的所有坐席信息
     * @return
     */
    public Map<String, Object> getAllAgents()
    {        
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/agentgroup/").append(agentId).append("/allagentstatus");
        return AgentRequest.get(agentId, url.toString());
    }

}
