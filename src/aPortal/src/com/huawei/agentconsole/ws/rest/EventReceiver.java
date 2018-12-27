
package com.huawei.agentconsole.ws.rest;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;
import com.huawei.agentconsole.ws.param.EventParam;

/**
 * 
 * <p>Title:  接收推送的事件</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年7月24日
 * @since
 */
@Path("/eventreceiver")
public class EventReceiver
{
    private static final Logger LOG = LoggerFactory.getLogger(EventReceiver.class);
  
    @POST
    @Path("/event")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> event(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @QueryParam("token") String token,
            @Valid EventParam eventParam)
    {
        //1. 根据token获取对应的座席工号
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("retcode", 0);
        AgentBaseInfoBean agentBaseInfoBean;
        String agentId = GlobalObject.getPushUrlToken(token);
        if (StringUtils.isNullOrEmpty(agentId))
        {
            if (null != eventParam.getEvent())
            {
                agentId = eventParam.getEvent().getWorkNo();
            }
            agentBaseInfoBean = GlobalObject.getAgentInfoFromTempByToken(token);
            if (null == agentBaseInfoBean)
            {
                LOG.error(LogUtils.METHOD_OUT + " . It's not dealed, Because no agentAuthInfoBean", 
                        LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(eventParam));
                return resultMap;
            }
            agentId = agentBaseInfoBean.getAgentId();
            if (null == agentId)
            {
                agentId = eventParam.getEvent().getWorkNo();
            }
            
        }
        else
        {
            agentBaseInfoBean = GlobalObject.getAgentBaseInfo(agentId);
            if (null == agentBaseInfoBean)
            {
                LOG.error(LogUtils.METHOD_OUT + " . It's not dealed. Because no agentAuthInfoBean", 
                        LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(eventParam));
                
                return resultMap;
            }
        }
        LOG.info(LogUtils.METHOD_OUT, 
                LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(eventParam));
        agentBaseInfoBean.getEventQueue().add(eventParam);
        return resultMap;
    }
    
}
