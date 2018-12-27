
package com.huawei.agentconsole.ws.rest;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.service.RecordService;
import com.huawei.agentconsole.ws.param.RestResponse;
import com.huawei.agentconsole.ws.param.SearchRecordParam;
/**
 * 
 * <p>Title:录音相关的接口  </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author l00357702
 * @version V1.0 2018年8月9日
 * @since
 */
@Path("/record")
public class Record
{

    private static final Logger LOG = LoggerFactory.getLogger(Record.class);
    
    /**
     * 查询录音接口
     * @param agentId
     * @param param
     * @return
     */
    @POST
    @Path("/recordinfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String,Object> searchRecordinfo(
            @QueryParam("agentId") String agentId,
            @Valid SearchRecordParam param
            )
    {
        //为避免多次查询日志打印过多，此处将日志级别设置为debug级别
        RecordService service = new RecordService(agentId);        
        AgentBaseInfoBean bean = GlobalObject.getAgentBaseInfo(agentId);
        if (null != bean)
        {
            param.getRequestParam().setVdnId(bean.getVdnId());
            
            if (!bean.isCensor() && !param.getRequestParam().getAgentId().equals(agentId))
            {
                LOG.error(LogUtils.AGENT_ID + "the agent is not censor! he just can see his own recoed!", 
                        LogUtils.encodeForLog(agentId));
                return null;
            }
        }
        else
        {
            return null;
        }
        return service.queryRecordInfo(param.getPage(),param.getRequestParam());
    }    
    
    /**
     * 生成录音接口
     * @param agentId
     * @param param
     * @return
     */
    @POST
    @Path("/recordfilepath")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String,Object> generateRecordFile(@QueryParam("agentId") String agentId, 
            @QueryParam("recordId") String recordId)
    {
        LOG.info(LogUtils.METHOD_IN + "recordId:{}", 
                LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(recordId));
       
       RecordService service =  new RecordService(agentId);
       RestResponse response = service.genTempRecordFile(recordId);
       LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId));
       return response.returnResult();
    }
    
    /**
     * 下载录音接口
     * @param response
     * @param agentId
     * @param recordId
     */
    @GET
    @Path("/recordfile")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @NoCache
    public void getRecord(@Context HttpServletRequest request,  @Context HttpServletResponse response,
            @QueryParam("agentId") String agentId,  @QueryParam("desFilePath") String desFilePath)
    {  
        LOG.info(LogUtils.METHOD_IN + "recordId:{}", 
                 LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(desFilePath));        
        RecordService service =  new RecordService(agentId);
        service.downloadRecordTempFile(desFilePath, request, response);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId));
    }
    
}
