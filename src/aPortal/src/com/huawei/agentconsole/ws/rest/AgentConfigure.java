package com.huawei.agentconsole.ws.rest;

/**
 * 
 * <p>Title: 座席配置读取和保存  </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author l00467145
 * @version V1.0 2018年11月1日
 * @since
 */

import java.util.Map;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.service.AgentConfigureService;
import com.huawei.agentconsole.ws.param.AgentConfigureParam;



@Path("/agentconfigure")
public class AgentConfigure
{
    private static final Logger LOG = LoggerFactory.getLogger(OnlineAgent.class);
    
    /**
     * 查询座席个人配置
     * @param agentId
     * @param agentConfigureParam
     * @return 
     */
    @GET
    @Path("/configure")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getAgentConfigure(
            @QueryParam("agentId") String agentId)
    {
        AgentConfigureService service = new AgentConfigureService(agentId);
        Map<String,Object> result = service.getAgentConfiguration(agentId);
        return result;
    }
    
    
    /**
     * 座席个人配置数据库保存
     * @param agentId
     * @param agentConfigureParam
     * @return 
     */
    @PUT
    @Path("/configure")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> setAgentConfigure(
            @QueryParam("agentId") String agentId,
            @Valid AgentConfigureParam agentConfigureParam)
    {
        LOG.info(LogUtils.METHOD_IN + " AgentConfigureParam :{}",  LogUtils.encodeForLog(agentId), agentConfigureParam);
        AgentConfigureService service = new AgentConfigureService(agentId);
        Map<String,Object> result = service.setAgentConfiguration(agentConfigureParam);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
        return result;
    }
}
