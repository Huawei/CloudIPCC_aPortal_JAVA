package com.huawei.agentconsole.bean;

/**
 * 技能队列话务量统计信息
 * 
 * @author gWX513762
 *
 */
public class SkillTraffic
{

    private String skillName;

    private Integer offeredCalls;

    private Integer answeredCalls;

    private String answerRate;

    private Integer lostCalls;

    private String lostRate;

    private Integer userAbanInQueue;

    private Integer sysAbanCallsInQueue;

    private Integer userAbanInRing;

    private Integer ringReject;

    private Integer sysAbanCallsInRing;

    private String sysSLARate;

    private Integer lostInRing;

    private String ringAnswerRate;

    private String avgAnsweredQueueTime;

    private String avgLostQueueTime;

    private String avgAnseredRingingTime;

    private String avgLostRingTime;

    private String talkTime;

    private String avgWaitTime;

//    private String maxQueueTime;
//
//    private String minQueueTime;
//
//    private Integer flowOutToQueue;
//
//    private String flowOutToQueueTime;
//
//    private Integer flowOutToAgent;

    public String getSkillName()
    {
        return skillName;
    }

    public void setSkillName(String skillName)
    {
        this.skillName = skillName;
    }

    public Integer getOfferedCalls()
    {
        return offeredCalls;
    }

    public void setOfferedCalls(Integer offeredCalls)
    {
        this.offeredCalls = offeredCalls;
    }

    public Integer getAnsweredCalls()
    {
        return answeredCalls;
    }

    public void setAnsweredCalls(Integer answeredCalls)
    {
        this.answeredCalls = answeredCalls;
    }

    public String getAnswerRate()
    {
        return answerRate;
    }

    public void setAnswerRate(String answerRate)
    {
        this.answerRate = answerRate;
    }

    public Integer getLostCalls()
    {
        return lostCalls;
    }

    public void setLostCalls(Integer lostCalls)
    {
        this.lostCalls = lostCalls;
    }

    public String getLostRate()
    {
        return lostRate;
    }

    public void setLostRate(String lostRate)
    {
        this.lostRate = lostRate;
    }

    public Integer getUserAbanInQueue()
    {
        return userAbanInQueue;
    }

    public void setUserAbanInQueue(Integer userAbanInQueue)
    {
        this.userAbanInQueue = userAbanInQueue;
    }

    public Integer getSysAbanCallsInQueue()
    {
        return sysAbanCallsInQueue;
    }

    public void setSysAbanCallsInQueue(Integer sysAbanCallsInQueue)
    {
        this.sysAbanCallsInQueue = sysAbanCallsInQueue;
    }

    public Integer getUserAbanInRing()
    {
        return userAbanInRing;
    }

    public void setUserAbanInRing(Integer userAbanInRing)
    {
        this.userAbanInRing = userAbanInRing;
    }

    public Integer getRingReject()
    {
        return ringReject;
    }

    public void setRingReject(Integer ringReject)
    {
        this.ringReject = ringReject;
    }

    public Integer getSysAbanCallsInRing()
    {
        return sysAbanCallsInRing;
    }

    public void setSysAbanCallsInRing(Integer sysAbanCallsInRing)
    {
        this.sysAbanCallsInRing = sysAbanCallsInRing;
    }

    public String getSysSLARate()
    {
        return sysSLARate;
    }

    public void setSysSLARate(String sysSLARate)
    {
        this.sysSLARate = sysSLARate;
    }

    public Integer getLostInRing()
    {
        return lostInRing;
    }

    public void setLostInRing(Integer lostInRing)
    {
        this.lostInRing = lostInRing;
    }

    public String getRingAnswerRate()
    {
        return ringAnswerRate;
    }

    public void setRingAnswerRate(String ringAnswerRate)
    {
        this.ringAnswerRate = ringAnswerRate;
    }

    public String getAvgAnsweredQueueTime()
    {
        return avgAnsweredQueueTime;
    }

    public void setAvgAnsweredQueueTime(String avgAnsweredQueueTime)
    {
        this.avgAnsweredQueueTime = avgAnsweredQueueTime;
    }

    public String getAvgLostQueueTime()
    {
        return avgLostQueueTime;
    }

    public void setAvgLostQueueTime(String avgLostQueueTime)
    {
        this.avgLostQueueTime = avgLostQueueTime;
    }

    public String getAvgAnseredRingingTime()
    {
        return avgAnseredRingingTime;
    }

    public void setAvgAnseredRingingTime(String avgAnseredRingingTime)
    {
        this.avgAnseredRingingTime = avgAnseredRingingTime;
    }

    public String getAvgLostRingTime()
    {
        return avgLostRingTime;
    }

    public void setAvgLostRingTime(String avgLostRingTime)
    {
        this.avgLostRingTime = avgLostRingTime;
    }

    public String getTalkTime()
    {
        return talkTime;
    }

    public void setTalkTime(String talkTime)
    {
        this.talkTime = talkTime;
    }

    public String getAvgWaitTime()
    {
        return avgWaitTime;
    }

    public void setAvgWaitTime(String avgWaitTime)
    {
        this.avgWaitTime = avgWaitTime;
    }


}
