
package com.huawei.agentconsole.ws.rest;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;
import com.huawei.agentconsole.service.AgentGroupService;
import com.huawei.agentconsole.service.ReportService;

/**
 * 
 * <p>Title: 座席信息查询 </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年7月31日
 * @since
 */
@Path("/agentgroup")
public class AgentGroup
{
    
    private static final Logger LOG = LoggerFactory.getLogger(AgentGroup.class);
    
    
    /**
     * 查询空闲在线座席
     * @param agentId
     * @return
     */
    @GET
    @Path("/allidleagent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Map<String, Object> getAllIdleAgent(@QueryParam("agentId")String agentId)
    {
        Map<String, Object> resultMap = new AgentGroupService(agentId).getAllIdleAgents();
        if (!AgentErrorCode.SUCCESS.equals(StringUtils.getRetCode(resultMap)))
        {
            LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(resultMap));  
        }
        return resultMap;
    }
    
    
    /**
     * 查询指定VDN下的所有坐席信息
     * @param agentId
     * @return
     */
    @GET
    @Path("/allagent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Map<String, Object> getAllAgentOnVdn(@QueryParam("agentId")String agentId){
        
        Map<String, Object> resultMap = new ReportService(agentId).getAllAgents();
        if (!AgentErrorCode.SUCCESS.equals(StringUtils.getRetCode(resultMap)))
        {
            LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(resultMap));  
        }
        return resultMap;
        
    }

}
