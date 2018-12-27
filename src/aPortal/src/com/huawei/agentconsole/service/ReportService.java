package com.huawei.agentconsole.service;


import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.Normalizer.Form;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.huawei.agentconsole.common.http.AgentRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.bean.AgentCallOutBriefBean;
import com.huawei.agentconsole.bean.AgentTraffic;
import com.huawei.agentconsole.bean.AgentWork;
import com.huawei.agentconsole.bean.SkillTraffic;
import com.huawei.agentconsole.bean.TenantInfo;
import com.huawei.agentconsole.bean.VdnTraffic;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.constant.ParamPatternConstant;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.util.ExcelUtils;
import com.huawei.agentconsole.common.util.FileUtils;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;
import com.huawei.agentconsole.common.util.RestUtils;
import com.huawei.agentconsole.dao.service.ReportDaoService;
import com.huawei.agentconsole.ws.param.AgentCallOutBriefParam;
import com.huawei.agentconsole.ws.param.AgentTrafficParam;
import com.huawei.agentconsole.ws.param.AgentWorkParam;
import com.huawei.agentconsole.ws.param.RestResponse;
import com.huawei.agentconsole.ws.param.SkillTrafficParam;
import com.huawei.agentconsole.ws.param.VdnTrafficParam;

import oracle.jdbc.internal.OracleTypes;

public class ReportService
{

    private static final Logger LOG = LoggerFactory
            .getLogger(ReportService.class);

    /**
     * 坐席数量限制
     */
    private static final int AGENT_LIMIT = 50;
    
    /**
     * 坐席工号
     */
    private String agentId;
    
    /**
     * 座席鉴权信息
     */
    private AgentBaseInfoBean agentBaseInfo;

    public ReportService()
    {
    }

    public ReportService(String agentId)
    {
        this.agentId = agentId;
        agentBaseInfo = GlobalObject.getAgentBaseInfo(agentId);
    }

    /**
     * VDN报表查询
     * 
     * @param VdnTrafficParam
     *            vdn报表查询结果参数
     * @param agentId
     *            座席工号
     * @return RestResponse 返回结果
     */
    public RestResponse queryVdnTraffic(VdnTrafficParam vdnTrafficParam,
            String agentId)
    {
        RestResponse restResponse = checkBasicParams(vdnTrafficParam.getStartTime(),vdnTrafficParam.getEndTime());
        if (null != restResponse)
        {
            return restResponse;
        }
        vdnTrafficParam.setStartTime(checkBeginTimeAndCreateTime(vdnTrafficParam.getStartTime()));
        try
        {
            //查询结束时间早于租户创建时间时，将结束时间设置为与查询开始时间相同
            if (getLongTime(vdnTrafficParam.getStartTime()) > getLongTime(vdnTrafficParam.getEndTime()))
            {
                vdnTrafficParam.setEndTime(vdnTrafficParam.getStartTime());
            }
        }
        catch (ParseException e)
        {
            LOG.error(LogUtils.AGENT_ID + " failed for parseException :{}",
                    LogUtils.encodeForLog(agentId),
                    LogUtils.encodeForLog(e.getMessage()));
        }
        if (CommonConstant.REPROTTYPE_MONTH == vdnTrafficParam.getReportType())
        {

            String subStartTime = getSubTimeString(
                     vdnTrafficParam.getStartTime());
            String subEndTime = getSubTimeString(vdnTrafficParam.getEndTime());
            vdnTrafficParam.setStartTime(subStartTime);
            vdnTrafficParam.setEndTime(subEndTime);
        }

        HashMap<String, Object> hashMap = initReportVdnQueryParamMap(vdnTrafficParam);
        restResponse = ReportDaoService.getInstance().queryVdnTraffic(hashMap,
                agentId);
        return restResponse;
    }
    
    
    /**
     * 生成vdn话务量报表excel文件
     * @param agentId
     * @param param
     * @return 
     */
    public RestResponse genVdnTrafficExcelFile(String agentId,VdnTrafficParam vdnTrafficParam)
    {       
        //查询报表
        RestResponse res = queryVdnTraffic(vdnTrafficParam, agentId);    
        if (!res.getReturnCode().equals(AgentErrorCode.SUCCESS))
        {
            return res;
        }
        //拷贝文件
        String templatePath = GlobalObject.getApp_path() + CommonConstant.TEMPLATE_VDNTRAFFIC_PATH; //excel模板文件路径
        String desFilePath = generateDesFilePath(CommonConstant.EXCEL_NAME_VDNTRAFFIC);  //生成的文件路径
        RestResponse restResponse = new RestResponse();
        if (!FileUtils.copyExcelFile(templatePath, desFilePath))
        {
            LOG.error(LogUtils.AGENT_ID + "copy report template file failed!", LogUtils.encodeForLog(agentId));
            restResponse.setReturnCode(AgentErrorCode.REPORT_FILE_COPY_ERROR);
            restResponse.setMessage("copy report template file failed!");
            return restResponse;
        }     
        //写入数据到文件
        @SuppressWarnings("unchecked")
        List<VdnTraffic> result = (List<VdnTraffic>)res.getRetObject("result");
        if (null != result)
        {
            ExcelUtils.genExcelData(agentId, desFilePath, result, 1);
            restResponse.setRetObject("desFilePath", desFilePath.replace(GlobalObject.getApp_path(), ""));
        }
        restResponse.setReturnCode(AgentErrorCode.SUCCESS);
        return restResponse;
    }
    
    /**
     * 下载已生成的vdn话务量报表excel文件
     * @param response
     * @param desFilePath 文件路径
     */
    public void exportVdnTraffic(HttpServletResponse response, String desFilePath)
    {
        desFilePath = Normalizer.normalize(desFilePath, Form.NFKC);
        //校验文件下载权限
        if (!checkFilePathPriv(desFilePath))
        {
            LOG.error(LogUtils.AGENT_ID + "check report excel desFilePath failed", LogUtils.encodeForLog(agentId));
            return;
        }         
        //将临时的excel文件文件流输出到response中，返回给客户端供下载
        String realFilePath = GlobalObject.getApp_path() + desFilePath; //完整路径
        FileUtils.fileToResponse(realFilePath, response);
    }
    
    
    /**
     * 校验报表文件下载权限
     * @param desFilePath
     * @return
     */
    private boolean checkFilePathPriv(String desFilePath)
    {
        //校验文件路径是否有效
        if (StringUtils.isNullOrBlank(desFilePath) || (!desFilePath.startsWith(CommonConstant.TEMPFILE_EXCEL_PATH)))
        {
            return false;
        }
       
        //文件名
        String fileName = desFilePath.substring(desFilePath.lastIndexOf("/")+1); 
        
        StringBuilder desFilePathBuffer = new StringBuilder();
        desFilePathBuffer.append(GlobalObject.getApp_path());
        desFilePathBuffer.append(desFilePath);    
        //请求的完整文件路径
        String requestPath = desFilePathBuffer.toString(); 
        File requestFile = new File(requestPath);  
        
        StringBuilder targetFilePathBuffer = new StringBuilder();
        targetFilePathBuffer.append(GlobalObject.getApp_path());
        targetFilePathBuffer.append(CommonConstant.TEMPFILE_EXCEL_PATH);
        targetFilePathBuffer.append("/");
        targetFilePathBuffer.append(fileName);
        //系统生成的文件路径
        String targetFilePath = targetFilePathBuffer.toString(); 
        File targetFile = new File(targetFilePath);    
        //检查文件是否存在
        if ((!requestFile.exists()) || (!requestFile.isFile()) ||(!targetFile.exists()))
        {
            return false;
        }
        try
        {
            //校验请求文件规范路径是否与系统设置的文件规范路径完全一致
            if (!requestFile.getCanonicalPath().equals(targetFile.getCanonicalPath()))
            {
                return false;
            }
        }
        catch (IOException e)
        {
            LOG.error(LogUtils.AGENT_ID + "analyze report excel desFilePath failed", LogUtils.encodeForLog(agentId));
            LOG.error(LogUtils.AGENT_ID + "analyze report excel desFilePath failed \r\n {}",
                    LogUtils.encodeForLog(agentId), 
                    LogUtils.encodeForLog(e.getMessage()));
            return false;
        }            
        //仅支持下载本坐席生成的文件       
        String sign = agentBaseInfo.getAgentId() + "_";
        if (StringUtils.isNullOrBlank(fileName) || !fileName.startsWith(sign))
        {
            return false;
        }
        return true;
    }
    
    /**
     * 生成技能话务量（按技能）报表excel文件
     * @param agentId
     * @param param
     */
    public RestResponse genSikillTrafficBySkillExcelFile(String agentId, SkillTrafficParam param)    
    {
        String skills = param.getSkills();
        if ( (null != skills) && skills.length() == 0)
        {
            //为null时代表全选
            param.setSkills(null);
        }
        param.setSkillQueryType(1);
        //查询报表
        RestResponse res = querySkillTraffic(param, agentId);
        if (!res.getReturnCode().equals(AgentErrorCode.SUCCESS))
        {
            return res;
        }
        //拷贝文件
        String templatePath = GlobalObject.getApp_path() + CommonConstant.TEMPLATE_SKILLTRAFFICBYSKILL_PATH;
        String desFilePath =  generateDesFilePath(CommonConstant.EXCEL_NAME_SKILLTRAFFICBYSKILL);
        RestResponse restResponse = new RestResponse();
        if (!FileUtils.copyExcelFile(templatePath, desFilePath))
        {
            LOG.error(LogUtils.AGENT_ID + "copy report template file failed!", LogUtils.encodeForLog(agentId));
            restResponse.setReturnCode(AgentErrorCode.REPORT_FILE_COPY_ERROR);
            restResponse.setMessage("copy report template file failed!");
            return restResponse;
        }
        //写入数据到文件
        @SuppressWarnings("unchecked")
        List<SkillTraffic> result = (List<SkillTraffic>)res.getRetObject("result");
        if (null != result)
        {
            ExcelUtils.genExcelData(agentId, desFilePath, result, CommonConstant.REPORTNAME_SKILLTRAFFICBYSKILL);
            restResponse.setRetObject("desFilePath", desFilePath.replace(GlobalObject.getApp_path(), ""));
        }
        restResponse.setReturnCode(AgentErrorCode.SUCCESS);
        return restResponse;
    }
    
    /**
     * 下载已生成的技能话务量（按技能）报表excel文件
     * @param response
     * @param desFilePath 文件路径
     */
    public void exportSikillTrafficBySkill(HttpServletResponse response, String desFilePath)
    {
        desFilePath = Normalizer.normalize(desFilePath, Form.NFKC);
        //校验文件下载权限
        if (!checkFilePathPriv(desFilePath))
        {
            LOG.error(LogUtils.AGENT_ID + "check report excel desFilePath failed", LogUtils.encodeForLog(agentId));
            return;
        }  
        //将临时的excel文件文件流输出到response中，返回给客户端供下载
        String realFilePath = GlobalObject.getApp_path() + desFilePath; //完整路径
        FileUtils.fileToResponse(realFilePath, response);
    }
    
    @SuppressWarnings("unchecked")
    public void exportAgentTraffic(HttpServletRequest request,HttpServletResponse response,String agentId,AgentTrafficParam param)
    {
        String agentIds = param.getAgentIds();
        if ( (null != agentIds) && agentIds.length() == 0)
        {
            //为null时代表全选
            param.setAgentIds(null);
        }
        RestResponse res = queryAgentTraffic(param, agentId);
        
        String templatePath = GlobalObject.getApp_path() + CommonConstant.TEMPLATE_AGENTTRAFFIC_PATH;
        String desFilePath = generateDesFilePath(CommonConstant.EXCEL_NAME_AGENTTRAFFIC);
        if (!FileUtils.copyExcelFile(templatePath, desFilePath))
        {
            LOG.error(LogUtils.AGENT_ID + "copy report template file failed!", LogUtils.encodeForLog(agentId));
            return;
        }
        
        List<AgentTraffic> result = (List<AgentTraffic>)res.getRetObject("result");
        if (null != result)
        {
            ExcelUtils.genExcelData(agentId, desFilePath, result, CommonConstant.REPORTNAME_AGENTTRAFFIC);
        }
        //强临时的excel文件文件流输出到response中，返回给客户端供下载
        FileUtils.fileToResponse(desFilePath, response);
    }
    
    
    @SuppressWarnings("unchecked")
    public void exportAgentWork(HttpServletRequest request, HttpServletResponse response, String agentId, AgentWorkParam param)
    {
        String agentIds = param.getAgentIds();
        if ( (null != agentIds) && agentIds.length() == 0)
        {
            //为null时代表全选
            param.setAgentIds(null);
        }
        RestResponse res = queryAgentWork(param, agentId);
        
        String templatePath = GlobalObject.getApp_path() + CommonConstant.TEMPLATE_AGENTWORK_PATH;
        String desFilePath = generateDesFilePath(CommonConstant.EXCEL_NAME_AGENTWORK);
        if (!FileUtils.copyExcelFile(templatePath, desFilePath))
        {
            LOG.error(LogUtils.AGENT_ID + "copy report template file failed!", LogUtils.encodeForLog(agentId));
            return;
        }
        
        List<AgentWork> result = (List<AgentWork>)res.getRetObject("result");
        if (null != result)
        {
            ExcelUtils.genExcelData(agentId, desFilePath, result, CommonConstant.REPORTNAME_AGENTWORK);
        }
        //强临时的excel文件文件流输出到response中，返回给客户端供下载
        FileUtils.fileToResponse(desFilePath, response);
    }
    
    @SuppressWarnings("unchecked")
    public void exportAgentCallOutBrief(HttpServletRequest request,HttpServletResponse response,String agentId,AgentCallOutBriefParam param)
    {
        String agentIds = param.getAgentIds();
        if ( (null != agentIds) && agentIds.length() == 0)
        {
            //为null时代表全选
            param.setAgentIds(null);
        }
        RestResponse res = queryAgentCallOutBrief(param, agentId);
        
        String templatePath = GlobalObject.getApp_path() + CommonConstant.TEMPLATE_AGENTOUTBRIEF_PATH;
        String desFilePath = generateDesFilePath(CommonConstant.EXCEL_NAME_AGENTOUTBRIEF);
        if (!FileUtils.copyExcelFile(templatePath, desFilePath))
        {
            LOG.error(LogUtils.AGENT_ID + "copy report template file failed!", LogUtils.encodeForLog(agentId));
            return;
        }
        
        List<AgentCallOutBriefBean> result = (List<AgentCallOutBriefBean>)res.getRetObject("result");
        if (null != result)
        {
            ExcelUtils.genExcelData(agentId, desFilePath, result, CommonConstant.REPORTNAME_AGENTCALLOUTBRIEF);
        }
        //强临时的excel文件文件流输出到response中，返回给客户端供下载
        FileUtils.fileToResponse(desFilePath, response);
    }
    
    @SuppressWarnings("unchecked")
    public void exportSkillTraffic(HttpServletRequest request,HttpServletResponse response,String agentId,SkillTrafficParam param)
    {
        String skills = param.getSkills();
        if ( (null != skills) && skills.length() == 0)
        {
            //为null时代表全选
            param.setSkills(null);
        }
        RestResponse res = querySkillTraffic(param, agentId);
        
        String templatePath = GlobalObject.getApp_path() + CommonConstant.TEMPLATE_SKILLTRAFFIC_PATH;
        String desFilePath = generateDesFilePath(CommonConstant.EXCEL_NAME_SKILLTRAFFIC);
        if (!FileUtils.copyExcelFile(templatePath, desFilePath))
        {
            LOG.error(LogUtils.AGENT_ID + "copy report template file failed!", LogUtils.encodeForLog(agentId));
            return;
        }
        
        List<SkillTraffic> result = (List<SkillTraffic>)res.getRetObject("result");
        if (null != result)
        {
            ExcelUtils.genExcelData(agentId, desFilePath, result, CommonConstant.REPORTNAME_SKILLTRAFFIC);
        }
        //强临时的excel文件文件流输出到response中，返回给客户端供下载
        FileUtils.fileToResponse(desFilePath, response);
    }
    
    
    
    /**
     * 生成临时excel文件名，陆经理如/tempfile/excel/110_VDNTRAFFIC20180801110303.xls
     * @param agentId当前座席工号
     * @param reportName报表类型
     * @return
     */
    private String generateDesFilePath(String reportName)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(GlobalObject.getApp_path());
        sb.append(CommonConstant.TEMPFILE_EXCEL_PATH);
        sb.append("/");
        sb.append(agentId).append('_');
        sb.append(reportName);
        sb.append(StringUtils.formateDateToString());
        sb.append(CommonConstant.EXCEL_SUFFIX);
        return sb.toString();
    }
    
    /**
     * 技能话务量报表查询
     * 
     * @param SkillTrafficParam
     *            技能话务量报表查询参数
     * @param agentId
     *            座席工号
     * @return RestResponse 返回结果
     */
    public RestResponse querySkillTraffic(SkillTrafficParam skillTrafficParam,
            String agentId)
    {
        RestResponse restResponse = null;
        // 检查技能队列是否超限以及技能队列id是否合法
        if (!isSkillsValid(skillTrafficParam))
        {
            LOG.info(
                    LogUtils.AGENT_ID
                            + "querySkillTraffic failed for skills invalid.",
                    LogUtils.encodeForLog(agentId));
            restResponse = RestUtils.getErrorResult(AgentErrorCode.REPORT_REST_INVALID, "skills is invalid.");
            return restResponse;
        }

        restResponse = checkBasicParams(skillTrafficParam.getStartTime(),skillTrafficParam.getEndTime());
        if (null != restResponse)
        {
            return restResponse;
        }
        skillTrafficParam.setStartTime(checkBeginTimeAndCreateTime(skillTrafficParam.getStartTime()));
        try
        {
            //查询结束时间早于租户创建时间时，将结束时间设置为与查询开始时间相同
            if (getLongTime(skillTrafficParam.getStartTime()) > getLongTime(skillTrafficParam.getEndTime()))
            {
                skillTrafficParam.setEndTime(skillTrafficParam.getStartTime());
            }
        }
        catch (ParseException e)
        {
            LOG.error(LogUtils.AGENT_ID + " failed for parseException :{}",
                    LogUtils.encodeForLog(agentId),
                    LogUtils.encodeForLog(e.getMessage()));
        }
        if (CommonConstant.REPROTTYPE_MONTH == skillTrafficParam.getReportType())
        {

            String subStartTime = getSubTimeString(
                    skillTrafficParam.getStartTime());
            String subEndTime = getSubTimeString(skillTrafficParam.getEndTime());
            skillTrafficParam.setStartTime(subStartTime);
            skillTrafficParam.setEndTime(subEndTime);
        }

        HashMap<String, Object> hashMap = initReportSkillQueryParamMap(
                skillTrafficParam);
        restResponse = ReportDaoService.getInstance().querySkillTraffic(hashMap,
                agentId);
        return restResponse;
    }

    /**
     * 话务员话务量报表查询
     * @param agentTrafficParam
     * @param agentId
     * @return
     */
    public RestResponse queryAgentTraffic(AgentTrafficParam agentTrafficParam,
            String agentId)
    {
        RestResponse restResponse = new RestResponse();
        //检查登录信息
        if (null == agentBaseInfo)
        {
            LOG.error(LogUtils.AGENT_ID + "has logout", LogUtils.encodeForLog(agentId));
            restResponse.setReturnCode(AgentErrorCode.AGENT_NOT_LOGIN);
            restResponse.setMessage("Agent has not login .");
            return restResponse;
        }        
        //检查坐席参数是否为空或者超限
        String agents = agentTrafficParam.getAgentIds();
        if (agents != null)
        {
            String[] agentIds = splitStringWithComma(agents);
            if ( agentIds.length == 0)
            {
                LOG.info(
                        LogUtils.AGENT_ID + "queryAgentTraffic failed for no agent is selected",
                        LogUtils.encodeForLog(agentId));
                restResponse.setReturnCode(AgentErrorCode.REPORT_REST_INVALID);
                restResponse.setMessage("agents selected out of bounds");
                return restResponse;
            }        
            if (agentIds.length > AGENT_LIMIT)
            {               
                LOG.info(
                        LogUtils.AGENT_ID + "queryAgentTraffic failed for agents selected out of bounds",
                        LogUtils.encodeForLog(agentId));
                restResponse.setReturnCode(AgentErrorCode.REPORT_REST_INVALID);
                restResponse.setMessage("agents selected out of bounds");
                return restResponse;              
            }
        }
        
        //参数校验
        if (!isEndTimeGrater(agentTrafficParam.getStartTime(),
                agentTrafficParam.getEndTime(), agentId))
        {
            LOG.info(
                    LogUtils.AGENT_ID
                            + "queryAgentTraffic failed for endTime is not grater than startTime.",
                    LogUtils.encodeForLog(agentId));
            restResponse.setReturnCode(AgentErrorCode.REPORT_REST_INVALID);
            restResponse.setMessage("endTime is not grater than startTime.");
            return restResponse;
        }
        
        try
        {
            if (!isDateInLimitTime(agentTrafficParam.getStartTime(),
                    agentTrafficParam.getEndTime()))
            {
                LOG.info(LogUtils.AGENT_ID
                        + "queryAgentTraffic failed for startTime or interval between startTime and endTime is not within the time limit.");
                return makeParamErrorResponse(restResponse);
            }
        }
        catch (ParseException e)
        {
            LOG.error(
                    LogUtils.AGENT_ID + "queryAgentTraffic failed for parseException :{}",
                    LogUtils.encodeForLog(agentId),
                    LogUtils.encodeForLog(e.getMessage()));
            restResponse.setMessage("starttime is not invalid or duriation is invalid.");
            restResponse.setReturnCode(AgentErrorCode.REPORT_REST_INVALID);
            return restResponse;
        }

        if (CommonConstant.REPROTTYPE_MONTH == agentTrafficParam.getReportType())
        {
            String subStartTime = getSubTimeString(
                    agentTrafficParam.getStartTime());
            String subEndTime = getSubTimeString(
                    agentTrafficParam.getEndTime());
            agentTrafficParam.setStartTime(subStartTime);
            agentTrafficParam.setEndTime(subEndTime);
        }

        if (!isReportTypeAndDataValid(agentTrafficParam.getStartTime(),
                agentTrafficParam.getEndTime(),
                agentTrafficParam.getReportType()))
        {
            LOG.error(
                    LogUtils.AGENT_ID + "queryAgentTraffic failed for param invalid .",
                    LogUtils.encodeForLog(agentId));
            return makeParamErrorResponse(restResponse);
        }

        //查询数据
        HashMap<String, Object> queryParamMap = initReportQueryAgentMap(agentTrafficParam);
        restResponse = ReportDaoService.getInstance().queryAgentraffic(queryParamMap, agentId);
        return restResponse;
    }
    
       
    /**
     * 话务员接续报表查询
     * @param agentWorkParam 
     * @param agentId
     * @return
     */
    public RestResponse queryAgentWork(AgentWorkParam agentWorkParam,
            String agentId)
    {
        
        RestResponse restResponse = new RestResponse();
        //检查登录信息
        if (null == agentBaseInfo)
        {
            LOG.error(LogUtils.AGENT_ID + "has logout", LogUtils.encodeForLog(agentId));
            restResponse.setReturnCode(AgentErrorCode.AGENT_NOT_LOGIN);
            restResponse.setMessage("Agent has not login .");
            return restResponse;
        }        
        //检查坐席参数是否为空或者超限
        String agents = agentWorkParam.getAgentIds();
        if (agents != null)  
        {
            String[] agentIds = splitStringWithComma(agents);
            if (agentIds.length == 0)
            {
                LOG.info(
                        LogUtils.AGENT_ID + "queryAgentWork failed for no agent is selected",
                        LogUtils.encodeForLog(agentId));
                restResponse.setReturnCode(AgentErrorCode.REPORT_REST_INVALID);
                restResponse.setMessage("agents selected out of bounds");
                return restResponse;
            }        
            if (agentIds.length > AGENT_LIMIT)
            {
                LOG.info(
                LogUtils.AGENT_ID + "queryAgentWork failed for agents selected out of bounds",
                            LogUtils.encodeForLog(agentId));
                restResponse.setReturnCode(AgentErrorCode.REPORT_REST_INVALID);
                restResponse.setMessage("agents selected out of bounds");
                return restResponse;         
            }
            
        }else {
          //agents为空时按全选处理
        }        
        
        //参数校验
        if (!isEndTimeGrater(agentWorkParam.getStartTime(),
                agentWorkParam.getEndTime(), agentId))
        {
            LOG.info(
                    LogUtils.AGENT_ID
                            + "queryAgentWork failed for endTime is not grater than startTime.",
                    LogUtils.encodeForLog(agentId));
            restResponse.setReturnCode(AgentErrorCode.REPORT_REST_INVALID);
            restResponse.setMessage("endTime is not grater than startTime.");
            return restResponse;
        }
        
        try
        {
            if (!isDateInLimitTime(agentWorkParam.getStartTime(),
                    agentWorkParam.getEndTime()))
            {
                LOG.info(LogUtils.AGENT_ID
                        + "queryAgentWork failed for startTime or interval between startTime and endTime is not within the time limit.");
                return makeParamErrorResponse(restResponse);
            }
        }
        catch (ParseException e)
        {
            LOG.error(
                    LogUtils.AGENT_ID + "queryAgentWork failed for parseException :{}",
                    LogUtils.encodeForLog(agentId),
                    LogUtils.encodeForLog(e.getMessage()));
            restResponse.setMessage("starttime is not invalid or duriation is invalid.");
            restResponse.setReturnCode(AgentErrorCode.REPORT_REST_INVALID);
            return restResponse;
        }

        if (CommonConstant.REPROTTYPE_MONTH == agentWorkParam.getReportType())
        {
            String subStartTime = getSubTimeString(
                    agentWorkParam.getStartTime());
            String subEndTime = getSubTimeString(
                    agentWorkParam.getEndTime());
            agentWorkParam.setStartTime(subStartTime);
            agentWorkParam.setEndTime(subEndTime);
        }

        if (!isReportTypeAndDataValid(agentWorkParam.getStartTime(),
                agentWorkParam.getEndTime(),
                agentWorkParam.getReportType()))
        {
            LOG.error(
                    LogUtils.AGENT_ID + "queryAgentWork failed for param invalid .",
                    LogUtils.encodeForLog(agentId));
            return makeParamErrorResponse(restResponse);
        }
        
        //查询数据
        HashMap<String, Object> queryParamMap = initReportQueryAgentWorkMap(agentWorkParam);
        restResponse = ReportDaoService.getInstance().queryAgentWork(queryParamMap, agentId);
        return restResponse;        
    }    
    
    


    /**
     * 话务员呼出报表查询
     * 
     * @param agentCallOutBriefParam
     *            话务员呼出报表查询参数
     * @param agentId
     *            座席工号
     * @return RestResponse
     */
    public RestResponse queryAgentCallOutBrief(
            AgentCallOutBriefParam agentCallOutBriefParam, String agentId)
    {
        if (!isAgentIdsValid(agentCallOutBriefParam))
        {
            LOG.info(
                    LogUtils.AGENT_ID
                    + "queryAgentCallOutBrief failed for agentId is invalid.",
                    LogUtils.encodeForLog(agentId)); 
            return RestUtils.getErrorResult(AgentErrorCode.REPORT_REST_INVALID, "agenIds is invalid.");
        }
        
        RestResponse restResponse = checkBasicParams(agentCallOutBriefParam.getStartTime(),agentCallOutBriefParam.getEndTime());
        if (null != restResponse)
        {
            return restResponse;
        }
        agentCallOutBriefParam.setStartTime(checkBeginTimeAndCreateTime(agentCallOutBriefParam.getStartTime()));
        if (CommonConstant.REPROTTYPE_MONTH == agentCallOutBriefParam.getReportType())
        {
            String subStartTime = getSubTimeString(
                    agentCallOutBriefParam.getStartTime());
            String subEndTime = getSubTimeString(
                    agentCallOutBriefParam.getEndTime());
            agentCallOutBriefParam.setStartTime(subStartTime);
            agentCallOutBriefParam.setEndTime(subEndTime);
        }

        HashMap<String, Object> hashMap = initReportAgentCallOutBriefParamMap(
                agentCallOutBriefParam, agentId);
        restResponse = ReportDaoService.getInstance()
                .queryAgentCallOutBrief(hashMap, agentId);

        return restResponse;
    }

    /**
     * 初始化调用存储过程P_CONSOLE_AGENTCALLOUTBRIEFPARAM需要的参数
     * 
     * @param agentCallOutBriefParam
     * @return HashMap<String, Object>
     */
    private HashMap<String, Object> initReportAgentCallOutBriefParamMap(
            AgentCallOutBriefParam agentCallOutBriefParam, String agentId)
    {
        HashMap<String, Object> hashMap = initBasicParam(
                agentCallOutBriefParam.getStartTime(),agentCallOutBriefParam.getEndTime(),agentCallOutBriefParam.getReportType(),agentCallOutBriefParam.getLanguageType());
        hashMap.put("i_AgentWorkGroup", "0|ALL|0|0");
        hashMap.put("i_AgentID",
                formatAgentIds(agentCallOutBriefParam.getAgentIds(), agentId));
        hashMap.put("i_Flag",
                CommonConstant.REPORT_AGENT_CALLOUT_BREIF_QUERYTYPE);
        hashMap.put("i_VDN", agentBaseInfo.getVdnId());

        return hashMap;
    }

    /**
     * 初始化调用存储过程P_CONSOLE_VDNTRAFFIC需要的参数
     * 
     * @param VdnTrafficParam
     *            VDN报表查询参数
     * @return HashMap<String, Object>
     */
    public HashMap<String, Object> initReportVdnQueryParamMap(
            VdnTrafficParam vdnTrafficParam)
    {
        String i_CCID_VDN = getI_CCID_VDN();
        HashMap<String, Object> hashMap = initBasicParam(vdnTrafficParam.getStartTime(),vdnTrafficParam.getEndTime(),vdnTrafficParam.getReportType(),vdnTrafficParam.getLanguageType());
        hashMap.put("i_CCID_VDN", i_CCID_VDN);
        return hashMap;
    }

    /**
     * 初始化调用存储过程P_CONSOLE_SKILLTRAFFIC需要的参数
     * 
     * @param SkillTrafficParam
     *            技能话务量报表请求参数
     * @return HashMap<String, Object>
     */
    public HashMap<String, Object> initReportSkillQueryParamMap(
            SkillTrafficParam skillTrafficParam)
    {
        String CCID = GlobalObject.getVdnInfo(CommonConstant.CCID);
        String i_SkillID = formatSkills(skillTrafficParam.getSkills(), CCID,
                agentBaseInfo.getVdnId());

        HashMap<String, Object> hashMap = initBasicParam(skillTrafficParam.getStartTime(),skillTrafficParam.getEndTime(),skillTrafficParam.getReportType(),skillTrafficParam.getLanguageType());
        hashMap.put("i_Type", skillTrafficParam.getSkillQueryType());
        hashMap.put("i_SkillID", i_SkillID);
        hashMap.put("i_CCID", CCID);
        hashMap.put("i_VDN", agentBaseInfo.getVdnId());
        return hashMap;
    }

    /**
     * 初始化报表查询的基本参数
     * 
     * @param vdnTrafficParam
     * @return HashMap<String, Object>
     */
    private HashMap<String, Object> initBasicParam(String startTime, String endTime, Integer reportType, Integer languageType
            )
    {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("i_StartDate", startTime);
        hashMap.put("i_EndDate", endTime);
        hashMap.put("i_TimeUnit", CommonConstant.REPORT_TIMEUNIT);
        hashMap.put("i_RptType", reportType);
        hashMap.put("i_VDNUserName", getI_VDNUserName(languageType));
        hashMap.put("result", OracleTypes.CURSOR);
        return hashMap;
    }

    /**
     * 校验VdnTrafficParam参数
     * 
     * @param vdnTrafficParam
     * @param agentId
     *            座席工号
     * @return RestResponse
     */
    public RestResponse checkBasicParams(String StartTime, String endTime)
    {
        if (null == agentBaseInfo)
        {
            LOG.error(LogUtils.AGENT_ID + "has logout",
                    LogUtils.encodeForLog(agentId));
            return RestUtils.getErrorResult(AgentErrorCode.AGENT_NOT_LOGIN, "Agent has not login.");
        }

        if (!validDateFormat(StartTime,
                endTime,
                ParamPatternConstant.WEEKORDAYTYPEREREGX))
        {
            LOG.info(LogUtils.AGENT_ID + "has logout",
                    LogUtils.encodeForLog(agentId));
            return RestUtils.getErrorResult(AgentErrorCode.REPORT_REST_INVALID, "time format is invalid.");
        }

        try
        {
            if (!isDateInLimitTime(StartTime, endTime))
            {
                LOG.info(LogUtils.AGENT_ID
                        + " failed for startTime or interval between startTime and endTime is not within the time limited.");
                return RestUtils.getErrorResult(AgentErrorCode.REPORT_REST_INVALID, "time param is invalid.");
            }
        }
        catch (ParseException e)
        {
            LOG.error(LogUtils.AGENT_ID + " failed for parseException :{}",
                    LogUtils.encodeForLog(agentId),
                    LogUtils.encodeForLog(e.getMessage()));
            return RestUtils.getErrorResult(AgentErrorCode.REPORT_REST_INVALID, "time param format is invalid.");
        }

        return null;
    }

    /**
     * 截取时间字符串
     * 
     * @param timeStr
     *            yyyy-MM-dd格式的时间字符串
     * @return 截取后的yyyy-MM格式的时间字符串
     */
    private String getSubTimeString(String timeStr)
    {
        int lastIndex = timeStr.lastIndexOf("-");
        String subTimeStr = timeStr.substring(0, lastIndex);
        return subTimeStr;
    }

    /**
     * 根据报表语言类型获取报表用户名称 报表用户名称格式：用户名|语言类型 示例："userName|zh_CN"或者"userName|en_Us"
     * 
     * @param languageType
     *            报表语言类型
     * @return 报表用户名
     */
    private String getI_VDNUserName(int languageType)
    {
        String VDNUserName = GlobalObject
                .getVdnInfo(CommonConstant.VDNUSERNAME);
        String i_VDNUserName;
        if (CommonConstant.LANGUAGE_CHINESE == languageType)
        {
            i_VDNUserName = VDNUserName + "|zh_CN";
        }
        else
        {
            i_VDNUserName = VDNUserName + "|en_US";
        }

        return i_VDNUserName;
    }

    /**
     * 获取i_CCID_VDN i_CCID_VDN格式为 "callCenterId|vdnID"
     * 
     * @return String 返回i_CCID_VDN
     */
    private String getI_CCID_VDN()
    {
        String vdnId = agentBaseInfo.getVdnId();
        String ccId = GlobalObject.getVdnInfo(CommonConstant.CCID);
        String i_CCID = ccId + "|" + vdnId;
        return i_CCID;
    }

    /**
     * 将String[] agentIds转化为存储过程中i_AgentId的格式
     * i_AgentId的格式:"agentid|agentId|callCenterId|vdnId,......."
     * 
     * @param agentIds
     *            座席工号数组 append(agentBaseInfo.getVdnId())
     * @return String i_AgentId 字符串
     */
    private String formatAgentIds(String agentIdsString,String agentId)
    {
        if(null == agentIdsString)
        {
            //当座席班组为全选时
            return "0|ALL|0|0";
        }
        
        StringBuffer stringBuffer = new StringBuffer();
        String[] agentIds = splitStringWithComma(agentIdsString);
        for (int i = 0; i < agentIds.length; i++)
        {
            stringBuffer.append(agentIds[i]).append("|").append(agentIds[i])
                    .append("|")
                    .append(GlobalObject.getVdnInfo(CommonConstant.CCID))
                    .append("|").append(agentBaseInfo.getVdnId());
            if (i != (agentIds.length - 1))
            {
                stringBuffer.append(",");
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 将String[] skillIds 格式化 为调用存储过程的参数i_skillI格式。
     * i_skillId格式:"skillId|skillId|callCenterId|vdnId,....."
     * 
     * @param skillIds
     *            技能队列数组
     * @param i_CCID
     *            callCenterId
     * @param vdn
     *            vdnId
     * @return String
     */
    public String formatSkills(String skillIdsString, String i_CCID, String vdn)
    {
        //当座席id为全选时
        if (null == skillIdsString)
        {
            return "0|ALL|0|0";
        }
        
        StringBuffer stringBuffer = new StringBuffer();
        String[] skillIds = splitStringWithComma(skillIdsString);
        for (int i = 0; i < skillIds.length; i++)
        {
            stringBuffer.append(skillIds[i]).append("|").append(skillIds[i])
                    .append("|").append(i_CCID).append("|").append(vdn);
            if (i != (skillIds.length - 1))
            {
                stringBuffer.append(",");
            }
        }
        String skill = stringBuffer.toString();
        return skill;
    }

    /**
     * 校验技能队列列表，技能队列列表不为空，且技能队列都为正整数
     * 
     * @param SkillTrafficParam
     * @return
     */
    private boolean isSkillsValid(SkillTrafficParam skillTrafficParam)
    {
        boolean isSkillValid = true;
        if (null == skillTrafficParam)
        {
            return false;
        }
        
        if (null != skillTrafficParam.getSkills())
        {
            String[] skills = splitStringWithComma(skillTrafficParam.getSkills());
            int skillsLength = skills.length;
            if (skillsLength>ParamPatternConstant.REPORT_MAX_SKILLS)
            {
                return false;
            }
            
            for (String skill : skills)
            {
                if (!isStringValidForRegex(skill,
                        ParamPatternConstant.SKILLREGEX))
                {
                    isSkillValid = false;
                }
            }
        }
        return isSkillValid;
    }

    /**
     * 校验座席工号列表，工号列表不能为空，工号格式符合座席工号正则表达式
     * 
     * @param agentCallOutBriefParam
     *            话务员外呼报表查询参数
     * @return boolean
     */
    private boolean isAgentIdsValid(
            AgentCallOutBriefParam agentCallOutBriefParam)
    {
        boolean isAgentIdsValid = true;
        if (null == agentCallOutBriefParam)
        {
            return false;
        }
        
        if (null!= agentCallOutBriefParam.getAgentIds())
        {
            String[] agentIds = splitStringWithComma(agentCallOutBriefParam.getAgentIds());
            int agentIdsLength = agentIds.length;
            if (agentIdsLength>ParamPatternConstant.REPORT_MAX_AGENTIDS)
            {
                return false;
            }
            
            for (String agentId : agentIds)
            {
                if (!isStringValidForRegex(agentId,
                        ParamPatternConstant.AGENTID_PATTERN))
                {
                    isAgentIdsValid = false;
                }
            }
        }
        return isAgentIdsValid;
    }

    /**
     * 校验字符串是否匹配正则表达式
     * 
     * @param string
     *            字符串
     * @param regex
     *            正则表达式
     * @return boolean
     */
    private boolean isStringValidForRegex(String string, String regex)
    {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(string).matches();
    }

    /**
     * 校验报表开始时间和结束时间格式是否正确
     * 
     * @param startTime
     *            报表开始时间
     * @param endTime
     *            报表结束时间
     * @param regStr
     *            时间格式
     * @return
     */
    private boolean validDateFormat(String startTime, String endTime,
            String regStr)
    {
        Pattern pattern = Pattern.compile(regStr);
        boolean matches = pattern.matcher(startTime).matches();
        boolean matches2 = pattern.matcher(endTime).matches();
        if (matches && matches2)
        {
            return true;
        }
        return false;
    }
    
    /**
     * 参数错误时，设置返回结果对象
     * 
     * @param responseResult
     *            结果对象
     * @return RestResponse 设置后的结果对象
     */
    private RestResponse makeParamErrorResponse(RestResponse restResponse)
    {
        restResponse.setMessage("param is invalid .");
        restResponse.setReturnCode(AgentErrorCode.REPORT_REST_INVALID);
        return restResponse;
    }

    /***
     * 将yyyy-MM-dd格式的时间字符串转化为毫秒时间
     * 
     * @param startTime
     *            yyyy-MM-dd格式时间字符串
     * @return Long
     * @throws ParseException
     */
    private Long getLongTime(String startTime) throws ParseException
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CommonConstant.DATE_YYYY_MM_DD);
        Date startDate = simpleDateFormat.parse(startTime);
        long startMTime = startDate.getTime();
        return startMTime;
    }

    /**
     * 开始时间和结束时间是否在限定时间范围
     * 
     * @param startTime
     *            报表开始时间
     * @param endTime
     *            报表结束时间
     * @param reportType
     *            报表类型
     * @return boolean
     * @throws ParseException
     */
    private boolean isDateInLimitTime(String startTime, String endTime) throws ParseException
    {
        boolean isDateInLimit = false;
        //报表开始时间和结束时间间隔最大为90天，开始时间是限制在90天以内
        if (isStartTimeInLimit(startTime, CommonConstant.REPORT_MAX_INTERVAL_DAYS))
        {
          isDateInLimit = isIntervalInLimitDays(startTime, endTime,
                  CommonConstant.REPORT_MAX_INTERVAL_DAYS);
        }        
        return isDateInLimit;
    }

    /**
     * 检查开始时间是否在限定范围内
     * 
     * @param startTime
     *            报表开始时间
     * @param year
     *            开始时间限定的天数
     * @return boolean
     * @throws ParseException
     */
    private boolean isStartTimeInLimit(String startTime, int day)
            throws ParseException
    {
        boolean isStartTimeInLimitMonths = false;
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        instance.add(Calendar.DAY_OF_YEAR, (0 - day));
        long validBeginTime = instance.getTime().getTime();
        
        Long longTime = getLongTime(startTime);
        if (longTime > validBeginTime)
        {
            isStartTimeInLimitMonths = true;
        }
        return isStartTimeInLimitMonths;
    }
    

    /**
     * 判断开始时间和结束时间的间隔是否小于限定的时间间隔天数
     * 
     * @param startTime
     *            开始时间
     * @param endTime
     *            结束时间
     * @param duration
     *            限定的时间间隔天数
     * @return boolean
     * @throws ParseException
     */
    private boolean isIntervalInLimitDays(String startTime, String endTime,
            long duration) throws ParseException
    {
        boolean isInLimitDays = false;
        Long milliSecondStartTime = getLongTime(startTime);
        Long milliSecondEndTime = getLongTime(endTime);
        if (0 < (milliSecondEndTime - milliSecondStartTime)
                && (milliSecondEndTime - milliSecondStartTime) < TimeUnit.DAYS
                        .toMillis(duration))
        {
            isInLimitDays = true;
        }
        return isInLimitDays;
    }
    
    /**
     * 报表查询结束时间是否大于开始时间
     * 
     * @param startTime
     *            开始时间
     * @param endTime
     *            结束时间
     * @return boolean
     */
    private boolean isEndTimeGrater(String startTime, String endTime,
            String agentId)
    {
        boolean isTimeGrater = false;
        if (validDateFormat(startTime, endTime,
                ParamPatternConstant.WEEKORDAYTYPEREREGX))
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd");
            try
            {
                Date startDate = simpleDateFormat.parse(startTime);
                Date endDate = simpleDateFormat.parse(endTime);
                long startDateTime = startDate.getTime();
                long endDateTime = endDate.getTime();
                if (endDateTime >= startDateTime)
                {
                    isTimeGrater = true;
                }
            }
            catch (ParseException e)
            {
                LOG.error(
                        LogUtils.AGENT_ID
                                + "isEndTimeGrater failed , receive message: {}",
                        LogUtils.encodeForLog(agentId),
                        LogUtils.encodeForLog(e.getMessage()));
            }
        }
        return isTimeGrater;
    }
    

    /**
     * 初始化请求参数(坐席话务量)
     * @param agentTrafficParam
     * @return
     */
    private HashMap<String, Object> initReportQueryAgentMap(AgentTrafficParam agentTrafficParam)
    {
        String CCID = GlobalObject.getVdnInfo(CommonConstant.CCID);
        String vdnId = agentBaseInfo.getVdnId();
        String i_VDNUserName = "CMS|zh_CN";
        String i_SkillID = "0|ALL|0|0";
        String i_AgentGroup = "0|ALL|0|0";
        //全选情况下agentId设置为"0|ALL|0|0"
        String i_AgentId;
        if (agentTrafficParam.getAgentIds() == null)
        {
            i_AgentId = "0|ALL|0|0";
        }
        else 
        {
            String[] agentIds = splitStringWithComma(agentTrafficParam.getAgentIds());
            i_AgentId = formatAgentIDs(agentIds, CCID, vdnId);
        }  
        int i_Flag = 0; // query type(1:by agent id, 2:by work group, 0:by agent id associate with work group)
        int i_Type = 0;

        HashMap<String, Object> queryParamMap = new HashMap<String, Object>();
        queryParamMap.put("i_StartDate", agentTrafficParam.getStartTime());
        queryParamMap.put("i_EndDate", agentTrafficParam.getEndTime());
        queryParamMap.put("i_TimeUnit", CommonConstant.REPORT_TIMEUNIT);        
        queryParamMap.put("i_Flag", i_Flag); 
        queryParamMap.put("i_AgentGroup", i_AgentGroup); 
        queryParamMap.put("i_AgentID", i_AgentId); 
        queryParamMap.put("i_SkillID", i_SkillID);      
        queryParamMap.put("i_CCID", String.format("%s|%s", CCID,CCID));        
        queryParamMap.put("i_Type", i_Type);
        queryParamMap.put("i_VDNUserName", i_VDNUserName);
        queryParamMap.put("i_RptType", agentTrafficParam.getReportType());
        queryParamMap.put("result", OracleTypes.CURSOR);
        queryParamMap.put("i_VDN", vdnId);
        
        return queryParamMap;
    }
    
    /**
     * 将字符串使用空格字符拆分成字符串数组
     * @return
     */
    private String[] splitStringWithComma(@NotNull String input)
    {
        if (input.length() == 0)
        {
            return new String[0];
        }
        String[] strings = input.trim().split(",");
        return strings;
    }
    
    /**
     * 格式化坐席工号信息
     * @param agentIds
     * @return
     */
    private static String formatAgentIDs(String[] agentIds, String CCID, String vdnId)
    {
        StringBuilder sb = new StringBuilder();
        for (String agentId : agentIds)
        {
            sb.append(String.format("%s|%s|%s|%s,", agentId,agentId,CCID,vdnId));
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    
    /**
     * 初始化请求参数(话务员接续)
     * @param agentTrafficParam
     * @return
     */
    private HashMap<String, Object> initReportQueryAgentWorkMap(AgentWorkParam agentWorkParam)
    {
        String CCID = GlobalObject.getVdnInfo(CommonConstant.CCID);
        String vdnId = agentBaseInfo.getVdnId();
        String i_VDNUserName = "CMS|zh_CN";
        String i_AgentGroup = "0|ALL|0|0";        
        //全选情况下agentId设置为"0|ALL|0|0"
        String i_AgentId;
        if (agentWorkParam.getAgentIds() == null)
        {
            i_AgentId = "0|ALL|0|0";
        }
        else 
        {
            String[] agentIds = splitStringWithComma(agentWorkParam.getAgentIds());
            i_AgentId = formatAgentIDs(agentIds, CCID, vdnId);
        }        
        int i_Flag = 1; // query type(1:by agent id, 2:by work group, 0:by agent id associate with work group)

        HashMap<String, Object> queryParamMap = new HashMap<String, Object>();
        queryParamMap.put("i_StartDate", agentWorkParam.getStartTime());
        queryParamMap.put("i_EndDate", agentWorkParam.getEndTime());
        queryParamMap.put("i_TimeUnit", CommonConstant.REPORT_TIMEUNIT);        
        queryParamMap.put("i_Flag", i_Flag); 
        queryParamMap.put("i_AgentGroup", i_AgentGroup); 
        queryParamMap.put("i_AgentID", i_AgentId);     
        queryParamMap.put("i_VDNUserName", i_VDNUserName);
        queryParamMap.put("i_RptType", agentWorkParam.getReportType());
        queryParamMap.put("result", OracleTypes.CURSOR);
        queryParamMap.put("i_VDN", vdnId);
        
        return queryParamMap;
    }
    
    /**
     * 检查报表类型和时间格式是否匹配 当报表类型为月报表时： 时间格式为 yyyy-MM 当报表类型为周报表或日报表时： 时间格式为
     * yyyy-MM-dd
     * 
     * @param startTime
     *            报表开始时间
     * @param endTime
     *            报表结束时间
     * @param reportType
     *            报表类型
     * @return
     */
    private boolean isReportTypeAndDataValid(String startTime, String endTime,
            int reportType)
    {
        boolean isReportTypeValid = false;
        switch (reportType)
        {
            // 报表类型为月报表
            case CommonConstant.REPROTTYPE_MONTH:
                if (validDateFormat(startTime, endTime,
                        ParamPatternConstant.MONTHRTYPEREGEX))
                {
                    isReportTypeValid = true;
                }
                break;
            // 报表类型为周报表或日报表
            case CommonConstant.REPROTTYPE_WEEK:
            case CommonConstant.REPROTTYPE_DAY:
                if (validDateFormat(startTime, endTime,
                        ParamPatternConstant.WEEKORDAYTYPEREREGX))
                {
                    isReportTypeValid = true;
                }
                break;
            default:
                break;
        }
        return isReportTypeValid;
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
    
    
    /**
     * 校验租户创建时间与报表查询开始时间比较，如果报表查询时间早于租户创建时间，则需要将查询开始设置为租户创建时间
     * @param startTime
     * @return
     */
    private String checkBeginTimeAndCreateTime(String startTime)
    {
        TenantInfo tenantInfo = agentBaseInfo.getTenantInfo();
        if (null != tenantInfo && null != tenantInfo.getCreateTime())
        {
            long createDate = tenantInfo.getCreateTime().getTime();
            long currentStartDate;
            try
            {
                currentStartDate = getLongTime(startTime);
                if (currentStartDate < createDate)
                {
                    startTime = getCreateDateString(tenantInfo.getCreateTime());
                }
            }
            catch (ParseException e)
            {
                LOG.error(LogUtils.AGENT_ID + " failed for parseException :{}",
                        LogUtils.encodeForLog(agentId),
                        LogUtils.encodeForLog(e.getMessage()));
            }
        }
        return startTime;
    }
    
    /**
     * 获取字符串格式的租户创建时间
     * @param createDate
     * @return
     */
    private String getCreateDateString(Date createDate) 
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(CommonConstant.DATE_YYYY_MM_DD);
        return dateFormat.format(createDate);
    }

}
