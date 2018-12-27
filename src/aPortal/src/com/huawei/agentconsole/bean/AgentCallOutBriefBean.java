package com.huawei.agentconsole.bean;

public class AgentCallOutBriefBean
{
    private String agentId;
    
    private String agentName;
    
    private String timeSegment;
    
    private Integer outBoundCalls;
    
    private Integer outBoundAnswered;
    
    private Integer outBoundAbandoned;
    
    private String outBoundAnswerRate;
    
    private String  outBoundTalkTime;
    
    private String avgOutBoundTalkTime;
    
    private String maxOutBoundTalkTime;
    
    private String minOutBoundTalkTime;

    public String getAgentId()
    {
        return agentId;
    }

    public void setAgentId(String agentId)
    {
        this.agentId = agentId;
    }

    public String getAgentName()
    {
        return agentName;
    }

    public void setAgentName(String agentName)
    {
        this.agentName = agentName;
    }

    public String getTimeSegment()
    {
        return timeSegment;
    }

    public void setTimeSegment(String timeSegment)
    {
        this.timeSegment = timeSegment;
    }

    public Integer getOutBoundCalls()
    {
        return outBoundCalls;
    }

    public void setOutBoundCalls(Integer outBoundCalls)
    {
        this.outBoundCalls = outBoundCalls;
    }

    public Integer getOutBoundAnswered()
    {
        return outBoundAnswered;
    }

    public void setOutBoundAnswered(Integer outboundAnswered)
    {
        this.outBoundAnswered = outboundAnswered;
    }

    public Integer getOutBoundAbandoned()
    {
        return outBoundAbandoned;
    }

    public void setOutBoundAbandoned(Integer outboundAbandoned)
    {
        this.outBoundAbandoned = outboundAbandoned;
    }

    public String getOutBoundAnswerRate()
    {
        return outBoundAnswerRate;
    }

    public void setOutBoundAnswerRate(String outboundAnswerRate)
    {
        this.outBoundAnswerRate = outboundAnswerRate;
    }

    public String getOutBoundTalkTime()
    {
        return outBoundTalkTime;
    }

    public void setOutBoundTalkTime(String outboundTalkTime)
    {
        this.outBoundTalkTime = outboundTalkTime;
    }

    public String getAvgOutBoundTalkTime()
    {
        return avgOutBoundTalkTime;
    }

    public void setAvgOutBoundTalkTime(String avgOutboundTalkTime)
    {
        this.avgOutBoundTalkTime = avgOutboundTalkTime;
    }

    public String getMaxOutBoundTalkTime()
    {
        return maxOutBoundTalkTime;
    }

    public void setMaxOutBoundTalkTime(String maxOutboundTalkTime)
    {
        this.maxOutBoundTalkTime = maxOutboundTalkTime;
    }

    public String getMinOutBoundTalkTime()
    {
        return minOutBoundTalkTime;
    }

    public void setMinOutBoundTalkTime(String minOutboundTalkTime)
    {
        this.minOutBoundTalkTime = minOutboundTalkTime;
    }

}
