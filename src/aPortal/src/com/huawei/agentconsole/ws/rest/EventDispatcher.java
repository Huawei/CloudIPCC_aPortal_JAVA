
package com.huawei.agentconsole.ws.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.common.config.ConfigList;
import com.huawei.agentconsole.common.config.ConfigProperties;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.ws.param.EventParam;

/**
 * 
 * <p>
 * Title: 轮训获取事件
 * </p>
 * <p>
 * Description:
 * </p>
 * 
 * <pre></pre>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company: Huawei Technologies Co.
 * </p>
 * 
 * @author j00204006
 * @version V1.0 2018年7月25日
 * @since
 */
@Path("/agentevent")
public class EventDispatcher
{
    private static final Logger LOG = LoggerFactory.getLogger(EventDispatcher.class);

    @GET
    @Path("/event")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public EventParam getAgentEvent(@QueryParam("agentId") String agentId)
    {

        EventParam eventParam = agentEventWaitProcess(agentId);
        if (null == eventParam)
        {
            eventParam = new EventParam();
            eventParam.setRetcode(AgentErrorCode.SUCCESS);
        }
        else
        {
            LOG.info(LogUtils.METHOD_OUT, 
                    LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(eventParam));
        }
        return eventParam;
    }

    private EventParam agentEventWaitProcess(String agentId)
    {
        AgentBaseInfoBean agentBaseInfoBean = GlobalObject
                .getAgentBaseInfo(agentId);
        if (null == agentBaseInfoBean)
        {
            // 座席已经签出
            return null;
        }
        agentBaseInfoBean.setLastCallTimes(0);
        String agentPollWaitTimes = ConfigProperties.getKey(ConfigList.BASIC,
                "EVENT_POLL_WAIT_TIMES");
        String agentPollWaitInterval = ConfigProperties.getKey(ConfigList.BASIC,
                "EVENT_POLL_WAIT_INTERVAL");

        try
        {
            int int_agentPollWaitTimes = Integer.parseInt(agentPollWaitTimes);
            for (int i = 0; i < int_agentPollWaitTimes; i++)
            {

                if (agentBaseInfoBean.getEventQueue().isEmpty())
                {
                    long sleepTime = new Long(agentPollWaitInterval)
                            .longValue();
                    if (sleepTime >= CommonConstant.EVENT_POLL_WAIT_INTERVAL_MIN
                            && sleepTime <= CommonConstant.EVENT_POLL_WAIT_INTERVAL_MAX)
                    {
                        Thread.sleep(sleepTime);
                    }
                    else
                    {
                        throw new Exception("Invalid sleep duration");
                    }
                }
                else
                {
                    return agentBaseInfoBean.getEventQueue().poll();
                }
            }
        }
        catch (RuntimeException e)
        {
            LOG.error("It has an error when wait the agent event. error {}",
                    LogUtils.encodeForLog(e.getMessage()));
        }
        catch (Exception e)
        {
            LOG.error("It has an error when wait the agent event. error {}",
                    LogUtils.encodeForLog(e.getMessage()));
        }
        return null;
    }

}
