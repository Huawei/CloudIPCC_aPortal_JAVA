
package com.huawei.agentconsole.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.bean.RecordInfoBean;
import com.huawei.agentconsole.bean.TenantInfo;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.http.AgentRequest;
import com.huawei.agentconsole.common.util.FileUtils;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;
import com.huawei.agentconsole.common.util.ValidateUtils;
import com.huawei.agentconsole.dao.service.RecordInfoService;
import com.huawei.agentconsole.ws.param.PageParam;
import com.huawei.agentconsole.ws.param.RequestParamForRecordBean;
import com.huawei.agentconsole.ws.param.RestResponse;
import com.huawei.agentconsole.ws.param.record.PageVO;
import com.huawei.agentconsole.ws.param.record.RecordResult;
import com.huawei.agentconsole.ws.param.record.Result;
import com.huawei.agentconsole.ws.param.record.ReturnData;

public class RecordService
{
    private static final Logger LOG = LoggerFactory.getLogger(RecordService.class);
    
    private String agentId;
    
    private AgentBaseInfoBean agentBaseInfo;
    
    public RecordService(String agentId)
    {
        this.agentId = agentId;
        agentBaseInfo = GlobalObject.getAgentBaseInfo(agentId);
    }
   
    /***
     * 将yyyy-MM-dd格式的时间字符串转化为毫秒时间
     * 
     * @param startTime
     *            yyyy-MM-dd hh:mm:ss格式时间字符串
     * @return Long
     * @throws ParseException
     */
    private Long getLongTime(String startTime) throws ParseException
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CommonConstant.DATE_FULL);
        Date startDate = simpleDateFormat.parse(startTime);
        long startMTime = startDate.getTime();
        return startMTime;
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
    private boolean isStartTimeInLimit(String startTime, int day) throws ParseException
    {
        boolean isStartTimeInLimitMonths = false;
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        instance.add(Calendar.DAY_OF_YEAR, (0 - day)); //限定时间范围为限制最早日期当天0点
        instance.set(Calendar.HOUR, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        long validBeginTime = instance.getTime().getTime();
        
        Long longTime = getLongTime(startTime);
        if (longTime > validBeginTime)
        {
            isStartTimeInLimitMonths = true;
        }
        return isStartTimeInLimitMonths;
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
     * 检查起止时间参数是否有效
     * @param begin
     * @param end
     * @return
     */
    private boolean isTimeParamValid(String begin, String end)
    {
        try
        {
            if (isDateInLimitTime(begin,end))
            {
                return true;
            }
        }
        catch (ParseException e)
        {
           return false;
        }
        return false;
    }
    
    /**
     * 返回查询结果
     * 包括数据和分页信息
     * @param page
     * @param searchParam
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> queryRecordInfo(PageParam page,RequestParamForRecordBean searchParam)
    {
        RecordResult recordResult = new RecordResult();        
        searchParam.setCcId(GlobalObject.getCallcenterId());
        
        //校验查询时间参数
        if (!isTimeParamValid(searchParam.getBegin(), searchParam.getEnd()))
        {
            LOG.error(LogUtils.AGENT_ID + "query time param invalid", LogUtils.encodeForLog(agentId));
            recordResult.setRecordRetCode(AgentErrorCode.AGENT_REST_INVALID);
            recordResult.setRetDesc("time param invalid when queryRecordInfo");
            return recordResult.returnResult();            
        }
        //校验开始时间和租户创建时间
        searchParam.setBegin(checkBeginTimeAndCreateTime(searchParam.getBegin()));
        try
        {
            //查询结束时间早于租户创建时间时，将结束时间设置为与查询开始时间相同
            if (getLongTime(searchParam.getBegin()) > getLongTime(searchParam.getEnd()))
            {
                searchParam.setEnd(searchParam.getBegin());
            }
        }
        catch (ParseException e)
        {
            LOG.error(LogUtils.AGENT_ID + " failed for parseException :{}",
                    LogUtils.encodeForLog(agentId),
                    LogUtils.encodeForLog(e.getMessage()));
        }
        Map<String, Object> queryInfo = RecordInfoService.getInstance().queryRecordInfo(page, searchParam);
        
        //map里面result代表数据列表
        List<RecordInfoBean> recordinfo = (List<RecordInfoBean>)queryInfo.get("result");
        
        List<Result> dataResult = new ArrayList<Result>();
        
        //此处用的原cms内部的一个存储过程，所以将查询结果做了一次转换，之后再返回结果
        if (null != recordinfo)
        {
            recordResult.setRecordRetCode(CommonConstant.RETURNCODE_SUCCESS);
            recordResult.setRetDesc(CommonConstant.RETURNDESC_SUCCESS);
            
            for (RecordInfoBean bean : recordinfo)
            {
                Result r = new Result();                
                r.setAgentId(bean.getAgentId());
                r.setBeginTime(bean.getBeginDate());
                r.setEndTime(bean.getEndDate());
                r.setCallerId(bean.getCallerNo());
                r.setCalledId(bean.getCalleeNo());
                r.setCallType(bean.getCallTypeId());
                r.setCallTimeCic(bean.getServerTime());
                r.setFileName(bean.getFileName().replaceAll("\\\\", "/").trim());
                dataResult.add(r);
            }
        }
        else
        {
            LOG.error(LogUtils.AGENT_ID + "Get recordInfo failed!", LogUtils.encodeForLog(agentId));
            recordResult.setRecordRetCode(CommonConstant.RETURNCODE_FAIL);
            recordResult.setRetDesc(CommonConstant.RETURNDESC_FAIL);
            
        }
        
        //暂时为了适配前端 定义pageVO对象
        PageVO pageVO = new PageVO();
        
        int curPage = StringUtils.strToInt(page.getCurPage());        
        pageVO.setCurPage(curPage);
        
        int pageSize = StringUtils.strToInt(page.getPageSize());        
        pageVO.setPageSize(pageSize);        
        
        int totalRows = StringUtils.strToInt(queryInfo.get("totalRows").toString());       
        pageVO.setTotalRows(totalRows);
        
        int totalPages = StringUtils.getCeil(totalRows, pageSize);        
        pageVO.setTotalPages(totalPages);
        
        ReturnData returnData = new ReturnData();       
        returnData.setPageVO(pageVO);     
        
        returnData.setResult(dataResult);        

        recordResult.setRetObject("returnData", returnData);        
        return recordResult.returnResult();
    }
    

    /**
     * 生成录音临时文件
     * @param recordId 
     * @return 文件路径
     */
    public RestResponse genTempRecordFile(String recordId)
    {
        RestResponse restResponse = new RestResponse();
        //校验录音ID是否有效
        if (!ValidateUtils.isRecordId(recordId))
        {
            LOG.error(LogUtils.METHOD_IN + "recordId is invalid", 
                    LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(recordId));
            restResponse.setReturnCode(AgentErrorCode.AGENT_REST_INVALID);
            restResponse.setMessage("recordId is invalid");
            return restResponse;
        }
        
        //向agent请求录音文件路径
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/qualitycontrol/").append(agentId);
        url.append("/localrecordpath?format=1");
        if (null != recordId)
        {
            try
            {
                String urlRecordId = URLEncoder.encode(recordId, CommonConstant.UTF_8);
                url.append("&recordId=").append(urlRecordId);
            } catch (UnsupportedEncodingException e)
            {
                LOG.info(LogUtils.AGENT_ID + "recordId URLEncode failed.  The exception is \r\n {} ",
                        LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
            }
        }      
        Map<String, Object> token_path_map = AgentRequest.get(agentId, url.toString());
        
        @SuppressWarnings("unchecked")
        Map<String, String> resultMap  = (Map<String, String>) token_path_map.get("result");
       
        String token = resultMap.get("token");
        String localRecordPath = resultMap.get("localRecordPath");
        if (StringUtils.isNullOrBlank(token) || StringUtils.isNullOrBlank(localRecordPath))
        {
            LOG.error(LogUtils.AGENT_ID + "The token or localRecordPath is null or blank", LogUtils.encodeForLog(agentId));
            restResponse.setReturnCode(AgentErrorCode.GENERATE_RECORD_TEMPFILE_ERROR);
            restResponse.setMessage("The token or localRecordPath is null or blank when query agent");
            return restResponse;
        }
        
        StringBuffer down_url = new StringBuffer();
        down_url.append(GlobalObject.getAgentServerUrl());
        down_url.append("/qualitycontrol/").append(agentId);
        try
        {
            String urlToken = URLEncoder.encode(token, CommonConstant.UTF_8);
            String urlLocalRecordPath = URLEncoder.encode(localRecordPath, CommonConstant.UTF_8);
            down_url.append("/recordfile?token=").append(urlToken);
            down_url.append("&localRecordPath=").append(urlLocalRecordPath);
        } catch (UnsupportedEncodingException e)
        {
            LOG.info(LogUtils.AGENT_ID + "token, localRecordPath URLEncode failed.  The exception is \r\n {} ",
                    LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
        }
        String fileName = localRecordPath.substring(localRecordPath.lastIndexOf("/")+1);
        
        String tempFilePath = AgentRequest.genRecordTempFile(fileName, agentId, down_url.toString()); //生成临时文件
        if (StringUtils.isNullOrBlank(tempFilePath))
        {
            restResponse.setReturnCode(AgentErrorCode.GENERATE_RECORD_TEMPFILE_ERROR);
            restResponse.setMessage("query record file from agent fail or copy data to tempfile fail");
        }else {
            restResponse.setReturnCode(AgentErrorCode.SUCCESS);
            restResponse.setRetObject("tempFilePath", tempFilePath.replace(GlobalObject.getApp_path(), "")); 
        }
        return restResponse;        
    }
    
    /**
     * 下载录音临时文件
     * @param tempFilePath
     * @param request
     * @param response
     */
    public void downloadRecordTempFile(String tempFilePath, HttpServletRequest request, HttpServletResponse response)
    {
        tempFilePath = Normalizer.normalize(tempFilePath, Form.NFKC);
        //检查文件下载权限
        if (!checkFilePathPriv(tempFilePath))
        {
            LOG.error(LogUtils.AGENT_ID + "check record trmpFilePath failed", LogUtils.encodeForLog(agentId));
            return;
        }
        
        String fileName = tempFilePath.substring(tempFilePath.lastIndexOf("/")+1);
        response.setContentType("audio/x-wav");
        response.addHeader("Cache", "no-cache");
        response.addHeader("Accept-Ranges", "bytes");
        response.addHeader("Cache-Control", "no-store,no-cache");
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        
        InputStream inputStream = null;  //用于读取录音临时文件    
        OutputStream outputStream = null;  //用于写入response
        File tempFile = null;
        try
        {            
            String realFilePath = GlobalObject.getApp_path() + tempFilePath;  //完整的文件路径
            tempFile = new File(realFilePath);
            if (!tempFile.exists())
            {
                return;
            }
            inputStream = new FileInputStream(tempFile);
            
            long length = tempFile.length();
            //此处加入header到响应中
            response.addHeader("Content-Length", length +"");
            response.addHeader("Content-Range", "bytes 0-" + (length-1) + "/" + length);            
            outputStream = response.getOutputStream();
            
            //读文件并写入response的输出流
            int i=0;
            byte[] fileBuffer = new byte[1024];
            while ((i = inputStream.read(fileBuffer)) > 0)
            {
                outputStream.write(fileBuffer,0,i);
            }
            outputStream.flush();
            
        }
        catch (FileNotFoundException e)
        {
            LOG.error(LogUtils.AGENT_ID + "find record tempfile failed \r\n {}",
                    LogUtils.encodeForLog(agentId), 
                    LogUtils.encodeForLog(e.getMessage()));
        }
        catch (IOException e)
        {
            LOG.error(LogUtils.AGENT_ID + "output failed \r\n {}",
                    LogUtils.encodeForLog(agentId), 
                    LogUtils.encodeForLog(e.getMessage()));
        }finally {
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    LOG.error(LogUtils.AGENT_ID + "release inputstream failed \r\n {}",
                            LogUtils.encodeForLog(agentId), 
                            LogUtils.encodeForLog(e.getMessage()));
                }
            }
            if(outputStream != null)
            {
                try
                {                    
                    outputStream.close();
                }               
                catch (IOException e) 
                {
                    LOG.error(LogUtils.AGENT_ID + "release entity failed \r\n {}",
                            LogUtils.encodeForLog(agentId), 
                            LogUtils.encodeForLog(e.getMessage()));
                }
            }
            
            //IE下多余请求时，不删除文件
            String headerStr = request.getHeader("User-Agent");  
            if (headerStr != null)
            {
                String header = headerStr.trim();
                if (header.trim().startsWith("NSPlayer") || header.startsWith("Windows-Media-Player"))
                {
                    return;
                }
            }            
            
            //下载完成后删除文件
            if (tempFile != null)
            {
                FileUtils.deleteFile(tempFile);  
            }
            
        }  
    }
    
    
    /**
     * 校验录音文件下载权限
     * @param desFilePath
     * @return
     */
    private boolean checkFilePathPriv(String desFilePath)
    {
        //校验文件路径是否有效
        if (StringUtils.isNullOrBlank(desFilePath) || (!desFilePath.startsWith(CommonConstant.TEMPFILE_RECORD_PATH)))
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
        targetFilePathBuffer.append(CommonConstant.TEMPFILE_RECORD_PATH);
        targetFilePathBuffer.append("/");
        targetFilePathBuffer.append(agentId);
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
            LOG.error(LogUtils.AGENT_ID + "analyze record desFilePath failed \r\n {}",
                    LogUtils.encodeForLog(agentId), 
                    LogUtils.encodeForLog(e.getMessage()));
            return false;
        }
        return true;
    }
    
    /**
     * 校验租户创建时间与录音开始查询比较，如果报表查询时间早于租户创建时间，则需要将查询开始设置为租户创建时间
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
        SimpleDateFormat dateFormat = new SimpleDateFormat(CommonConstant.DATE_FULL);
        return dateFormat.format(createDate);
    }
   
}
