
package com.huawei.agentconsole.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentCallOutBriefBean;
import com.huawei.agentconsole.bean.AgentTraffic;
import com.huawei.agentconsole.bean.AgentWork;
import com.huawei.agentconsole.bean.SkillTraffic;
import com.huawei.agentconsole.bean.VdnTraffic;
import com.huawei.agentconsole.common.constant.CommonConstant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * 
 * <p>Title:  </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年9月28日
 * @since
 */
public class ExcelUtils
{

    private static final Logger LOG = LoggerFactory.getLogger(ExcelUtils.class);
    
    /**
     * 基于模板生成报表
     * @param filePath 临时报表文件
     * @param result 需要写入到Excel中的结果
     * @param reportName 报表类型
     */
    public static void genExcelData(String agentId, String filePath, List<?> result, int reportName)
    {
        File file = new File(filePath);
        InputStream inputStream = null;
        HSSFWorkbook book = null;
        FileOutputStream fileOutputStream = null;
        try
        {
            inputStream = new FileInputStream(file);
            POIFSFileSystem poifsFileSystem=new POIFSFileSystem(inputStream); 
            book = new HSSFWorkbook(poifsFileSystem);
        }
        catch (IOException e)
        {
            LOG.error(LogUtils.AGENT_ID + "read template file failed. The exception is \r\n",
                    LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
        }
        finally
        {
            FileUtils.closeInputStream(inputStream);
        }
        
        if (null == book)
        {
            return;
        }
        
        try
        {
            
            HSSFSheet sheet = book.getSheetAt(0);
            int size = result.size();
            if (reportName == CommonConstant.REPORTNAME_VDNTRAFFIC)
            {
                VdnTraffic bean = null;
                for(int i = 0; i < size; i++)
                {
                    HSSFRow row = sheet.createRow(3+i);
                    bean = (VdnTraffic) result.get(i);
                    row.createCell(0).setCellValue(bean.getStatTime());
                    row.createCell(1).setCellValue(bean.getInBoundCalls());
                    row.createCell(2).setCellValue(bean.getAnswerdCalls());
                    row.createCell(3).setCellValue(bean.getAnswerRate());
                    row.createCell(4).setCellValue(bean.getIvrInBoundCalls());
                    row.createCell(5).setCellValue(bean.getIvrAnswerdCalls());
                    row.createCell(6).setCellValue(bean.getIvrAnswerRate());
                    row.createCell(7).setCellValue(bean.getSkillInBoundCalls());
                    row.createCell(8).setCellValue(bean.getSkillAnswerdCalls());
                    row.createCell(9).setCellValue(bean.getSkillAnswerRate());
                    row.createCell(10).setCellValue(bean.getOutBoundCalls());
                    row.createCell(11).setCellValue(bean.getOutBoundAnsweredCalls());
                    row.createCell(12).setCellValue(bean.getOutBoundIVRAnsweredCalls());
                    row.createCell(13).setCellValue(bean.getOutBoundSkillAnsweredCalls());
                }  
            }
            else if(reportName == CommonConstant.REPORTNAME_SKILLTRAFFIC || reportName == CommonConstant.REPORTNAME_SKILLTRAFFICBYSKILL)
            {
                SkillTraffic bean = null;
                for(int i = 0; i < size; i++)
                {
                    HSSFRow row = sheet.createRow(3+i);
                    bean = (SkillTraffic)result.get(i);
                    row.createCell(0).setCellValue(bean.getSkillName());
                    row.createCell(1).setCellValue(bean.getOfferedCalls());
                    row.createCell(2).setCellValue(bean.getAnsweredCalls());
                    row.createCell(3).setCellValue(bean.getAnswerRate());
                    row.createCell(4).setCellValue(bean.getLostCalls());
                    row.createCell(5).setCellValue(bean.getLostRate());
                    row.createCell(6).setCellValue(bean.getUserAbanInQueue());
                    row.createCell(7).setCellValue(bean.getSysAbanCallsInQueue());
                    row.createCell(8).setCellValue(bean.getUserAbanInRing());
                    row.createCell(9).setCellValue(bean.getRingReject());
                    row.createCell(10).setCellValue(bean.getSysAbanCallsInRing());
                    row.createCell(11).setCellValue(bean.getSysSLARate());
                    row.createCell(12).setCellValue(bean.getLostInRing());
                    row.createCell(13).setCellValue(bean.getRingAnswerRate());
                    row.createCell(14).setCellValue(bean.getAvgAnsweredQueueTime());
                    row.createCell(15).setCellValue(bean.getAvgLostQueueTime());
                    row.createCell(16).setCellValue(bean.getAvgAnseredRingingTime());
                    row.createCell(17).setCellValue(bean.getAvgLostRingTime());
                    row.createCell(18).setCellValue(bean.getTalkTime());
                    row.createCell(19).setCellValue(bean.getAvgWaitTime());
                }
            }
            else if(reportName == CommonConstant.REPORTNAME_AGENTTRAFFIC)
            {
                AgentTraffic bean = null;
                for(int i = 0; i < size; i++)
                {
                    HSSFRow row = sheet.createRow(3+i);
                    bean = (AgentTraffic)result.get(i);
                    row.createCell(0).setCellValue(bean.getAgentId());
                    row.createCell(1).setCellValue(bean.getAgentName());
                    row.createCell(2).setCellValue(bean.getSkillName());
                    row.createCell(3).setCellValue(bean.getOfferedCalls());
                    row.createCell(4).setCellValue(bean.getAnsweredCalls());
                    row.createCell(5).setCellValue(bean.getLostCalls());
                    row.createCell(6).setCellValue(bean.getAbortInRing());
                    row.createCell(7).setCellValue(bean.getAnswerRate());
                    row.createCell(8).setCellValue(bean.getAnswerRateInServiceLevel());
                    row.createCell(9).setCellValue(bean.getRingOverTime());
                    row.createCell(10).setCellValue(bean.getRingReject());
                    row.createCell(11).setCellValue(bean.getUserAbanInSLA());
                    row.createCell(12).setCellValue(bean.getUserAbanOverSLA());
                    row.createCell(13).setCellValue(bean.getAvgRingTime());
                    row.createCell(14).setCellValue(bean.getAvgTalkTime());
                    row.createCell(15).setCellValue(bean.getMaxTalkTime());
                    row.createCell(16).setCellValue(bean.getMinTalkTime());
                }
            }
            else if(reportName == CommonConstant.REPORTNAME_AGENTWORK )
            {
                AgentWork bean = null;
                for(int i = 0; i < size; i++)
                {
                    HSSFRow row = sheet.createRow(3+i);
                    bean = (AgentWork)result.get(i);
                    row.createCell(0).setCellValue(bean.getAgentId());
                    row.createCell(1).setCellValue(bean.getAgentName());
                    row.createCell(2).setCellValue(bean.getSkillName());
                    row.createCell(3).setCellValue(bean.getLoginTimes());
                    row.createCell(4).setCellValue(bean.getLoginDuration());
                    row.createCell(5).setCellValue(bean.getCallinTalkTimes());
                    row.createCell(6).setCellValue(bean.getCallinTalkDuration());
                    row.createCell(7).setCellValue(bean.getCalloutTalkTimes());
                    row.createCell(8).setCellValue(bean.getCalloutTalkDuration());
                    row.createCell(9).setCellValue(bean.getArrangeTimes());
                    row.createCell(10).setCellValue(bean.getArrangeDuration());
                    row.createCell(11).setCellValue(bean.getRestTimes());
                    row.createCell(12).setCellValue(bean.getRestDuration());
                    row.createCell(13).setCellValue(bean.getHoldTimes());
                    row.createCell(14).setCellValue(bean.getHoldDuration());
                    row.createCell(15).setCellValue(bean.getBusyTimes());
                    row.createCell(16).setCellValue(bean.getBusyDuration());
                    row.createCell(17).setCellValue(bean.getRingDuration());
                    row.createCell(18).setCellValue(bean.getIdelTime());
                    row.createCell(19).setCellValue(bean.getWorkTimeUseRateWithACW());
                    row.createCell(20).setCellValue(bean.getWorkTimeUseRateWithoutACW());
                    row.createCell(21).setCellValue(bean.getInternalTransferTimes());
                    row.createCell(22).setCellValue(bean.getTransferOutTimes());
                    row.createCell(23).setCellValue(bean.getHangUpToIVRTimes());
                    row.createCell(24).setCellValue(bean.getThreePartyCalls());
                    row.createCell(25).setCellValue(bean.getInternalCalls());
                    row.createCell(26).setCellValue(bean.getInternalHelpTimes());
                }
                
            }
            else if (reportName == CommonConstant.REPORTNAME_AGENTCALLOUTBRIEF)
            {
                AgentCallOutBriefBean bean = null;
                for(int i = 0; i < size; i++)
                {
                    HSSFRow row = sheet.createRow(2+i);
                    bean = (AgentCallOutBriefBean)result.get(i);
                    row.createCell(0).setCellValue(bean.getAgentId());
                    row.createCell(1).setCellValue(bean.getAgentName());
                    row.createCell(2).setCellValue(bean.getTimeSegment());
                    row.createCell(3).setCellValue(bean.getOutBoundCalls());
                    row.createCell(4).setCellValue(bean.getOutBoundAnswered());
                    row.createCell(5).setCellValue(bean.getOutBoundAbandoned());
                    row.createCell(6).setCellValue(bean.getOutBoundAnswerRate());
                    row.createCell(7).setCellValue(bean.getOutBoundTalkTime());
                    row.createCell(8).setCellValue(bean.getAvgOutBoundTalkTime());
                    row.createCell(9).setCellValue(bean.getMaxOutBoundTalkTime());
                    row.createCell(10).setCellValue(bean.getMinOutBoundTalkTime());
                }
                    
            }
            
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.flush();
            book.write(fileOutputStream);
        }
        catch (IOException e)
        {
            LOG.error(LogUtils.AGENT_ID + "Generate report to {} failed ,the exception is \r\n {}",
                    new Object[]{ LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(filePath),
                    LogUtils.encodeForLog(e.getMessage())});
        }
        finally 
        {
            FileUtils.closeOutputStream(fileOutputStream);
        }
    }
    
    
    public List<?> getResult(List<?> result, int reportType)
    {
        int size = result.size();
        if (reportType == 1)
        {
            List<VdnTraffic> vdnTraffics = new ArrayList<VdnTraffic>(size);
            
            for (int i = 0; i< size; i++)
            {
                vdnTraffics.add((VdnTraffic) result.get(i));
            }
            return vdnTraffics;
        }
        return null;
    }
}
