package com.huawei.agentconsole.bean;

/**
 * 话务员话务量
 * @author w00466288
 * @version V1.0 2018年8月23日
 * @since
 */
public class AgentTraffic
{
    
    private String agentId;
    
    private String agentName;

    private String skillName;
    
    private int offeredCalls;
    
    private int answeredCalls;
    
    private String lostRate;
    
    private int lostCalls;
    
    private int abortInRing;
    
    private String answerRate;
    
    private String answerRateInServiceLevel;
    
    private int ringOverTime;
    
    private int ringReject;
    
    private int userAbanInSLA;
    
    private int userAbanOverSLA;
    
    private String avgRingTime;
    
    private String avgTalkTime;
    
    private String maxTalkTime;
    
    private String minTalkTime;

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

    public String getSkillName()
    {
        return skillName;
    }

    public void setSkillName(String skillName)
    {
        this.skillName = skillName;
    }

    public int getOfferedCalls()
    {
        return offeredCalls;
    }

    public void setOfferedCalls(int offeredCalls)
    {
        this.offeredCalls = offeredCalls;
    }

    public int getAnsweredCalls()
    {
        return answeredCalls;
    }

    public void setAnsweredCalls(int answeredCalls)
    {
        this.answeredCalls = answeredCalls;
    }

    public String getLostRate()
    {
        return lostRate;
    }

    public void setLostRate(String lostRate)
    {
        this.lostRate = lostRate;
    }

    public int getLostCalls()
    {
        return lostCalls;
    }

    public void setLostCalls(int lostCalls)
    {
        this.lostCalls = lostCalls;
    }

    public int getAbortInRing()
    {
        return abortInRing;
    }

    public void setAbortInRing(int abortInRing)
    {
        this.abortInRing = abortInRing;
    }

    public String getAnswerRate()
    {
        return answerRate;
    }

    public void setAnswerRate(String answerRate)
    {
        this.answerRate = answerRate;
    }

    public String getAnswerRateInServiceLevel()
    {
        return answerRateInServiceLevel;
    }

    public void setAnswerRateInServiceLevel(String answerRateInServiceLevel)
    {
        this.answerRateInServiceLevel = answerRateInServiceLevel;
    }

    public int getRingOverTime()
    {
        return ringOverTime;
    }

    public void setRingOverTime(int ringOverTime)
    {
        this.ringOverTime = ringOverTime;
    }

    public int getRingReject()
    {
        return ringReject;
    }

    public void setRingReject(int ringReject)
    {
        this.ringReject = ringReject;
    }

    public int getUserAbanInSLA()
    {
        return userAbanInSLA;
    }

    public void setUserAbanInSLA(int userAbanInSLA)
    {
        this.userAbanInSLA = userAbanInSLA;
    }

    public int getUserAbanOverSLA()
    {
        return userAbanOverSLA;
    }

    public void setUserAbanOverSLA(int userAbanOverSLA)
    {
        this.userAbanOverSLA = userAbanOverSLA;
    }

    public String getAvgRingTime()
    {
        return avgRingTime;
    }

    public void setAvgRingTime(String avgRingTime)
    {
        this.avgRingTime = avgRingTime;
    }

    public String getAvgTalkTime()
    {
        return avgTalkTime;
    }

    public void setAvgTalkTime(String avgTalkTime)
    {
        this.avgTalkTime = avgTalkTime;
    }

    public String getMaxTalkTime()
    {
        return maxTalkTime;
    }

    public void setMaxTalkTime(String maxTalkTime)
    {
        this.maxTalkTime = maxTalkTime;
    }

    public String getMinTalkTime()
    {
        return minTalkTime;
    }

    public void setMinTalkTime(String minTalkTime)
    {
        this.minTalkTime = minTalkTime;
    }    
    
}
