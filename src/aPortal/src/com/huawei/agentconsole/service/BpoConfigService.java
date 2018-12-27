
package com.huawei.agentconsole.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.bean.DBOperResult;
import com.huawei.agentconsole.common.config.ConfigList;
import com.huawei.agentconsole.common.config.ConfigProperties;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.dao.service.saas.BpoConfigDaoService;
import com.huawei.agentconsole.ws.param.RestResponse;

public class BpoConfigService
{
    private static final Logger LOG = LoggerFactory.getLogger(OnlineAgentService.class);
    
    private String agentId;
    
    private AgentBaseInfoBean agentBaseInfoBean;
    
    
    public BpoConfigService(String agentId)
    {
        this.agentId = agentId;
        agentBaseInfoBean = GlobalObject.getAgentBaseInfo(agentId);
    }
    
    
    /**
     * 获取试用被叫号码
     * @return
     */
    public RestResponse getTrialCalledNumber()
    {
        int ccId =  Integer.valueOf(ConfigProperties.getKey(ConfigList.BASIC, "CALLCENTER_ID"));
        BpoConfigDaoService daoService = new BpoConfigDaoService(agentId, ccId, Integer.valueOf(agentBaseInfoBean.getVdnId()));
        DBOperResult<List<String>> calledListResult = daoService.getTrialCalledList();
        RestResponse restResponse = new RestResponse();
        if (calledListResult.isSucess())
        {
            restResponse.setReturnCode(AgentErrorCode.SUCCESS);
            restResponse.setRetObject("result", calledListResult.getResult());
            agentBaseInfoBean.setTrialCalledList(calledListResult.getResult());
        }
        else
        {
            restResponse.setReturnCode(AgentErrorCode.SYS_DATABASE_ERROR);
            LOG.error(LogUtils.AGENT_ID + "database error when getTrialCalledList", LogUtils.encodeForLog(agentId)); 
        }
        return restResponse;
    }
}
