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

import org.jboss.resteasy.annotations.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.service.ReportService;
import com.huawei.agentconsole.ws.param.AgentCallOutBriefParam;
import com.huawei.agentconsole.ws.param.AgentTrafficParam;
import com.huawei.agentconsole.ws.param.AgentWorkParam;
import com.huawei.agentconsole.ws.param.RestResponse;
import com.huawei.agentconsole.ws.param.SkillTrafficParam;
import com.huawei.agentconsole.ws.param.VdnTrafficParam;

@Path("/report")
public class ReportQuery
{
    
    private static final Logger LOG = LoggerFactory.getLogger(ReportQuery.class);
    
    /**
     * VDN报表查询
     * @param vdnQueryParam VDN报表话务量查询参数
     * @return RestResponse 返回结果
     */
    @POST
    @Path("/vdntraffic")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> queryVdnTraffic(
            @QueryParam("agentId") String agentId,
            @Valid VdnTrafficParam vdnTrafficParam)
    {
        RestResponse result = new ReportService(agentId).queryVdnTraffic(vdnTrafficParam, agentId);
        return result.returnResult();
    }

    
    /**
     * 生成vdn话务量报表excel文件
     * @param vdnQueryParam VDN报表话务量查询参数
     * @return RestResponse 返回结果
     */
    @POST
    @Path("/vdntrafficfilepath")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> genVdnTrafficExcelFile(@QueryParam("agentId") String agentId,
            @Valid VdnTrafficParam vdnTrafficParam)
    {
        LOG.info(LogUtils.METHOD_IN + "vdnTrafficParam:{}", 
                LogUtils.encodeForLog(agentId), vdnTrafficParam);
        RestResponse result = new ReportService(agentId).genVdnTrafficExcelFile(agentId, vdnTrafficParam);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
        return result.returnResult();
    }
    
    /**
     * 下载已生成的vdn话务量报表excel文件
     * @param request
     * @param response
     * @param agentId
     * @param desFilePath 临时文件路径
     */
    @GET
    @Path("/vdntrafficfile")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void downloadVdnTrafficExcelFile( 
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @QueryParam("agentId") String agentId, @QueryParam("desFilePath") String desFilePath)
    {
        LOG.info(LogUtils.METHOD_IN + "desFilePath:{}", 
                LogUtils.encodeForLog(agentId), desFilePath);
        ReportService reportService = new ReportService(agentId);
        reportService.exportVdnTraffic(response, desFilePath);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId),"export excel finished");
    }
    
    /**
     * 生成技能话务量报表（按技能）excel文件
     * @param skillTrafficParam 话务量报表（按技能）查询参数
     * @return RestResponse 返回结果
     */
    @POST
    @Path("/skilltrafficbyskillfilepath")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> genSkillTrafficBySkillExcelFile(@QueryParam("agentId") String agentId,
            @Valid SkillTrafficParam skillTrafficParam)
    {
        LOG.info(LogUtils.METHOD_IN + "skillTrafficParam:{}", 
                LogUtils.encodeForLog(agentId), skillTrafficParam);
        RestResponse result = new ReportService(agentId).genSikillTrafficBySkillExcelFile(agentId, skillTrafficParam);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
        return result.returnResult();
    }
    
    /**
     * 下载已生成的技能话务量报表（按技能）excel文件
     * @param request
     * @param response
     * @param agentId
     * @param desFilePath 临时文件路径
     */
    @GET
    @Path("/skilltrafficbyskillfile")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void downloadSkillTrafficBySkillExcelFile( 
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @QueryParam("agentId") String agentId, @QueryParam("desFilePath") String desFilePath)
    {
        LOG.info(LogUtils.METHOD_IN + "desFilePath:{}", 
                LogUtils.encodeForLog(agentId), desFilePath);
        ReportService reportService = new ReportService(agentId);
        reportService.exportSikillTrafficBySkill(response, desFilePath);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId),"export excel finished");
    }
    
    
    
    /**
     * 技能队列话务量报表查询
     * @param skillTrafficParamParam 技能队列话务量报表查询参数
     * @return RestResponse 返回结果
     */
    @POST
    @Path("/skilltraffic")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> querySkillTraffic(
            @QueryParam("agentId") String agentId,
            @Valid SkillTrafficParam skillTrafficParamParam)
    {
        RestResponse result = new ReportService(agentId).querySkillTraffic(skillTrafficParamParam, agentId);
        return result.returnResult();
    }

    
    @POST
    @Path("/skilltrafficfile")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void exportSkillTraffic(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @QueryParam("agentId") String agentId,
            @Valid @Form  SkillTrafficParam skillTrafficParam)
    {
        LOG.info(LogUtils.METHOD_IN + "skillTrafficParam:{}", 
                LogUtils.encodeForLog(agentId), skillTrafficParam);
        ReportService reportService = new ReportService(agentId);
        reportService.exportSkillTraffic(request, response, agentId, skillTrafficParam);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId),"export excel finished");
    }
    
    
    /**
     * 话务员话务量报表查询
     * @param agentTrafficParam 话务员话务量报表查询参数
     * @return RestResponse 返回结果
     */
    @POST
    @Path("/agenttraffic")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> queryAgentTraffic(
            @QueryParam("agentId") String agentId,
            @Valid AgentTrafficParam agentTrafficParam)
    {
        RestResponse result = new ReportService(agentId).queryAgentTraffic(agentTrafficParam, agentId);
        return result.returnResult();
    }
    
    @POST
    @Path("agenttrafficfile")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void exportAgentTraffic(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @QueryParam("agentId") String agentId,
            @Valid @Form AgentTrafficParam agentTrafficParam)
    {
        LOG.info(LogUtils.METHOD_IN + "reportQueryAgentTrafficParam:{}", LogUtils.encodeForLog(agentId),
                LogUtils.encodeForLog(agentTrafficParam));
        ReportService reportService = new ReportService(agentId);
        reportService.exportAgentTraffic(request, response, agentId, agentTrafficParam);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId),"export excel finished");
    }
    
    
    
    /**
     * 话务员接续报表查询
     * @param agentTrafficParam 话务员接续报表查询参数
     * @return RestResponse 返回结果
     */
    @POST
    @Path("/agentwork")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> queryAgentWork(
            @QueryParam("agentId") String agentId,
            @Valid AgentWorkParam agentWorkParam)
    {
        RestResponse result = new ReportService(agentId).queryAgentWork(agentWorkParam, agentId);
        return result.returnResult();
    }
    
    @POST
    @Path("agentworkfile")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void exportAgentWork(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @QueryParam("agentId") String agentId,
            @Valid @Form AgentWorkParam agentWorkParam)
    {
        LOG.info(LogUtils.METHOD_IN + "agentWorkParam:{}", 
                LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(agentWorkParam));
        ReportService reportService = new ReportService(agentId);
        reportService.exportAgentWork(request, response, agentId, agentWorkParam);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId),"export excel finished");
    }
    
    
    /**
     * 话务员呼出报表查询
     * @param agentCallOutBriefParam 话务员呼出报表查询参数
     * @return RestResponse 返回结果
     */
    @POST
    @Path("/agentcalloutbrief")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> queryAgentCallOutBrief(
            @QueryParam("agentId") String agentId,
            @Valid AgentCallOutBriefParam agentCallOutBriefParam)
    {
        RestResponse result = new ReportService(agentId).queryAgentCallOutBrief(agentCallOutBriefParam, agentId);
        return result.returnResult();
    }
    
    @POST
    @Path("agentoutbrieffile")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void exportAgentCallOutBrief(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @QueryParam("agentId") String agentId,
            @Valid @Form AgentCallOutBriefParam agentCallOutBriefParam)
    {
        LOG.info(LogUtils.METHOD_IN + "agentCallOutBriefParam:{}", LogUtils.encodeForLog(agentId), 
                LogUtils.encodeForLog(agentCallOutBriefParam));
        ReportService reportService = new ReportService(agentId);
        reportService.exportAgentCallOutBrief(request, response, agentId, agentCallOutBriefParam);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId),"export excel finished");
    }
}
