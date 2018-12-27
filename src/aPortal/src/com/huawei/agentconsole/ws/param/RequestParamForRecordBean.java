
package com.huawei.agentconsole.ws.param;

import javax.validation.constraints.Pattern;

import com.huawei.agentconsole.common.constant.ParamPatternConstant;

/**
 * 
 * <p>Title:  查询录音时候的bean对象</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author l00357702
 * @version V1.0 2018年8月6日
 * @since
 */
public class RequestParamForRecordBean
{
    /**
     * 主叫号码
     */
    @Pattern(regexp=ParamPatternConstant.RECORDQUERY_CALLERNO_PATTERN)
    private String caller;
    
    /**
     * 被叫号码
     */
    @Pattern(regexp=ParamPatternConstant.RECORDQUERY_CALLEDNO_PATTERN)
    private String called;
    
    /**
     * 坐席的id
     */
    @Pattern(regexp=ParamPatternConstant.RECORDQUERY_AGENTID_PATTERN)
    private String agentId;
    
    /**
     * 查询的开始时间
     */
    @Pattern(regexp=ParamPatternConstant.RECORDQUERY_TIME_PATTERN)
    private String begin;
    
    /**
     * 查询的结束时间
     */
    @Pattern(regexp=ParamPatternConstant.RECORDQUERY_TIME_PATTERN)
    private String end;
    
    /**
     * 最小通话时长
     */
    private String callTimeMin;
    
    /**
     * 最大通话时长
     */
    private String callTimeMax;
    
    private String ccId;
    
    private String vdnId;
    

    /**
     * @return the caller
     */
    public String getCaller()
    {
        return caller;
    }

    /**
     * @param caller the caller to set
     */
    public void setCaller(String caller)
    {
        this.caller = caller;
    }

    /**
     * @return the called
     */
    public String getCalled()
    {
        return called;
    }

    /**
     * @param called the called to set
     */
    public void setCalled(String called)
    {
        this.called = called;
    }

    /**
     * @return the agentId
     */
    public String getAgentId()
    {
        return agentId;
    }

    /**
     * @param agentId the agentId to set
     */
    public void setAgentId(String agentId)
    {
        this.agentId = agentId;
    }

    /**
     * @return the begin
     */
    public String getBegin()
    {
        return begin;
    }

    /**
     * @param begin the begin to set
     */
    public void setBegin(String begin)
    {
        this.begin = begin;
    }

    /**
     * @return the end
     */
    public String getEnd()
    {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(String end)
    {
        this.end = end;
    }

    /**
     * @return the callTimeMin
     */
    public String getCallTimeMin()
    {
        return callTimeMin;
    }

    /**
     * @param callTimeMin the callTimeMin to set
     */
    public void setCallTimeMin(String callTimeMin)
    {
        this.callTimeMin = callTimeMin;
    }

    /**
     * @return the callTimeMax
     */
    public String getCallTimeMax()
    {
        return callTimeMax;
    }

    /**
     * @param callTimeMax the callTimeMax to set
     */
    public void setCallTimeMax(String callTimeMax)
    {
        this.callTimeMax = callTimeMax;
    }

    
    public String getCcId()
    {
        return ccId;
    }

    public void setCcId(String ccId)
    {
        this.ccId = ccId;
    }

    public String getVdnId()
    {
        return vdnId;
    }

    public void setVdnId(String vdnId)
    {
        this.vdnId = vdnId;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("RequestParamForRecordBean [caller=");
        builder.append(caller);
        builder.append(", called=");
        builder.append(called);
        builder.append(", agentId=");
        builder.append(agentId);
        builder.append(", begin=");
        builder.append(begin);
        builder.append(", end=");
        builder.append(end);
        builder.append(", callTimeMin=");
        builder.append(callTimeMin);
        builder.append(", callTimeMax=");
        builder.append(callTimeMax);
        builder.append("]");
        return builder.toString();
    }
    
    
    
}
