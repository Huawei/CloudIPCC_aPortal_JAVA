package com.huawei.agentconsole.ws.param;

import javax.validation.constraints.Pattern;
import javax.ws.rs.FormParam;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import com.huawei.agentconsole.common.constant.ParamPatternConstant;

public class AgentCallOutBriefParam
{

    /**
     * 报表统计开始时间 ,时间格式为：yyyy-MM-dd。
     */
    @Pattern(regexp = ParamPatternConstant.REPORT_TIME_PATTERN)
    @NotBlank
    @FormParam("agentCallOutBriefReport_startTime")
    private String startTime;

    /**
     * 报表统计结束时间,间格式为：yyyy-MM-dd。
     */
    @Pattern(regexp = ParamPatternConstant.REPORT_TIME_PATTERN)
    @NotBlank
    @FormParam("agentCallOutBriefReport_endTime")
    private String endTime;

    /**
     * 报表类型：2：日报表, 3：月报表，4：周报表
     */
    @Range(min = 2, max = 4)
    @FormParam("agentCallOutBriefReport_type")
    private int reportType = 3;

    /**
     * 语言类型：0：中文，1：英文。
     */
    @Range(min = 0, max = 1)
    private int languageType;
    
    /**
     * 座席工号
     */
    @FormParam("agentCallOutBriefReport_agentId")
    private String agentIds;

    public void setAgentIds(String agentIds)
    {
        this.agentIds = agentIds;
    }

    public String getStartTime()
    {
        return startTime;
    }

    public void setStartTime(String startTime)
    {
        this.startTime = startTime;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }

    public int getReportType()
    {
        return reportType;
    }

    public void setReportType(int reportType)
    {
        this.reportType = reportType;
    }

    public int getLanguageType()
    {
        return languageType;
    }

    public void setLanguageType(int languageType)
    {
        this.languageType = languageType;
    }

    public String getAgentIds()
    {
        return agentIds;
    }
}
