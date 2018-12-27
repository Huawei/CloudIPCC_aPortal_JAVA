
package com.huawei.agentconsole.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.http.AgentRequest;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;

/**
 * 
 * <p>Title: 进行定时心跳检测  </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年7月25日
 * @since
 */
public class HeartCheckTaskThread extends Thread
{
    private static final Logger LOG = LoggerFactory.getLogger(HeartCheckTaskDispatch.class);
    
    /**
     * 鉴权失败或者为登录的场景下检查2次。
     */
    private static final int CHECK_OUT_FOR_NORIGHTANDLOGIN = 2;
    
    /**
     * 其次失败场景检查5次
     */
    private static final int CHECK_OUT_FOR_OTHERREASON = 5;
    
    private String agentId;
    
    public HeartCheckTaskThread(String agentId)
    {
        this.agentId = agentId;
    }
    
    public void run()
    {
        heartCheck();
    }
    
    private void heartCheck()
    {
        AgentBaseInfoBean agentInfo = GlobalObject.getAgentBaseInfo(agentId);
        if (null == agentInfo)
        {
            LOG.error(LogUtils.AGENT_ID + "not logined", agentId);
            return;
        }
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/onlineagent/").append(agentId).append("/heartbeat");
        Map<String, Object> result = AgentRequest.put(agentId, url.toString(), null);
        String retCode = StringUtils.getRetCode(result);
        if (AgentErrorCode.SUCCESS.equals(retCode))
        {
            agentInfo.resetHeartTimes();
            return;
        }
        else
        {
            LOG.error(LogUtils.AGENT_ID + "check heart failed, the reason is {}", agentId, result);
            int currentTimes = agentInfo.addFailedHeartTimes();
            if (AgentErrorCode.AGENT_NOT_LOGIN.equals(retCode) || AgentErrorCode.AGENT_REST_NORIGHT.equals(retCode))
            {
                //鉴权失败或者为登录的场景下检查2次。
                if (CHECK_OUT_FOR_NORIGHTANDLOGIN <= currentTimes)
                {
                    GlobalObject.delAgentBaseInfo(agentId);
                }
            }
            else
            {
                //其次失败场景检查5次
                if (CHECK_OUT_FOR_OTHERREASON <= currentTimes)
                {
                    GlobalObject.delAgentBaseInfo(agentId);
                }
            }
        }
        
        
    }
}
